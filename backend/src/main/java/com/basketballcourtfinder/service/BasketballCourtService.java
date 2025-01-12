package com.basketballcourtfinder.service;

import com.basketballcourtfinder.entity.BasketballCourt;
import com.basketballcourtfinder.jsonmapping.OverpassResponse;
import com.basketballcourtfinder.repository.BasketballCourtRepository;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BasketballCourtService {
    private final BasketballCourtRepository repository;

    private final RestTemplate restTemplate;

    // Cache for addresses to reduce API calls
    private final Map<String, Map<String, String>> addressCache = new HashMap<>();

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

                if (court.getAddress().isIncomplete()) {
                    court.setAddress(getAddressDetails(court.getLat(), court.getLon()));
                }
            }
        }

        return court;
    }

    /*
    * Retrieves address details from the nominatim API
    * */
    Map<String, String> getAddressDetails(double lat, double lon) {
        // Checks cache before calling API
        String key = lat + "," + lon;
        if (addressCache.containsKey(key)) {
            return addressCache.get(key);
        }

        String uri = String.format("https://nominatim.openstreetmap.org/reverse?lat=%f&lon=%f&format=json&addressdetails=1", lat, lon);

        Map<String, Object> response = restTemplate.getForObject(uri, Map.class);

        Map<String, String> address = response.containsKey("address")
                ? (Map<String, String>) response.get("address")
                : Collections.emptyMap();

        // Puts address details into API
        addressCache.put(key, address);
        return address;
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

        // Get list of elements
        List<OverpassResponse.Element> elements = response.getElements();
        Set<BasketballCourt> courts = Collections.synchronizedSet(new HashSet<>());

        // Create a list of IDs from the elements
        List<Long> courtIds = elements.stream().map(OverpassResponse.Element::getId).collect(Collectors.toList());

        // Fetch the existing courts from the repository
        Map<Long, BasketballCourt> existingCourtMap = repository.findByIdIn(courtIds).stream()
                .collect(Collectors.toMap(BasketballCourt::getId, court -> court));

        // For each element, retrieve the BasketballCourt object, complete address and add to final hash set
        elements.parallelStream().forEach(element -> {
            // Get from DB or create object from API
            BasketballCourt court = existingCourtMap.getOrDefault(element.getId(), new BasketballCourt(element));

            // If no lat/lon given, incomplete data, skip court
            if (!(court.getLat() == 0 && court.getLon() == 0)) {
                // If address is incomplete, get address details from Nominatim API
                if (court.getAddress().isIncomplete()) {
                    court.setAddress(getAddressDetails(court.getLat(), court.getLon()));
                }

                courts.add(court);
            }
        });

        return courts;
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
        String apiUrl = String.format("https://overpass-api.de/api/interpreter?data=%s", query);
        return apiUrl;
    }
}
