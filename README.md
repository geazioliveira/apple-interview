# Apple Interview

Author: Geazi El-Hanã de Oliveira

## Description

This project is a Take Home Exercise for my application at Apple. 
This is a RESTful weather API that aggregates geocoding and weather data 
from multiple providers, with cache to minimize external API calls and 
optimize response times. 

**Key Features:**
- ZIP code to weather data conversion
- 15 minute in-memory caching (Caffeine)
- Multi-day weather forecast
- Geocoding
- Weather data from Open-Meteo

---

## How to build & Run

### 1. Clone the repository
```bash
git clone https://github.com/geazioliveira/apple-interview.git
cd apple-interview
```

### 2. Build the Project
```bash
./gradlew clean build
```

### 3. Run the App
```bash
./gradlew bootRun
```

This will start the application on localhost on port 8080.


## API How to use

### Endpoint:
```http
GET /api/weather?zip={zip}&country={country}
```

### Parameters:
- zip: ZIP code to be used for geocoding and weather lookup.
- country: Country code to be used for geocoding and weather lookup.

### Example Request
```bash
curl http://localhost:8080/api/weather?zip=32801&country=US
```

### Example Response
```json
{
  "zip": "32801",
  "country": "US",
  "latitude": 28.5432055,
  "longitude": -81.3766922,
  "fromCache": true,
  "currentTemperatureC": 8.3,
  "todayMinC": 8.5,
  "todayMaxC": 22.2,
  "forecast": [
    {
      "date": "2026-01-21",
      "minC": 8.5,
      "maxC": 22.2
    },
    {
      "date": "2026-01-22",
      "minC": 11.6,
      "maxC": 24.3
    },
    {
      "date": "2026-01-23",
      "minC": 15.9,
      "maxC": 27.1
    },
    {
      "date": "2026-01-24",
      "minC": 17.2,
      "maxC": 26.9
    },
    {
      "date": "2026-01-25",
      "minC": 18.8,
      "maxC": 29.4
    },
    {
      "date": "2026-01-26",
      "minC": 13.3,
      "maxC": 21.2
    },
    {
      "date": "2026-01-27",
      "minC": 5.6,
      "maxC": 14.9
    }
  ]
}
```

1. Clone the repository.
2. Install the required dependencies.
3. Run the application.

## Approach & Rationale

### High-Level Approach:

1. Client calls `GET /api/weather?zip={zip}&country={country}`
2. Server checks cache using key {country}:{zip}
3. If cache HIT:
    - Return cached response with `fromCache`=true and ``X-Cache: HIT` header
4. If cache MISS:
    - Call geocoding provider (Nominatim) with ZIP and country to get coordinates.
    - Call weather provider (OpenWeatherMap) with coordinates to get weather data (current temperature + daily forecast).
    - Build a normalized response payload.
    - Store the result in cache for 15 minutes.
    - Return response with `fromCache=false` and `X-Cache: MISS`.

This design will ensures the deterministic, fast on repeated request on my endpoint, and polite to upstream services.

### Geocoding Nominatim (OpenStreetMap):

For my geocoding, I chose Nominatim. It's free and easy to use, and it provides accurate results for most locations, 
since is based on OpenStreetMap data.
They have a great documentation, and works globally.

### Weather: Open-Meteo

I chose OpenWeatherMap for my weather provider, cause **no API Key is required**, provides current conditions and multi-day forecast
in a single request. Simple API surface.

## Library / Framework choices

### Spring Boot + WebClient (spring-boot-start-webflux)

I chose Spring Boot with webclient for my application, cause:
    - WebClient is the modern HTTP client recommended by Spring for non-blocking I/O.
    - It's easy to use and provides a fluent API.

### Caffeine Cache (in-memory)

Is a lightweight, fast, production-proven local cache. TTL-based expiration is supported.
Best for single-instance or small deployment where cache consistency across nodes is not required.

### Validation and Error handling

I use `@Validated` + `@RequestParam` validation to fail on invalid input.
Also I use the `@RestControllerAdvice` to produce a consistent error responses and avoid leaking stack traces.

## Code Organization & Best Practices

I structure my code following the Clean Code principles, avoiding hardcoded values, using typed DTOs, handle nullability defensively,
and keep my logic cohesive; My controller is thin, my service has orchestration and DTOs do mapping.

I structure my packages:
- configs: deals with application properties
- controllers: deals only with HTTP concerns (parameters, status and headers).
- dtos: maps responses into typed records and maps my final response.
- exceptions: handles application errors and provides consistent error responses.
- services: owns business logic and orchestrates the application flow: geocoding call, weather call and mapping response.

## Challenges / Complexities Encountered

I encountered challenges with error handling and validation, as I needed to ensure consistent error responses across 
the application. Additionally, I had a problem with the WebClient what I had to change how I was using it. I had to implement 
the use of `Mono` instead of `Flux` to avoid blocking the thread.
I had another issue that is not related to the application, but with the geocoding.
When I've made a request using my own zipcode (09260590) and the my country (BR) the Nomination didn't handle with that and returned a blank array.

## Enhancements / Future Work

If I had more time I would like to:
- Add tests.
- Add timeouts and retries with backoff.
- Introduce a circuit breaker.
- Add better fallback message for partial outages.
- Improve the caching strategy.
- Add a Rate Limiting
- Add Observability