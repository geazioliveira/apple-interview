package org.example.appleinterview.controllers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.example.appleinterview.dtos.WeatherResponse;
import org.example.appleinterview.services.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/weather")
@Validated
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }


    @GetMapping
    public Mono<ResponseEntity<WeatherResponse>> getWeather(
            @RequestParam @NotBlank String zip,
            @RequestParam @NotBlank @Pattern(regexp = "^[A-Za-z]{2}$", message = "country must be ISO-3166 alpha-2 (e.g. BR, US)") String country
    ){
        return weatherService.getWeather(zip.trim(), country.trim().toUpperCase())
                .map(result -> {
                    String cacheHeaderValue = result.fromCache() ? "HIT" : "MISS";

                    return ResponseEntity.ok()
                            .header("X-Cache", cacheHeaderValue)
                            .body(result.response());
                });


    }

}
