package com.yugabyte.app.yugastore.admin.domain;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Product metadata entity for YCQL access
 * Extends the base products table with admin-specific fields
 */
@Table("products")
public class ProductMetadata {

    @PrimaryKey
    private String asin;

    @Column("title")
    private String title;

    @Column("description")
    private String description;

    @Column("price")
    private Double price;

    @Column("imurl")
    private String imageUrl;

    @Column("also_bought")
    private List<String> alsoBought;

    @Column("also_viewed")
    private List<String> alsoViewed;

    @Column("bought_together")
    private List<String> boughtTogether;

    @Column("buy_after_viewing")
    private List<String> buyAfterViewing;

    @Column("brand")
    private String brand;

    @Column("categories")
    private Set<String> categories;

    @Column("num_reviews")
    private Integer numReviews;

    @Column("num_stars")
    private Integer numStars;

    @Column("avg_stars")
    private Double avgStars;

    // Admin-specific fields
    @Column("is_active")
    private Boolean isActive;

    @Column("deactivated_at")
    private Instant deactivatedAt;

    @Column("deactivated_by")
    private String deactivatedBy;

    @Column("version")
    private Integer version;

    // Constructors
    public ProductMetadata() {
    }

    public ProductMetadata(String asin) {
        this.asin = asin;
        this.isActive = true;
        this.version = 0;
    }

    // Getters and Setters
    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getAlsoBought() {
        return alsoBought;
    }

    public void setAlsoBought(List<String> alsoBought) {
        this.alsoBought = alsoBought;
    }

    public List<String> getAlsoViewed() {
        return alsoViewed;
    }

    public void setAlsoViewed(List<String> alsoViewed) {
        this.alsoViewed = alsoViewed;
    }

    public List<String> getBoughtTogether() {
        return boughtTogether;
    }

    public void setBoughtTogether(List<String> boughtTogether) {
        this.boughtTogether = boughtTogether;
    }

    public List<String> getBuyAfterViewing() {
        return buyAfterViewing;
    }

    public void setBuyAfterViewing(List<String> buyAfterViewing) {
        this.buyAfterViewing = buyAfterViewing;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }

    public Integer getNumReviews() {
        return numReviews;
    }

    public void setNumReviews(Integer numReviews) {
        this.numReviews = numReviews;
    }

    public Integer getNumStars() {
        return numStars;
    }

    public void setNumStars(Integer numStars) {
        this.numStars = numStars;
    }

    public Double getAvgStars() {
        return avgStars;
    }

    public void setAvgStars(Double avgStars) {
        this.avgStars = avgStars;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Instant getDeactivatedAt() {
        return deactivatedAt;
    }

    public void setDeactivatedAt(Instant deactivatedAt) {
        this.deactivatedAt = deactivatedAt;
    }

    public String getDeactivatedBy() {
        return deactivatedBy;
    }

    public void setDeactivatedBy(String deactivatedBy) {
        this.deactivatedBy = deactivatedBy;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    // Utility methods
    public void incrementVersion() {
        this.version = (this.version == null ? 0 : this.version) + 1;
    }

    public boolean isActive() {
        return isActive != null && isActive;
    }
}
