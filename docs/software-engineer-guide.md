# YugaStore - Software Engineer Quick Reference Guide

**Version:** 1.0  
**Date:** January 27, 2026  
**Audience:** Software Engineers, Developers, Individual Contributors

---

## Quick Start

### Your First Day

**Essential Setup:**
- Java 17 JDK installed
- Maven 3.6+ installed
- YugabyteDB installed locally
- IDE configured (IntelliJ IDEA recommended)
- Git repository cloned
- Access to team Slack/communication channels

**Time to First Commit:** 2-3 days (includes setup and learning)

---

## Development Environment Setup

### Prerequisites

```bash
# Verify Java version
java -version  # Should show Java 17

# Verify Maven
mvn -version

# Verify YugabyteDB installation
yugabyted version
```

### Clone and Build

```bash
# Clone repository
git clone https://github.com/YugabyteDB-Samples/yugastore-java.git
cd yugastore-java

# Build all services
mvn clean package -DskipTests

# This will take 3-5 minutes first time
```

### Database Setup

```bash
# Start YugabyteDB
yugabyted start

# Verify it's running
curl http://localhost:15433

# Create schemas
cd resources
cqlsh -f schema.cql    # YCQL tables
ysqlsh -f schema.sql   # YSQL tables

# Load test data
./dataload.sh          # Takes 2-3 minutes
```

### IDE Setup (IntelliJ IDEA)

1. **Open Project:**
   - File → Open → Select yugastore-java directory
   - Import as Maven project

2. **Configure Java SDK:**
   - File → Project Structure → Project
   - Set SDK to Java 17
   - Set Language Level to 17

3. **Enable Annotation Processing:**
   - Preferences → Build, Execution, Deployment → Compiler → Annotation Processors
   - Check "Enable annotation processing"

4. **Install Plugins (Recommended):**
   - Lombok
   - Spring Boot
   - Docker
   - Database Navigator

5. **Code Style:**
   - Preferences → Editor → Code Style → Java
   - Set indent to 2 spaces
   - Import code style from team settings (if available)

---

## Project Structure Navigation

```
yugastore-java/
├── pom.xml                          # Parent POM, defines all modules
├── resources/                       # Database schemas and data loading
│   ├── schema.cql                  # YCQL (Cassandra) table definitions
│   ├── schema.sql                  # YSQL (PostgreSQL) table definitions
│   └── dataload.sh                 # Data loading script
│
├── eureka-server-local/            # Service discovery (start first)
│   ├── pom.xml
│   └── src/main/java/
│       └── com/yugabyte/yugastore/eureka/
│           └── YugastoreEurekaServer.java
│
├── products-microservice/          # Product catalog (YCQL)
│   ├── pom.xml
│   └── src/main/java/
│       └── com/yugabyte/app/yugastore/
│           ├── YugastoreProducts.java          # Main class
│           ├── controller/
│           │   └── ProductCatalogController.java
│           ├── service/
│           │   ├── ProductService.java
│           │   └── ProductRankingService.java
│           ├── repository/
│           │   ├── ProductRepository.java
│           │   └── ProductRankingRepository.java
│           └── domain/
│               ├── ProductMetadata.java
│               └── ProductRanking.java
│
├── cart-microservice/              # Shopping cart (YSQL)
│   ├── pom.xml
│   └── src/main/java/
│       └── com/yugabyte/app/yugastore/cart/
│           ├── YugastoreCart.java
│           ├── controller/
│           │   └── ShoppingCartController.java
│           ├── service/
│           │   └── ShoppingCartImpl.java
│           ├── repository/
│           │   └── ShoppingCartRepository.java
│           └── domain/
│               └── ShoppingCart.java
│
├── checkout-microservice/          # Order processing (YCQL)
│   ├── pom.xml
│   └── src/main/java/
│       └── com/yugabyte/app/yugastore/cronoscheckoutapi/
│           ├── YugastoreCheckout.java
│           ├── controller/
│           │   └── CheckoutController.java
│           ├── service/
│           │   └── CheckoutServiceImpl.java
│           ├── rest/clients/       # Feign clients
│           │   ├── ShoppingCartRestClient.java
│           │   └── ProductCatalogRestClient.java
│           ├── repository/
│           └── domain/
│
├── api-gateway-microservice/       # API Gateway (aggregation layer)
│   ├── pom.xml
│   └── src/main/java/
│       └── com/yugabyte/app/yugastore/
│           ├── YugastoreApiGateway.java
│           ├── controller/
│           ├── service/
│           └── rest/clients/       # Feign clients to backend services
│
└── react-ui/                       # Frontend + BFF
    ├── pom.xml
    ├── src/main/java/              # Spring Boot BFF layer
    └── frontend/                   # React application
        ├── package.json
        ├── public/
        └── src/
            ├── index.js
            └── components/
                ├── App/
                ├── Cart/
                ├── Home/
                ├── Products/
                └── ShowProduct/
```

---

## Running the Application Locally

### Option 1: Run All Services (Recommended for Development)

**Terminal 1 - Eureka:**
```bash
cd eureka-server-local
mvn spring-boot:run
# Wait for "Started YugastoreEurekaServer" message
# Verify: http://localhost:8761
```

**Terminal 2 - Products:**
```bash
cd products-microservice
mvn spring-boot:run
# Wait for service registration with Eureka
```

**Terminal 3 - Cart:**
```bash
cd cart-microservice
mvn spring-boot:run
```

**Terminal 4 - Checkout:**
```bash
cd checkout-microservice
mvn spring-boot:run
```

**Terminal 5 - API Gateway:**
```bash
cd api-gateway-microservice
mvn spring-boot:run
```

**Terminal 6 - React UI:**
```bash
cd react-ui
mvn spring-boot:run
# Access application: http://localhost:8080
```

### Option 2: Run Only Services You're Working On

If you're only working on the Products service:

```bash
# Start minimal dependencies
cd eureka-server-local && mvn spring-boot:run &
sleep 30

# Start your service
cd products-microservice
mvn spring-boot:run
```

You can test your service directly:
```bash
curl http://localhost:8082/products-microservice/products?limit=10
```

### Option 3: Use Docker

```bash
# Build images
mvn clean package -DskipTests

# Start all containers
./docker-run.sh

# Check status
docker ps | grep yugastore
```

---

## Making Your First Code Change

### Example: Add a New Field to Product

**Step 1: Understand the Requirement**
- What field are we adding? (e.g., "manufacturer")
- What data type? (String, Integer, etc.)
- Where is it displayed? (Product detail page)
- Test cases? (Existing products, new products, null values)

**Step 2: Database Change (YCQL)**

Edit `resources/schema.cql`:
```cql
-- Add column to products table
ALTER TABLE cronos.products ADD manufacturer text;
```

Apply change:
```bash
cqlsh -f resources/schema_update.cql
```

**Step 3: Update Domain Model**

Edit `products-microservice/src/main/java/com/yugabyte/app/yugastore/domain/ProductMetadata.java`:

```java
@Table(value = "products", keyspace = "cronos")
public class ProductMetadata {
    
    @PrimaryKey
    private String asin;
    
    private String title;
    private String description;
    private Double price;
    private String imurl;
    
    // Add new field
    @Column("manufacturer")
    private String manufacturer;
    
    // Add getter and setter
    public String getManufacturer() {
        return manufacturer;
    }
    
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
}
```

**Step 4: Test Service Layer**

Service layer automatically picks up the new field (Spring Data Cassandra).

Test manually:
```bash
curl http://localhost:8082/products-microservice/product/B00001
# Should include "manufacturer" field in response
```

**Step 5: Update Frontend (if needed)**

Edit `react-ui/frontend/src/components/ShowProduct/index.js`:

```javascript
render() {
    const product = this.state.product;
    return (
        <div>
            <h2>{product.title}</h2>
            <p>Manufacturer: {product.manufacturer}</p>
            {/* ... rest of component ... */}
        </div>
    );
}
```

**Step 6: Write Tests**

Edit `products-microservice/src/test/java/com/yugabyte/app/yugastore/service/ProductServiceTest.java`:

```java
@Test
public void testProductHasManufacturer() {
    ProductMetadata product = productService.findById("B00001").get();
    assertNotNull(product);
    assertNotNull(product.getManufacturer());
}

@Test
public void testProductWithNullManufacturer() {
    ProductMetadata product = new ProductMetadata();
    product.setAsin("TEST001");
    product.setManufacturer(null);
    
    // Should not throw exception
    productRepository.save(product);
    
    Optional<ProductMetadata> retrieved = productService.findById("TEST001");
    assertTrue(retrieved.isPresent());
    assertNull(retrieved.get().getManufacturer());
}
```

Run tests:
```bash
cd products-microservice
mvn test
```

**Step 7: Commit Your Changes**

```bash
git checkout -b feature/add-manufacturer-field
git add .
git commit -m "Add manufacturer field to products

- Added manufacturer column to YCQL schema
- Updated ProductMetadata domain model
- Added getters/setters for manufacturer field
- Updated UI to display manufacturer
- Added unit tests for null and non-null cases"

git push origin feature/add-manufacturer-field
```

**Step 8: Create Pull Request**

- Include description of the change
- Link to user story/ticket
- List testing performed
- Add screenshots (if UI change)

---

## Common Development Tasks

### Task: Add a New REST Endpoint

**Scenario:** Add endpoint to get products by price range

**Step 1: Add Repository Method**

`products-microservice/.../repository/ProductRepository.java`:
```java
@Repository
public interface ProductRepository extends CassandraRepository<ProductMetadata, String> {
    
    // Spring Data Cassandra doesn't support range queries well on non-partition keys
    // Use custom query
    @Query("SELECT * FROM cronos.products")
    List<ProductMetadata> findAll();
}
```

**Step 2: Add Service Method**

`products-microservice/.../service/ProductService.java`:
```java
@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    public List<ProductMetadata> findByPriceRange(double minPrice, double maxPrice) {
        // Since YCQL doesn't support range queries on non-partition keys,
        // we filter in application layer (or create secondary index)
        return productRepository.findAll().stream()
            .filter(p -> p.getPrice() != null)
            .filter(p -> p.getPrice() >= minPrice && p.getPrice() <= maxPrice)
            .collect(Collectors.toList());
    }
}
```

**Step 3: Add Controller Endpoint**

`products-microservice/.../controller/ProductCatalogController.java`:
```java
@RestController
@RequestMapping(value = "/products-microservice")
public class ProductCatalogController {
    
    @Autowired
    ProductService productService;
    
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/products/priceRange",
        produces = "application/json"
    )
    public ResponseEntity<List<ProductMetadata>> getProductsByPriceRange(
            @RequestParam("minPrice") double minPrice,
            @RequestParam("maxPrice") double maxPrice) {
        
        List<ProductMetadata> products = 
            productService.findByPriceRange(minPrice, maxPrice);
        
        return new ResponseEntity<>(products, HttpStatus.OK);
    }
}
```

**Step 4: Test the Endpoint**

```bash
# Restart the service
mvn spring-boot:run

# Test with curl
curl "http://localhost:8082/products-microservice/products/priceRange?minPrice=10&maxPrice=50"
```

**Step 5: Add to API Gateway**

Add Feign client method and gateway controller if needed.

---

### Task: Fix a Bug

**Scenario:** Cart quantity increments by 2 instead of 1

**Step 1: Reproduce the Bug**

```bash
# Add product to cart
curl -X GET "http://localhost:8083/cart-microservice/shoppingCart/addProduct?userid=u1001&asin=B00001"

# Check cart
curl "http://localhost:8083/cart-microservice/shoppingCart/productsInCart?userid=u1001"
# Bug: Quantity shows 2 instead of 1
```

**Step 2: Find the Code**

`cart-microservice/.../service/ShoppingCartImpl.java`:
```java
public void addProductToShoppingCart(String userId, String asin) {
    String key = userId + "_" + asin;
    Optional<ShoppingCart> cartItem = shoppingCartRepository.findById(key);
    
    if (cartItem.isPresent()) {
        ShoppingCart item = cartItem.get();
        item.setQuantity(item.getQuantity() + 1);  // Bug might be here
        shoppingCartRepository.save(item);
    } else {
        // Create new item
        ShoppingCart newItem = new ShoppingCart();
        newItem.setCartKey(key);
        newItem.setUserId(userId);
        newItem.setAsin(asin);
        newItem.setQuantity(1);  // Or here
        newItem.setTimeAdded(new Date().toString());
        shoppingCartRepository.save(newItem);
    }
}
```

**Step 3: Add Logging**

```java
public void addProductToShoppingCart(String userId, String asin) {
    String key = userId + "_" + asin;
    System.out.println("Adding product to cart: " + key);
    
    Optional<ShoppingCart> cartItem = shoppingCartRepository.findById(key);
    
    if (cartItem.isPresent()) {
        ShoppingCart item = cartItem.get();
        int oldQuantity = item.getQuantity();
        item.setQuantity(item.getQuantity() + 1);
        System.out.println("Updated quantity from " + oldQuantity + " to " + item.getQuantity());
        shoppingCartRepository.save(item);
    } else {
        System.out.println("Creating new cart item with quantity 1");
        // ...
    }
}
```

**Step 4: Identify Root Cause**

After testing with logs, you discover the method is being called twice (duplicate API call from frontend).

**Step 5: Write a Test**

```java
@Test
public void testAddProductToCartIncrementsCorrectly() {
    String userId = "testUser";
    String asin = "B00001";
    
    // Clear any existing items
    shoppingCart.clearCart(userId);
    
    // Add product once
    shoppingCart.addProductToShoppingCart(userId, asin);
    
    // Verify quantity is 1
    Map<String, Integer> cart = shoppingCart.getProductsInCart(userId);
    assertEquals(Integer.valueOf(1), cart.get(asin));
    
    // Add same product again
    shoppingCart.addProductToShoppingCart(userId, asin);
    
    // Verify quantity is 2
    cart = shoppingCart.getProductsInCart(userId);
    assertEquals(Integer.valueOf(2), cart.get(asin));
}
```

**Step 6: Fix the Issue**

If the issue is in the frontend (duplicate call), fix it there.
If the issue is in the backend, make the operation idempotent.

**Step 7: Commit with Clear Message**

```bash
git commit -m "Fix: Cart quantity increments correctly

Issue: Cart quantity was incrementing by 2 when adding product
Root cause: Duplicate API calls from frontend onClick handler
Solution: Added event.preventDefault() and disabled button during API call

Test: Added regression test for quantity increment behavior"
```

---

### Task: Write Unit Tests

**Good Unit Test Characteristics:**
- Fast (runs in milliseconds)
- Isolated (mocks external dependencies)
- Repeatable (same result every time)
- Self-validating (passes or fails clearly)
- Timely (written before or with production code)

**Example: Testing ProductService**

```java
package com.yugabyte.app.yugastore.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @InjectMocks
    private ProductService productService;
    
    private ProductMetadata testProduct;
    
    @BeforeEach
    public void setUp() {
        testProduct = new ProductMetadata();
        testProduct.setAsin("B00001");
        testProduct.setTitle("Test Product");
        testProduct.setPrice(29.99);
        testProduct.setDescription("Test description");
    }
    
    @Test
    public void testFindById_ProductExists_ReturnsProduct() {
        // Arrange
        when(productRepository.findById("B00001"))
            .thenReturn(Optional.of(testProduct));
        
        // Act
        Optional<ProductMetadata> result = productService.findById("B00001");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("B00001", result.get().getAsin());
        assertEquals("Test Product", result.get().getTitle());
        verify(productRepository, times(1)).findById("B00001");
    }
    
    @Test
    public void testFindById_ProductNotExists_ReturnsEmpty() {
        // Arrange
        when(productRepository.findById("INVALID"))
            .thenReturn(Optional.empty());
        
        // Act
        Optional<ProductMetadata> result = productService.findById("INVALID");
        
        // Assert
        assertFalse(result.isPresent());
        verify(productRepository, times(1)).findById("INVALID");
    }
    
    @Test
    public void testFindByPriceRange_ReturnsFilteredProducts() {
        // Arrange
        ProductMetadata product1 = new ProductMetadata();
        product1.setAsin("B00001");
        product1.setPrice(15.00);
        
        ProductMetadata product2 = new ProductMetadata();
        product2.setAsin("B00002");
        product2.setPrice(25.00);
        
        ProductMetadata product3 = new ProductMetadata();
        product3.setAsin("B00003");
        product3.setPrice(35.00);
        
        when(productRepository.findAll())
            .thenReturn(Arrays.asList(product1, product2, product3));
        
        // Act
        List<ProductMetadata> results = 
            productService.findByPriceRange(20.00, 30.00);
        
        // Assert
        assertEquals(1, results.size());
        assertEquals("B00002", results.get(0).getAsin());
    }
    
    @Test
    public void testFindByPriceRange_HandlesNullPrices() {
        // Arrange
        ProductMetadata productWithPrice = new ProductMetadata();
        productWithPrice.setAsin("B00001");
        productWithPrice.setPrice(25.00);
        
        ProductMetadata productWithoutPrice = new ProductMetadata();
        productWithoutPrice.setAsin("B00002");
        productWithoutPrice.setPrice(null);
        
        when(productRepository.findAll())
            .thenReturn(Arrays.asList(productWithPrice, productWithoutPrice));
        
        // Act
        List<ProductMetadata> results = 
            productService.findByPriceRange(20.00, 30.00);
        
        // Assert
        assertEquals(1, results.size());
        assertEquals("B00001", results.get(0).getAsin());
    }
}
```

**Run Tests:**
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ProductServiceTest

# Run specific test method
mvn test -Dtest=ProductServiceTest#testFindById_ProductExists_ReturnsProduct
```

---

### Task: Debug a Service

**Scenario:** Checkout is failing with "Product not found"

**Step 1: Check Logs**

```bash
# Tail logs
tail -f checkout-microservice/target/*.log

# Or watch console output
cd checkout-microservice
mvn spring-boot:run
```

**Step 2: Add Debug Logging**

```java
@RestController
public class CheckoutController {
    
    @RequestMapping(method = RequestMethod.POST, value = "/shoppingCart/checkout")
    public CheckoutStatus checkout() {
        String userId = "u1001";
        System.out.println("DEBUG: Starting checkout for user: " + userId);
        
        CheckoutStatus checkoutStatus = new CheckoutStatus();
        try {
            Order currentOrder = checkoutService.checkout(userId);
            System.out.println("DEBUG: Order created: " + currentOrder.getId());
            // ...
        } catch (Exception e) {
            System.err.println("ERROR: Checkout failed: " + e.getMessage());
            e.printStackTrace();
        }
        return checkoutStatus;
    }
}
```

**Step 3: Use Debugger (IntelliJ)**

1. Set breakpoint in `CheckoutServiceImpl.checkout()`
2. Right-click on `YugastoreCheckout.java` → Debug
3. Trigger checkout from UI or curl
4. Step through code (F8 = step over, F7 = step into)
5. Inspect variables in Debug panel
6. Evaluate expressions in Evaluate window (Alt+F8)

**Step 4: Check Service Communication**

```bash
# Verify Eureka registration
curl http://localhost:8761/eureka/apps

# Test Products service directly
curl http://localhost:8082/products-microservice/product/B00001

# Test Cart service directly
curl "http://localhost:8083/cart-microservice/shoppingCart/productsInCart?userid=u1001"
```

**Step 5: Check Database**

```bash
# YCQL (Products, Checkout)
cqlsh

USE cronos;

SELECT * FROM products WHERE asin = 'B00001';
SELECT * FROM product_inventory WHERE asin = 'B00001';
SELECT * FROM orders LIMIT 10;

# YSQL (Cart)
ysqlsh

SELECT * FROM shopping_cart WHERE user_id = 'u1001';
```

**Step 6: Use Postman/Curl for Isolation**

```bash
# Test checkout directly (bypass UI)
curl -X POST http://localhost:8086/checkout-microservice/shoppingCart/checkout

# Test with verbose output
curl -v -X POST http://localhost:8086/checkout-microservice/shoppingCart/checkout
```

---

## Code Style Guide

### Java Conventions

**Naming:**
```java
// Classes: PascalCase
public class ProductService { }

// Methods: camelCase
public List<Product> getProductsByCategory() { }

// Variables: camelCase
private String userName;

// Constants: UPPER_SNAKE_CASE
private static final int MAX_RETRY_ATTEMPTS = 3;

// Packages: lowercase
package com.yugabyte.app.yugastore.service;
```

**Formatting:**
```java
// 2-space indentation
public void exampleMethod() {
  if (condition) {
    doSomething();
  }
}

// Line length: 120 characters max

// Braces: K&R style (opening brace on same line)
if (condition) {
  // code
} else {
  // code
}
```

**Comments:**
```java
/**
 * Javadoc for public methods
 * 
 * @param userId The unique identifier for the user
 * @return List of products in user's cart
 * @throws UserNotFoundException if user doesn't exist
 */
public List<Product> getCartItems(String userId) {
  // Inline comments for complex logic
  // Explain WHY, not WHAT
  
  // Good: Explain the reasoning
  // We cache for 5 minutes to reduce database load during peak hours
  cache.put(key, value, Duration.ofMinutes(5));
  
  // Bad: Obvious comment
  // Set quantity to 1
  item.setQuantity(1);
}
```

### REST API Conventions

**Endpoint Naming:**
```
GET    /products              # Get all products
GET    /products/{id}         # Get specific product
POST   /products              # Create product
PUT    /products/{id}         # Update product
DELETE /products/{id}         # Delete product

# Use plural nouns
# Use hyphens for multi-word (not camelCase or snake_case)
GET    /product-categories
```

**Response Format:**
```java
// Success: HTTP 200, 201, 204
{
  "data": { ... },
  "message": "Success"
}

// Error: HTTP 400, 404, 500
{
  "error": "Product not found",
  "code": "PRODUCT_NOT_FOUND",
  "timestamp": "2026-01-27T10:30:00Z"
}
```

---

## Testing Best Practices

### Test Naming Convention

```java
// Pattern: methodName_scenario_expectedBehavior

@Test
public void addProductToCart_newProduct_createsCartItem() { }

@Test
public void addProductToCart_existingProduct_incrementsQuantity() { }

@Test
public void checkout_emptyCart_throwsException() { }

@Test
public void checkout_insufficientStock_returnsFailureStatus() { }
```

### Test Structure: AAA Pattern

```java
@Test
public void calculateTotal_multipleItems_returnsSumOfPrices() {
    // Arrange - Set up test data
    ShoppingCart cart = new ShoppingCart();
    cart.addItem(new CartItem("B00001", 2, 10.00));
    cart.addItem(new CartItem("B00002", 1, 15.00));
    
    // Act - Execute the method being tested
    double total = cart.calculateTotal();
    
    // Assert - Verify the results
    assertEquals(35.00, total, 0.01);
}
```

### Mock vs Real Dependencies

**Use Mocks When:**
- External services (APIs, databases)
- Slow operations (network calls, file I/O)
- Non-deterministic behavior (random, time-based)

**Use Real Objects When:**
- Simple POJOs
- Value objects
- Utility classes

```java
// Mock external dependency
@Mock
private ProductRepository productRepository;

// Use real object
private ProductMetadata product = new ProductMetadata();
```

### Test Coverage Goals

- **Critical Paths:** 90%+ (checkout, payment)
- **Business Logic:** 80%+ (cart operations, pricing)
- **Controllers:** 70%+ (endpoint validation)
- **Utilities:** 60%+ (helpers, formatters)

Check coverage:
```bash
mvn test jacoco:report
# Open: target/site/jacoco/index.html
```

---

## Git Workflow

### Branch Strategy

```bash
# Main branches
main           # Production-ready code
develop        # Integration branch (if using GitFlow)

# Feature branches
git checkout -b feature/add-manufacturer-field
git checkout -b feature/JIRA-123-product-search

# Bug fix branches
git checkout -b fix/cart-quantity-bug
git checkout -b fix/JIRA-456-checkout-error

# Hotfix branches (for production issues)
git checkout -b hotfix/critical-security-patch
```

### Commit Messages

**Format:**
```
<type>: <subject>

<body>

<footer>
```

**Types:**
- `feat:` New feature
- `fix:` Bug fix
- `refactor:` Code refactoring
- `test:` Adding tests
- `docs:` Documentation
- `style:` Formatting, whitespace
- `chore:` Build, dependencies

**Examples:**

```bash
# Good commit message
git commit -m "feat: Add manufacturer field to products

- Added manufacturer column to YCQL products table
- Updated ProductMetadata domain model with getter/setter
- Updated product detail page to display manufacturer
- Added unit tests for null and non-null manufacturer values

Closes JIRA-123"

# Bad commit messages
git commit -m "fixed bug"
git commit -m "wip"
git commit -m "updated code"
```

### Pull Request Checklist

Before submitting PR:

- [ ] Code builds without errors
- [ ] All tests pass
- [ ] New tests added for new functionality
- [ ] Code follows style guidelines
- [ ] No commented-out code or debug logs
- [ ] Documentation updated (if needed)
- [ ] Self-reviewed code
- [ ] Meaningful commit messages
- [ ] PR description explains what and why

---

## Code Review Guidelines

### As Author

**Before Requesting Review:**
1. Self-review your changes
2. Run all tests locally
3. Check for typos and formatting
4. Remove debug code and logs
5. Ensure PR is focused (one feature/fix)

**Responding to Feedback:**
- Respond to every comment
- Accept constructive criticism professionally
- Ask questions if feedback is unclear
- Make requested changes or explain why not
- Thank reviewers for their time

### As Reviewer

**What to Look For:**
- **Correctness:** Does code do what it's supposed to?
- **Quality:** Is code readable, maintainable, tested?
- **Design:** Does it fit the architecture?
- **Performance:** Any obvious performance issues?
- **Security:** Any security vulnerabilities?

**How to Give Feedback:**

```
# Good feedback
"Consider extracting this logic into a separate method for readability.
It would make testing easier too."

"This query might be slow with large datasets. 
Have you considered adding an index on the 'category' column?"

# Bad feedback
"This is wrong."
"Why did you do it this way?"
```

**Approval Criteria:**
- Code meets acceptance criteria
- Tests are adequate
- No critical issues
- Minor issues can be addressed in follow-up

---

## Common Patterns in YugaStore

### Pattern: Service Layer with Repository

```java
// Repository (Data Access)
@Repository
public interface ProductRepository extends CassandraRepository<ProductMetadata, String> {
    List<ProductMetadata> findByCategory(String category);
}

// Service (Business Logic)
@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    public Optional<ProductMetadata> findById(String asin) {
        return productRepository.findById(asin);
    }
    
    public List<ProductMetadata> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }
}

// Controller (API)
@RestController
@RequestMapping("/products-microservice")
public class ProductCatalogController {
    
    @Autowired
    private ProductService productService;
    
    @GetMapping("/product/{asin}")
    public ResponseEntity<ProductMetadata> getProduct(@PathVariable String asin) {
        Optional<ProductMetadata> product = productService.findById(asin);
        
        return product
            .map(p -> new ResponseEntity<>(p, HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
```

### Pattern: Feign Client for Service-to-Service Communication

```java
// Define interface
@FeignClient("products-microservice")
public interface ProductCatalogRestClient {
    
    @RequestMapping("/products-microservice/product/{asin}")
    ProductMetadata getProductDetails(@PathVariable("asin") String asin);
}

// Use in service
@Service
public class CheckoutServiceImpl {
    
    @Autowired
    private ProductCatalogRestClient productCatalogClient;
    
    public void validateProduct(String asin) {
        ProductMetadata product = productCatalogClient.getProductDetails(asin);
        if (product == null) {
            throw new ProductNotFoundException(asin);
        }
    }
}
```

### Pattern: Exception Handling

```java
// Custom exception
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String asin) {
        super("Product not found: " + asin);
    }
}

// Controller advice (global exception handler)
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(
            ProductNotFoundException ex) {
        
        ErrorResponse error = new ErrorResponse(
            "PRODUCT_NOT_FOUND",
            ex.getMessage(),
            LocalDateTime.now()
        );
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}
```

---

## Troubleshooting Guide

### Problem: Service Won't Start

**Symptoms:**
- `Address already in use` error
- Service crashes on startup
- `Connection refused` errors

**Solutions:**

```bash
# Check if port is in use
lsof -i :8082
# Kill process
kill -9 <PID>

# Check if Eureka is running
curl http://localhost:8761

# Check if YugabyteDB is running
cqlsh  # Should connect
ysqlsh # Should connect

# View full error
mvn spring-boot:run | grep -A 20 "ERROR"
```

### Problem: Tests Failing Locally

**Symptoms:**
- Tests pass in IDE but fail in Maven
- Intermittent test failures
- Database connection errors in tests

**Solutions:**

```bash
# Clean and rebuild
mvn clean install

# Run tests with debug output
mvn test -X

# Run single test
mvn test -Dtest=ProductServiceTest

# Skip flaky tests temporarily
mvn test -Dtest=!FlakyTest
```

### Problem: Can't Connect to Database

**Symptoms:**
- `Connection refused` to YugabyteDB
- Timeout errors
- `No suitable driver found`

**Solutions:**

```bash
# Check YugabyteDB status
yugabyted status

# Restart YugabyteDB
yugabyted stop
yugabyted start

# Verify ports are open
nc -zv localhost 5433  # YSQL
nc -zv localhost 9042  # YCQL

# Check application.yml has correct connection string
cat cart-microservice/src/main/resources/application.yml | grep url
```

### Problem: Service Not Registering with Eureka

**Symptoms:**
- Service not visible in Eureka dashboard
- `No instances available` errors
- Service can't discover other services

**Solutions:**

```bash
# Check Eureka is running
curl http://localhost:8761/eureka/apps

# Check bootstrap.yml
cat products-microservice/src/main/resources/bootstrap.yml

# Wait 30-60 seconds after service starts
# Services register asynchronously

# Check service logs for registration errors
tail -f logs/spring.log | grep "Eureka"
```

### Problem: Feign Client Timeout

**Symptoms:**
- `Read timed out` errors
- Service calls taking too long
- Intermittent failures

**Solutions:**

```java
// Increase timeout in application.yml
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 10000

// Add logging to see what's happening
logging:
  level:
    com.yugabyte: DEBUG
    feign: DEBUG
```

---

## Performance Tips

### Database Query Optimization

**YCQL (Cassandra):**

```java
// Bad: Full table scan
List<ProductMetadata> products = productRepository.findAll();
products.stream()
    .filter(p -> p.getCategory().equals("Electronics"))
    .collect(Collectors.toList());

// Good: Use index
List<ProductRanking> products = 
    productRankingService.getProductsByCategory("Electronics", 100, 0);
```

**YSQL (PostgreSQL):**

```java
// Bad: N+1 query problem
List<Cart> carts = cartRepository.findAll();
for (Cart cart : carts) {
    List<CartItem> items = itemRepository.findByCartId(cart.getId());
    // Process items
}

// Good: Use JOIN or batch fetch
@Query("SELECT c FROM Cart c JOIN FETCH c.items WHERE c.userId = :userId")
Cart findCartWithItems(@Param("userId") String userId);
```

### Avoid Common Anti-Patterns

```java
// Anti-pattern: String concatenation in loop
String result = "";
for (String item : items) {
    result += item + ", ";  // Creates new String each iteration
}

// Better: Use StringBuilder
StringBuilder result = new StringBuilder();
for (String item : items) {
    result.append(item).append(", ");
}

// Anti-pattern: Catching generic Exception
try {
    doSomething();
} catch (Exception e) {  // Too broad
    // Can't handle properly
}

// Better: Catch specific exceptions
try {
    doSomething();
} catch (ProductNotFoundException e) {
    // Handle product not found
} catch (DatabaseException e) {
    // Handle database issues
}
```

---

## Quick Reference Commands

### Maven Commands

```bash
# Build project (skip tests)
mvn clean package -DskipTests

# Build and run tests
mvn clean install

# Run specific service
cd <service-directory>
mvn spring-boot:run

# Debug mode (port 5005)
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"

# Run tests
mvn test

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Generate test coverage report
mvn clean test jacoco:report
```

### Database Commands

```bash
# YCQL (Cassandra)
cqlsh

# View keyspaces
DESCRIBE KEYSPACES;

# Use keyspace
USE cronos;

# View tables
DESCRIBE TABLES;

# View table schema
DESCRIBE TABLE products;

# Query data
SELECT * FROM products LIMIT 10;
SELECT COUNT(*) FROM products;

# YSQL (PostgreSQL)
ysqlsh

# List databases
\l

# Connect to database
\c postgres

# List tables
\dt

# Describe table
\d shopping_cart

# Query data
SELECT * FROM shopping_cart LIMIT 10;

# Exit
\q
```

### Docker Commands

```bash
# List containers
docker ps

# View logs
docker logs <container-id>

# Follow logs
docker logs -f <container-id>

# Stop container
docker stop <container-id>

# Remove container
docker rm <container-id>

# Remove all stopped containers
docker container prune

# View images
docker images

# Remove image
docker rmi <image-name>
```

### Git Commands

```bash
# Create branch
git checkout -b feature/my-feature

# Stage changes
git add .                  # All files
git add file.java          # Specific file

# Commit
git commit -m "message"

# Push
git push origin feature/my-feature

# Pull latest from main
git checkout main
git pull origin main

# Rebase feature branch
git checkout feature/my-feature
git rebase main

# Stash changes
git stash
git stash pop

# View status
git status

# View diff
git diff
git diff --staged
```

---

## Resources

### Documentation

- **Project README:** [../README.md](../README.md)
- **Architecture:** [architecture.md](architecture.md)
- **Business Requirements:** [business-requirements.md](business-requirements.md)
- **Delivery Guide:** [delivery-guide.md](delivery-guide.md)
- **Coding Standards:** [coding-standards.md](coding-standards.md) ⭐ NEW - Idiomatic standards for code generation and reviews

### External Resources

**Spring Boot:**
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Spring Data Cassandra](https://docs.spring.io/spring-data/cassandra/docs/current/reference/html/)

**YugabyteDB:**
- [YugabyteDB Docs](https://docs.yugabyte.com/)
- [YSQL Reference](https://docs.yugabyte.com/latest/api/ysql/)
- [YCQL Reference](https://docs.yugabyte.com/latest/api/ycql/)

**Testing:**
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)

**React:**
- [React Documentation](https://react.dev/)
- [React Router](https://reactrouter.com/)

---

## Getting Help

### When You're Stuck

1. **Check documentation** (this guide, README, architecture docs)
2. **Search logs** for error messages
3. **Google the error** (Stack Overflow, GitHub issues)
4. **Ask teammate** (Slack, pair programming)
5. **Ask team lead** (after spending 30-60 minutes troubleshooting)

### Asking Good Questions

**Bad:**
> "It doesn't work"

**Good:**
> "I'm trying to add a product to cart (code in CartController line 45), but getting a NullPointerException. I've verified the user exists in the database and the product exists. Logs show: [paste log]. Any ideas?"

**Include:**
- What you're trying to do
- What you've tried
- Error messages/logs
- Relevant code snippets
- Expected vs actual behavior

---

## Your Development Workflow Checklist

### Starting a New Task

- [ ] Read user story and acceptance criteria
- [ ] Ask clarifying questions if needed
- [ ] Check out new feature branch
- [ ] Verify services are running locally
- [ ] Identify affected services and files
- [ ] Write failing test (TDD approach) or plan test strategy

### During Development

- [ ] Follow code style guidelines
- [ ] Write meaningful commit messages
- [ ] Commit frequently (small, logical changes)
- [ ] Run tests after each change
- [ ] Add logging for debugging
- [ ] Test manually in browser/Postman

### Before Code Review

- [ ] Self-review code
- [ ] Run all tests (mvn test)
- [ ] Build project (mvn package)
- [ ] Test full user flow manually
- [ ] Remove debug code and console.logs
- [ ] Update documentation if needed
- [ ] Write clear PR description

### After Code Review

- [ ] Address all review comments
- [ ] Re-test after making changes
- [ ] Thank reviewers
- [ ] Merge after approval
- [ ] Delete feature branch
- [ ] Verify in deployed environment

---

**Document Version:** 1.0  
**Last Updated:** January 27, 2026  
**Next Review:** Quarterly or as needed

---

*Happy coding! Remember: clean code is better than clever code. When in doubt, ask for help.*
