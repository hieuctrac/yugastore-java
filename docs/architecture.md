# YugaStore - System Architecture Documentation

**Version:** 1.0  
**Date:** January 27, 2026  
**Status:** Current State Documentation

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [For Delivery Leads: Quick Reference](#for-delivery-leads-quick-reference)
3. [System Overview](#system-overview)
4. [Architecture Principles](#architecture-principles)
5. [Component Architecture](#component-architecture)
6. [Data Architecture](#data-architecture)
7. [Communication Patterns](#communication-patterns)
8. [Service Discovery & Registration](#service-discovery--registration)
9. [Technology Stack](#technology-stack)
10. [Deployment Architecture](#deployment-architecture)
11. [Testing Strategy & Quality Assurance](#testing-strategy--quality-assurance)
12. [Deployment Procedures & Rollback](#deployment-procedures--rollback)
13. [Technical Complexity Assessment](#technical-complexity-assessment)
14. [Team Skills & Capacity Requirements](#team-skills--capacity-requirements)
15. [Operational Considerations](#operational-considerations)
16. [Technical Debt & Maintenance](#technical-debt--maintenance)
17. [Security Architecture](#security-architecture)
18. [Scalability & Performance](#scalability--performance)
19. [Integration Points](#integration-points)
20. [Architecture Decisions](#architecture-decisions)
21. [Risk Register & Mitigation](#risk-register--mitigation)

---

## Executive Summary

YugaStore is a microservices-based eCommerce platform built on Spring Boot and YugabyteDB, demonstrating modern distributed system architecture patterns. The system implements a polyglot persistence strategy, service discovery, API gateway patterns, and distributed transactions across 6 independent microservices.

**Key Architectural Characteristics:**
- **Distributed:** Microservices with independent deployment and scaling
- **Polyglot Persistence:** YSQL (PostgreSQL-compatible) and YCQL (Cassandra-compatible)
- **Resilient:** Service discovery, client-side load balancing, and fault tolerance
- **Scalable:** Horizontal scaling support at both application and database tiers
- **Cloud-Native:** Container-ready with Docker support

---

## For Delivery Leads: Quick Reference

### Critical Dependencies

**Infrastructure Requirements:**
- YugabyteDB cluster (YSQL + YCQL) - Must be running before any service starts
- Eureka Server (Port 8761) - Must start first, all services depend on it
- Network connectivity between all services

**Service Startup Order:**
```
1. YugabyteDB (database layer)
2. Eureka Server (service discovery)
3. Backend services in parallel:
   - Products (8082)
   - Cart (8083)
   - Checkout (8086)
4. API Gateway (8081) - Depends on backend services
5. React UI (8080) - Depends on API Gateway
```

### Technical Complexity by Service

| Service | Complexity | Key Challenges | Team Skill Required |
|---------|------------|----------------|---------------------|
| Products | Medium | YCQL/Cassandra data modeling, Spring Data Cassandra | Backend: Java, Cassandra/CQL |
| Cart | Low-Medium | JPA/Hibernate with YSQL, transaction handling | Backend: Java, SQL, JPA |
| Checkout | High | Multi-service orchestration, distributed transactions | Backend: Java, Feign, Cassandra, distributed systems |
| API Gateway | Medium | Service aggregation, Feign clients, error handling | Backend: Java, Spring Cloud, REST APIs |
| React UI | Medium | React components, BFF pattern, API integration | Frontend: React, JavaScript, REST |
| Eureka Server | Low | Configuration only, Spring Cloud Netflix | Backend: Java, Spring Cloud |

### Testing Complexity

- **Unit Testing:** Straightforward with Spring Boot Test
- **Integration Testing:** Requires running database and Eureka
- **End-to-End Testing:** Requires all services running
- **Test Data:** 6,000+ products must be loaded for realistic testing

### Deployment Risk Assessment

**Low Risk Changes:**
- UI component updates (React frontend)
- Product catalog data updates
- Non-transactional service logic

**Medium Risk Changes:**
- API Gateway routing changes
- Service endpoint modifications
- Cart business logic

**High Risk Changes:**
- Database schema changes (especially YCQL)
- Checkout orchestration flow
- Eureka configuration changes
- Transaction-enabled table modifications

### Typical Delivery Timelines

**New Feature (Simple):** 2-5 days
- Example: Add new product attribute to display
- Tasks: Schema update, service change, UI update, testing

**New Feature (Medium):** 1-2 weeks
- Example: Add product reviews capability
- Tasks: New tables, service logic, API changes, UI components, integration testing

**New Feature (Complex):** 2-4 weeks
- Example: Implement user authentication (Login service)
- Tasks: User schema, auth service, session management, security integration across services

**Bug Fix:** 1-3 days
- Depends on root cause location and testing requirements

### Known Technical Debt

1. **Fixed User ID:** All operations use "u1001" - blocks multi-user functionality
2. **Login Service Incomplete:** Authentication not implemented
3. **No Error Handling Strategy:** Services fail silently or propagate errors inconsistently
4. **Missing Monitoring:** No centralized logging or APM
5. **No Rate Limiting:** API Gateway has no protection against abuse
6. **Hard-Coded Configuration:** Many values in application.yml should be externalized
7. **Test Coverage Gaps:** Integration tests minimal, no load testing

### Quick Troubleshooting Guide

**Services Won't Start:**
- Check Eureka is running at localhost:8761
- Verify YugabyteDB is accessible (YSQL: 5433, YCQL: 9042)
- Check port conflicts (8080-8086, 8761)

**Services Not Registering with Eureka:**
- Allow 30-60 seconds for registration
- Check bootstrap.yml has correct Eureka URL
- Verify network connectivity to Eureka

**Cart Operations Failing:**
- Check YSQL connection (postgresql://localhost:5433)
- Verify shopping_cart table exists
- Check user_id and data format

**Checkout Failing:**
- Verify Products service is accessible
- Verify Cart service is accessible
- Check inventory data exists in product_inventory table
- Review transaction logs in YugabyteDB

---

## System Overview

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Browser Client                            │
└───────────────────────────────┬─────────────────────────────────┘
                                │ HTTP/HTTPS
                                │
┌───────────────────────────────▼─────────────────────────────────┐
│                     React UI (Port 8080)                         │
│                   Static Assets + BFF Pattern                    │
└───────────────────────────────┬─────────────────────────────────┘
                                │ REST API
                                │
┌───────────────────────────────▼─────────────────────────────────┐
│                  API Gateway (Port 8081)                         │
│               Request Routing & Aggregation                      │
└─────────┬──────────────┬──────────────┬────────────────────────┘
          │              │              │
          │              │              │
    ┌─────▼────┐   ┌────▼─────┐  ┌────▼─────┐
    │ Products │   │   Cart   │  │ Checkout │
    │  (8082)  │   │  (8083)  │  │  (8086)  │
    └─────┬────┘   └────┬─────┘  └────┬─────┘
          │              │              │
          │ YCQL         │ YSQL         │ YCQL
          │              │              │
    ┌─────▼──────────────▼──────────────▼─────┐
    │                                          │
    │         YugabyteDB Cluster               │
    │    (YSQL: 5433  |  YCQL: 9042)          │
    │                                          │
    └──────────────────────────────────────────┘

                ┌─────────────────┐
                │ Eureka Server   │
                │    (8761)       │
                │Service Registry │
                └─────────────────┘
                        ▲
                        │
        ┌───────────────┼───────────────┐
        │               │               │
    Registration    Registration   Registration
```

### System Components

| Component | Type | Port | Technology | Purpose |
|-----------|------|------|------------|---------|
| React UI | Frontend/BFF | 8080 | React 16.2, Spring Boot | Customer-facing web application |
| API Gateway | Gateway | 8081 | Spring Boot, OpenFeign | Request routing and service aggregation |
| Products Service | Microservice | 8082 | Spring Boot, YCQL | Product catalog management |
| Cart Service | Microservice | 8083 | Spring Boot, YSQL, JPA | Shopping cart operations |
| Checkout Service | Microservice | 8086 | Spring Boot, YCQL | Order processing and inventory |
| Login Service | Microservice | 8085 | Spring Boot, YSQL | Authentication (WIP) |
| Eureka Server | Service Registry | 8761 | Spring Cloud Netflix | Service discovery |
| YugabyteDB | Database | 5433, 9042 | Distributed SQL | Data persistence layer |

---

## Architecture Principles

### 1. Microservices Architecture
- **Single Responsibility**: Each service owns a specific business capability
- **Independent Deployment**: Services can be deployed independently
- **Decentralized Data Management**: Each service manages its own data
- **Technology Diversity**: Services can use different data access patterns (YSQL vs YCQL)

### 2. API Gateway Pattern
- **Single Entry Point**: All external requests route through the API Gateway
- **Service Aggregation**: Gateway combines responses from multiple services
- **Protocol Translation**: Gateway handles HTTP to internal service protocols
- **Cross-Cutting Concerns**: Authentication, rate limiting, logging at gateway level

### 3. Service Discovery
- **Dynamic Service Registration**: Services register with Eureka on startup
- **Client-Side Load Balancing**: Ribbon for load distribution
- **Health Monitoring**: Eureka tracks service health and availability
- **Failover Support**: Automatic routing to healthy instances

### 4. Polyglot Persistence
- **YSQL (PostgreSQL-compatible)**: For transactional data requiring ACID guarantees
  - Shopping cart operations
  - User authentication data
- **YCQL (Cassandra-compatible)**: For high-throughput, read-heavy workloads
  - Product catalog
  - Product rankings
  - Order history
  - Inventory management

### 5. Backend for Frontend (BFF)
- React UI service acts as a BFF, aggregating API calls for the frontend
- Reduces chattiness between browser and backend services
- Optimizes data transfer for UI requirements

---

## Component Architecture

### 1. React UI Service (Port 8080)

**Purpose:** Customer-facing web application serving static assets and acting as a Backend for Frontend.

**Technology Stack:**
- Spring Boot 2.6.3 (Backend server)
- React 16.2 (Frontend framework)
- Bootstrap 3.3.7 (UI styling)
- Axios (HTTP client)
- React Router DOM (Client-side routing)

**Key Components:**
```
react-ui/
├── frontend/                    # React application
│   ├── src/
│   │   ├── components/
│   │   │   ├── App/            # Main application component
│   │   │   ├── Cart/           # Shopping cart UI
│   │   │   ├── Home/           # Homepage
│   │   │   ├── Products/       # Product listing
│   │   │   ├── ShowProduct/    # Product details
│   │   │   └── Main/           # Layout components
│   │   ├── index.js            # React entry point
│   │   └── index.css           # Global styles
│   └── package.json
└── src/main/java/              # Spring Boot BFF layer
    └── com/yugabyte/yugastore/ui/
        ├── controller/
        │   └── CronosProductsController.java
        └── rest/
            └── DashboardRestConsumer.java
```

**Responsibilities:**
- Serve React single-page application
- Act as BFF aggregating backend API calls
- Handle browser-to-backend communication
- Manage session state
- Transform API responses for UI consumption

**External Dependencies:**
- API Gateway (http://localhost:8081)

**API Endpoints:**
```
GET  /products                          # Fetch product list
GET  /products/category/{category}      # Products by category
GET  /products/details?asin={asin}      # Product details
POST /cart/add                          # Add to cart
POST /cart/get                          # Get cart contents
POST /cart/checkout                     # Checkout cart
```

---

### 2. API Gateway Service (Port 8081)

**Purpose:** Central entry point for all backend API requests, providing service routing and aggregation.

**Technology Stack:**
- Spring Boot 2.6.3
- Spring Cloud OpenFeign (Service clients)
- Spring Cloud Netflix Eureka Client (Service discovery)
- Spring MVC (REST endpoints)

**Key Components:**
```
api-gateway-microservice/
└── src/main/java/
    └── com/yugabyte/app/yugastore/
        ├── controller/
        │   ├── ProductCatalogController.java
        │   └── ShoppingCartController.java
        ├── service/
        │   ├── ProductCatalogServiceRest.java
        │   ├── ShoppingCartServiceRest.java
        │   └── CheckoutServiceRest.java
        └── rest/clients/
            ├── ProductCatalogRestClient.java   # Feign client to Products
            ├── ShoppingCartRestClient.java     # Feign client to Cart
            └── CheckoutRestClient.java         # Feign client to Checkout
```

**Responsibilities:**
- Route requests to appropriate microservices
- Aggregate responses from multiple services
- Service orchestration and composition
- Handle cross-cutting concerns (future: auth, rate limiting)
- Provide unified API interface to clients

**Service Dependencies:**
- Products Microservice (via Feign client)
- Cart Microservice (via Feign client)
- Checkout Microservice (via Feign client)
- Eureka Server (service discovery)

**API Endpoints:**
```
# Product Catalog
GET  /api/v1/product/{asin}                    # Get product details
GET  /api/v1/products?limit&offset             # Get all products paginated
GET  /api/v1/products/category/{category}      # Get products by category

# Shopping Cart
POST /api/v1/shoppingCart                      # Get cart contents
POST /api/v1/shoppingCart/addProduct?asin      # Add product to cart
POST /api/v1/shoppingCart/removeProduct?asin   # Remove product from cart
POST /api/v1/shoppingCart/checkout             # Checkout cart
```

**Communication Pattern:**
- Uses Spring Cloud OpenFeign for declarative REST clients
- Service discovery via Eureka (dynamic endpoint resolution)
- Synchronous HTTP communication
- Client-side load balancing with Ribbon

---

### 3. Products Microservice (Port 8082)

**Purpose:** Manage product catalog including metadata, rankings, and recommendations.

**Technology Stack:**
- Spring Boot 2.6.3
- Spring Data Cassandra
- YugabyteDB Java Driver 4.6.0-yb-10
- Spring Cloud Netflix Eureka Client

**Database:** YugabyteDB YCQL (Cassandra-compatible API)

**Key Components:**
```
products-microservice/
└── src/main/java/
    └── com/yugabyte/app/yugastore/
        ├── controller/
        │   └── ProductCatalogController.java
        ├── service/
        │   ├── ProductService.java
        │   └── ProductRankingService.java
        ├── repository/
        │   ├── ProductRepository.java
        │   └── ProductRankingRepository.java
        └── domain/
            ├── ProductMetadata.java
            └── ProductRanking.java
```

**Data Model (YCQL):**

```cql
-- Product master data
CREATE TABLE cronos.products (
    asin text PRIMARY KEY,
    title text,
    description text,
    price double,
    imurl text,
    also_bought frozen<list<text>>,
    also_viewed frozen<list<text>>,
    bought_together frozen<list<text>>,
    buy_after_viewing frozen<list<text>>,
    brand text,
    categories set<text>,
    num_reviews int,
    num_stars int,
    avg_stars double
) WITH transactions = {'enabled': 'false'};

-- Product rankings by category
CREATE TABLE cronos.product_rankings (
    asin text,
    category text,
    sales_rank int,
    title text,
    price double,
    imurl text,
    num_reviews int,
    num_stars int,
    avg_stars double,
    PRIMARY KEY (asin, category)
) WITH CLUSTERING ORDER BY (category ASC)
    AND transactions = {'enabled': 'false'};

-- Index for efficient category-based queries
CREATE INDEX top_products_in_category
    ON cronos.product_rankings (category, sales_rank);
```

**Responsibilities:**
- Store and retrieve product metadata
- Manage product categorization and rankings
- Provide product recommendations (also_bought, also_viewed, etc.)
- Support pagination for large product sets
- Handle high-volume read operations efficiently

**API Endpoints:**
```
GET /products-microservice/product/{asin}             # Get product by ID
GET /products-microservice/products?limit&offset      # Get all products
GET /products-microservice/products/category/{cat}    # Products by category
```

**Design Rationale:**
- YCQL chosen for high read throughput on product catalog
- Denormalized data model for query performance
- Secondary index on (category, sales_rank) for efficient sorted retrieval
- No transactions needed for read-heavy product data

---

### 4. Cart Microservice (Port 8083)

**Purpose:** Manage shopping cart operations with ACID transaction guarantees.

**Technology Stack:**
- Spring Boot 2.6.3
- Spring Data JPA
- PostgreSQL JDBC Driver
- YugabyteDB YSQL (PostgreSQL-compatible API)
- Spring Cloud Netflix Eureka Client

**Database:** YugabyteDB YSQL (PostgreSQL-compatible API)

**Key Components:**
```
cart-microservice/
└── src/main/java/
    └── com/yugabyte/app/yugastore/cart/
        ├── controller/
        │   └── ShoppingCartController.java
        ├── service/
        │   └── ShoppingCartImpl.java
        ├── repository/
        │   └── ShoppingCartRepository.java
        └── domain/
            └── ShoppingCart.java
```

**Data Model (YSQL):**

```sql
CREATE TABLE shopping_cart (
    cart_key TEXT NOT NULL,
    user_id TEXT NOT NULL,
    asin TEXT NOT NULL,
    time_added TEXT NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (cart_key)
);
```

**Responsibilities:**
- Add products to user shopping carts
- Update product quantities
- Remove products from carts
- Retrieve cart contents with quantities
- Clear cart after successful checkout
- Maintain cart persistence across sessions

**API Endpoints:**
```
GET /cart-microservice/shoppingCart/addProduct?userid&asin      # Add to cart
GET /cart-microservice/shoppingCart/productsInCart?userid       # Get cart
GET /cart-microservice/shoppingCart/removeProduct?userid&asin   # Remove item
GET /cart-microservice/shoppingCart/clearCart?userid            # Clear cart
```

**Design Rationale:**
- YSQL chosen for ACID transaction guarantees on cart operations
- JPA/Hibernate for familiar ORM patterns
- PostgreSQL compatibility for easy development and tooling
- Simple schema for fast reads and writes

**Current Implementation Notes:**
- Fixed user ID ("u1001") used across the application
- Cart key generated as combination of user_id and asin
- Quantity increments when adding existing products

---

### 5. Checkout Microservice (Port 8086)

**Purpose:** Process orders and manage product inventory with distributed transactions.

**Technology Stack:**
- Spring Boot 2.6.3
- Spring Data Cassandra
- YugabyteDB Java Driver 4.6.0-yb-10
- Spring Cloud OpenFeign (service communication)
- Spring Cloud Netflix Eureka Client

**Database:** YugabyteDB YCQL (Cassandra-compatible API)

**Key Components:**
```
checkout-microservice/
└── src/main/java/
    └── com/yugabyte/app/yugastore/cronoscheckoutapi/
        ├── controller/
        │   └── CheckoutController.java
        ├── service/
        │   └── CheckoutServiceImpl.java
        ├── repository/
        │   ├── OrderRepository.java
        │   └── ProductInventoryRepository.java
        ├── rest/clients/
        │   ├── ShoppingCartRestClient.java      # Feign client to Cart
        │   └── ProductCatalogRestClient.java    # Feign client to Products
        └── domain/
            ├── Order.java
            ├── ProductInventory.java
            └── CheckoutStatus.java
```

**Data Model (YCQL):**

```cql
-- Orders table with transactions enabled
CREATE TABLE cronos.orders (
    order_id text PRIMARY KEY,
    user_id text,
    order_details text,
    order_time text,
    order_total double
) WITH transactions = {'enabled': 'true'};

-- Inventory tracking with transactions
CREATE TABLE cronos.product_inventory (
    asin text PRIMARY KEY,
    quantity int
) WITH transactions = {'enabled': 'true'};
```

**Responsibilities:**
- Coordinate checkout process across multiple services
- Validate product availability
- Create and persist orders
- Update inventory quantities atomically
- Handle out-of-stock scenarios
- Trigger cart clearing on successful checkout

**API Endpoints:**
```
POST /checkout-microservice/shoppingCart/checkout    # Process checkout
```

**Checkout Flow:**
1. Retrieve cart contents from Cart Microservice
2. Validate products exist (Products Microservice)
3. Check inventory availability
4. Create order record
5. Update inventory quantities (atomic transaction)
6. Clear cart (Cart Microservice)
7. Return order confirmation

**Design Rationale:**
- YCQL with transactions enabled for atomic inventory updates
- Service orchestration pattern for multi-step checkout
- Feign clients for inter-service communication
- Exception handling for insufficient inventory

**Transaction Guarantees:**
- Order creation and inventory update execute within YugabyteDB transaction
- Ensures consistency between orders and inventory
- Rollback on failures (insufficient stock, database errors)

---

### 6. Login Microservice (Port 8085)

**Purpose:** User authentication and session management (Work in Progress).

**Technology Stack:**
- Spring Boot 2.6.3
- Spring Data JPA
- PostgreSQL JDBC Driver
- YugabyteDB YSQL

**Database:** YugabyteDB YSQL

**Status:** Under development

**Planned Responsibilities:**
- User registration
- Login/logout functionality
- Session management
- Password security
- User profile management

---

### 7. Eureka Server (Port 8761)

**Purpose:** Service discovery and registration for microservices communication.

**Technology Stack:**
- Spring Boot 2.6.3
- Spring Cloud Netflix Eureka Server

**Configuration:**
```yaml
server:
  port: 8761
eureka:
  client:
    register-with-eureka: false    # Eureka server doesn't register itself
    fetch-registry: false           # Eureka server doesn't fetch registry
```

**Responsibilities:**
- Service registration from microservices
- Maintain service registry with health status
- Provide service lookup for clients
- Enable client-side load balancing
- Health monitoring and heartbeat management

**How It Works:**
1. Services register on startup with Eureka
2. Services send heartbeats every 30 seconds (default)
3. Eureka marks services as UP or DOWN based on heartbeats
4. Clients query Eureka to discover service instances
5. Ribbon uses registry for client-side load balancing

**Service Registration Example:**
```yaml
# bootstrap.yml in each microservice
spring:
  application:
    name: products-microservice
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka
```

**Dashboard:** http://localhost:8761

---

## Data Architecture

### Database Strategy: Polyglot Persistence

YugaStore leverages YugabyteDB's dual API support to implement polyglot persistence within a single database cluster.

```
┌────────────────────────────────────────────────────────────┐
│                   YugabyteDB Cluster                        │
│                                                             │
│  ┌─────────────────────────┬─────────────────────────┐   │
│  │                         │                         │   │
│  │  YSQL API (Port 5433)   │  YCQL API (Port 9042)  │   │
│  │  PostgreSQL-compatible  │  Cassandra-compatible   │   │
│  │                         │                         │   │
│  └──────────┬──────────────┴────────────┬───────────┘   │
│             │                            │                │
│             │                            │                │
│      ┌──────▼─────┐              ┌──────▼──────┐        │
│      │            │              │              │        │
│      │   Cart     │              │  Products    │        │
│      │   Users    │              │  Rankings    │        │
│      │            │              │  Orders      │        │
│      │            │              │  Inventory   │        │
│      └────────────┘              └──────────────┘        │
│                                                           │
│         Distributed Storage Layer                        │
│         (Common DocDB Backend)                           │
└───────────────────────────────────────────────────────────┘
```

### YSQL (PostgreSQL-Compatible) Usage

**Use Cases:**
- Transactional data requiring strong ACID guarantees
- Relational data models
- Complex queries with JOINs
- Applications using PostgreSQL tooling

**Services Using YSQL:**
- Cart Microservice
- Login Microservice (WIP)

**Benefits:**
- Full ACID transactions
- PostgreSQL compatibility (drivers, tools)
- JPA/Hibernate support
- SQL familiarity for developers

### YCQL (Cassandra-Compatible) Usage

**Use Cases:**
- High-throughput, read-heavy workloads
- Denormalized data models
- Time-series or log data
- Massive scale requirements

**Services Using YCQL:**
- Products Microservice (product catalog)
- Checkout Microservice (orders, inventory)

**Benefits:**
- High read throughput
- Wide column data model
- Efficient range queries
- Selective transaction support

### Data Distribution

**Products Data:**
- ~6,000 products in catalog
- Denormalized for query performance
- Indexed by category and sales rank
- No foreign key constraints

**Cart Data:**
- Normalized relational model
- Single table with composite key
- High write frequency
- Strong consistency requirements

**Order & Inventory Data:**
- Orders require transactional consistency
- Inventory updates are atomic
- Transaction-enabled YCQL tables

### Data Loading

Product data is loaded via Python scripts and cassandra-loader:

```bash
# Parse JSON to CSV
python parse_metadata_json.py products.json

# Load into YugabyteDB
./cassandra-loader -f cronos_products.csv \
  -host localhost \
  -schema "cronos.products(...)"
```

---

## Communication Patterns

### 1. Synchronous REST Communication

**Pattern:** Request-Response over HTTP

**Implementation:**
- Spring RestTemplate (React UI to API Gateway)
- Spring Cloud OpenFeign (Inter-service communication)

**Example Flow:**
```
Browser → [HTTP] → React UI → [REST] → API Gateway
                                            ↓
                                    [Feign Client]
                                            ↓
                              Products/Cart/Checkout Service
```

**Characteristics:**
- Blocking, synchronous calls
- Immediate response required
- Simple to implement and debug
- Tight coupling between services

### 2. Service Discovery Pattern

**Pattern:** Client-side service discovery with Eureka

**How It Works:**
```
1. Service Startup
   ├─> Register with Eureka (service name, host, port, health URL)
   └─> Send heartbeat every 30s

2. Client Request
   ├─> Query Eureka for service instances
   ├─> Ribbon load balancer selects instance
   └─> Make HTTP request to selected instance

3. Health Monitoring
   ├─> Eureka checks heartbeats
   └─> Remove unhealthy instances from registry
```

**Benefits:**
- Dynamic service location
- Automatic failover
- Client-side load balancing
- No hard-coded endpoints

### 3. API Gateway Pattern

**Pattern:** Single entry point with request routing

**Request Flow:**
```
Client Request → API Gateway → Route Analysis
                                    ↓
                          ┌─────────┼─────────┐
                          ▼         ▼         ▼
                      Products   Cart    Checkout
                          │         │         │
                          └─────────┼─────────┘
                                    ▼
                          Aggregate Response
                                    ▼
                          Client Response
```

**Aggregation Example:**
```java
// API Gateway aggregates cart details
public Map<String, Object> getCartDetails(String userId) {
    // 1. Get product IDs from Cart Service
    Map<String, Integer> cartItems = cartClient.getCart(userId);
    
    // 2. Get product details from Products Service
    List<ProductMetadata> products = cartItems.keySet()
        .stream()
        .map(asin -> productsClient.getProduct(asin))
        .collect(Collectors.toList());
    
    // 3. Aggregate and return
    return aggregateCartWithProducts(cartItems, products);
}
```

### 4. Backend for Frontend (BFF) Pattern

**Pattern:** React UI acts as BFF layer

**Responsibilities:**
- Aggregate multiple API calls for UI
- Transform backend data for frontend consumption
- Handle browser-specific concerns
- Cache responses when appropriate

**Example:**
```javascript
// Frontend requests single endpoint
fetch('/cart/get')

// BFF makes multiple backend calls
GET /api/v1/shoppingCart          // Get cart items
GET /api/v1/product/{asin}        // Get product details (for each item)
→ Returns aggregated response
```

---

## Service Discovery & Registration

### Eureka Server Configuration

**Server-Side:**
```yaml
server:
  port: 8761
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

### Client Configuration

**Each Microservice:**
```yaml
# bootstrap.yml
spring:
  application:
    name: products-microservice    # Service identifier
eureka:
  instance:
    hostname: localhost
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka
```

**Java Configuration:**
```java
@SpringBootApplication
@EnableDiscoveryClient    // Enable Eureka client
public class YugastoreProducts {
    public static void main(String[] args) {
        SpringApplication.run(YugastoreProducts.class, args);
    }
}
```

### Feign Client Integration

**Declarative REST Client:**
```java
@FeignClient("products-microservice")  // Service name from Eureka
public interface ProductCatalogRestClient {
    
    @RequestMapping("/products-microservice/product/{asin}")
    ProductMetadata getProductDetails(@PathVariable("asin") String asin);
    
    @RequestMapping("/products-microservice/products")
    List<ProductMetadata> getProducts(
        @RequestParam("limit") int limit,
        @RequestParam("offset") int offset
    );
}
```

**How Feign + Eureka Work Together:**
1. Feign client references service by logical name ("products-microservice")
2. Eureka resolves name to physical instances (host:port)
3. Ribbon load balancer selects instance
4. Feign makes HTTP call to selected instance
5. Automatic retry on failure to different instance

---

## Technology Stack

### Backend Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 | Primary programming language |
| Spring Boot | 2.6.3 | Microservices framework |
| Spring Cloud | 2021.0.0 | Cloud-native patterns |
| Spring Data JPA | 2.6.3 | YSQL database access (Cart) |
| Spring Data Cassandra | 2.6.3 | YCQL database access (Products, Checkout) |
| Spring Cloud Netflix Eureka | 2021.0.0 | Service discovery |
| Spring Cloud OpenFeign | 2021.0.0 | Declarative REST clients |
| YugabyteDB Java Driver | 4.6.0-yb-10 | Native YugabyteDB driver |
| Maven | 3.6+ | Build and dependency management |

### Frontend Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| React | 16.2 | UI framework |
| React Router DOM | 4.2.2 | Client-side routing |
| Axios | 0.18.0 | HTTP client |
| Bootstrap | 3.3.7 | CSS framework |
| React Bootstrap | 0.32.4 | React Bootstrap components |

### Database

| Component | Version | APIs | Purpose |
|-----------|---------|------|---------|
| YugabyteDB | Latest | YSQL, YCQL | Distributed SQL database |
| YSQL | PostgreSQL 11+ compatible | SQL | Transactional data |
| YCQL | Cassandra 3.x compatible | CQL | High-throughput reads |

### Infrastructure

| Technology | Purpose |
|------------|---------|
| Docker | Container runtime |
| Eclipse Temurin JRE 17 | Java runtime for containers |
| Python 3 | Data loading scripts |

---

## Deployment Architecture

### Local Development Deployment

```
┌─────────────────────────────────────────────────────────────┐
│                    Development Machine                       │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Eureka     │  │  API Gateway │  │   React UI   │     │
│  │   :8761      │  │    :8081     │  │    :8080     │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Products   │  │     Cart     │  │   Checkout   │     │
│  │   :8082      │  │    :8083     │  │    :8086     │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐   │
│  │            YugabyteDB Local Cluster                  │   │
│  │         (yugabyted start)                           │   │
│  │         YSQL: 5433  |  YCQL: 9042                  │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

**Start Command per Service:**
```bash
cd <service-directory>
mvn spring-boot:run
```

### Docker Container Deployment

```
┌─────────────────────────────────────────────────────────────┐
│                      Docker Host                             │
│                                                              │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐           │
│  │ Container  │  │ Container  │  │ Container  │           │
│  │ Eureka     │  │ API GW     │  │ React UI   │           │
│  │ :8761      │  │ :8081      │  │ :8080      │           │
│  └────────────┘  └────────────┘  └────────────┘           │
│                                                              │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐           │
│  │ Container  │  │ Container  │  │ Container  │           │
│  │ Products   │  │ Cart       │  │ Checkout   │           │
│  │ :8082      │  │ :8083      │  │ :8086      │           │
│  └────────────┘  └────────────┘  └────────────┘           │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐   │
│  │       YugabyteDB (Host or Container)                 │   │
│  └─────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘
```

**Dockerfile Example:**
```dockerfile
FROM eclipse-temurin:17-jre
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

**Start Script:**
```bash
./docker-run.sh
# Starts all services as Docker containers
```

### Cloud Deployment Considerations

**Multi-Region Architecture:**
```
Region 1                    Region 2                    Region 3
┌──────────┐               ┌──────────┐               ┌──────────┐
│ Services │               │ Services │               │ Services │
│ Layer    │               │ Layer    │               │ Layer    │
└────┬─────┘               └────┬─────┘               └────┬─────┘
     │                          │                          │
     └──────────────────┬───────┴──────────────────────────┘
                        │
            ┌───────────▼───────────┐
            │   YugabyteDB Cluster  │
            │   Multi-Region Sync   │
            │   (3-node minimum)    │
            └───────────────────────┘
```

**Kubernetes Deployment Pattern:**
- Services deployed as Kubernetes Deployments
- Service discovery via Kubernetes Services
- YugabyteDB deployed via Helm chart or YugabyteDB Operator
- Horizontal Pod Autoscaling for services
- Ingress controller for external access

---

## Security Architecture

### Current State

**Authentication:**
- Login service under development
- Current implementation uses fixed user ID ("u1001")
- No authentication required for current demo

**Authorization:**
- No role-based access control implemented
- All endpoints publicly accessible

**Data Security:**
- Database connections over localhost (development)
- No encryption in transit
- No encryption at rest (YugabyteDB supports both)

### Recommended Security Enhancements

**1. Authentication & Authorization:**
- Implement JWT-based authentication
- Add Spring Security to API Gateway
- Token validation at gateway level
- User session management

**2. Network Security:**
- TLS/SSL for all HTTP communication
- Service-to-service authentication
- API Gateway rate limiting
- CORS configuration

**3. Database Security:**
- YugabyteDB authentication enabled
- Role-based database access
- Encryption in transit (TLS)
- Encryption at rest

**4. Secrets Management:**
- Externalize database credentials
- Use environment variables or secret management service
- Rotate credentials regularly

---

## Scalability & Performance

### Horizontal Scaling Capabilities

**Application Tier:**
- All microservices are stateless
- Can scale horizontally by adding instances
- Eureka handles service discovery for multiple instances
- Ribbon provides client-side load balancing

**Example Scaling:**
```bash
# Scale Products service to 3 instances
mvn spring-boot:run -Dserver.port=8082  # Instance 1
mvn spring-boot:run -Dserver.port=8092  # Instance 2
mvn spring-boot:run -Dserver.port=8102  # Instance 3

# All register with Eureka, Ribbon distributes load
```

**Database Tier:**
- YugabyteDB supports horizontal scaling
- Add nodes to increase throughput and storage
- Automatic data rebalancing
- Read replicas for read-heavy workloads

### Performance Characteristics

**Product Catalog:**
- Read-optimized with YCQL
- Secondary indexes for category queries
- Denormalized data model
- Expected: 10,000+ reads/second per node

**Shopping Cart:**
- YSQL with ACID guarantees
- Simple single-table queries
- Expected: 1,000+ writes/second per node

**Checkout:**
- Transaction-enabled YCQL
- Atomic inventory updates
- Order processing latency: <100ms typical

### Caching Strategy

**Current State:**
- No distributed cache implemented
- Spring Data caching at repository layer
- Browser caching for static assets

**Recommended Enhancements:**
- Redis/Memcached for session data
- Product catalog caching
- Cache-aside pattern at API Gateway
- TTL-based cache invalidation

---

## Integration Points

### External Integrations

**Current:**
- None (self-contained demo application)

**Future Considerations:**
- Payment gateway integration (Stripe, PayPal)
- Email service for order confirmations
- Inventory management system integration
- Analytics platform integration
- CDN for static assets

### Internal Service Integration Matrix

| From Service | To Service | Method | Purpose |
|--------------|------------|--------|---------|
| React UI | API Gateway | REST/HTTP | All backend requests |
| API Gateway | Products | Feign/HTTP | Product queries |
| API Gateway | Cart | Feign/HTTP | Cart operations |
| API Gateway | Checkout | Feign/HTTP | Order processing |
| Checkout | Cart | Feign/HTTP | Get cart items |
| Checkout | Products | Feign/HTTP | Validate products |
| All Services | Eureka | HTTP | Service registration |

---

## Architecture Decisions

### ADR-001: Microservices Architecture

**Decision:** Implement microservices instead of monolithic architecture.

**Context:** Need to demonstrate modern distributed system patterns and allow independent scaling of components.

**Rationale:**
- Independent deployment and scaling
- Technology diversity (YSQL vs YCQL)
- Fault isolation
- Easier to understand as demo application

**Consequences:**
- Increased operational complexity
- Network latency between services
- Need for service discovery
- Distributed transaction challenges

---

### ADR-002: Polyglot Persistence (YSQL + YCQL)

**Decision:** Use both YSQL and YCQL APIs within the same YugabyteDB cluster.

**Context:** Different services have different data access patterns and consistency requirements.

**Rationale:**
- Cart requires ACID transactions → YSQL
- Products requires high read throughput → YCQL
- Single database cluster simplifies operations
- Demonstrates YugabyteDB's dual API capability

**Consequences:**
- Developers need knowledge of both SQL and CQL
- Different data modeling approaches
- No cross-API JOINs
- Operational benefits of single cluster

---

### ADR-003: Spring Cloud Netflix Eureka for Service Discovery

**Decision:** Use Eureka instead of Consul, etcd, or Kubernetes native discovery.

**Context:** Need service discovery for dynamic service location and load balancing.

**Rationale:**
- Native Spring Cloud integration
- Simple setup for development
- Proven at scale (Netflix)
- Good documentation and community support

**Consequences:**
- Couples to Spring ecosystem
- Requires separate Eureka server
- AP system (availability over consistency)
- Additional component to manage

---

### ADR-004: Spring Cloud OpenFeign for Inter-Service Communication

**Decision:** Use Feign declarative REST clients instead of RestTemplate or WebClient.

**Context:** Need clean abstraction for REST-based service-to-service communication.

**Rationale:**
- Declarative interface-based approach
- Integrates with Eureka for service discovery
- Built-in Ribbon for load balancing
- Reduces boilerplate code

**Consequences:**
- Synchronous blocking calls only
- Learning curve for Feign annotations
- Tight coupling to HTTP/REST
- No reactive programming support

---

### ADR-005: API Gateway Pattern with Aggregation

**Decision:** Implement API Gateway to handle all external requests.

**Context:** Need single entry point for React UI and potential future clients.

**Rationale:**
- Single entry point for external traffic
- Response aggregation for UI
- Future: auth, rate limiting, monitoring
- Reduces chattiness from browser

**Consequences:**
- Gateway becomes critical path
- Potential performance bottleneck
- Additional latency hop
- Gateway logic complexity grows

---

### ADR-006: Backend for Frontend (BFF) in React UI

**Decision:** React UI service acts as BFF, not just static file server.

**Context:** Need to optimize API calls for browser clients.

**Rationale:**
- Aggregate multiple backend calls
- Transform data for UI consumption
- Reduce browser-to-backend round trips
- Handle browser-specific concerns

**Consequences:**
- React UI becomes more than static host
- Logic duplication between API Gateway and BFF
- Additional deployment component
- Tighter coupling UI to backend APIs

---

### ADR-007: Fixed User ID for Demo Purposes

**Decision:** Use hardcoded user ID ("u1001") instead of implementing full authentication.

**Context:** Demo application focusing on architecture patterns, not security.

**Rationale:**
- Simplifies demo and testing
- Focuses on distributed system patterns
- Reduces initial development complexity
- Login service marked as WIP

**Consequences:**
- Not production-ready
- Single-user experience
- Cart and orders not user-isolated
- Must implement auth before real use

---

## Testing Strategy & Quality Assurance

### Testing Pyramid

```
                    ┌─────────────┐
                    │   E2E Tests │  ← Few (5-10)
                    └─────────────┘
                 ┌──────────────────┐
                 │ Integration Tests│  ← Some (20-30)
                 └──────────────────┘
            ┌────────────────────────────┐
            │      Unit Tests           │  ← Many (100+)
            └────────────────────────────┘
```

### Unit Testing

**Framework:** JUnit 5, Mockito, Spring Boot Test

**Coverage Target:** 70%+ for business logic

**What to Test:**
- Service layer business logic
- Controller request/response handling
- Data transformation logic
- Validation rules
- Error handling

**Example Test Structure:**
```java
@SpringBootTest
class ProductServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @InjectMocks
    private ProductService productService;
    
    @Test
    void testGetProductById() {
        // Arrange
        ProductMetadata product = new ProductMetadata();
        product.setAsin("B00001");
        when(productRepository.findById("B00001"))
            .thenReturn(Optional.of(product));
        
        // Act
        ProductMetadata result = productService.findById("B00001");
        
        // Assert
        assertNotNull(result);
        assertEquals("B00001", result.getAsin());
    }
}
```

**Current State:**
- Minimal unit test coverage
- No standardized test structure
- Test data management needs improvement

**Recommendation:**
- Achieve 70% coverage before major features
- Add test for each bug fix
- Use test containers for repository tests

---

### Integration Testing

**Framework:** Spring Boot Test, TestContainers, WireMock

**What to Test:**
- Database integration (YSQL and YCQL)
- Service-to-service communication
- Feign client interactions
- Transaction boundaries
- Error propagation

**Test Environment Requirements:**
- YugabyteDB (via TestContainers or local instance)
- Eureka Server (embedded or standalone)
- Test data loaded

**Example Integration Test:**
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class CartIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("yugabytedb/yugabyte:latest");
    
    @Autowired
    private ShoppingCartController cartController;
    
    @Test
    void testAddProductToCart() {
        // Test full flow from controller through to database
        ResponseEntity<?> response = 
            cartController.addProductToCart("B00001");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
```

**Current State:**
- Limited integration test coverage
- Manual testing required for service interactions
- No automated E2E tests

**Recommendation:**
- Add integration tests for critical paths (checkout flow)
- Use TestContainers for database isolation
- Mock external service calls with WireMock

---

### End-to-End Testing

**Framework:** Selenium, Cypress, or Playwright

**What to Test:**
- Complete user journeys
- Browse products → Add to cart → Checkout
- Error scenarios across services
- Performance under load

**Test Scenarios:**
1. **Product Browsing:**
   - Load homepage
   - Navigate to category
   - View product details
   - Verify product information displays

2. **Shopping Cart:**
   - Add product to cart
   - Verify cart updates
   - Modify quantity
   - Remove item

3. **Checkout:**
   - Complete checkout flow
   - Verify order creation
   - Verify inventory update
   - Verify cart cleared

**Current State:**
- No automated E2E tests
- Manual testing only

**Recommendation:**
- Start with smoke tests (happy path)
- Add E2E for critical revenue flows
- Run before each release

---

### Test Data Management

**Product Catalog:**
- 6,000+ products loaded from JSON
- Use subset for testing (100-500 products)
- Maintain test data scripts in `resources/`

**Shopping Cart:**
- Clear between tests
- Use unique test user IDs
- Reset to known state

**Inventory:**
- Set known quantities for test products
- Verify updates in transaction tests
- Reset after test runs

**Database Reset Strategy:**
```bash
# Drop and recreate tables
cqlsh -f resources/schema.cql
ysqlsh -f resources/schema.sql

# Load minimal test data
python resources/load_test_data.py
```

---

### Performance Testing

**Tools:** JMeter, Gatling, or k6

**Load Test Scenarios:**

1. **Product Catalog Load:**
   - Target: 1,000 req/sec
   - Endpoint: GET /api/v1/products
   - Expected: <100ms p95 latency

2. **Add to Cart:**
   - Target: 500 req/sec
   - Endpoint: POST /api/v1/shoppingCart/addProduct
   - Expected: <200ms p95 latency

3. **Checkout:**
   - Target: 100 req/sec
   - Endpoint: POST /api/v1/shoppingCart/checkout
   - Expected: <500ms p95 latency

**Current State:**
- No performance testing framework
- Performance characteristics unknown
- No baseline metrics

**Recommendation:**
- Establish baseline performance metrics
- Run load tests before major releases
- Monitor for performance regression

---

### Quality Gates

**Definition of Done:**
- [ ] Code reviewed by at least one team member
- [ ] Unit tests written and passing (70%+ coverage for new code)
- [ ] Integration tests for external dependencies
- [ ] No critical or high severity bugs
- [ ] API documentation updated
- [ ] Deployment runbook updated
- [ ] Manual testing completed
- [ ] Performance impact assessed
- [ ] Security review for sensitive changes

**Release Criteria:**
- [ ] All tests passing in CI/CD pipeline
- [ ] No open P0/P1 bugs
- [ ] Performance within acceptable thresholds
- [ ] Database migrations tested
- [ ] Rollback procedure documented and tested
- [ ] Monitoring and alerts configured
- [ ] Business stakeholder approval

---

## Deployment Procedures & Rollback

### Pre-Deployment Checklist

**Infrastructure:**
- [ ] YugabyteDB cluster healthy (all nodes UP)
- [ ] Sufficient disk space on all nodes
- [ ] Backup completed within last 24 hours
- [ ] Network connectivity verified
- [ ] DNS/Load balancer configuration verified

**Application:**
- [ ] All services built successfully
- [ ] Docker images pushed to registry (if using containers)
- [ ] Configuration files reviewed and updated
- [ ] Database migrations tested in staging
- [ ] Feature flags configured (if applicable)

**Team Readiness:**
- [ ] Deployment runbook reviewed
- [ ] Rollback procedure tested
- [ ] On-call engineer identified
- [ ] Communication plan ready (if customer-impacting)
- [ ] Monitoring dashboards ready

---

### Deployment Procedure (Local/Development)

**Step 1: Database Preparation**
```bash
# Backup current database
yugabyted backup --backup_location=/backup/$(date +%Y%m%d)

# Apply schema changes (if any)
cqlsh -f migrations/001_add_column.cql
ysqlsh -f migrations/001_add_column.sql

# Verify schema changes
cqlsh -e "DESCRIBE TABLE cronos.products"
```

**Step 2: Service Deployment**
```bash
# Stop services (reverse order)
# Stop React UI
pkill -f react-ui

# Stop API Gateway
pkill -f api-gateway

# Stop backend services
pkill -f products-microservice
pkill -f cart-microservice
pkill -f checkout-microservice

# Build new versions
mvn -DskipTests clean package

# Start Eureka (if not running)
cd eureka-server-local && mvn spring-boot:run &

# Wait for Eureka to start
sleep 30

# Start backend services
cd products-microservice && mvn spring-boot:run &
cd cart-microservice && mvn spring-boot:run &
cd checkout-microservice && mvn spring-boot:run &

# Wait for service registration
sleep 60

# Start API Gateway
cd api-gateway-microservice && mvn spring-boot:run &

# Wait for API Gateway registration
sleep 30

# Start React UI
cd react-ui && mvn spring-boot:run &
```

**Step 3: Verification**
```bash
# Check Eureka registration
curl http://localhost:8761/eureka/apps

# Health checks
curl http://localhost:8082/actuator/health  # Products
curl http://localhost:8083/actuator/health  # Cart
curl http://localhost:8086/actuator/health  # Checkout
curl http://localhost:8081/actuator/health  # API Gateway

# Smoke tests
curl http://localhost:8081/api/v1/products?limit=10
curl -X POST http://localhost:8081/api/v1/shoppingCart
```

---

### Deployment Procedure (Docker)

**Step 1: Build Images**
```bash
# Build all services
mvn -DskipTests clean package

# Verify images created
docker images | grep yugastore
```

**Step 2: Deploy Containers**
```bash
# Run deployment script
./docker-run.sh

# Verify containers running
docker ps | grep yugastore

# Check logs
docker logs <container-id>
```

**Step 3: Verification**
```bash
# Check Eureka
curl http://localhost:8761/eureka/apps

# Access application
curl http://localhost:8080
```

---

### Rollback Procedures

**Database Rollback:**
```bash
# Restore from backup
yugabyted restore --backup_location=/backup/20260127

# If migration needs reverting
cqlsh -f migrations/001_rollback.cql
ysqlsh -f migrations/001_rollback.sql
```

**Application Rollback (Local):**
```bash
# Checkout previous version
git checkout <previous-tag>

# Rebuild and deploy
mvn -DskipTests clean package

# Follow deployment procedure above
```

**Application Rollback (Docker):**
```bash
# Stop current containers
docker stop $(docker ps -q --filter \"name=yugastore\")

# Remove current containers
docker rm $(docker ps -aq --filter \"name=yugastore\")

# Pull previous images
docker pull yugastore-products:<previous-tag>
docker pull yugastore-cart:<previous-tag>
# ... etc

# Run with previous version
./docker-run.sh
```

**Rollback Decision Criteria:**

Rollback immediately if:
- Critical functionality broken (checkout, cart operations)
- Data corruption detected
- Severe performance degradation (>5x latency increase)
- Security vulnerability introduced
- Cascading failures across services

Consider rollback if:
- Minor functionality broken (non-critical features)
- Moderate performance impact (2-3x latency increase)
- High error rates but system stable

Monitor before deciding if:
- Cosmetic issues only
- Performance slightly degraded but acceptable
- Issues affect <5% of users

---

### Monitoring During Deployment

**Metrics to Watch:**

1. **Service Health:**
   - All services registered in Eureka
   - Health check endpoints returning 200
   - No repeated restarts

2. **Error Rates:**
   - HTTP 5xx responses
   - Exception logs
   - Failed database connections

3. **Performance:**
   - API response times
   - Database query latency
   - Service-to-service call latency

4. **Business Metrics:**
   - Successful checkouts
   - Cart operations
   - Product page views

**Warning Signs:**
- Error rate >1%
- Response time >500ms p95
- Any service not registering with Eureka
- Database connection failures
- Memory leaks (heap usage climbing)

---

## Technical Complexity Assessment

### Service Complexity Matrix

| Service | Lines of Code | Cyclomatic Complexity | External Dependencies | Change Frequency | Maintenance Difficulty |
|---------|--------------|---------------------|---------------------|-----------------|----------------------|
| Products | ~2,500 | Medium | YugabyteDB YCQL, Eureka | Low | Medium |
| Cart | ~1,200 | Low | YugabyteDB YSQL, Eureka | Medium | Low |
| Checkout | ~3,000 | High | YugabyteDB YCQL, Products, Cart, Eureka | Medium | High |
| API Gateway | ~2,000 | Medium | All backend services, Eureka | Medium | Medium |
| React UI | ~3,500 | Medium | API Gateway | High | Medium |
| Eureka | ~500 | Low | None | Very Low | Low |

---

### Complexity by Technical Domain

**Data Access Layer:**
- **YCQL (Cassandra):** High complexity
  - Non-relational data modeling
  - CQL query syntax different from SQL
  - Limited transaction support (even when enabled)
  - No foreign keys or joins
  - Steep learning curve

- **YSQL (PostgreSQL):** Medium complexity
  - Familiar SQL syntax
  - JPA/Hibernate abstraction
  - Standard relational patterns
  - Well-documented

**Service Communication:**
- **Feign Clients:** Medium complexity
  - Declarative REST clients
  - Eureka integration
  - Error handling
  - Retry logic
  - Timeout configuration

**Service Orchestration:**
- **Checkout Service:** High complexity
  - Multi-step workflow
  - Distributed transaction coordination
  - Error handling across services
  - Compensation logic for failures
  - Requires deep understanding of all services

**Frontend:**
- **React Components:** Medium complexity
  - Component lifecycle
  - State management
  - API integration
  - Error handling
  - Responsive design

---

### Technology Learning Curve

**For New Team Members:**

**Week 1-2: Foundation**
- Spring Boot basics
- Maven build process
- Git workflow
- Local development setup
- YugabyteDB installation and basics

**Week 3-4: Core Technologies**
- Spring Cloud concepts (Eureka, Feign)
- YSQL vs YCQL differences
- Microservices communication patterns
- Docker containerization
- Testing frameworks

**Week 5-6: System Understanding**
- Service dependencies
- Data flow through system
- Debugging distributed systems
- Deployment procedures
- Monitoring and troubleshooting

**Week 7-8: Productive Development**
- First feature implementation
- Code review process
- Release procedures
- On-call responsibilities

**Estimated Ramp-Up Time:**
- Junior Developer: 8-12 weeks to full productivity
- Mid-Level Developer: 4-6 weeks to full productivity
- Senior Developer: 2-3 weeks to full productivity

---

### Common Development Challenges

1. **Service Discovery Issues**
   - **Problem:** Services not finding each other
   - **Root Cause:** Eureka not running, network issues, wrong service names
   - **Resolution Time:** 15-30 minutes
   - **Prevention:** Startup scripts, health checks

2. **Database Connection Problems**
   - **Problem:** Can't connect to YSQL or YCQL
   - **Root Cause:** YugabyteDB not running, wrong port, credentials
   - **Resolution Time:** 10-20 minutes
   - **Prevention:** Docker compose setup, connection retry logic

3. **Feign Client Timeouts**
   - **Problem:** Service calls timing out
   - **Root Cause:** Slow service, network issues, missing endpoints
   - **Resolution Time:** 30-60 minutes
   - **Prevention:** Configure appropriate timeouts, circuit breakers

4. **YCQL Query Performance**
   - **Problem:** Slow queries, timeouts
   - **Root Cause:** Missing indexes, inefficient queries, large result sets
   - **Resolution Time:** 2-4 hours (analysis + fix)
   - **Prevention:** Query planning, index design, pagination

5. **Transaction Failures**
   - **Problem:** Orders not created, inventory not updated
   - **Root Cause:** Transaction conflicts, isolation issues
   - **Resolution Time:** 1-4 hours
   - **Prevention:** Transaction design, retry logic, monitoring

---

## Team Skills & Capacity Requirements

### Core Skills Required

**Backend Development (4-5 engineers):**

**Must Have:**
- Java 11+ proficiency
- Spring Boot framework
- RESTful API design
- Git version control
- Unit testing (JUnit, Mockito)
- SQL fundamentals

**Should Have:**
- Spring Cloud (Eureka, Feign, Config)
- Microservices architecture patterns
- Docker containerization
- NoSQL databases (Cassandra/CQL knowledge helpful)
- Integration testing
- CI/CD pipelines

**Nice to Have:**
- Distributed systems experience
- YugabyteDB specific knowledge
- Kubernetes
- Performance tuning
- Security best practices

**Frontend Development (2-3 engineers):**

**Must Have:**
- JavaScript ES6+
- React framework
- HTML5/CSS3
- RESTful API consumption
- Git version control
- Browser debugging tools

**Should Have:**
- React Router
- State management (Redux/Context API)
- Responsive design
- npm/yarn package management
- Webpack/build tools
- Unit testing (Jest, React Testing Library)

**Nice to Have:**
- TypeScript
- UI/UX design principles
- Accessibility standards
- Performance optimization

**DevOps/Infrastructure (1-2 engineers):**

**Must Have:**
- Linux system administration
- Docker containerization
- Shell scripting
- Network fundamentals
- Git version control

**Should Have:**
- YugabyteDB administration
- Database backup/restore
- Monitoring tools
- Log aggregation
- CI/CD pipelines (Jenkins, GitLab CI, GitHub Actions)

**Nice to Have:**
- Kubernetes orchestration
- Terraform/Infrastructure as Code
- Cloud platforms (AWS, GCP, Azure)
- Service mesh (Istio, Linkerd)

---

### Team Capacity Planning

**For New Feature Development:**

**Small Feature (2-5 days):**
- 1 Backend Engineer
- 1 Frontend Engineer (if UI changes)
- Part-time: QA, DevOps

**Medium Feature (1-2 weeks):**
- 2 Backend Engineers
- 1 Frontend Engineer
- Part-time: QA, DevOps, Product Owner

**Large Feature (2-4 weeks):**
- 3-4 Backend Engineers
- 2 Frontend Engineers
- Full-time: 1 QA Engineer
- Part-time: DevOps, Product Owner, Architect

**For Maintenance & Bug Fixes:**
- Allocate 20-30% of sprint capacity
- 1-2 engineers on rotation for production support

**For Technical Debt:**
- Allocate 15-20% of sprint capacity
- Dedicated time each sprint
- Track as backlog items

---

### Skill Gaps & Training Needs

**Current Gaps:**

1. **YugabyteDB Expertise:**
   - Need: Deep understanding of YCQL and YSQL
   - Training: Official YugabyteDB certification, hands-on workshops
   - Timeline: 2-3 months to proficiency

2. **Distributed Systems:**
   - Need: Transaction handling, consistency patterns
   - Training: Books, online courses, mentorship
   - Timeline: 3-6 months to proficiency

3. **Production Operations:**
   - Need: Monitoring, incident response, troubleshooting
   - Training: On-call shadowing, runbook creation
   - Timeline: 2-3 months to confidence

4. **Security Best Practices:**
   - Need: Authentication, authorization, secure coding
   - Training: Security workshops, code review focus
   - Timeline: 1-2 months for basics

**Recommended Training Path:**

**Month 1:**
- Spring Boot advanced topics
- YugabyteDB fundamentals
- Microservices patterns

**Month 2:**
- YugabyteDB administration
- Distributed transactions
- Service discovery patterns

**Month 3:**
- Production operations
- Monitoring and alerting
- Incident response

---

## Operational Considerations

### System Health Monitoring

**Service-Level Metrics:**

1. **Availability:**
   - Target: 99.9% uptime (43 minutes downtime/month)
   - Measurement: Health check endpoint success rate
   - Alert: Any service down >5 minutes

2. **Response Time:**
   - Target: p95 < 200ms (API Gateway)
   - Measurement: HTTP request duration
   - Alert: p95 > 500ms for >5 minutes

3. **Error Rate:**
   - Target: <0.1% HTTP 5xx responses
   - Measurement: Error count / total requests
   - Alert: Error rate >1% for >2 minutes

4. **Throughput:**
   - Baseline: ~100 req/sec current capacity
   - Measurement: Requests per second
   - Alert: Sudden drop >50%

**Database Metrics:**

1. **Connection Pool:**
   - Target: <80% utilization
   - Alert: >90% utilization for >5 minutes

2. **Query Latency:**
   - Target: p95 < 50ms
   - Alert: p95 > 200ms for >5 minutes

3. **Disk Space:**
   - Target: <70% utilization
   - Alert: >85% utilization

4. **Replication Lag:**
   - Target: <1 second
   - Alert: >10 seconds

**Business Metrics:**

1. **Checkout Success Rate:**
   - Target: >95%
   - Alert: <90% for >10 minutes

2. **Cart Operations:**
   - Target: >99% success
   - Alert: <95% success

3. **Product Page Load:**
   - Target: 100% success
   - Alert: <98% success

---

### Logging Strategy

**Log Levels:**
- **ERROR:** Service errors, exceptions, failures (page on-call)
- **WARN:** Degraded performance, retries, recoverable errors
- **INFO:** Business events, service lifecycle, key operations
- **DEBUG:** Detailed flow, variable values (development only)

**Log Structure (JSON):**
```json
{
  \"timestamp\": \"2026-01-27T10:30:00Z\",
  \"level\": \"ERROR\",
  \"service\": \"checkout-microservice\",
  \"traceId\": \"abc123\",
  \"userId\": \"u1001\",
  \"message\": \"Inventory check failed\",
  \"exception\": \"InsufficientStockException\",
  \"asin\": \"B00001\"
}
```

**What to Log:**
- Service startup/shutdown
- All API requests (with response time)
- Database queries (slow queries >100ms)
- Service-to-service calls
- Authentication attempts
- Business transactions (cart add, checkout)
- Errors and exceptions with stack traces
- Configuration changes

**What NOT to Log:**
- Passwords or credentials
- Full credit card numbers
- Personal identifiable information (PII)
- Large request/response payloads

---

### Backup and Recovery

**Database Backup Strategy:**

**Full Backup:**
- Frequency: Daily at 2 AM
- Retention: 30 days
- Location: Local disk + offsite storage
- Command: `yugabyted backup --backup_location=/backup/$(date +%Y%m%d)`

**Incremental Backup:**
- Frequency: Every 4 hours
- Retention: 7 days
- Not currently implemented (YugabyteDB feature limitation)

**Recovery Time Objective (RTO):**
- Target: 4 hours
- Full database restore from backup

**Recovery Point Objective (RPO):**
- Target: 24 hours (daily backup)
- Acceptable data loss: 1 day of transactions

**Disaster Recovery Procedure:**
1. Identify failure scope (database vs. application)
2. Restore YugabyteDB from latest backup
3. Rebuild and deploy application services
4. Verify data integrity
5. Resume normal operations
6. Analyze root cause

---

### Capacity Planning

**Current Capacity:**
- Products: 6,000 items
- Concurrent Users: ~50 (estimated based on dev setup)
- Transactions/Day: <1,000
- Storage: ~5 GB

**Growth Projections:**

**Year 1:**
- Products: 10,000 items
- Concurrent Users: 500
- Transactions/Day: 10,000
- Storage: 20 GB

**Scaling Actions Needed:**
- Add YugabyteDB nodes (3 → 5)
- Scale services horizontally (2-3 instances each)
- Add load balancer
- Implement caching layer

**Year 2:**
- Products: 50,000 items
- Concurrent Users: 2,000
- Transactions/Day: 50,000
- Storage: 100 GB

**Scaling Actions Needed:**
- YugabyteDB cluster expansion (5 → 9 nodes)
- Service scaling (3-5 instances each)
- CDN for static assets
- Database read replicas
- Multi-region deployment

---

### On-Call and Incident Response

**On-Call Rotation:**
- 1 week rotations
- Primary + Secondary engineer
- 24/7 coverage recommended for production

**Incident Severity Levels:**

**P0 (Critical):**
- System down, checkout broken
- Response: Immediate (<5 minutes)
- Escalation: Management notified immediately
- Examples: Database crash, all services down

**P1 (High):**
- Major feature broken, significant degradation
- Response: <30 minutes
- Escalation: Manager notified within 1 hour
- Examples: Cart not working, slow response times

**P2 (Medium):**
- Minor feature broken, limited impact
- Response: <2 hours
- Escalation: Manager notified next business day
- Examples: UI glitch, non-critical API error

**P3 (Low):**
- Cosmetic issues, no functional impact
- Response: Next business day
- Escalation: None required
- Examples: Typo, minor UI misalignment

**Incident Response Playbook:**

1. **Acknowledge:** Respond to alert within SLA
2. **Assess:** Determine severity and impact
3. **Communicate:** Update status page, notify stakeholders
4. **Mitigate:** Apply immediate fix or rollback
5. **Resolve:** Implement permanent solution
6. **Document:** Write post-mortem
7. **Learn:** Update runbooks, add monitoring

---

## Technical Debt & Maintenance

### Current Technical Debt Inventory

**Priority 1 (High Impact, Should Fix Soon):**

1. **Fixed User ID (\"u1001\")**
   - Impact: Blocks multi-user functionality
   - Effort: 2-3 weeks (complete Login service)
   - Dependencies: User database schema, session management
   - Business Impact: Cannot support real customers

2. **No Centralized Error Handling**
   - Impact: Inconsistent error responses, poor debugging
   - Effort: 1 week
   - Dependencies: None
   - Business Impact: Longer incident resolution time

3. **Missing Monitoring and Observability**
   - Impact: Cannot detect issues proactively
   - Effort: 2 weeks (Prometheus + Grafana setup)
   - Dependencies: Infrastructure setup
   - Business Impact: Higher MTTR (Mean Time To Repair)

**Priority 2 (Medium Impact, Plan for Next Quarter):**

4. **Hard-Coded Configuration**
   - Impact: Difficult to deploy across environments
   - Effort: 1 week (Spring Cloud Config)
   - Dependencies: Config server setup
   - Business Impact: Slower deployments, config errors

5. **No Rate Limiting or Circuit Breakers**
   - Impact: Vulnerable to cascading failures
   - Effort: 1-2 weeks (Resilience4j)
   - Dependencies: None
   - Business Impact: Service instability under load

6. **Limited Test Coverage**
   - Impact: Regression risks, slow development
   - Effort: Ongoing (3-4 sprints to reach 70%)
   - Dependencies: Testing framework setup
   - Business Impact: More production bugs

**Priority 3 (Low Impact, Nice to Have):**

7. **React UI Using Older React Version**
   - Impact: Missing modern React features
   - Effort: 1-2 weeks
   - Dependencies: Component rewrite
   - Business Impact: Slower frontend development

8. **No API Documentation (Swagger/OpenAPI)**
   - Impact: Harder for frontend developers
   - Effort: 3-5 days
   - Dependencies: None
   - Business Impact: Slower integration work

9. **Inconsistent Code Style**
   - Impact: Code review friction
   - Effort: 2-3 days (CheckStyle setup)
   - Dependencies: None
   - Business Impact: Slower code reviews

---

### Maintenance Windows

**Regular Maintenance:**
- **Frequency:** Bi-weekly (every other Sunday 2-6 AM)
- **Activities:**
  - Database optimization
  - Log rotation
  - Disk space cleanup
  - Security patches
  - Dependency updates

**Emergency Maintenance:**
- **Trigger:** Critical security vulnerability, data corruption
- **Process:**
  1. Assess severity and impact
  2. Notify stakeholders (15 minutes notice if possible)
  3. Take backup
  4. Apply fix
  5. Verify system health
  6. Post-incident review

**Planned Downtime:**
- **Target:** <4 hours/year
- **Schedule:** Coordinated with business (avoid peak shopping periods)
- **Communication:** 2 weeks notice, status page updates

---

### Dependency Management

**Java Dependencies:**
- **Review Frequency:** Monthly
- **Update Strategy:** Minor versions quarterly, major versions semi-annually
- **Security Updates:** Within 1 week of CVE disclosure

**Critical Dependencies:**
- Spring Boot 2.6.3 → Monitor for 2.7.x updates
- YugabyteDB Driver 4.6.0-yb-10 → Follow YugabyteDB releases
- React 16.2 → Plan upgrade to React 18

**Dependency Risks:**
- Spring Boot 2.x will reach end-of-life
- Older React version missing features
- Some dependencies have known CVEs (low severity)

**Recommended Actions:**
- Quarterly dependency review
- Automated security scanning (Dependabot, Snyk)
- Test suite to validate updates

---

## Risk Register & Mitigation

### Technical Risks

**Risk 1: YugabyteDB Single Point of Failure**
- **Likelihood:** Medium
- **Impact:** Critical (complete system outage)
- **Mitigation:**
  - Deploy YugabyteDB in HA configuration (3+ nodes, RF=3)
  - Regular backup testing
  - Documented recovery procedures
  - Monitoring and alerting
- **Owner:** DevOps Lead
- **Status:** Partially mitigated (backups exist, multi-node not deployed)

**Risk 2: Eureka Server Failure**
- **Likelihood:** Low
- **Impact:** High (services cannot discover each other)
- **Mitigation:**
  - Deploy multiple Eureka instances
  - Services cache registry locally
  - Fallback to direct service URLs
  - Quick restart procedure (<5 minutes)
- **Owner:** Delivery Lead
- **Status:** Low mitigation (single Eureka instance)

**Risk 3: Cascade Failure (Checkout → Cart → Products)**
- **Likelihood:** Medium
- **Impact:** High (checkout broken, revenue impact)
- **Mitigation:**
  - Implement circuit breakers (Resilience4j)
  - Set appropriate timeouts
  - Graceful degradation patterns
  - Rate limiting per service
- **Owner:** Delivery Lead
- **Status:** Not mitigated (no circuit breakers)

**Risk 4: YCQL Transaction Conflicts**
- **Likelihood:** Medium
- **Impact:** Medium (checkout failures, inventory errors)
- **Mitigation:**
  - Implement retry logic with exponential backoff
  - Optimize transaction isolation levels
  - Reduce transaction scope
  - Monitor transaction conflicts
- **Owner:** Backend Tech Lead
- **Status:** Partially mitigated (basic retry exists)

**Risk 5: Insufficient Monitoring**
- **Likelihood:** High
- **Impact:** High (cannot detect or diagnose issues)
- **Mitigation:**
  - Deploy Prometheus + Grafana
  - Add custom business metrics
  - Set up alerting rules
  - Create operational dashboards
- **Owner:** DevOps Lead
- **Status:** High risk (minimal monitoring)

### Delivery Risks

**Risk 6: Knowledge Silos**
- **Likelihood:** Medium
- **Impact:** Medium (slows delivery, blocks releases)
- **Mitigation:**
  - Documentation (architecture, runbooks)
  - Pair programming
  - Code reviews mandatory
  - Knowledge sharing sessions
  - Cross-training on all services
- **Owner:** Delivery Lead
- **Status:** Partially mitigated (docs improving)

**Risk 7: Complex Deployments**
- **Likelihood:** Medium
- **Impact:** Medium (deployment failures, rollback needs)
- **Mitigation:**
  - Automated deployment scripts
  - Blue-green deployment strategy
  - Thorough pre-deploy checklist
  - Deployment rehearsals in staging
- **Owner:** DevOps Lead
- **Status:** Partially mitigated (scripts exist, not automated)

**Risk 8: Inadequate Testing**
- **Likelihood:** High
- **Impact:** High (production bugs, customer impact)
- **Mitigation:**
  - Improve test coverage to 70%+
  - Add integration and E2E tests
  - Performance testing before releases
  - Staging environment for validation
- **Owner:** Delivery Lead
- **Status:** High risk (low test coverage)

### Business Risks

**Risk 9: Fixed User ID Limitation**
- **Likelihood:** High (if trying to launch to customers)
- **Impact:** Critical (cannot support multiple users)
- **Mitigation:**
  - Prioritize Login service completion
  - Implement proper authentication
  - Add user session management
- **Owner:** Product Owner
- **Status:** Accepted for demo, must fix for production

**Risk 10: No Security Implementation**
- **Likelihood:** High (if exposed to internet)
- **Impact:** Critical (security breach, data loss)
- **Mitigation:**
  - Implement authentication and authorization
  - Add API rate limiting
  - Security audit before production
  - TLS/SSL for all communications
- **Owner:** Security Representative
- **Status:** High risk (demo only, not production-ready)

---

## Appendix

### Key Configuration Files

**Eureka Server (application.yml):**
```yaml
server:
  port: 8761
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

**Microservice (bootstrap.yml):**
```yaml
spring:
  application:
    name: products-microservice
server:
  port: 8082
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka
```

**Products Service (application.yml):**
```yaml
cronos:
  yugabyte:
    keyspace: cronos
    hostname: 127.0.0.1
    port: 9042
```

**Cart Service (application.yml):**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://127.0.0.1:5433/postgres
    username: postgres
    password: ""
  jpa:
    database: postgresql
    hibernate:
      ddl-auto: update
```

### Build and Run Commands

**Build All Services:**
```bash
mvn -DskipTests package
```

**Run Individual Service:**
```bash
cd <service-directory>
mvn spring-boot:run
```

**Docker Build and Run:**
```bash
# Build (done automatically with mvn package)
docker build -t yugastore-products products-microservice/

# Run all services
./docker-run.sh
```

**Database Setup:**
```bash
# Create YCQL schema
cqlsh -f resources/schema.cql

# Create YSQL schema
ysqlsh -f resources/schema.sql

# Load sample data
cd resources
./dataload.sh
```

### Monitoring and Observability

**Current State:**
- Spring Boot Actuator endpoints
- Console logging
- Eureka dashboard for service health

**Recommended Additions:**
- Prometheus for metrics collection
- Grafana for visualization
- ELK stack for centralized logging
- Distributed tracing (Zipkin/Jaeger)
- APM tools (New Relic, Datadog)

### Related Documentation

- [README.md](../README.md) - Getting started guide
- [Business Requirements](business-requirements.md) - Business logic documentation
- [Business Purpose](businesspurpose.md) - Platform objectives
- [Project Charter](projectcharter.md) - Project governance

---

**Document History:**

| Date | Version | Changes | Author |
|------|---------|---------|--------|
| 2026-01-27 | 1.0 | Initial architecture documentation | System Architect |

---

*This architecture document represents the current state of YugaStore as of January 2026. For questions or updates, please refer to the project repository.*
