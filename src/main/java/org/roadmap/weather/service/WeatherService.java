package org.roadmap.weather.service;

import org.roadmap.weather.dto.LocationDto;
import org.roadmap.weather.dto.Weather;
import org.roadmap.weather.entity.Location;
import org.roadmap.weather.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@PropertySource("classpath:application.properties")
public class WeatherService {
    @Value("${weather.api.key}")
    private String apiKey;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final LocationRepository locationRepository;

    public WeatherService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public List<LocationDto> findByName(String locationName, Integer userId) {
        // сначала по имени чекаем в БД (чтобы уменьшить кол-во запросов?), если там нет, то делаем запрос
        // продумать later

        // не забыть про повторяющиеся имена локаций (повторные названия для городов)

        String url = String.format("https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s", locationName, apiKey);

        String json = restTemplate.getForObject(url, String.class);
        JsonNode root = jsonMapper.readTree(json);

        BigDecimal latitude = root.path("coord").path("lat").asDecimal();
        BigDecimal longitude = root.path("coord").path("lon").asDecimal();

        // save in DB
        Location location = new Location(
                locationName,
                userId,
                latitude,
                longitude
        );

        // для сокрытия id локации от юзера:
        // savedLocation = repository.save()
        locationRepository.save(location);

        // с помощью маппера из savedLocationEntity в дто
        LocationDto locationForUser = new LocationDto(
                locationName,
                userId,
                latitude,
                longitude
        );
        List<LocationDto> locations = new ArrayList<>();
        locations.add(locationForUser);
        return locations;
    }

    public List<Weather> getWeathersForUser(Integer userId) {
        List<Location> locations = locationRepository.getByUserId(userId);
        List<Weather> weathers = new ArrayList<>();

        for (Location location : locations) {
            String url = String.format(
                    "https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s",
                    location.getLongitude(),
                    location.getLatitude(),
                    apiKey
            );
            String json = restTemplate.getForObject(url, String.class);
            JsonNode root = jsonMapper.readTree(json);

            String name = location.getName();
            BigDecimal temp = root.path("main").path("temp").asDecimal();
            BigDecimal tempFeelsLike = root.path("main").path("feels_like").asDecimal();
            BigDecimal humidity = root.path("main").path("humidity").asDecimal();
            String clouds = root.path("weather").get(0).path("description").asString();
            weathers.add(
                    new Weather(
                            name,
                            temp,
                            tempFeelsLike,
                            humidity,
                            clouds
                    )
            );
        }
        return weathers;
    }
}
