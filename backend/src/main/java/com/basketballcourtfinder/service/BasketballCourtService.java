package com.basketballcourtfinder.service;

import com.basketballcourtfinder.entity.BasketballCourt;
import com.basketballcourtfinder.jsonmapping.OverpassResponse;
import com.basketballcourtfinder.repository.BasketballCourtRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BasketballCourtService {
    private final BasketballCourtRepository repository;

    // Cache for addresses to reduce API calls
    private final Map<String, Map<String, String>> addressCache = new HashMap<>();

    public BasketballCourtService(BasketballCourtRepository repository) {
        this.repository = repository;
    }

    /*
    * Retrieves information of a basketball court by ID
    * */
    public BasketballCourt getCourt(long court_id) {
        BasketballCourt court = repository.findById(court_id).orElse(null);

        // If the court doesn't exist in our database, we must call the API
        if (court == null) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("referer", "https://jinhakimgh.github.io/Basketball-Court-Finder");
            headers.set("User-Agent", "Basketball Court Finder");

            HttpEntity<?> entity = new HttpEntity<>(headers);

            UriComponents uriComponents = UriComponentsBuilder.newInstance()
                    .scheme("https")
                    .host("overpass-api.de")
                    .path("/api/interpreter")
                    .queryParam("data", String.format("[out:json];way(%s);out tags;", court_id))
                    .build()
                    .encode();

            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<OverpassResponse> response = restTemplate.exchange(
                    uriComponents.toUri(),
                    HttpMethod.GET,
                    entity,
                    OverpassResponse.class
            );

            // Retrieve the court from API response, should be first element in list
            court = new BasketballCourt(response.getBody().getElements().get(0));

            // Obtain address details from API if incomplete
            if (court.getAddress().isIncomplete()) {
                court.setAddress(getAddressDetails(court.getLat(), court.getLon()));
            }

        }

        return court;
    }

    /*
    * Retrieves address details from the nominatim API
    * */
    private Map<String, String> getAddressDetails(double lat, double lon) {
        // Checks cache before calling API
        String key = lat + "," + lon;
        if (addressCache.containsKey(key)) {
            return addressCache.get(key);
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = String.format("https://nominatim.openstreetmap.org/reverse?lat=%f&lon=%f&format=json&addressdetails=1", lat, lon);

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        Map<String, Object> responseBody = response.getBody();
        Map<String, String> address = responseBody != null ? (Map<String, String>) responseBody.get("address") : Collections.emptyMap();

        // Puts address details into API
        addressCache.put(key, address);
        return Collections.emptyMap();
    }

    /*
    * Retrieves all courts within a given area.
    * */
    public Set<BasketballCourt> getCourtsInArea(double latitude, double longitude, int range) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("referer", "https://jinhakimgh.github.io/Basketball-Court-Finder");
        headers.set("User-Agent", "Basketball Court Finder");

        HttpEntity<?> entity = new HttpEntity<>(headers);

        String locationFilter = String.format("around:%d,%f,%f", range, latitude, longitude);

        // Api call to overpass api
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("overpass-api.de")
                .path("/api/interpreter")
                .queryParam("data", String.format("[out:json];(way(%s)[\"amenity\"=\"community_centre\"];" +
                        "way(%s)[\"leisure\"=\"pitch\"][\"sport\"=\"basketball\"];" +
                        "way(%s)[\"amenity\"=\"school\"][\"sport\"=\"basketball\"];" +
                        ");out center; out tags;", locationFilter, locationFilter, locationFilter))
                .build()
                .encode();


        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<OverpassResponse> response = restTemplate.exchange(
                uriComponents.toUri(),
                HttpMethod.GET,
                entity,
                OverpassResponse.class
        );

        OverpassResponse responseBody = response.getBody();

        // Get list of elements
        List<OverpassResponse.Element> elements = responseBody != null ? responseBody.getElements() :
                Collections.emptyList();
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
}
