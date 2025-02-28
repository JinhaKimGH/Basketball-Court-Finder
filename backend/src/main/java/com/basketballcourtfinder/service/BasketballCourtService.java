package com.basketballcourtfinder.service;

import com.basketballcourtfinder.entity.BasketballCourt;
import com.basketballcourtfinder.jsonmapping.OverpassResponse;
import com.basketballcourtfinder.repository.BasketballCourtRepository;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class BasketballCourtService {
    private final BasketballCourtRepository repository;

    private final RestTemplate restTemplate;

    public BasketballCourtService(BasketballCourtRepository repository, RestTemplateBuilder restTemplateBuilder) {
        this.repository = repository;
        this.restTemplate = restTemplateBuilder.build();
    }

    /*
    * Retrieves information of a basketball court by ID
    * */
    public BasketballCourt getCourt(long court_id) {
        BasketballCourt court = repository.findById(court_id).orElse(null);

        // If the court doesn't exist in our database, we must call the API
        if (court == null) {
            String query = String.format("[out:json];way(%s);out tags;", court_id);

            // Using RestClient to make the API call
            String apiUrl = String.format("https://overpass-api.de/api/interpreter?data=%s", query);

            OverpassResponse response = restTemplate.getForObject(apiUrl, OverpassResponse.class);

            if (!response.getElements().isEmpty()) {
                court = new BasketballCourt(response.getElements().get(0));
            }

            if (court != null) {
                court.setId(court_id);
                repository.save(court);
            }
        }

        return court;
    }

    /*
    * Retrieves all courts within a given area.
    * */
    public Set<BasketballCourt> getCourtsInArea(double latitude, double longitude, int range) {
        String apiUrl = getApiUrl(latitude, longitude, range);
        OverpassResponse response = restTemplate.getForObject(apiUrl, OverpassResponse.class);

        if (response.getElements().isEmpty()) {
            return Collections.emptySet();
        }

        Set<BasketballCourt> existingCourts = new HashSet<>(repository.findByIdIn(
                response.getElements().stream()
                        .map(OverpassResponse.Element::getId)
                        .toList()
        ));

        // Process each element and filter out missing coordinates
        List<BasketballCourt> newCourts = response.getElements().stream()
                .map(element -> existingCourts.stream()
                        .filter(court -> court.getId() == element.getId())
                        .findFirst()
                        .orElseGet(() -> {
                            BasketballCourt newCourt = new BasketballCourt(element);
                            return (newCourt.getLat() == 0 && newCourt.getLon() == 0) ? null : newCourt;
                        })
                )
                .filter(Objects::nonNull)
                .toList();

        // Save new courts in batch
        if (!newCourts.isEmpty()) {
            repository.saveAll(newCourts);
            existingCourts.addAll(newCourts);
        }

        return existingCourts;
    }

    private static String getApiUrl(double latitude, double longitude, int range) {
        String locationFilter = String.format("around:%d,%f,%f", range, latitude, longitude);

        // Api call to overpass api
        String query = String.format("[out:json];" +
                "(way(%s)[\"amenity\"=\"community_centre\"];" +
                "way(%s)[\"leisure\"=\"pitch\"][\"sport\"=\"basketball\"];" +
                "way(%s)[\"amenity\"=\"school\"][\"sport\"=\"basketball\"];" +
                ");out center; out tags;", locationFilter, locationFilter, locationFilter);

        // Using RestClient for the API call
        return String.format("https://overpass-api.de/api/interpreter?data=%s", query);
    }
}
