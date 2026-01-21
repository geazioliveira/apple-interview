package org.example.appleinterview.dtos;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoForecast(
        double latitude,
        double longitude,
        Current current,
        Daily daily
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Current(
            String time,
            Double temperature_2m
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Daily(
            List<String> time,
            List<Double> temperature_2m_max,
            List<Double> temperature_2m_min
    ) {}

}
