# YugaStore - Java Microservices eCommerce Platform

![Homepage](docs/home.png)

YugaStore is a full-featured microservices-based retail marketplace demonstrating modern distributed application architecture. Built with **Spring Boot microservices**, a **React frontend**, and **YugabyteDB** as the distributed SQL database, it showcases a production-grade eCommerce platform with service discovery, API gateway patterns, and polyglot persistence using both YSQL and YCQL APIs.

If you're using this demo app, please :star: this repository to show your support!

## Key Features

- **Microservices Architecture**: 7 independent Spring Boot services with Eureka service discovery
- **Polyglot Persistence**: Demonstrates both YSQL (PostgreSQL-compatible) and YCQL (Cassandra-compatible) APIs
- **Modern UI**: React-based responsive frontend with Bootstrap styling
- **Admin Portal**: Role-based product management with JWT authentication and audit logging
- **Real Product Data**: Over 6,000 products with categories, ratings, and recommendations
- **Production Patterns**: API gateway, service registry, distributed transactions, and container deployment

## Technology Stack

* **Java 17** - Modern LTS Java version
* **Spring Boot 2.6.3** - Microservices framework
* **Spring Cloud 2021.0.0** - Service discovery and cloud patterns
* **Yugabyte Java Driver 4.6.0-yb-10** - YugabyteDB native driver
* **React 16.2** - Frontend UI framework
* **YugabyteDB** - Distributed SQL database (YSQL + YCQL)
* **Eureka** - Service discovery and registration
* **Maven** - Build and dependency management
* **Docker** - Container deployment
* **Python 3** - Data loading utilities

## Architecture Overview

YugaStore implements a microservices architecture with clear separation of concerns and distributed data management.

![Architecture of microservices based retail marketplace app](yugastore-java-architecture.png)

### Microservices

| Service | Database API | Port | Description |
| ------- | ------------ | ---- | ----------- |
| **[Eureka Server](eureka-server-local/)** | - | [8761](http://localhost:8761) | Service discovery registry. All microservices register here for dynamic service location, load balancing, and health monitoring. |
| **[React UI](react-ui/)** | - | [8080](http://localhost:8080) | Customer-facing React single-page application. Provides product browsing, shopping cart, and checkout interfaces. |
| **[API Gateway](api-gateway-microservice/)** | - | [8081](http://localhost:8081) | Central entry point for all external requests. Routes and aggregates calls to backend microservices. Only service the UI communicates with directly. |
| **[Products](products-microservice/)** | YCQL | [8082](http://localhost:8082) | Product catalog service. Manages product information, categories, rankings, and recommendations. Uses Cassandra-compatible YCQL API for high-read performance. |
| **[Cart](cart-microservice/)** | YSQL | [8083](http://localhost:8083) | Shopping cart management. Handles add/remove items and cart state. Uses PostgreSQL-compatible YSQL API with JPA for ACID transactions. |
| **[Admin Portal](admin-microservice/)** | YSQL + YCQL | [8084](http://localhost:8084) | Admin portal for product management. Role-based access control (Admin/Editor/Viewer), JWT authentication, audit logging. Uses YSQL for admin users and audit logs, YCQL for product catalog access. |
| **[Login](login-microservice/)** | YSQL | [8085](http://localhost:8085) | User authentication service. Manages user credentials and sessions using YSQL. *(In development)* |
| **[Checkout](checkout-microservice/)** | YCQL | [8086](http://localhost:8086) | Order processing and inventory management. Handles order placement and stock verification using YCQL with transactions enabled. |

### Database Schema

**YCQL (Cassandra-compatible) Tables:**
- `cronos.products` - Product catalog with titles, descriptions, prices, images, and relationships
- `cronos.product_rankings` - Product rankings by category for efficient sorted retrieval
- `cronos.orders` - Order records with transaction support
- `cronos.product_inventory` - Real-time inventory tracking with transaction support

**YSQL (PostgreSQL-compatible) Tables:**
- `shopping_cart` - User cart items with quantities and timestamps
- `admin_users` - Admin portal user accounts with role-based access control
- `product_audit_log` - Audit trail for all product changes made through admin portal

## Quick Start

### Prerequisites

- **Java 17** or later
- **Maven 3.6+**
- **Node.js 14+** and npm (for React UI)
- **YugabyteDB** (see installation instructions below)

---

# Build and run

## Building the Application

Build all microservices from the root directory:

```bash
mvn -DskipTests package
```

This command:
- Compiles all 6 microservices
- Packages them as executable JARs
- Builds the React frontend
- Creates Docker images for each service

---

## Option 1: Running on Host Machine

Run YugaStore locally with all services on your host machine.

### Step 1: Install YugabyteDB

Install YugabyteDB by following the [official installation guide](https://docs.yugabyte.com/latest/quick-start/).

Start a local YugabyteDB cluster:

```bash
yugabyted start
```

Verify the cluster is running at [http://localhost:15433](http://localhost:15433).

### Step 2: Create Database Schema

Create the YCQL tables:

```bash
cd resources
cqlsh -f schema.cql
```

Create the YSQL tables:

```bash
ysqlsh -f schema.sql
```

### Step 3: Load Sample Data

Load 6,000+ products into the database:

```bash
cd resources
./dataload.sh
```

This script:
- Parses product metadata JSON
- Loads products, rankings, and inventory into YCQL tables
- Uses the cassandra-loader utility (downloaded automatically)

### Step 4: Start Microservices

Start each service in a separate terminal window in the following order:

**1. Start Eureka Service Discovery:**
```bash
cd eureka-server-local/
mvn spring-boot:run
```
Verify at [http://localhost:8761](http://localhost:8761)

**2. Start API Gateway:**
```bash
cd api-gateway-microservice/
mvn spring-boot:run
```

**3. Start Products Service:**
```bash
cd products-microservice/
mvn spring-boot:run
```

**4. Start Checkout Service:**
```bash
cd checkout-microservice/
mvn spring-boot:run
```

**5. Start Cart Service:**
```bash
cd cart-microservice/
mvn spring-boot:run
```

**6. Start React UI:**
```bash
cd react-ui/
mvn spring-boot:run
```

### Step 5: Access the Application

Open [http://localhost:8080](http://localhost:8080) in your browser.

Wait for all services to register with Eureka (check the [Eureka dashboard](http://localhost:8761)).

---

## Option 2: Running with Docker

Run YugaStore in Docker containers for easier deployment and isolation.

### Prerequisites

1. Install and start YugabyteDB (same as Option 1, Step 1)
2. Create database schema (same as Option 1, Step 2)
3. Load sample data (same as Option 1, Step 3)
4. Build the application to create Docker images: `mvn -DskipTests package`

### Start All Services

Run the provided script to start all Docker containers:

```bash
./docker-run.sh
```

This script:
- Starts Eureka server on port 8761
- Starts API Gateway on port 8081
- Starts Products service on port 8082
- Starts Checkout service on port 8086
- Starts Cart service on port 8083
- Starts React UI on port 8080

### Verify and Access

1. Check that all services are registered at [http://localhost:8761](http://localhost:8761)
2. Once registered, access the application at [http://localhost:8080](http://localhost:8080)

### Stopping Services

To stop all containers:

```bash
docker ps  # List running containers
docker stop <container_id>  # Stop each container
```

---

## Configuration

### Database Connection Settings

**Products & Checkout Services (YCQL):**
- Edit `application.yml` in each service
- Default: `localhost:9042`, keyspace: `cronos`

**Cart & Login Services (YSQL):**
- Edit `application.yml` in each service  
- Default: `localhost:5433`, database: `postgres`

### Service Ports

All ports are configurable in each service's `application.yml`:

```yaml
server:
  port: 8082  # Change as needed
```

### Eureka Service Discovery

Services auto-register with Eureka on startup. Configure in `bootstrap.yml`:

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

---

## Admin Portal

YugaStore now includes an **Admin Portal** for managing the product catalog through a secure web interface. The admin portal provides role-based access control with three user roles:

### Features

- **JWT Authentication**: Secure token-based authentication with 30-minute session timeout
- **Role-Based Access Control**:
  - **ADMIN**: Full access - create, read, update, delete products
  - **EDITOR**: Create and update products (cannot delete)
  - **VIEWER**: Read-only access to products and audit logs
- **Audit Logging**: Complete audit trail of all product changes
- **Product Management**: Search, create, update, deactivate, and delete products
- **Dual Database**: YSQL for admin users/audit logs, YCQL for product catalog

### Quick Start

1. **Initialize admin database schema**:
```bash
psql -h 127.0.0.1 -p 5433 -U yugabyte -d yugabyte \
  -f admin-microservice/src/main/resources/schema-admin.sql
```

2. **Create default admin users**:
```bash
psql -h 127.0.0.1 -p 5433 -U yugabyte -d yugabyte \
  -f resources/seed-admin-users.sql
```

3. **Start the admin microservice**:
```bash
cd admin-microservice
mvn spring-boot:run
```

4. **Access the admin portal**:
   - URL: `http://localhost:3000/admin/login`
   - Login: `admin` / `admin123` (default - change in production!)

### Default Test Users

| Username | Password   | Role   | Permissions              |
|----------|------------|--------|--------------------------|
| admin    | admin123   | ADMIN  | Full access              |
| editor   | editor123  | EDITOR | Create & update only     |
| viewer   | viewer123  | VIEWER | Read-only access         |

**⚠️ WARNING**: Change these passwords before deploying to production!

### API Endpoints

- `POST /api/admin/auth/login` - Authenticate and get JWT token
- `GET /api/admin/auth/me` - Get current user info
- `POST /api/admin/auth/logout` - Logout (clear session)

For complete documentation, see:
- [Admin Portal Quick Start Guide](docs/ADMIN-PORTAL-QUICKSTART.md)
- [Admin Microservice README](admin-microservice/README.md)

### Implementation Status

- ✅ **Phase 1-2 Complete**: Foundation (authentication, security, audit logging)
- 🚧 **Phase 3-5 In Progress**: Product search, CRUD operations, bulk updates
- 📋 **Phase 6-10 Planned**: Advanced features (deactivation, delete, history viewer)

---

## Development

### Project Structure

```
yugastore-java/
├── admin-microservice/          # Admin portal (YSQL + YCQL, JWT auth)
├── api-gateway-microservice/    # API Gateway (Spring Cloud Gateway)
├── cart-microservice/           # Shopping cart (YSQL + JPA)
├── checkout-microservice/       # Order processing (YCQL + Cassandra driver)
├── eureka-server-local/         # Service registry (Eureka Server)
├── login-microservice/          # Authentication (YSQL + JPA) [WIP]
├── products-microservice/       # Product catalog (YCQL + Cassandra driver)
├── react-ui/                    # Frontend (React + Spring Boot)
│   ├── frontend/                # React source code
│   │   └── src/
│   │       └── components/
│   │           └── Admin/       # Admin portal components
├── resources/                   # Database schemas and data loading
│   ├── schema.cql              # YCQL table definitions
│   ├── schema.sql              # YSQL table definitions
│   ├── schema-products-v2.cql  # YCQL schema extensions for admin
│   ├── seed-admin-users.sql    # Default admin users
│   ├── dataload.sh             # Data loading script
│   └── parse_metadata_json.py  # JSON to CSV converter
├── docs/                        # Documentation and business requirements
│   └── ADMIN-PORTAL-QUICKSTART.md  # Admin portal setup guide
└── specs/                       # Feature specifications
    └── 002-admin-portal/        # Admin portal spec and planning
```

### Adding New Microservices

1. Create a new Maven module in `pom.xml`
2. Extend from `spring-boot-starter-parent`
3. Add `spring-cloud-starter-netflix-eureka-client` dependency
4. Annotate main class with `@SpringBootApplication` and `@EnableDiscoveryClient`
5. Configure `application.yml` with unique service name and port

### Running Tests

```bash
mvn test  # Run all tests
mvn test -pl products-microservice  # Test specific service
```

---
## Application Features

### Customer-Facing Features

- **Product Browsing**: Browse 6,000+ products organized by categories
- **Product Search**: Navigate by category with sales rank-based sorting
- **Product Details**: View comprehensive product information including:
  - Titles, descriptions, and pricing
  - Product images
  - Customer ratings and reviews
  - Related product recommendations
- **Shopping Cart**: 
  - Add products to cart with automatic quantity management
  - Update item quantities
  - Remove items
  - Real-time cart total calculations
- **Checkout Process**:
  - Order placement with inventory verification
  - Transaction integrity with YugabyteDB's ACID guarantees
  - Order confirmation

### Technical Features

- **Service Discovery**: Automatic service registration and discovery with Eureka
- **API Gateway Pattern**: Centralized request routing and aggregation
- **Polyglot Persistence**: 
  - YCQL for high-read product catalog and order management
  - YSQL for transactional cart and user data
- **Distributed Transactions**: Enabled on critical tables (orders, inventory)
- **Containerization**: Docker support for all microservices
- **Scalability**: Designed for horizontal scaling and multi-region deployment
- **RESTful APIs**: Clean service interfaces with JSON payloads

---

## Troubleshooting

### Services Not Registering with Eureka

- Verify Eureka is running at [http://localhost:8761](http://localhost:8761)
- Check `bootstrap.yml` has correct Eureka URL
- Allow 30-60 seconds for initial registration
- Check service logs for connection errors

### Database Connection Errors

**YCQL Connection Issues:**
```bash
# Verify YugabyteDB is running
cqlsh localhost 9042

# Check keyspace exists
cqlsh> DESCRIBE KEYSPACE cronos;
```

**YSQL Connection Issues:**
```bash
# Verify PostgreSQL-compatible API
ysqlsh -h localhost -p 5433

# Check tables exist
\dt
```

### Port Already in Use

If a port is already occupied:
```bash
# Find process using the port
lsof -i :8080

# Kill the process
kill -9 <PID>
```

Or change the port in the service's `application.yml`.

### React UI Shows "Service Unavailable"

1. Verify all backend services are running and registered with Eureka
2. Check API Gateway is accessible at [http://localhost:8081](http://localhost:8081)
3. Verify proxy configuration in `react-ui/frontend/package.json` points to port 8081

### Data Loading Fails

- Ensure schema is created before running `dataload.sh`
- Verify `cassandra-loader` binary has execute permissions: `chmod +x cassandra-loader`
- Check disk space is available
- Review error output from the loader for specific issues

---

## Documentation

Additional documentation is available in the `docs/` directory:

- **[System Architecture](docs/architecture.md)** - Comprehensive architecture documentation including component details, data flows, testing strategy, deployment procedures, and design decisions
- **[Delivery Lead Guide](docs/delivery-guide.md)** - Quick reference for delivery planning, effort estimation, risk assessment, and incident response
- **[Software Engineer Guide](docs/software-engineer-guide.md)** - Practical guide for developers including setup instructions, common development tasks, debugging tips, testing best practices, and code examples
- **[Architecture Diagram](docs/architecture-diagram.txt)** - Visual ASCII diagrams showing system components and interactions
- **[Business Purpose](docs/businesspurpose.md)** - Platform objectives and business outcomes
- **[Project Charter](docs/projectcharter.md)** - Project governance and stakeholder roles
- **[Business Requirements](docs/business-requirements.md)** - Implemented business rules and logic
- **[Personas](docs/personas/)** - User personas and stakeholder profiles

---

## Contributing

This is a demonstration application showcasing YugabyteDB with Spring Boot microservices. Contributions are welcome!

### Development Workflow

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Make your changes
4. Run tests: `mvn test`
5. Commit with clear messages: `git commit -m "Add feature: description"`
6. Push to your fork: `git push origin feature/my-feature`
7. Create a Pull Request

### Code Style

- Follow Java naming conventions
- Use Spring Boot best practices
- Add comments for complex business logic
- Write unit tests for new features

---

## License

See the [LICENSE](LICENSE) file for details.

---

## Resources

- **YugabyteDB Documentation**: [https://docs.yugabyte.com](https://docs.yugabyte.com)
- **YugabyteDB GitHub**: [https://github.com/yugabyte/yugabyte-db](https://github.com/yugabyte/yugabyte-db)
- **Spring Boot**: [https://spring.io/projects/spring-boot](https://spring.io/projects/spring-boot)
- **Spring Cloud**: [https://spring.io/projects/spring-cloud](https://spring.io/projects/spring-cloud)

---

## Screenshots

### Home Page
![Home Page](docs/home.png)

### Product Category Page
![Product Category](docs/product-category.png)

### Product Detail Page
![Product Page](docs/product.png)

### Shopping Cart
![Cart](docs/cart.png)

### Checkout
![Checkout](docs/checkout.png)

---

## Support

For issues and questions:

- **YugabyteDB Issues**: [YugabyteDB GitHub Issues](https://github.com/yugabyte/yugabyte-db/issues)
- **Application Issues**: [YugaStore GitHub Issues](https://github.com/YugabyteDB-Samples/yugastore-java/issues)
- **YugabyteDB Community**: [Community Forum](https://forum.yugabyte.com)

---
