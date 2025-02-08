package com.basketballcourtfinder.controller;

import com.basketballcourtfinder.dto.CourtsDTO;
import com.basketballcourtfinder.entity.BasketballCourt;
import com.basketballcourtfinder.service.BasketballCourtService;
import com.basketballcourtfinder.service.UserService;
import com.basketballcourtfinder.util.PasswordUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Court 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Test Court 2"));
    }
}
