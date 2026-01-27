package entities;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Role entity for the RBAC (Role-Based Access Control) system.
 * Maps to the 'roles' table in YugabyteDB YSQL.
 *
 * Defines the four primary roles in YugaStore:
 * - ROLE_ANONYMOUS: Unauthenticated users (product browsing only)
 * - ROLE_CUSTOMER: Registered customers (cart, orders, profile management)
 * - ROLE_SUPPORT: Customer support representatives (read-only customer data access)
 * - ROLE_ADMIN: System administrators (full system access with audit logging)
 */
@Entity
@Table(name = "roles")
public class Role {

    // Role constants for type safety and consistency
    public static final String ROLE_ANONYMOUS = "ROLE_ANONYMOUS";
    public static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";
    public static final String ROLE_SUPPORT = "ROLE_SUPPORT";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    @Id
    @GeneratedValue(generator = "uuid2")
    @Column(name = "role_id", columnDefinition = "UUID")
    private UUID roleId;

    @NotBlank(message = "Role name is required")
    @Size(max = 50, message = "Role name must be less than 50 characters")
    @Column(name = "role_name", unique = true, nullable = false, length = 50)
    private String roleName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Many-to-many relationship with User through UserRole
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    // Constructors
    public Role() {
        this.createdAt = LocalDateTime.now();
    }

    public Role(String roleName) {
        this();
        this.roleName = roleName;
    }

    public Role(String roleName, String description) {
        this(roleName);
        this.description = description;
    }

    // JPA lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Static factory methods for creating standard roles
    public static Role createAnonymousRole() {
        return new Role(ROLE_ANONYMOUS, "Unauthenticated users - can browse products only");
    }

    public static Role createCustomerRole() {
        return new Role(ROLE_CUSTOMER, "Registered customers - can manage own cart, orders, and profile");
    }

    public static Role createSupportRole() {
        return new Role(ROLE_SUPPORT, "Customer support representatives - read-only access to customer data");
    }

    public static Role createAdminRole() {
        return new Role(ROLE_ADMIN, "System administrators - full access with audit logging");
    }

    // Getters and Setters
    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    // Utility methods
    public void addUser(User user) {
        this.users.add(user);
        user.getRoles().add(this);
    }

    public void removeUser(User user) {
        this.users.remove(user);
        user.getRoles().remove(this);
    }

    public boolean isActive() {
        return isActive != null && isActive;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    // Role hierarchy and permission checking methods
    public boolean isAnonymous() {
        return ROLE_ANONYMOUS.equals(this.roleName);
    }

    public boolean isCustomer() {
        return ROLE_CUSTOMER.equals(this.roleName);
    }

    public boolean isSupport() {
        return ROLE_SUPPORT.equals(this.roleName);
    }

    public boolean isAdmin() {
        return ROLE_ADMIN.equals(this.roleName);
    }

    /**
     * Determines if this role has higher or equal authority than the specified role.
     * Role hierarchy (from lowest to highest authority):
     * ANONYMOUS < CUSTOMER < SUPPORT < ADMIN
     */
    public boolean hasAuthorityOver(Role otherRole) {
        if (otherRole == null) return true;

        int thisAuthority = getAuthorityLevel();
        int otherAuthority = otherRole.getAuthorityLevel();

        return thisAuthority >= otherAuthority;
    }

    /**
     * Gets the authority level for role hierarchy comparisons.
     * Higher numbers indicate higher authority.
     */
    public int getAuthorityLevel() {
        switch (this.roleName) {
            case ROLE_ANONYMOUS:
                return 1;
            case ROLE_CUSTOMER:
                return 2;
            case ROLE_SUPPORT:
                return 3;
            case ROLE_ADMIN:
                return 4;
            default:
                return 0; // Unknown roles have no authority
        }
    }

    /**
     * Checks if this role can read data belonging to users with the specified role.
     */
    public boolean canReadDataFrom(Role targetUserRole) {
        // Admins can read all data
        if (isAdmin()) return true;

        // Support can read customer data but not admin/support data
        if (isSupport()) {
            return targetUserRole.isCustomer() || targetUserRole.isAnonymous();
        }

        // Customers can only read their own data
        if (isCustomer()) {
            return this.equals(targetUserRole);
        }

        // Anonymous users can't read any user data
        return false;
    }

    /**
     * Checks if this role can modify data belonging to users with the specified role.
     */
    public boolean canModifyDataFrom(Role targetUserRole) {
        // Only admins can modify other users' data
        if (isAdmin()) return true;

        // Users can only modify their own data
        return this.equals(targetUserRole);
    }

    // equals and hashCode based on business key (roleName)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Role role = (Role) obj;
        return roleName != null && roleName.equals(role.roleName);
    }

    @Override
    public int hashCode() {
        return roleName != null ? roleName.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Role{" +
                "roleId=" + roleId +
                ", roleName='" + roleName + '\'' +
                ", description='" + description + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", userCount=" + (users != null ? users.size() : 0) +
                '}';
    }
}