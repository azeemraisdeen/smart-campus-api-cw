# Smart Campus Sensor & Room Management API - Coursework

**Name:** Azeem Raisdeen

**UoW ID:** 21202472

**IIT ID:** 20231169

**Module:** 5COSC022C.2 - Client-Server Architectures  

A RESTful API built with JAX-RS (Jersey 2) and Grizzly embedded server for managing campus rooms and IoT sensors.

---

## Tech Stack

- Java 11
- JAX-RS 2.1 (Jersey 2.39.1)
- Grizzly2 HTTP Server (embedded)
- Jackson for JSON
- Maven

No database is used. All data is stored in `ConcurrentHashMap` in memory.

---

## Project Structure

```
smart-campus-api/
├── pom.xml
└── src/main/java/com/smartcampus/
    ├── application/
    │   ├── Main.java
    │   └── SmartCampusApplication.java
    ├── model/
    │   ├── Room.java
    │   ├── Sensor.java
    │   └── SensorReading.java
    ├── storage/
    │   └── DataStore.java
    ├── service/
    │   ├── RoomService.java
    │   ├── SensorService.java
    │   └── SensorReadingService.java
    ├── resource/
    │   ├── DiscoveryResource.java
    │   ├── RoomResource.java
    │   ├── SensorResource.java
    │   └── SensorReadingResource.java
    ├── exception/
    │   ├── ErrorResponse.java
    │   ├── RoomNotFoundException.java + RoomNotFoundExceptionMapper.java
    │   ├── RoomNotEmptyException.java + RoomNotEmptyExceptionMapper.java
    │   ├── LinkedResourceNotFoundException.java + LinkedResourceNotFoundExceptionMapper.java
    │   ├── SensorUnavailableException.java + SensorUnavailableExceptionMapper.java
    │   └── GlobalExceptionMapper.java
    └── filter/
        └── LoggingFilter.java
```

---

## How to Build and Run

### Requirements
- Java 11+
- Maven 3.6+

### Steps

```bash
git clone https://github.com/azeemraisdeen/smart-campus-api-cw.git
cd smart-campus-api
mvn clean package
java -jar target/smart-campus-api-1.0.0.jar
```

Server starts at `http://localhost:8080`. Press ENTER to stop.

The API is pre-loaded with sample data (rooms: LIB-301, CS-101, HALL-1 and sensors: TEMP-001, CO2-001, OCC-001, TEMP-002) so you can test straight away.

---

## API Endpoints

Base URL: `http://localhost:8080/api/v1`

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| GET | `/api/v1` | Discovery / metadata | 200 |
| GET | `/api/v1/rooms` | List all rooms | 200 |
| POST | `/api/v1/rooms` | Create a room | 201 |
| GET | `/api/v1/rooms/{roomId}` | Get room by ID | 200 |
| DELETE | `/api/v1/rooms/{roomId}` | Delete room | 204 |
| GET | `/api/v1/sensors` | List all sensors | 200 |
| GET | `/api/v1/sensors?type=CO2` | Filter by type | 200 |
| POST | `/api/v1/sensors` | Register a sensor | 201 |
| GET | `/api/v1/sensors/{sensorId}/readings` | Get readings | 200 |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add reading | 201 |

### Error responses

| Code | When |
|------|------|
| 404 | Room or sensor not found |
| 409 | Deleting a room that still has sensors |
| 422 | Creating a sensor with a roomId that doesn't exist |
| 403 | Posting a reading to a MAINTENANCE or OFFLINE sensor |
| 500 | Any unexpected server error |

---

## curl Examples

```bash
# 1. Discovery endpoint
curl http://localhost:8080/api/v1

# 2. List all rooms
curl http://localhost:8080/api/v1/rooms

# 3. Create a new room
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"ENG-101","name":"Engineering Lab","capacity":25}'

# 4. Get a specific room
curl http://localhost:8080/api/v1/rooms/LIB-301

# 5. Try to delete a room that has sensors (expect 409)
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301

# 6. Delete a room with no sensors (expect 204)
curl -X DELETE http://localhost:8080/api/v1/rooms/HALL-1

# 7. Register a sensor
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-999","type":"CO2","status":"ACTIVE","currentValue":0.0,"roomId":"CS-101"}'
```

---

## Report - Answers

### Question 1.1 - In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

By default JAX-RS creates a new resource instance per request, meaning instance variables are reset every time. Because of this I couldn't store my room/sensor data directly in the resource class fields.

My solution was a static singleton `DataStore` class using `ConcurrentHashMap`. Since it's static, every resource instance accesses the same maps even though JAX-RS keeps creating new resource objects. `ConcurrentHashMap` is thread-safe for individual operations which is enough here - two requests reading at the same time won't interfere with each other.

### Question 1.2 - Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

HATEOAS is about including links in API responses so clients can navigate without needing outside documentation. My `GET /api/v1` endpoint returns a list of the main resource URLs so a client can discover everything from that one entry point.

The advantage over static docs is that the links are always live and accurate. If I change a URL, clients following the links get the update automatically instead of breaking. It also makes it easier to explore the API from scratch.

### Question 2.1 - When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.

Returning only IDs keeps responses small which is good when you have lots of rooms. The problem is the client then needs a separate request for each room's details which gets slow.

Returning full objects avoids those extra calls but the payload gets big quickly. For this project I return full objects since the Room model is small, but in a real system you'd probably want pagination or let the client choose which fields to include.

### Question 2.2 - Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

Yes, DELETE is idempotent. The spec says calling it multiple times should have the same outcome as calling it once.

The first DELETE removes the room and returns 204. If you call it again, the room is already gone so it returns 404. The status code changes but the actual result is the same - the room doesn't exist either way. That's still idempotent because the server state ends up in the same place regardless of how many times you send the request.

### Question 3.1 - We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

`@Consumes(APPLICATION_JSON)` means the method only accepts requests with `Content-Type: application/json`. If a client sends `text/plain` or `application/xml` instead, JAX-RS returns 415 Unsupported Media Type before the method body even runs.

This is useful because it rejects the wrong format automatically at the framework level instead of letting bad data reach the business logic and cause confusing errors.

### Question 3.2 - You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

Using `/sensors?type=CO2` makes more sense because the resource is still `/sensors` - you're just filtering the results. If you put it in the path like `/sensors/type/CO2` it implies CO2 is its own resource which isn't really right.

Query params are also optional so `/sensors` still works with no filter. And if you need multiple filters like type and status you can just add them to the query string, whereas doing that in the path gets messy.

### Question 4.1 - Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?

The sub-resource locator in `SensorResource` returns a `SensorReadingResource` instance instead of handling readings itself. This keeps each class responsible for one thing - sensors vs reading history.

If I'd put all the reading endpoints in `SensorResource` it would get large very quickly, especially if more sub-resources got added later. Having them in separate classes is easier to read and each class can be tested on its own.

### Question 5.2 - Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

When a client tries to create a sensor with a `roomId` that doesn't exist, the endpoint `/api/v1/sensors` itself is valid and responding fine. Returning 404 would make it look like the endpoint is missing which is wrong.

422 is more accurate because it tells the client the request body was understood but the data inside it is invalid - the room it referenced doesn't exist. It's a better signal than 404 because it points to the payload being the problem rather than the URL.

### Question 5.4 - From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

If a stack trace gets returned to a client it leaks things like exact library versions and internal class names. An attacker can look up those library versions in CVE databases to find known vulnerabilities. The class names and package structure also help someone map out how the code works internally.

My `GlobalExceptionMapper` stops this by catching all unhandled exceptions, logging the full details to the server log where only I can see them, and just sending back a generic error message to the client.

### Question 5.5 - Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single re-source method?

Using a filter means logging happens in one place automatically for every request. If I put `Logger.info()` calls in every resource method I'd have to update each one whenever the log format changes, and I'd inevitably miss some paths like error responses.

The filter runs on every request regardless of what happens in between, including requests that hit an exception mapper. It keeps the resource methods clean and makes sure nothing gets missed.

---
