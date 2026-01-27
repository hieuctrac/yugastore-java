# YugaStore - System Architecture Documentation

**Version:** 1.0  
**Date:** January 27, 2026  
**Status:** Current State Documentation

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [System Overview](#system-overview)
3. [Architecture Principles](#architecture-principles)
4. [Component Architecture](#component-architecture)
5. [Data Architecture](#data-architecture)
6. [Communication Patterns](#communication-patterns)
7. [Service Discovery & Registration](#service-discovery--registration)
8. [Technology Stack](#technology-stack)
9. [Deployment Architecture](#deployment-architecture)
10. [Security Architecture](#security-architecture)
11. [Scalability & Performance](#scalability--performance)
12. [Integration Points](#integration-points)
13. [Architecture Decisions](#architecture-decisions)

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
