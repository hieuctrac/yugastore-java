# YugaStore Java - Coding Standards

**Version:** 1.0  
**Date:** January 27, 2026  
**Audience:** Software Engineers, Copilot Agents, Code Reviewers  
**Purpose:** Defines idiomatic coding standards for YugaStore Java microservices

---

## Table of Contents

1. [Overview](#overview)
2. [Package Structure and Naming](#package-structure-and-naming)
3. [Java Language Standards](#java-language-standards)
4. [Spring Boot Conventions](#spring-boot-conventions)
5. [REST API Design](#rest-api-design)
6. [Database Integration](#database-integration)
7. [Service Layer Patterns](#service-layer-patterns)
8. [Error Handling](#error-handling)
9. [Testing Standards](#testing-standards)
10. [Documentation Requirements](#documentation-requirements)
11. [Code Quality Rules](#code-quality-rules)

---

## Overview

### Purpose

This document establishes coding standards for the YugaStore Java microservices platform. These standards:

- **Ensure consistency** across all microservices
- **Enable Copilot Agents** to generate idiomatic code
- **Guide code reviews** with objective criteria
- **Facilitate onboarding** for new developers
- **Maintain quality** as the codebase evolves

### Scope

Standards apply to all Java code in:
- Microservices (`*-microservice/` directories)
- Domain models
- Service layers
- Controllers
- Repositories
- Configuration classes
- Test code

### Enforcement

- **MUST**: Mandatory requirement, non-negotiable
- **SHOULD**: Strong recommendation, exceptions require justification
- **MAY**: Optional, use discretion

---

## Package Structure and Naming

### Standard Package Hierarchy

**Pattern:**
```
com.yugabyte.app.yugastore.<microservice>.<layer>
```

**MUST follow this structure:**

```
products-microservice/src/main/java/
└── com/yugabyte/app/yugastore/
    ├── controller/              # REST controllers
    ├── service/                 # Business logic
    │   └── impl/               # Service implementations (if using interfaces)
    ├── repository/             # Data access (alternative: repo/)
    ├── domain/                 # Domain models/entities
    ├── config/                 # Configuration classes
    ├── rest/clients/           # Feign clients
    └── exception/              # Custom exceptions
```

**Examples from codebase:**
- ✅ `com.yugabyte.app.yugastore.controller` (products-microservice)
- ✅ `com.yugabyte.app.yugastore.cart.service` (cart-microservice)
- ✅ `com.yugabyte.app.yugastore.cronoscheckoutapi.service` (checkout-microservice)

### Package Naming Rules

**MUST:**
- Use all lowercase letters
- Use plural nouns for collections: `repositories`, `controllers`, `services`
- Be concise but descriptive

**SHOULD NOT:**
- Mix singular/plural inconsistently
- Use abbreviations unless widely understood (e.g., `impl` for implementations)

---

## Java Language Standards

### Naming Conventions

**Classes (PascalCase):**
```java
// ✅ CORRECT
public class ProductCatalogController { }
public class ShoppingCartServiceImpl { }
public class ProductMetadata { }

// ❌ INCORRECT
public class productController { }
public class Shopping_Cart_Service { }
```

**Methods and Variables (camelCase):**
```java
// ✅ CORRECT
public List<Product> getProductsByCategory(String categoryName) {
    int maxResults = 100;
    String userId = "u1001";
}

// ❌ INCORRECT
public List<Product> GetProductsByCategory(String category_name) {
    int MaxResults = 100;
}
```

**Constants (UPPER_SNAKE_CASE):**
```java
// ✅ CORRECT
private static final int DEFAULT_QUANTITY = 1;
private static final String DEFAULT_USER_ID = "u1001";

// ❌ INCORRECT
private static final int defaultQuantity = 1;
```

**Package Names (lowercase):**
```java
// ✅ CORRECT
package com.yugabyte.app.yugastore.service;

// ❌ INCORRECT
package com.yugabyte.app.yugastore.Service;
package com.yugabyte.app.yugastore.serviceLayer;
```

### Code Formatting

**Indentation:**
- **MUST** use 2 spaces (not tabs)
- **MUST** be consistent across entire file

**Braces:**
- **MUST** use K&R style (opening brace on same line)
```java
// ✅ CORRECT
if (condition) {
  doSomething();
} else {
  doSomethingElse();
}

// ❌ INCORRECT
if (condition)
{
  doSomething();
}
```

**Line Length:**
- **SHOULD** limit lines to 120 characters
- **MAY** exceed for URLs, imports, or when breaking degrades readability

**Blank Lines:**
- **MUST** use one blank line between methods
- **SHOULD** use blank lines to separate logical sections within methods

### Imports

**MUST:**
- Use explicit imports (no wildcards except for static imports)
- Organize imports by groups (Java standard, third-party, project)
- Remove unused imports

```java
// ✅ CORRECT
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yugabyte.app.yugastore.domain.ProductMetadata;

// ❌ INCORRECT
import java.util.*;
import com.yugabyte.app.yugastore.domain.*;
```

---

## Spring Boot Conventions

### Annotation Standards

**Controller Layer:**
```java
// ✅ CORRECT - REST API controllers
@RestController
@RequestMapping(value = "/products-microservice")
public class ProductCatalogController {
  // Field injection NOT recommended, use constructor
  private final ProductService productService;
  
  @Autowired
  public ProductCatalogController(ProductService productService) {
    this.productService = productService;
  }
  
  @RequestMapping(
    method = RequestMethod.GET, 
    value = "/product/{asin}", 
    produces = "application/json"
  )
  public ProductMetadata getProductDetails(@PathVariable String asin) {
    return productService.findById(asin).get();
  }
}

// ✅ CORRECT - MVC controllers
@Controller
public class UserController {
  @GetMapping("/login")
  public String login(Model model) {
    return "login";
  }
}
```

**MUST:**
- Use `@RestController` for REST APIs that return JSON/XML
- Use `@Controller` for MVC controllers that return views
- Prefer constructor injection over field injection
- Make injected fields `final` when using constructor injection

**Service Layer:**
```java
// ✅ CORRECT - Simple service
@Service
public class ProductServiceImpl implements ProductService {
  private final ProductMetadataRepo productRepository;

  @Autowired
  public ProductServiceImpl(ProductMetadataRepo productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public Optional<ProductMetadata> findById(String id) {
    return productRepository.findById(id);
  }
}

// ✅ CORRECT - Session-scoped stateful service
@Service
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Transactional
public class ShoppingCartImpl {
  private final ShoppingCartRepository shoppingCartRepository;

  @Autowired
  public ShoppingCartImpl(ShoppingCartRepository shoppingCartRepository) {
    this.shoppingCartRepository = shoppingCartRepository;
  }
}
```

**MUST:**
- Use `@Service` annotation for service layer classes
- Use `@Transactional` for methods/classes requiring database transactions
- Specify scope explicitly if not singleton (e.g., session-scoped services)

**Repository Layer:**

```java
// ✅ CORRECT - YCQL (Cassandra) repository
@RepositoryRestResource(path = "product")
public interface ProductMetadataRepo extends CassandraRepository<ProductMetadata, String> {
  
  @Query("SELECT * FROM cronos.products limit ?0 offset ?1")
  @RestResource(path = "products", rel = "products")
  List<ProductMetadata> getProducts(@Param("limit") int limit, @Param("offset") int offset);

  Optional<ProductMetadata> findById(String id);
}

// ✅ CORRECT - YSQL (PostgreSQL) repository
@RepositoryRestResource
public interface ShoppingCartRepository extends CrudRepository<ShoppingCart, String> {
  
  @Query("SELECT * FROM shopping_cart WHERE user_id = ?1")
  Optional<List<ShoppingCart>> findProductsInCartByUserId(String userId);
  
  @Modifying
  @Transactional
  @Query(value = "UPDATE shopping_cart SET quantity = quantity + 1 WHERE user_id = ?1 AND asin = ?2", 
         nativeQuery = true)
  void updateQuantityForShoppingCart(String userId, String asin);
}
```

**MUST:**
- Extend appropriate base repository (`CassandraRepository` for YCQL, `JpaRepository`/`CrudRepository` for YSQL)
- Use `@Query` for custom queries
- Mark modifying queries with `@Modifying` and `@Transactional`

### Configuration Classes

```java
// ✅ CORRECT
@Configuration
@EnableAutoConfiguration
@Profile(value = "local")
class YugabyteYCQLConfig {

  @Configuration
  @EnableCassandraRepositories(basePackages = { "com.yugabyte.app.yugastore.repo" })
  class CassandraConfig extends AbstractCassandraConfiguration {

    @Value("${cronos.yugabyte.hostname:localhost}")
    private String cassandraHost;

    @Value("${cronos.yugabyte.port:9042}")
    private int cassandraPort;

    @Value("${cronos.yugabyte.keyspace:cronos}")
    private String keyspace;

    @Override
    public String getKeyspaceName() {
      return keyspace;
    }

    @Override
    public String getContactPoints() {
      return cassandraHost;
    }

    @Override
    public int getPort() {
      return cassandraPort;
    }
  }
}
```

**MUST:**
- Use `@Configuration` for configuration classes
- Use `@Profile` to differentiate local/cloud configurations
- Externalize configuration values to `application.yml`
- Provide sensible defaults with `@Value("${key:defaultValue}")`

---

## REST API Design

### Endpoint Naming

**MUST follow REST conventions:**

```java
// ✅ CORRECT
GET    /products                        // List all products
GET    /products?limit=10&offset=0      // Paginated list
GET    /product/{asin}                  // Single product by ID
POST   /products                        // Create product
PUT    /product/{asin}                  // Update product
DELETE /product/{asin}                  // Delete product

// Collection operations
GET    /products/category/{category}    // Products by category
POST   /shoppingCart/addProduct         // Add to cart
POST   /shoppingCart/checkout           // Checkout cart

// ❌ INCORRECT
GET    /getProducts
POST   /addProductToCart
GET    /product_details
```

**Rules:**
- Use **plural nouns** for collections: `/products`, `/orders`
- Use **singular nouns** for specific resources: `/product/{id}`
- Use **hyphens** for multi-word resources: `/product-categories`
- Use **HTTP methods** to indicate action, not URL verbs
- Use **path parameters** for resource identifiers: `/{asin}`
- Use **query parameters** for filtering/pagination: `?limit=10&offset=0`

### Request Mapping

**MUST specify method and content type:**

```java
// ✅ CORRECT
@RequestMapping(
  method = RequestMethod.GET, 
  value = "/products", 
  produces = "application/json"
)
public List<ProductMetadata> getProducts(
    @Param("limit") int limit,
    @Param("offset") int offset) {
  return productService.findAllProductsPageable(limit, offset);
}

// ✅ ALSO CORRECT - Using shorthand annotations
@GetMapping(value = "/products", produces = "application/json")
public List<ProductMetadata> getProducts(
    @RequestParam("limit") int limit,
    @RequestParam("offset") int offset) {
  return productService.findAllProductsPageable(limit, offset);
}
```

**MUST:**
- Specify HTTP method explicitly (`method = RequestMethod.GET` or `@GetMapping`)
- Specify `produces` for response content type
- Specify `consumes` when accepting request body
- Use `@PathVariable` for path parameters
- Use `@RequestParam` (not `@Param`) for query parameters

**Path Parameters vs Query Parameters:**
```java
// ✅ CORRECT - Path parameter for resource ID
@GetMapping("/product/{asin}")
public ProductMetadata getProduct(@PathVariable String asin) { }

// ✅ CORRECT - Query parameters for filtering/pagination
@GetMapping("/products")
public List<Product> getProducts(
    @RequestParam("limit") int limit,
    @RequestParam("offset") int offset) { }

// ✅ CORRECT - Combining both
@GetMapping("/products/category/{category}")
public List<ProductRanking> getProductsByCategory(
    @PathVariable String category,
    @RequestParam("limit") int limit,
    @RequestParam("offset") int offset) { }
```

### Response Handling

**MUST use ResponseEntity for explicit HTTP control:**

```java
// ✅ CORRECT - Explicit status codes and type safety
@GetMapping("/product/{asin}")
public ResponseEntity<ProductMetadata> getProductDetails(@PathVariable("asin") String asin) {
  ProductMetadata product = productService.getProductDetails(asin);
  return new ResponseEntity<>(product, HttpStatus.OK);
}

// ✅ ACCEPTABLE - Direct return when always 200 OK
@GetMapping("/product/{asin}")
public ProductMetadata getProductDetails(@PathVariable String asin) {
  return productService.findById(asin).get();
}

// ✅ CORRECT - Error handling with appropriate status
@GetMapping("/product/{asin}")
public ResponseEntity<ProductMetadata> getProductDetails(@PathVariable String asin) {
  Optional<ProductMetadata> product = productService.findById(asin);
  
  return product
    .map(p -> new ResponseEntity<>(p, HttpStatus.OK))
    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
}
```

**HTTP Status Codes:**
- `200 OK` - Successful GET, PUT, PATCH
- `201 Created` - Successful POST (resource created)
- `204 No Content` - Successful DELETE
- `400 Bad Request` - Invalid input
- `404 Not Found` - Resource doesn't exist
- `500 Internal Server Error` - Server-side error

---

## Database Integration

### Domain Models

**YCQL (Cassandra) Entities:**

```java
// ✅ CORRECT
@Table(value = "products")
public class ProductMetadata {
  
  @PrimaryKey
  private String id;
  
  private String title;
  
  private String description;
  
  private Double price;
  
  @Column(value = "imurl")
  private String imUrl;
  
  @CassandraType(type = Name.SET, typeArguments = Name.TEXT)
  private Set<String> categories;
  
  @CassandraType(type = Name.LIST, typeArguments = Name.TEXT)
  private List<String> also_bought;

  // Getters and setters...
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
```

**MUST:**
- Use `@Table(value = "table_name")` to specify table
- Use `@PrimaryKey` for primary keys
- Use `@Column(value = "column_name")` when Java field name differs from DB column
- Use `@CassandraType` for collection types (Set, List, Map)
- Provide both getter and setter methods

**YSQL (PostgreSQL) Entities:**

```java
// ✅ CORRECT
@Entity(name = "shopping_cart")
@Table(name = "shopping_cart")
public class ShoppingCart {
  
  @Id
  @Column(name = "cart_key")
  private String cartKey;

  @Column(name = "user_id")
  private String userId;
  
  @Column(name = "asin")
  private String asin;

  @Column(name = "time_added")
  private String time_added;
  
  @Column(name = "quantity")
  private int quantity;

  // Getters and setters...
}
```

**MUST:**
- Use `@Entity` and `@Table` annotations
- Use `@Id` for primary key
- Use `@Column(name = "column_name")` for all fields
- Follow JPA conventions

### Composite Keys (YCQL)

```java
// ✅ CORRECT - Composite key class
@PrimaryKeyClass
public class ProductRankingKey implements Serializable {
  
  private static final long serialVersionUID = -6646128061564873843L;

  @PrimaryKeyColumn(name = "asin", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
  private String asin;
  
  @PrimaryKeyColumn(name = "category", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
  private String category;

  // Getters, setters, equals, hashCode...
  
  @Override
  public int hashCode() {
    return Objects.hash(asin, category);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    ProductRankingKey other = (ProductRankingKey) obj;
    return Objects.equals(asin, other.asin) && Objects.equals(category, other.category);
  }
}

// ✅ CORRECT - Entity using composite key
@Table(value = "product_rankings")
public class ProductRanking {

  @PrimaryKey
  private ProductRankingKey asin;

  @Column(value = "sales_rank")
  private int salesRank;
  
  private String title;
}
```

**MUST:**
- Implement `Serializable` for composite key classes
- Override `equals()` and `hashCode()` methods
- Use `@PrimaryKeyColumn` with correct ordinal and type

### Repository Custom Queries

**YCQL Queries:**
```java
// ✅ CORRECT
@Query("SELECT * FROM cronos.products limit ?0 offset ?1")
List<ProductMetadata> getProducts(@Param("limit") int limit, @Param("offset") int offset);

@Query("SELECT * FROM cronos.product_rankings where category = ?0 limit ?1 offset ?2")
List<ProductRanking> getProductsByCategory(
    @Param("name") String category, 
    @Param("limit") int limit, 
    @Param("offset") int offset
);
```

**YSQL Queries:**
```java
// ✅ CORRECT - Native query
@Query(value = "UPDATE shopping_cart SET quantity = quantity + 1 WHERE user_id = ?1 AND asin = ?2", 
       nativeQuery = true)
@Modifying
@Transactional
void updateQuantityForShoppingCart(String userId, String asin);

// ✅ CORRECT - JPQL query
@Query("SELECT c FROM shopping_cart c WHERE c.userId = :userId")
Optional<List<ShoppingCart>> findProductsInCartByUserId(@Param("userId") String userId);
```

**MUST:**
- Use positional parameters (`?0`, `?1`) or named parameters (`:userId`)
- Specify `nativeQuery = true` for native SQL queries
- Add `@Modifying` for UPDATE/DELETE queries
- Add `@Transactional` for modifying queries

---

## Service Layer Patterns

### Service Implementation

**Interface + Implementation (Recommended for complex services):**

```java
// ✅ CORRECT - Service interface
public interface ProductService {
  Optional<ProductMetadata> findById(String id);
  List<ProductMetadata> findAllProductsPageable(int limit, int offset);
}

// ✅ CORRECT - Service implementation
@Service
public class ProductServiceImpl implements ProductService {
  
  private final ProductMetadataRepo productRepository;

  @Autowired
  public ProductServiceImpl(ProductMetadataRepo productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public Optional<ProductMetadata> findById(String id) {
    return productRepository.findById(id);
  }

  @Override
  public List<ProductMetadata> findAllProductsPageable(int limit, int offset) {
    return productRepository.getProducts(limit, offset);
  }
}
```

**Direct Implementation (Acceptable for simple services):**

```java
// ✅ ACCEPTABLE - No interface for simple services
@Service
public class ShoppingCartImpl {
  private final ShoppingCartRepository shoppingCartRepository;

  @Autowired
  public ShoppingCartImpl(ShoppingCartRepository shoppingCartRepository) {
    this.shoppingCartRepository = shoppingCartRepository;
  }

  public void addProductToShoppingCart(String userId, String asin) {
    // Business logic
  }
}
```

### Dependency Injection

**MUST use constructor injection:**

```java
// ✅ CORRECT
@RestController
public class ProductCatalogController {
  
  private final ProductService productService;
  private final ProductRankingService productRankingService;

  @Autowired
  public ProductCatalogController(
      ProductService productService,
      ProductRankingService productRankingService) {
    this.productService = productService;
    this.productRankingService = productRankingService;
  }
}

// ❌ INCORRECT - Field injection
@RestController
public class ProductCatalogController {
  
  @Autowired
  private ProductService productService;  // Avoid this
  
  @Autowired
  private ProductRankingService productRankingService;  // Avoid this
}
```

**Constructor injection benefits:**
- Immutability (fields can be `final`)
- Testability (easier to mock dependencies)
- Explicit dependencies (visible in constructor signature)

### Feign Clients

**MUST follow this pattern for inter-service communication:**

```java
// ✅ CORRECT - Feign client interface
@FeignClient("products-microservice")
public interface ProductCatalogRestClient {
  
  @RequestMapping("/products-microservice/product/{asin}")
  ProductMetadata getProductDetails(@PathVariable("asin") String asin);
  
  @RequestMapping("/products-microservice/products")
  List<ProductMetadata> getProducts(
      @RequestParam("limit") int limit, 
      @RequestParam("offset") int offset
  );
}

// ✅ CORRECT - Using Feign client in service
@Service
public class CheckoutServiceImpl {
  
  private final ProductCatalogRestClient productCatalogClient;
  
  @Autowired
  public CheckoutServiceImpl(ProductCatalogRestClient productCatalogClient) {
    this.productCatalogClient = productCatalogClient;
  }
  
  public void validateProduct(String asin) {
    ProductMetadata product = productCatalogClient.getProductDetails(asin);
    if (product == null) {
      throw new ProductNotFoundException(asin);
    }
  }
}
```

**MUST:**
- Use service name in `@FeignClient` (not hardcoded URLs)
- Match endpoint paths exactly from target microservice
- Use appropriate parameter annotations (`@PathVariable`, `@RequestParam`)

---

## Error Handling

### Custom Exceptions

**MUST create domain-specific exceptions:**

```java
// ✅ CORRECT
public class ProductNotFoundException extends RuntimeException {
  public ProductNotFoundException(String asin) {
    super("Product not found: " + asin);
  }
}

public class NotEnoughProductsInStockException extends RuntimeException {
  public NotEnoughProductsInStockException(String asin) {
    super("Not enough products in stock: " + asin);
  }
}
```

### Exception Handling in Controllers

**SHOULD handle exceptions at controller level:**

```java
// ✅ CORRECT - Try-catch in controller method
@PostMapping("/shoppingCart/checkout")
public CheckoutStatus checkout() {
  String userId = "u1001";
  CheckoutStatus checkoutStatus = new CheckoutStatus();
  
  try {
    Order currentOrder = checkoutService.checkout(userId);
    if (currentOrder != null) {
      checkoutStatus.setOrderNumber(currentOrder.getId().toString());
      checkoutStatus.setStatus(CheckoutStatus.SUCCESS);
    } else {
      checkoutStatus.setStatus(CheckoutStatus.FAILURE);
      checkoutStatus.setOrderDetails("Product is Out of Stock!");
    }
  } catch (NotEnoughProductsInStockException e) {
    checkoutStatus.setStatus(CheckoutStatus.FAILURE);
    checkoutStatus.setOrderDetails(e.getMessage());
  }
  
  return checkoutStatus;
}
```

**PREFERRED - Global exception handler (for future implementation):**

```java
// ✅ RECOMMENDED (not yet in codebase, but best practice)
@RestControllerAdvice
public class GlobalExceptionHandler {
  
  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFoundException ex) {
    ErrorResponse error = new ErrorResponse(
      "PRODUCT_NOT_FOUND",
      ex.getMessage(),
      LocalDateTime.now()
    );
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }
  
  @ExceptionHandler(NotEnoughProductsInStockException.class)
  public ResponseEntity<ErrorResponse> handleOutOfStock(NotEnoughProductsInStockException ex) {
    ErrorResponse error = new ErrorResponse(
      "OUT_OF_STOCK",
      ex.getMessage(),
      LocalDateTime.now()
    );
    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
  }
}
```

---

## Testing Standards

### Test Naming

**MUST use descriptive test names:**

```java
// ✅ CORRECT - Pattern: methodName_scenario_expectedBehavior
@Test
public void addProductToCart_newProduct_createsCartItem() { }

@Test
public void addProductToCart_existingProduct_incrementsQuantity() { }

@Test
public void checkout_emptyCart_throwsException() { }

@Test
public void findById_productExists_returnsProduct() { }

@Test
public void findById_productNotFound_returnsEmpty() { }

// ❌ INCORRECT
@Test
public void test1() { }

@Test
public void testAddProduct() { }
```

### Test Structure (AAA Pattern)

**MUST follow Arrange-Act-Assert:**

```java
// ✅ CORRECT
@Test
public void getProductDetails_validAsin_returnsProduct() {
  // Arrange
  String asin = "B00001";
  ProductMetadata expectedProduct = new ProductMetadata();
  expectedProduct.setId(asin);
  expectedProduct.setTitle("Test Product");
  
  when(productRepository.findById(asin))
    .thenReturn(Optional.of(expectedProduct));
  
  // Act
  ProductMetadata result = productService.findById(asin).get();
  
  // Assert
  assertNotNull(result);
  assertEquals(asin, result.getId());
  assertEquals("Test Product", result.getTitle());
}
```

### Mocking Dependencies

**MUST use Mockito for unit tests:**

```java
// ✅ CORRECT
@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
  
  @Mock
  private ProductMetadataRepo productRepository;
  
  @InjectMocks
  private ProductServiceImpl productService;
  
  @Test
  public void findById_productExists_returnsProduct() {
    // Arrange
    ProductMetadata product = new ProductMetadata();
    product.setId("B00001");
    
    when(productRepository.findById("B00001"))
      .thenReturn(Optional.of(product));
    
    // Act
    Optional<ProductMetadata> result = productService.findById("B00001");
    
    // Assert
    assertTrue(result.isPresent());
    assertEquals("B00001", result.get().getId());
    verify(productRepository, times(1)).findById("B00001");
  }
}
```

### Test Classes

**MUST:**
- Name test classes with `Test` suffix: `ProductServiceTest`, `ShoppingCartControllerTest`
- Use `@SpringBootTest` for integration tests
- Use `@ExtendWith(MockitoExtension.class)` for unit tests
- Keep tests isolated and independent

---

## Documentation Requirements

### Class-Level Comments

**SHOULD document purpose of non-trivial classes:**

```java
// ✅ CORRECT
/**
 * The controller that handles all calls related to the product catalog.
 */
@RestController
@RequestMapping(value = "/api/v1")
public class ProductCatalogController {
  // ...
}
```

### Method-Level Comments

**SHOULD document public methods, MUST document complex logic:**

```java
// ✅ CORRECT
/**
 * Fetch a listing of products, given a limit and offset.
 * 
 * @param limit Maximum number of products to return
 * @param offset Starting position in the result set
 * @return List of products matching the criteria
 */
@GetMapping("/products")
public ResponseEntity<List<ProductMetadata>> getProducts(
    @Param("limit") int limit,
    @Param("offset") int offset) {
  List<ProductMetadata> products = productCatalogServiceRest.getProducts(limit, offset);
  return new ResponseEntity<>(products, HttpStatus.OK);
}

// ✅ ACCEPTABLE - Self-documenting simple methods may omit Javadoc
@GetMapping("/product/{asin}")
public ProductMetadata getProductDetails(@PathVariable String asin) {
  return productService.findById(asin).get();
}
```

### Inline Comments

**SHOULD clarify complex business logic:**

```java
// ✅ CORRECT
public void addProductToShoppingCart(String userId, String asin) {
  String shoppingCartKeyStr = userId + "-" + asin;
  
  // Check if product already exists in cart
  if (shoppingCartRepository.findById(shoppingCartKeyStr).isPresent()) {
    // Increment quantity if exists
    shoppingCartRepository.updateQuantityForShoppingCart(userId, asin);
  } else {
    // Create new cart item with default quantity
    ShoppingCart currentShoppingCart = createCartObject(currentKey);
    shoppingCartRepository.save(currentShoppingCart);
  }
}
```

**MUST NOT:**
- State the obvious:
```java
// ❌ INCORRECT
// Set the user ID
userId = "u1001";

// Return the product
return product;
```

---

## Code Quality Rules

### No Debug Code in Production

**MUST remove before committing:**

```java
// ❌ INCORRECT - Remove System.out.println
System.out.println("****inside hello****");
System.out.println("Adding product: " + asin);
System.out.println("Decrementing product: " + asin + " quantity");

// ✅ CORRECT - Use proper logging
private static final Logger log = LoggerFactory.getLogger(ProductService.class);

log.debug("Adding product: {}", asin);
log.info("Order created: {}", orderId);
log.error("Product not found: {}", asin);
```

### Avoid Magic Numbers

**MUST use named constants:**

```java
// ❌ INCORRECT
if (quantity > 10) { }
List<Product> products = getProducts(100, 0);

// ✅ CORRECT
private static final int MAX_QUANTITY_PER_ORDER = 10;
private static final int DEFAULT_PAGE_SIZE = 100;
private static final int FIRST_PAGE_OFFSET = 0;

if (quantity > MAX_QUANTITY_PER_ORDER) { }
List<Product> products = getProducts(DEFAULT_PAGE_SIZE, FIRST_PAGE_OFFSET);
```

### Null Handling

**SHOULD use Optional for potentially null values:**

```java
// ✅ CORRECT
public Optional<ProductMetadata> findById(String id) {
  return productRepository.findById(id);
}

// Usage
Optional<ProductMetadata> product = productService.findById(asin);
product.ifPresent(p -> log.info("Found: {}", p.getTitle()));

// ✅ ALSO CORRECT - With fallback
ProductMetadata product = productService.findById(asin)
  .orElseThrow(() -> new ProductNotFoundException(asin));
```

**MUST validate input parameters:**

```java
// ✅ CORRECT
public void addProduct(String userId, String asin) {
  if (userId == null || userId.isEmpty()) {
    throw new IllegalArgumentException("User ID cannot be null or empty");
  }
  if (asin == null || asin.isEmpty()) {
    throw new IllegalArgumentException("ASIN cannot be null or empty");
  }
  // Process...
}
```

### Code Duplication

**MUST extract common logic:**

```java
// ❌ INCORRECT - Duplicated response handling
@GetMapping("/products")
public ResponseEntity<List<Product>> getProducts() {
  List<Product> products = productService.findAll();
  if (products == null) {
    return new ResponseEntity<>(products, HttpStatus.INTERNAL_SERVER_ERROR);
  }
  return new ResponseEntity<>(products, HttpStatus.OK);
}

@GetMapping("/product/{id}")
public ResponseEntity<Product> getProduct(@PathVariable String id) {
  Product product = productService.findById(id);
  if (product == null) {
    return new ResponseEntity<>(product, HttpStatus.INTERNAL_SERVER_ERROR);
  }
  return new ResponseEntity<>(product, HttpStatus.OK);
}

// ✅ CORRECT - Extract to utility method
private <T> ResponseEntity<T> createResponse(T data) {
  if (data == null) {
    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
  }
  return new ResponseEntity<>(data, HttpStatus.OK);
}

@GetMapping("/products")
public ResponseEntity<List<Product>> getProducts() {
  return createResponse(productService.findAll());
}

@GetMapping("/product/{id}")
public ResponseEntity<Product> getProduct(@PathVariable String id) {
  return createResponse(productService.findById(id));
}
```

### Method Length

**SHOULD keep methods focused and concise:**

- **Ideal:** 10-20 lines
- **Maximum:** 50 lines
- **If longer:** Extract helper methods

```java
// ✅ CORRECT - Focused method
public CheckoutStatus checkout(String userId) {
  Map<String, Integer> cartItems = getCartItems(userId);
  validateInventory(cartItems);
  Order order = createOrder(userId, cartItems);
  updateInventory(cartItems);
  clearCart(userId);
  return createSuccessStatus(order);
}
```

### Immutability

**SHOULD prefer immutable objects when possible:**

```java
// ✅ CORRECT - Immutable with constructor injection
@RestController
public class ProductController {
  private final ProductService productService;
  
  @Autowired
  public ProductController(ProductService productService) {
    this.productService = productService;
  }
}

// ✅ CORRECT - Constants
private static final String DEFAULT_USER_ID = "u1001";
private static final int DEFAULT_QUANTITY = 1;
```

---

## Version History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-01-27 | System | Initial coding standards based on codebase analysis |

---

## References

- [YugaStore Architecture](architecture.md)
- [Software Engineer Guide](software-engineer-guide.md)
- [Spring Boot Best Practices](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Effective Java by Joshua Bloch](https://www.oreilly.com/library/view/effective-java/9780134686097/)

---

*This document should be reviewed and updated quarterly or when significant architectural changes occur.*
