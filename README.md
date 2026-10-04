# Pipi's Wishes Server 💫

Backend for a small application where wishes can be created, tracked and eventually fulfilled.

One person leaves a wish — the other makes it happen. That's pretty much it :)

The Russian-language frontend is served by Spring Boot at `/`, alongside the REST API.

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
| `GET` | `/wish?status=PENDING&pageSize=5&pageNum=0` | Get a page of pending wishes |
| `GET` | `/wish?status=EXECUTED&pageSize=5&pageNum=0` | Get a page of executed wishes |
| `POST` | `/wish` | Create a new wish |
| `PUT` | `/wish/{id}` | Update an existing wish |
| `DELETE` | `/wish/{id}` | Delete a wish |
| `POST` | `/wish/{id}/execute` | Mark a wish as executed |

## Configuration

`GET /wish` accepts optional `status` (`PENDING` or `EXECUTED`), `pageSize` (default 5), and `pageNum` (zero-based, default 0). Omit `status` to include all wishes. Responses are arrays without a total count. The frontend displays five wishes per page and checks the following page to enable navigation. Changing the filter resets navigation to the first page.

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

Alternatively, configure `DB_PASSWORD` (or the existing `POSTGRES_PASSWORD`) in a local `.env` file, then run `docker compose up --build`. The current Dockerfile builds the JAR itself; Compose starts both the application and PostgreSQL 18. The volume remains mounted at `/var/lib/postgresql`, matching the existing PostgreSQL 18 data layout.

## Frontend

Open http://localhost:8080/ to create, edit, delete and fulfill wishes, or filter them by status. The responsive interface uses HTML, CSS and vanilla JavaScript with no frontend build step or external assets.

Files in `src/main/resources/static/` are automatically packaged into the application JAR and Docker image:

- `index.html` — page and accessible form dialogs
- `css/style.css` — responsive styling and reduced-motion support
- `js/app.js` — rendering, notifications and same-origin API requests

The frontend sends `{ "title": "...", "description": "..." }` for POST and PUT. Responses contain `id`, `title`, `description`, and `status` (`PENDING` or `EXECUTED`); list endpoints return arrays. Creation must omit `id` and `status`. The server sets the status and only allows editing or fulfilling pending wishes. Executed wishes can still be deleted; their edit button is disabled with an explanation.

The UI requires a nonblank title and limits title and description to 255 characters to match the entity's default string column lengths. The backend currently has no required-field validation. API URLs are relative (`/wish`); no deployment-specific hostname is needed.

## Status

🚧 **Work in progress**

This is a small pet project built for fun and practice.

More features may appear whenever Pipi has more wishes. ✨
