package com.prestek.davivienda.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prestek.davivienda.config.TestSecurityConfig;
import com.prestek.davivienda.service.ApplicationService;
import com.prestek.FinancialEntityCore.dto.ApplicationDto;
import com.prestek.FinancialEntityCore.model.Application;
import com.prestek.FinancialEntityCore.request.CreateApplicationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApplicationController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("ApplicationController Integration Tests")
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApplicationService applicationService;

    private ApplicationDto testApplicationDto;

    @BeforeEach
    void setUp() {
        testApplicationDto = ApplicationDto.builder()
                .id(1L)
                .userId("user123")
                .amount(5000000.0)
                .status(Application.ApplicationStatus.PENDING)
                .applicationDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("GET /api/applications - Should return all applications")
    void shouldGetAllApplications() throws Exception {
        // Given
        List<ApplicationDto> applications = Arrays.asList(testApplicationDto);
        when(applicationService.getAllApplications()).thenReturn(applications);

        // When & Then
        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].userId").value("user123"))
                .andExpect(jsonPath("$[0].amount").value(5000000.0));

        verify(applicationService, times(1)).getAllApplications();
    }

    @Test
    @DisplayName("GET /api/applications/{id} - Should return application by ID")
    void shouldGetApplicationById() throws Exception {
        // Given
        when(applicationService.getApplicationById(1L)).thenReturn(Optional.of(testApplicationDto));

        // When & Then
        mockMvc.perform(get("/api/applications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value("user123"));

        verify(applicationService, times(1)).getApplicationById(1L);
    }

    @Test
    @DisplayName("GET /api/applications/{id} - Should return 404 when not found")
    void shouldReturn404WhenApplicationNotFound() throws Exception {
        // Given
        when(applicationService.getApplicationById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/applications/999"))
                .andExpect(status().isNotFound());

        verify(applicationService, times(1)).getApplicationById(999L);
    }

    @Test
    @DisplayName("GET /api/applications/user/{userId} - Should return applications by user ID")
    void shouldGetApplicationsByUserId() throws Exception {
        // Given
        List<ApplicationDto> applications = Arrays.asList(testApplicationDto);
        when(applicationService.getApplicationsByUserId("user123")).thenReturn(applications);

        // When & Then
        mockMvc.perform(get("/api/applications/user/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("user123"));

        verify(applicationService, times(1)).getApplicationsByUserId("user123");
    }

    @Test
    @DisplayName("GET /api/applications/status/{status} - Should return applications by status")
    void shouldGetApplicationsByStatus() throws Exception {
        // Given
        List<ApplicationDto> applications = Arrays.asList(testApplicationDto);
        when(applicationService.getApplicationsByStatus(Application.ApplicationStatus.PENDING))
                .thenReturn(applications);

        // When & Then
        mockMvc.perform(get("/api/applications/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(applicationService, times(1)).getApplicationsByStatus(Application.ApplicationStatus.PENDING);
    }

    @Test
    @DisplayName("POST /api/applications - Should create new application")
    void shouldCreateApplication() throws Exception {
        // Given
        CreateApplicationRequest request = new CreateApplicationRequest();
        request.setUserId("user123");
        request.setAmount(5000000.0);

        when(applicationService.createApplication(anyString(), any(Double.class)))
                .thenReturn(testApplicationDto);

        // When & Then
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.amount").value(5000000.0));

        verify(applicationService, times(1)).createApplication("user123", 5000000.0);
    }

    @Test
    @DisplayName("POST /api/applications - Should return 400 when userId is missing")
    void shouldReturn400WhenUserIdMissing() throws Exception {
        // Given
        CreateApplicationRequest request = new CreateApplicationRequest();
        request.setAmount(5000000.0);

        // When & Then
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(applicationService, never()).createApplication(anyString(), any(Double.class));
    }

    @Test
    @DisplayName("PATCH /api/applications/{id}/status - Should update application status")
    void shouldUpdateApplicationStatus() throws Exception {
        // Given
        Map<String, String> statusUpdate = new HashMap<>();
        statusUpdate.put("status", "APPROVED");
        statusUpdate.put("notes", "Application approved");

        ApplicationDto updatedDto = ApplicationDto.builder()
                .id(1L)
                .userId("user123")
                .amount(5000000.0)
                .status(Application.ApplicationStatus.APPROVED)
                .notes("Application approved")
                .applicationDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(applicationService.updateApplicationStatus(eq(1L), any(), anyString()))
                .thenReturn(Optional.of(updatedDto));

        // When & Then
        mockMvc.perform(patch("/api/applications/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(applicationService, times(1)).updateApplicationStatus(eq(1L), any(), eq("Application approved"));
    }

    @Test
    @DisplayName("PATCH /api/applications/{id}/status - Should return 400 when status is missing")
    void shouldReturn400WhenStatusMissing() throws Exception {
        // Given
        Map<String, String> statusUpdate = new HashMap<>();
        statusUpdate.put("notes", "Some notes");

        // When & Then
        mockMvc.perform(patch("/api/applications/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isBadRequest());

        verify(applicationService, never()).updateApplicationStatus(anyLong(), any(), anyString());
    }

    @Test
    @DisplayName("GET /api/applications/user/{userId}/count - Should return application count")
    void shouldGetApplicationCount() throws Exception {
        // Given
        when(applicationService.getApplicationCountByUserId("user123")).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/api/applications/user/user123/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));

        verify(applicationService, times(1)).getApplicationCountByUserId("user123");
    }

    @Test
    @DisplayName("DELETE /api/applications/{id} - Should delete application")
    void shouldDeleteApplication() throws Exception {
        // Given
        when(applicationService.deleteApplication(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/applications/1"))
                .andExpect(status().isNoContent());

        verify(applicationService, times(1)).deleteApplication(1L);
    }

    @Test
    @DisplayName("DELETE /api/applications/{id} - Should return 404 when application not found")
    void shouldReturn404WhenDeletingNonExistentApplication() throws Exception {
        // Given
        when(applicationService.deleteApplication(999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/applications/999"))
                .andExpect(status().isNotFound());

        verify(applicationService, times(1)).deleteApplication(999L);
    }
}
