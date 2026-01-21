package org.example.appleinterview.dtos;

import java.util.ArrayList;
import java.util.List;

public record WeatherResponse(String zip, String country, double latitude, double longitude, boolean fromCache,
                              Double currentTemperatureC, Double todayMinC, Double todayMaxC,
                              List<DailyForecastItem> forecast) {
    public static WeatherResponse from(OpenMeteoForecast om, String zip, String country, Coordinates coords, boolean fromCache) {
        Double current = om.current() != null ? om.current().temperature_2m() : null;

        Double todayMin = null;
        Double todayMax = null;
        List<DailyForecastItem> items = new ArrayList<>();

        if (om.daily() != null && om.daily().time() != null) {
            List<String> dates = om.daily().time();
            List<Double> mins = om.daily().temperature_2m_min();
            List<Double> maxs = om.daily().temperature_2m_max();

            for (int i = 0; i < dates.size(); i++) {
                Double min = (mins != null && i < mins.size()) ? mins.get(i) : null;
                Double max = (maxs != null && i < maxs.size()) ? maxs.get(i) : null;

                if (i == 0) {
                    todayMin = min;
                    todayMax = max;
                }

                items.add(new DailyForecastItem(dates.get(i), min, max));
            }
        }

        return new WeatherResponse(zip, country, coords.latitude(), coords.longitude(), fromCache, current, todayMin, todayMax, items);
    }

    public WeatherResponse withFromCache(boolean fromCache) {
        return new WeatherResponse(zip, country, latitude, longitude, fromCache, currentTemperatureC, todayMinC, todayMaxC, forecast);
    }

    public record DailyForecastItem(String date, Double minC, Double maxC) {
    }

}
