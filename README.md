# Microservices Project: ms_order, ms_product, and ms-order-clojure

## Overview

This repository contains microservices demonstrating different approaches to building order and product management systems:

- **`ms_order`** (Kotlin + Spring Boot): Original order service using gRPC and MySQL
- **`ms_product`** (Kotlin + Spring Boot): Product service acting as gRPC server
- **`ms-order-clojure`** (Clojure + Ring): Native Clojure implementation of the order service with MongoDB

The Kotlin services integrate using gRPC, while the Clojure version demonstrates functional programming and modern Clojure practices. The primary goals are to study Kotlin, explore Clojure's functional paradigm, and compare different architectural approaches.

## Services

### ms_order (Kotlin + Spring Boot)

The `ms_order` service is responsible for managing orders. It communicates with the `ms_product` service via gRPC to retrieve product information. The service also provides RESTful endpoints for creating, retrieving, and managing orders.

**Technology Stack:**
- Kotlin + Spring Boot
- MySQL + JPA/Hibernate
- gRPC client
- REST API

#### REST Endpoints

- **Create Order**
  - **POST** `/orders`
  - Request Body: `CreateOrderRequest`
  - Response: `CreateOrderResponse`

- **Get All Orders**
  - **GET** `/orders`
  - Response: List of `FindOrderResponse`

- **Get Order by ID**
  - **GET** `/orders/{id}`
  - Path Variable: `id`
  - Response: `FindOrderResponse`

### ms-order-clojure (Clojure Native + Ring)

The `ms-order-clojure` service is a complete rewrite of the order service in native Clojure, demonstrating functional programming principles and modern Clojure practices.

**Technology Stack:**
- Clojure (functional language)
- MongoDB (NoSQL database)
- Ring + Compojure (HTTP server and routing)
- Mock gRPC client (can be replaced with real implementation)

**Key Features:**
- ✨ Functional programming paradigm
- 🔄 Immutable data structures
- 🚀 Fast startup time (~1-2s)
- 💾 Low memory footprint (~100MB)
- 📚 Comprehensive documentation in Portuguese
- 🧪 Unit tests included

#### REST Endpoints

- **Health Check**
  - **GET** `/health`
  - Response: Service status

- **Create Order**
  - **POST** `/orders`
  - Request Body: `{"products": {"1": 2, "3": 1}}`
  - Response: Order with ID, products, and calculated price

- **Get All Orders**
  - **GET** `/orders`
  - Response: List of all orders

- **Get Order by ID**
  - **GET** `/orders/{id}`
  - Path Variable: `id` (MongoDB ObjectId)
  - Response: Order details

- **Get Order Summary**
  - **GET** `/orders/{id}/summary`
  - Response: Order with detailed product information

- **Delete Order**
  - **DELETE** `/orders/{id}`
  - Response: Success message

**Documentation:**
- 📖 [Complete Guide (Portuguese)](./ms-order-clojure/GUIA_COMPLETO.md) - Detailed explanation of every Clojure concept
- 📘 [Tutorial (Portuguese)](./ms-order-clojure/TUTORIAL.md) - Step-by-step execution guide
- 📊 [Comparison (Portuguese)](./ms-order-clojure/COMPARACAO.md) - Kotlin vs Clojure detailed comparison
- 🧪 [Tests Guide (Portuguese)](./ms-order-clojure/TESTES.md) - Testing and validation instructions

**Quick Start:**
```bash
cd ms-order-clojure
docker-compose up -d mongodb  # Start MongoDB
clojure -M:run                # Start application
```

### ms_product (Kotlin + Spring Boot)

The `ms_product` service manages product information and serves as a gRPC server. It provides product details to the `ms_order` service via gRPC and also exposes RESTful endpoints for CRUD operations on products.

#### REST Endpoints

- **Create Product**
  - **POST** `/products`
  - Request Body: `CreateProductRequest`
  - Response: `CreateProductResponse`

- **Get All Products**
  - **GET** `/products`
  - Response: List of `FindProductResponse`

- **Get Product by ID**
  - **GET** `/products/{id}`
  - Path Variable: `id`
  - Response: `FindProductResponse`

- **Update Product**
  - **PUT** `/products/{id}`
  - Path Variable: `id`
  - Request Body: `UpdateProductRequest`
  - Response: `UpdateProductResponse`


## Technologies Used

### Kotlin Services (ms_order, ms_product)
- **Kotlin**: The primary programming language used for both services.
- **Spring Boot**: Framework used to build the microservices.
- **gRPC**: Communication protocol used for inter-service communication.
- **MySQL + JPA**: Relational database with ORM.
- **Gradle**: Build tool used for managing dependencies and building the project.

### Clojure Service (ms-order-clojure)
- **Clojure 1.11**: Functional programming language on JVM
- **Ring**: HTTP server abstraction library
- **Compojure**: Routing library for Ring
- **MongoDB**: NoSQL document database
- **Cheshire**: JSON encoding/decoding
- **Clojure CLI Tools**: Dependency management and execution

## Learning Objectives

The main objectives of this repository are:

### Kotlin Services
- Understanding Kotlin syntax and features.
- Implementing gRPC communication between microservices.
- Exposing RESTful endpoints for CRUD operations.
- Applying business rules and logic within the services.
- Working with Spring Boot ecosystem.

### Clojure Service
- Learning functional programming paradigm.
- Understanding Clojure's immutable data structures.
- Building microservices without heavy frameworks.
- Working with MongoDB from Clojure.
- Comparing OOP (Kotlin) vs FP (Clojure) approaches.
- REPL-driven development workflow.

## Architecture Comparison

| Aspect | Kotlin (ms_order) | Clojure (ms-order-clojure) |
|--------|-------------------|----------------------------|
| **Paradigm** | OOP + Functional | Pure Functional |
| **Framework** | Spring Boot | Ring + Compojure |
| **Database** | MySQL + JPA | MongoDB Direct |
| **Type System** | Static | Dynamic |
| **Startup Time** | ~3-5 seconds | ~1-2 seconds |
| **Memory** | ~200MB | ~100MB |
| **Lines of Code** | ~500 | ~300 (effective) |
| **Dependencies** | 15+ | 6 |

## Getting Started

### Kotlin Services (ms_order, ms_product)

To get started with the Kotlin services:

```bash
# Clone repository
git clone <repository-url>
cd kotlin-grpc-api-microservices

# Build and run ms_product (gRPC server)
cd ms_product
./gradlew build
./gradlew bootRun

# In another terminal, build and run ms_order (gRPC client)
cd ms_order
docker-compose up -d  # Start MySQL
./gradlew build
./gradlew bootRun
```

### Clojure Service (ms-order-clojure)

To get started with the Clojure service:

```bash
# Prerequisites: Install Clojure CLI tools
# Linux: curl -O https://download.clojure.org/install/linux-install-1.11.1.1413.sh
# macOS: brew install clojure/tools/clojure

cd ms-order-clojure

# Start MongoDB
docker-compose up -d mongodb

# Run application
clojure -M:run

# Access API at http://localhost:8082
# Full tutorial available in TUTORIAL.md (Portuguese)
```

### Testing the Services

**Kotlin ms_order:**
```bash
# Create order (connects to ms_product via gRPC)
curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -d '{"products": {"1": 2, "2": 1}}'
```

**Clojure ms-order-clojure:**
```bash
# Health check
curl http://localhost:8082/health

# Create order (uses mock product data)
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"products": {"1": 2, "3": 1}}'
```
## Conclusion

This project serves as a comprehensive example of building microservices using different approaches:

1. **Kotlin + Spring Boot**: Traditional enterprise approach with full framework support, type safety, and ORM.
2. **Clojure + Ring**: Lightweight functional approach with immutability, composable libraries, and REPL-driven development.

Both approaches are valid and have their use cases. This repository allows you to:
- Compare OOP vs FP paradigms
- Understand trade-offs between frameworks and libraries
- Learn two different JVM languages
- See real-world microservice implementations
- Choose the best tool for your specific needs

### Key Takeaways

**Choose Kotlin + Spring Boot when:**
- Team is familiar with Java/Kotlin
- Type safety is critical
- Enterprise features are needed
- Large team collaboration

**Choose Clojure + Ring when:**
- Team values functional programming
- Fast startup and low memory are important
- Flexibility and simplicity are priorities
- REPL-driven development is desired

---

**🇧🇷 Documentação completa em Português disponível em [ms-order-clojure/](./ms-order-clojure/)**

**📚 Full Portuguese documentation available in [ms-order-clojure/](./ms-order-clojure/)**
