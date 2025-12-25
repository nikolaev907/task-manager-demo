CREATE TABLE IF NOT EXISTS tasks
(
    id          SERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    status      VARCHAR(50) DEFAULT 'PENDING',
    created_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO tasks (title, description, status, created_at)
VALUES ('Setup project structure', 'Initialize Spring Boot project with Maven', 'COMPLETED', now()),
       ('Configure PostgreSQL', 'Setup database connection and JPA entities', 'IN_PROGRESS', now()),
       ('Implement Kafka messaging', 'Create producer and consumer for notifications', 'PENDING', now()),
       ('Add WebSocket support', 'Implement real-time updates for frontend', 'PENDING', now()),
       ('Create Docker configuration', 'Setup docker-compose for all services', 'PENDING', now());

