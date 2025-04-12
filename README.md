# Problem Set 4 for STDISCM

**Link to Github Repository:** [Github](https://github.com/Waynes-Wu/discm-p4)

## Prerequisites
- Docker and Docker Compose
- At least 4GB of RAM available
- Ports 8080-8085 available on your system

## Instructions to Run
1. **Build and Start the Services**
   ```docker-compose up -d --build
   ```
   This will:
   - Build all service containers
   - Start the PostgreSQL database
   - Start all microservices
   - Set up the network between services

2. **Verify Services are Running**
   ```docker-compose ps
   ```
   All services should show as "Up" in the status column.

3. **Access the Application**
   - Open your web browser and navigate to: `http://localhost:8080`
   - The main service will be available on port 8080
   - Other services will be accessible through the main service

4. **Stopping the Services**
   ```docker-compose down
   ```

### Docker Compose Architecture
| Service | Port | Description |
|---------|------|-------------|
| postgres | 5432 | Database service |
| main-service | 8080 | Frontend and orchestration |
| auth-service | 8081 | Authentication service |
| course-service | 8082 | Course management |
| enrollment-service | 8083 | Enrollment management |
| grades-service | 8084 | Grade management |
| faculty-service | 8085 | Faculty management |
