# Enrollment System Architecture Documentation

## Table of Contents
1. [Network Setup and Containerization](#1-network-setup-and-containerization)
2. [JWT Implementation](#2-jwt-implementation)
3. [Multi-tier Architecture](#3-multi-tier-architecture)
4. [Error Handling and Service Communication](#4-error-handling-and-service-communication)
5. [Additional Features](#5-additional-features)

## 1. Network Setup and Containerization

### Docker Compose Architecture
The system consists of the following services:

| Service | Port | Description |
|---------|------|-------------|
| postgres | 5432 | Database service |
| main-service | 8080 | Frontend and orchestration |
| auth-service | 8081 | Authentication service |
| course-service | 8082 | Course management |
| enrollment-service | 8083 | Enrollment management |
| grades-service | 8084 | Grade management |
| faculty-service | 8085 | Faculty management |

### Network Configuration
- All services are connected through Docker's internal network
- Shared PostgreSQL instance with separate tables per service
- Services communicate via HTTP/REST APIs
- Port mappings allow external access to services

## 2. JWT Implementation

### Authentication Flow
1. **User Authentication**:
   - Users submit credentials to main-service
   - Main-service forwards to auth-service
   - Auth-service validates credentials and generates JWT

2. **JWT Structure**:
   - Contains user ID, role, and name
   - Signed with HS256 algorithm
   - Stored in HTTP-only cookie
   - 24-hour expiration

3. **Session Management**:
   - JWT stored in HTTP-only cookie
   - Cookie path set to "/"
   - Main-service validates token for each request
   - Token passed to other services via Authorization header

### Security Implementation
- **Auth Service**:
  - JWT generation and validation
  - Password hashing with BCrypt
  - Role-based access control
  - Token expiration handling

- **Main Service**:
  - JWT validation filter
  - Cookie management
  - Service-to-service authentication
  - CORS configuration

## 3. Multi-tier Architecture

### Model-View-Controller Implementation
- **Model Layer**:
  - Entity classes in each service
  - DTOs for data transfer
  - Repository interfaces for data access

- **View Layer**:
  - Thymeleaf templates
  - Role-based view rendering
  - Responsive design

- **Controller Layer**:
  - REST endpoints
  - Service orchestration
  - Error handling
  - Authentication checks

### Service Communication
- **Inter-Service Communication**:
  - RestTemplate for HTTP calls
  - Error handling with try-catch blocks
  - Default values for service failures
  - JWT token propagation

## 4. Error Handling and Service Communication

### Error Handling
- **Global Exception Handling**:
  - `GlobalExceptionHandler` for centralized error management
  - Custom exceptions for different error types
  - User-friendly error messages
  - Error logging for debugging

- **Service Communication Error Handling**:
  - Try-catch blocks around service calls
  - Default values when services are unavailable
  - Error logging for debugging
  - HTTP status code handling

### Service Independence
- **Database Management**:
  - Shared PostgreSQL instance
  - Separate tables per service
  - Foreign key relationships maintained
  - Data consistency through transactions

## 5. Additional Features

### Role-Based Access Control
- Three user types: STUDENT, FACULTY, ADMIN
- Different views and permissions per role
- Dashboard customization by role

### Data Management
- Transaction management
- Data validation
- Error handling
- Basic logging

### User Experience
- Responsive design
- Error feedback
- Session management
- Role-specific dashboards

## Technical Details

### Database Schema
- Shared PostgreSQL instance
- Separate tables per service
- Foreign key relationships maintained
- Data consistency through transactions

### API Documentation
- RESTful endpoints
- JSON request/response format
- Standard HTTP status codes
- Error response format

### Security Measures
- JWT token validation
- Role-based access control
- Password hashing
- CORS configuration

### Error Handling and Logging
- Global exception handling
- Service communication error handling
- Basic error logging
- User-friendly error messages

## Development Guidelines

### Code Organization
- Clear package structure
- Consistent naming conventions
- Documentation requirements
- Error handling standards

### Deployment Process
- Docker containerization
- Environment configuration
- Database initialization
- Service orchestration