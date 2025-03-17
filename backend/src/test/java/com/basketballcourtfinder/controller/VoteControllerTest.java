package com.basketballcourtfinder.controller;

import com.basketballcourtfinder.entity.VoteType;
import com.basketballcourtfinder.exceptions.EntityAlreadyExistsException;
import com.basketballcourtfinder.exceptions.EntityNotFoundException;
import com.basketballcourtfinder.service.VoteService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class VoteControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VoteService voteService;

    @Mock
    Authentication authentication;

    public void setup_authUser() {
        // Create a mocked Authentication object
        Long mockUserId = 1L;
        authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(mockUserId);

        // Mock the SecurityContextHolder
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testAddUpvote() throws Exception {
        setup_authUser();
        doNothing().when(voteService).addVote(1L, 1L, VoteType.UPVOTE);
        mockMvc.perform(post("/api/vote/1/upvote"))
                .andExpect(status().isOk());

        verify(voteService).addVote(1L, 1L, VoteType.UPVOTE);
    }

    @Test
    public void testAddUpvote_NotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/vote/1/upvote"))
                .andExpect(status().isUnauthorized());

        verify(voteService, never()).addVote(1L, 1L, VoteType.UPVOTE);
    }

    @Test
    public void testAddupvote_EntityNotFoundException() throws Exception {
        setup_authUser();
        doThrow(new EntityNotFoundException("review", 1L)).when(voteService).addVote(1L, 1L, VoteType.UPVOTE);
        mockMvc.perform(post("/api/vote/1/upvote"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testAddupvote_EntityAlreadyExistsException() throws Exception {
        setup_authUser();
        doThrow(new EntityAlreadyExistsException("review")).when(voteService).addVote(1L, 1L, VoteType.UPVOTE);
        mockMvc.perform(post("/api/vote/1/upvote"))
                .andExpect(status().isConflict());
    }

    @Test
    public void testAddDownvote() throws Exception {
        setup_authUser();
        doNothing().when(voteService).addVote(1L, 1L, VoteType.DOWNVOTE);
        mockMvc.perform(post("/api/vote/1/downvote"))
                .andExpect(status().isOk());

        verify(voteService).addVote(1L, 1L, VoteType.DOWNVOTE);
    }

    @Test
    public void testAddDownvote_NotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/vote/1/downvote"))
                .andExpect(status().isUnauthorized());

        verify(voteService, never()).addVote(1L, 1L, VoteType.DOWNVOTE);
    }

    @Test
    public void testAddDownvote_EntityNotFoundException() throws Exception {
        setup_authUser();
        doThrow(new EntityNotFoundException("review", 1L)).when(voteService).addVote(1L, 1L, VoteType.DOWNVOTE);
        mockMvc.perform(post("/api/vote/1/downvote"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testAddDownvote_EntityAlreadyExistsException() throws Exception {
        setup_authUser();
        doThrow(new EntityAlreadyExistsException("review")).when(voteService).addVote(1L, 1L, VoteType.DOWNVOTE);
        mockMvc.perform(post("/api/vote/1/downvote"))
                .andExpect(status().isConflict());
    }

    @Test
    public void testDeleteVote() throws Exception {
        setup_authUser();
        doReturn(ResponseEntity.ok(true)).when(voteService).removeVote(1L, 1L);
        mockMvc.perform(delete("/api/vote/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteVote_NotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/vote/1"))
                .andExpect(status().isUnauthorized());
    }
}
