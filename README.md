# Pipi's Wishes Server 💫

Backend for a small application where wishes can be created, tracked and eventually fulfilled.

One person leaves a wish — the other makes it happen. That's pretty much it :)

The project is currently backend-only. A separate frontend is planned.

## Features

The application provides a simple REST API for managing wishes:

- Create a new wish
- View all wishes
- View a wish by ID
- Update a wish
- Delete a wish
- Mark a wish as executed
- View pending wishes
- View executed wishes

## Tech Stack

- Java 21
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- Hibernate
- PostgreSQL
- Maven
- Docker

## API

Base path:

```text
/wish
```

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/wish` | Get all wishes |
| `GET` | `/wish/{id}` | Get a wish by ID |
| `GET` | `/wish/pending` | Get all pending wishes |
| `GET` | `/wish/executed` | Get all executed wishes |
| `POST` | `/wish` | Create a new wish |
| `PUT` | `/wish/{id}` | Update an existing wish |
| `DELETE` | `/wish/{id}` | Delete a wish |
| `POST` | `/wish/{id}/execute` | Mark a wish as executed |

## Configuration

The application uses PostgreSQL.

Database connection settings can be provided through environment variables:

```properties
DB_URL=jdbc:postgresql://localhost:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=your_password
```

Do not store real passwords or other secrets in the repository.

## Running locally

Make sure PostgreSQL is running and the database connection is configured.

Build the project:

```bash
mvn clean package
```

Run it:

```bash
java -jar target/pipiswishes-0.0.1-SNAPSHOT.jar
```

By default, the server is available at:

```text
http://localhost:8080
```

## Running with Docker

Build the application first:

```bash
mvn clean package
```

Build the Docker image:

```bash
docker build -t pipiswishes .
```

The application and PostgreSQL should be connected to the same Docker network.

Example application container:

```bash
docker run \
  --name pipiswishes-app \
  --network pipiswishes-network \
  -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://pipiswishes-db:5432/postgres \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=your_password \
  pipiswishes
```

Docker Compose support is planned to make running the whole application easier.

## Frontend

Currently, this repository contains only the backend.

A separate frontend application is planned.

## Status

🚧 **Work in progress**

This is a small pet project built for fun and practice.

More features may appear whenever Pipi has more wishes. ✨