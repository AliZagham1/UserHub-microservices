# HealthHub - User Lifecycle Management Platform
## Overview

HealthHub manages complete user lifecycles through distributed microservices running on AWS ECS. The system handles user registration, billing automation, and analytics processing with event-driven communication between services.
# Objectives
This project addresses common challenges in user management systems:

- Managing high user volumes without performance degradation
- Maintaining data consistency across multiple services
- Processing billing operations immediately after user creation
- Generating real-time insights from user activities
- Deploying services independently without system downtime

# Architecture
The platform consists of five core services deployed as Docker containers on AWS ECS:

## Authentication Service
Handles user login, JWT token management, and access control. Validates requests before forwarding to downstream services.

## API Gateway
Central routing service that distributes incoming requests to appropriate microservices. Implements rate limiting and request logging.
## Patient Service
Manages user profiles, registration workflows, and account updates. Publishes user creation events to Kafka topics.
## Billing Service
Subscribes to user creation events and automatically generates billing records. 
## Analytics Service
Consumes all Kafka events to build user activity reports and usage statistics. Stores processed data for dashboard visualization. This service is not fully completed and currently under process.
# Infrastructure Setup
The system runs within an AWS VPC using these managed services:

- ECS Cluster: Hosts containerized services with auto-scaling groups
- Application Load Balancer: Routes external traffic to healthy service instances
- RDS PostgreSQL: Stores user data and billing information in separate databases
- MSK Kafka: Handles event streaming between services
- Private Subnets: Isolates backend services from direct internet access

# Communication Flow
Services communicate through two patterns:
- Request-Response: API Gateway uses gRPC calls to fetch user data from Patient Service. Authentication Service validates tokens synchronously before processing requests.
- Event Publishing: When users register, Patient Service publishes creation events to Kafka. Billing Service subscribes to these events and creates billing records. Analytics Service consumes all events for reporting.
# Technical Implementation
- Backend Framework: Java Spring Boot with embedded Tomcat servers
- Message Broker: Apache Kafka with Spring Kafka integration
- API Communication: gRPC for internal calls, REST for client interfaces
- Data Storage: PostgreSQL with JPA/Hibernate ORM
- Containerization: Multi-stage Docker builds for optimized images
- Local Development: LocalStack simulates AWS services during testing
# Business Applications
The architecture supports various user management scenarios:
Healthcare systems can track patient registrations and automatically trigger insurance billing. E-commerce platforms can onboard customers and initialize payment processing. SaaS applications can manage subscriber lifecycles and usage analytics.
Each service handles specific business logic while maintaining loose coupling through event-driven communication. This separation allows teams to develop and deploy services independently.

Built to demonstrate production-ready microservices patterns with cloud-native infrastructure and event-driven workflows.