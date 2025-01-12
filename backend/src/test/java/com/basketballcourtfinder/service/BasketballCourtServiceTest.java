package com.basketballcourtfinder.service;

import com.basketballcourtfinder.entity.BasketballCourt;
import com.basketballcourtfinder.repository.BasketballCourtRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RestClientTest(BasketballCourtService.class)
@AutoConfigureDataJpa
public class BasketballCourtServiceTest {
    @Autowired
    private BasketballCourtService service;

    @MockitoBean
    private BasketballCourtRepository repository;

    @MockitoBean
    private Map<String, Map<String, String>> addressCache;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void test_getCourtInDatabase() throws Exception {
        long mock_courtId = 1L;
        BasketballCourt mockCourt = new BasketballCourt(mock_courtId, "Test Court");

        // Mocking repository
        when(repository.findById(mock_courtId)).thenReturn(Optional.of(mockCourt));

        // When
        BasketballCourt court = service.getCourt(mock_courtId);

        // Then
        assertNotNull(court);
        assertEquals("Test Court", court.getName());
    }

    @Test
    public void test_getCourtNotInDatabase() throws Exception {
        long mock_courtId = 1L;

        // Mocking repository to return Optional.empty for the court
        when(repository.findById(mock_courtId)).thenReturn(Optional.empty());

        // Prepare mock response for the RestClient when court is not found
        String mockOverpassResponseJson = "{ \"elements\": [ { \"id\": " + mock_courtId +
                ", \"tags\": { \"name\": \"Test Court\", \"addr:housenumber\": \"Test Street\", \"addr:city\":  " +
                "\"Test City\", \"addr:street\": \"Test Street\", \"addr:postcode\": \"12345\"}, \"lat\": 52.0, " +
                "\"lon\": 13.0 } ] }";

        // Mocking the response from RestClient API
        this.server.expect(requestTo("https://overpass-api.de/api/interpreter?data=%5Bout:json%5D;way(" + mock_courtId + ");out%20tags;"))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mockOverpassResponseJson));


        // Calling the service method
        BasketballCourt court = service.getCourt(mock_courtId);

        // Verifying that the response is not null and the expected court name is returned
        assertNotNull(court);  // Ensure the court is not null
        assertEquals("Test Court", court.getName());  // Ensure the court name is correctly mapped
    }

    @Test
    public void test_getAddressDetails() throws Exception {
        double lat = 1;
        double lon = 2;

        // Mock Address cache
        when(addressCache.containsKey(lat + "," + lon)).thenReturn(false);

        // Mocking response from the API
        String mockResponseJson = "{ \"address\": { \"addr:housenumber\": \"Test Street\", \"addr:city\":  " +
                "\"Test City\", \"addr:street\": \"Test Street\", \"addr:postcode\": \"12345\"}}";

        // Mocking the response from RestClient API
        this.server.expect(requestTo(String.format("https://nominatim.openstreetmap.org/reverse?lat=%f&lon=%f&format=json&addressdetails=1", lat, lon)))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mockResponseJson));

        Map<String, String> address = service.getAddressDetails(lat, lon);

        // Verifying that the response is not null and the expected postal code is returned
        assertNotNull(address);  // Ensure the address is not null
        assertEquals("12345", address.get("addr:postcode"));
    }

    @Test
    public void test_getCourtsInArea() throws Exception {
        double lat = 1;
        double lon = 2;
        int range = 1000;

        // Prepare mock response for the RestClient when court is not found
        String mockOverpassResponseJson = "{ \"elements\": [ { \"id\": 1" +
                ", \"tags\": { \"name\": \"Test Court\", \"addr:housenumber\": \"Test Street\", \"addr:city\":  " +
                "\"Test City\", \"addr:street\": \"Test Street\", \"addr:postcode\": \"12345\"}, \"center\": {\"lat\": 1, " +
                "\"lon\": 2 }} ] }";

        // Mock repository
        when(repository.findByIdIn(List.of(1L))).thenReturn(List.of());


        // Mocking the response from RestClient API
        this.server.expect(requestTo( "https://overpass-api.de/api/interpreter?data=%5Bout:json%5D;" +
                        "(way(around:1000,1.000000,2.000000)%5B%22amenity%22%3D%22community_centre%22%5D;" +
                        "way(around:1000,1.000000,2.000000)%5B%22leisure%22%3D%22pitch%22%5D%5B%22sport%22%3D%22" +
                        "basketball%22%5D;way(around:1000,1.000000,2.000000)%5B%22amenity%22%3D%22school" +
                        "%22%5D%5B%22sport%22%3D%22basketball%22%5D;);out%20center;%20out%20tags;"))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mockOverpassResponseJson));

        // Calling service
        Set<BasketballCourt> courts = service.getCourtsInArea(lat, lon, range);

        // Verifying that the response is not null and the expected court name is returned
        assertNotNull(courts);  // Ensure the court is not null
        assert(courts.size() == 1); // Make sure something in the set
    }
}
