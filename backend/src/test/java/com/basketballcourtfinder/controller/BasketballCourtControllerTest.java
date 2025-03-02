package com.basketballcourtfinder.controller;

import com.basketballcourtfinder.dto.CourtsDTO;
import com.basketballcourtfinder.entity.BasketballCourt;
import com.basketballcourtfinder.service.BasketballCourtService;
import com.basketballcourtfinder.service.UserService;
import com.basketballcourtfinder.util.PasswordUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class BasketballCourtControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BasketballCourtService courtService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PasswordUtils passwordUtils;

    @Test
    public void testGetCourt() throws Exception {
        long courtId = 1L;
        BasketballCourt mockCourt = new BasketballCourt(1L, "Test Court");

        when(courtService.getCourt(courtId)).thenReturn(mockCourt);

        mockMvc.perform(get("/api/courts").param("court_id", String.valueOf(courtId))
                .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(courtId))
                .andExpect(jsonPath("$.name").value("Test Court"));
    }

    @Test
    public void testGetCourts() throws Exception {
        CourtsDTO courtsDTO = new CourtsDTO(100, 200, 5);
        Set<BasketballCourt> mockCourts = new HashSet<>();

        mockCourts.add(new BasketballCourt(1L, "Test Court 1"));
        mockCourts.add(new BasketballCourt(2L, "Test Court 2"));

        when(courtService.getCourtsInArea(courtsDTO.getLatitude(),
                courtsDTO.getLongitude(), courtsDTO.getRange())).thenReturn(mockCourts);

        mockMvc.perform(get("/api/courts/around")
                .param("latitude", String.valueOf(courtsDTO.getLatitude()))
                .param("longitude", String.valueOf(courtsDTO.getLongitude()))
                .param("range", String.valueOf(courtsDTO.getRange()))
                .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))  // Check for 2 items
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(1, 2))) // Verify ids in any order
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Test Court 1", "Test Court 2"))); // Verify names in any order
    }

    @Test
    public void testPartialUpdateSuccess() throws Exception {
        long mockId = 1L;

        BasketballCourt court = new BasketballCourt(mockId, "Test Court 1");


        // Convert the User object to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String courtJson = objectMapper.writeValueAsString(court);

        when(courtService.partialUpdate(mockId, court)).thenReturn(court);

        mockMvc.perform(patch("/api/courts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courtJson) // Send JSON in request body
                        .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Court 1"));
    }

    @Test
    public void testPartialUpdateFail() throws Exception {
        long mockId = 1L;

        BasketballCourt court = new BasketballCourt(mockId, "Test Court 1");


        // Convert the User object to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String courtJson = objectMapper.writeValueAsString(court);

        when(courtService.partialUpdate(mockId, court))
                .thenThrow(new IllegalArgumentException("Rim type must be set to a predefined option."));

        mockMvc.perform(patch("/api/courts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courtJson) // Send JSON in request body
                        .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Rim type must be set to a predefined option."));
    }
}
