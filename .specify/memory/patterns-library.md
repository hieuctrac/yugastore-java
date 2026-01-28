# YugaStore Engineering Patterns Library

This library captures reusable patterns discovered and refined during YugaStore development.

## Microservice Patterns

### 1. Database Selection Pattern
**When to use**: Choosing between YSQL (PostgreSQL) and YCQL (Cassandra) for a new service

**Decision Tree**:
```
Does the service need ACID transactions?
├─ YES: Use YSQL
│   └─ Examples: Cart (shopping cart integrity), Login (user state), Admin (audit logs)
└─ NO: Does the service need high read throughput?
    ├─ YES: Use YCQL
    │   └─ Examples: Products (catalog browsing), Orders (historical queries)
    └─ NO: Consider data complexity
        ├─ Complex relations: YSQL
        └─ Simple key-value: YCQL
```

**Template Configuration**:
```yaml
# For YSQL services
database:
  url: jdbc:postgresql://localhost:5433/yugabyte
  driver: org.postgresql.Driver
  jpa:
    hibernate.ddl-auto: validate
    show-sql: false

# For YCQL services
cassandra:
  contact-points: localhost
  port: 9042
  keyspace-name: cronos
  local-datacenter: datacenter1
```

### 2. Inter-Service Communication Pattern
**When to use**: Service A needs data from Service B

**Pattern**: OpenFeign client with circuit breaker
```java
@FeignClient(name = "products-service", fallback = ProductServiceFallback.class)
public interface ProductServiceClient {
    @GetMapping("/products/{asin}")
    Product getProduct(@PathVariable String asin);
}

@Component
public class ProductServiceFallback implements ProductServiceClient {
    @Override
    public Product getProduct(String asin) {
        return Product.builder()
            .asin(asin)
            .title("Product temporarily unavailable")
            .price(BigDecimal.ZERO)
            .build();
    }
}
```

### 3. JWT Security Pattern
**When to use**: Securing any microservice endpoint

**Pattern**: Consistent security configuration
```java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer().jwt()
            .and().build();
    }
}
```

## Data Modeling Patterns

### 1. YCQL Entity Pattern
**When to use**: Creating entities for Cassandra-compatible storage

**Pattern**: Partition key optimization for query patterns
```java
@Table("products")
public class Product {
    @PartitionKey
    private String asin;          // Primary access pattern

    @ClusteringColumn
    private String category;      // Secondary sort/filter

    // Avoid secondary indexes - use materialized views instead
    // Design tables around query patterns, not normalization
}
```

### 2. YSQL Entity Pattern
**When to use**: Creating entities for PostgreSQL-compatible storage

**Pattern**: Standard JPA with relationships
```java
@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Use standard JPA relationships and constraints
    // Leverage foreign keys and transactions
}
```

### 3. Audit Logging Pattern
**When to use**: Tracking changes to critical business data

**Pattern**: Consistent audit entity structure
```java
@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tableName;
    private String operation;     // INSERT, UPDATE, DELETE
    private String userId;
    private String oldValues;     // JSON
    private String newValues;     // JSON
    private LocalDateTime timestamp;

    // Always include: what, who, when, before/after
}
```

## API Design Patterns

### 1. REST Controller Pattern
**When to use**: Exposing service functionality via HTTP API

**Pattern**: Consistent error handling and validation
```java
@RestController
@RequestMapping("/api/v1/products")
@Validated
public class ProductController {

    @GetMapping("/{asin}")
    public ResponseEntity<Product> getProduct(
        @PathVariable @NotBlank String asin) {

        return productService.findByAsin(asin)
            .map(product -> ResponseEntity.ok(product))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(
        @Valid @RequestBody CreateProductRequest request) {

        Product created = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
```

### 2. Error Response Pattern
**When to use**: Returning consistent error information

**Pattern**: Standardized error structure
```java
public class ErrorResponse {
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
    private Map<String, String> validationErrors;

    // Always include enough context for client debugging
    // Use specific error codes for programmatic handling
}
```

### 3. Pagination Pattern
**When to use**: Returning large result sets

**Pattern**: Cursor-based for YCQL, offset-based for YSQL
```java
// YCQL - cursor based (better performance)
public class ProductPage {
    private List<Product> products;
    private String nextCursor;      // Last item's partition key
    private boolean hasNext;
}

// YSQL - offset based (familiar pattern)
public class CartItemPage {
    private List<CartItem> items;
    private int page;
    private int size;
    private long totalElements;
    private boolean hasNext;
}
```

## Testing Patterns

### 1. Repository Test Pattern
**When to use**: Testing database operations

**Pattern**: Testcontainers with YugabyteDB
```java
@DataJpaTest
@Testcontainers
class ProductRepositoryTest {

    @Container
    static YugabyteDBYSQLContainer yugabyte = new YugabyteDBYSQLContainer("yugabytedb/yugabyte:latest");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", yugabyte::getJdbcUrl);
        registry.add("spring.datasource.username", yugabyte::getUsername);
        registry.add("spring.datasource.password", yugabyte::getPassword);
    }

    // Test actual database behavior, not mocks
}
```

### 2. Service Test Pattern
**When to use**: Testing business logic

**Pattern**: Mock external dependencies, test core logic
```java
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository repository;
    @Mock private ProductServiceClient productServiceClient;

    @InjectMocks private ProductService service;

    // Focus on business rules and error handling
    // Mock infrastructure, test domain logic
}
```

### 3. Contract Test Pattern
**When to use**: Testing API compatibility between services

**Pattern**: Pact or Spring Cloud Contract
```java
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "products-service")
class ProductServiceContractTest {

    @Pact(consumer = "cart-service")
    public RequestResponsePact getProduct(PactDslWithProvider builder) {
        return builder
            .given("product exists")
            .uponReceiving("get product request")
            .path("/products/B001234567")
            .method("GET")
            .willRespondWith()
            .status(200)
            .body(newJsonObject(product -> {
                product.stringType("asin", "B001234567");
                product.stringType("title", "Sample Product");
                product.decimalType("price", 29.99);
            }))
            .toPact();
    }
}
```

## Deployment Patterns

### 1. Docker Configuration Pattern
**When to use**: Containerizing microservices

**Pattern**: Multi-stage build with JVM optimization
```dockerfile
FROM openjdk:17-jdk-slim as builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jre-slim
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]

# Always use multi-stage builds for smaller images
# Include health check endpoint
# Use slim images for production
```

### 2. Service Discovery Pattern
**When to use**: Enabling service-to-service communication

**Pattern**: Eureka registration with health checks
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka
  instance:
    prefer-ip-address: true
    health-check-url-path: /actuator/health

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

## Performance Patterns

### 1. YCQL Query Optimization
**When to use**: Querying Cassandra-compatible tables

**Anti-patterns to avoid**:
- Secondary indexes (use materialized views)
- Large partition keys
- Unbounded queries

**Best practices**:
```java
// Good - partition key + clustering column
@Query("SELECT * FROM products WHERE asin = ?0 AND category = ?1")
List<Product> findByAsinAndCategory(String asin, String category);

// Good - partition key range
@Query("SELECT * FROM products WHERE token(asin) > ?0 AND token(asin) <= ?1")
List<Product> findByTokenRange(String startToken, String endToken);

// Bad - full table scan
@Query("SELECT * FROM products WHERE title LIKE ?0")  // Avoid!
```

### 2. Connection Pool Pattern
**When to use**: Managing database connections efficiently

**Pattern**: Optimized HikariCP configuration
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      leak-detection-threshold: 60000

# Size pools based on actual concurrent load
# Monitor connection usage in production
```

## Monitoring Patterns

### 1. Custom Metrics Pattern
**When to use**: Tracking business-specific metrics

**Pattern**: Micrometer with business context
```java
@Service
public class ProductService {
    private final Counter productViews = Metrics.counter("product.views");
    private final Timer searchDuration = Metrics.timer("product.search.duration");

    public Product findProduct(String asin) {
        return Timer.Sample.start()
            .stop(searchDuration)
            .recordCallable(() -> {
                productViews.increment();
                return repository.findByAsin(asin);
            });
    }
}
```

### 2. Correlation ID Pattern
**When to use**: Tracing requests across services

**Pattern**: MDC with request tracing
```java
@Component
public class CorrelationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler) {
        String correlationId = request.getHeader("X-Correlation-ID");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put("correlationId", correlationId);
        response.setHeader("X-Correlation-ID", correlationId);
        return true;
    }
}
```

## Anti-Patterns to Avoid

### 1. Database Anti-Patterns
- **Shared database between services** - Breaks service independence
- **Cross-service transactions** - Use saga pattern instead
- **Secondary indexes in YCQL** - Use materialized views
- **Unbounded queries** - Always include limits

### 2. API Anti-Patterns
- **Chatty interfaces** - Aggregate data in single calls
- **Exposing internal IDs** - Use business identifiers (ASIN)
- **Synchronous chains** - Use async messaging for long chains
- **Missing error handling** - Always handle partial failures

### 3. Security Anti-Patterns
- **Trusting client validation** - Always validate server-side
- **Hardcoded secrets** - Use environment variables/secrets management
- **Overprivileged roles** - Implement least privilege access
- **Missing audit logs** - Log all privileged operations

## Pattern Evolution

This patterns library should evolve as the system grows:

1. **Document new patterns** when solving novel problems
2. **Refine existing patterns** based on production experience
3. **Deprecate patterns** that prove problematic in practice
4. **Extract common patterns** into shared libraries when stable

Remember: Patterns are guides, not rules. Adapt them to your specific context while maintaining the architectural principles in the constitution.