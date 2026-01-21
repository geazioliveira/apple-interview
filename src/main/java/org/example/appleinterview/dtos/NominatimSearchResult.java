package org.example.appleinterview.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NominatimSearchResult(
        String lat,
        String lon,
        String display_name
) {}
