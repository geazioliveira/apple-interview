package org.example.appleinterview.services;

import com.github.benmanes.caffeine.cache.Cache;
import org.example.appleinterview.dtos.Coordinates;
import org.example.appleinterview.dtos.NominatimSearchResult;
import org.example.appleinterview.dtos.OpenMeteoForecast;
import org.example.appleinterview.dtos.WeatherResponse;
import org.example.appleinterview.exceptions.UpstreamException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class WeatherService {

    private final WebClient webClient;
    private final Cache<String, WeatherResponse> cache;
    private final int forecastDays;
    public WeatherService(
            WebClient webClient,
            Cache<String, WeatherResponse> cache,
            @Value("${app.weather.forecast-days}") int forecastDays
    ) {
        this.webClient = webClient;
        this.cache = cache;
        this.forecastDays = forecastDays;
    }

    private static double parseDoubleOrThrow(String value, String field) {
        try {
            return Double.parseDouble(Objects.requireNonNull(value));
        } catch (Exception e) {
            throw new UpstreamException("Invalid '%s' value from geocoding provider: %s".formatted(field, value));
        }
    }

    public Mono<Result> getWeather(String zip, String country) {
        String cacheKey = country + ":" + zip;

        WeatherResponse cached = cache.getIfPresent(cacheKey);

        if (cached != null) {
            WeatherResponse fromCacheResp = cached.withFromCache(true);
            return Mono.just(new Result(fromCacheResp, true));
        }

        return resolveCoordinates(zip, country)
                .flatMap(coords -> fetchForecast(coords)
                        .map(forecast -> {
                            WeatherResponse response = WeatherResponse.from(forecast, zip, country, coords, true);
                            cache.put(cacheKey, response);
                            return new Result(response, false);
                        }));
    }

    private Mono<Coordinates> resolveCoordinates(String zip, String country) {
        String url = UriComponentsBuilder
                .fromUriString("https://nominatim.openstreetmap.org/search")
                .queryParam("format", "jsonv2")
                .queryParam("limit", 1)
                .queryParam("addressdetails", 0)
                .queryParam("postalcode", zip)
                .queryParam("countrycodes", country.toLowerCase())
                .build().toUriString();

        return webClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(NominatimSearchResult.class)
                .collectList()
                .map(results -> {
                    if (results == null || results.isEmpty()) {
                        throw new IllegalArgumentException("Could not resolve coordinates for zip '%s' in country '%s'".formatted(zip, country));
                    }
                    NominatimSearchResult first = results.getFirst();

                    if (first.lat() == null || first.lon() == null) {
                        throw new IllegalArgumentException("Geocoding returned no lat/lon for zip '%s' in country '%s'".formatted(zip, country));
                    }

                    return new Coordinates(
                            parseDoubleOrThrow(first.lat(), "latitude"),
                            parseDoubleOrThrow(first.lon(), "longitude")
                    );
                });
    }

    private Mono<OpenMeteoForecast> fetchForecast(Coordinates coords) {
        String url = UriComponentsBuilder
                .fromUriString("https://api.open-meteo.com/v1/forecast")
                .queryParam("latitude", coords.latitude())
                .queryParam("longitude", coords.longitude())
                .queryParam("current", "temperature_2m")
                .queryParam("daily", "temperature_2m_max,temperature_2m_min")
                .queryParam("forecast_days", forecastDays)
                .queryParam("timezone", "auto")
                .build().toUriString();

        return webClient
                .get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(OpenMeteoForecast.class)
                .map(forecast -> {
                    if (forecast == null || forecast.current() == null) {
                        throw new UpstreamException("Could not fetch weather forecast for coordinates %s".formatted(coords));
                    }

                    return forecast;
                });
    }

    public record Result(WeatherResponse response, boolean fromCache) {}
}
