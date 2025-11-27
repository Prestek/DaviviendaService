package com.prestek.davivienda.service;

import com.prestek.davivienda.repository.ApplicationRepository;
import com.prestek.FinancialEntityCore.dto.ApplicationDto;
import com.prestek.FinancialEntityCore.model.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationService Unit Tests")
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private ApplicationService applicationService;

    private Application testApplication;
    private String testUserId;
    private Double testAmount;

    @BeforeEach
    void setUp() {
        testUserId = "user123";
        testAmount = 5000000.0;

        testApplication = Application.builder()
                .id(1L)
                .userId(testUserId)
                .amount(testAmount)
                .status(Application.ApplicationStatus.PENDING)
                .applicationDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should get all applications successfully")
    void shouldGetAllApplications() {
        // Given
        List<Application> applications = Arrays.asList(testApplication);
        when(applicationRepository.findAll()).thenReturn(applications);

        // When
        List<ApplicationDto> result = applicationService.getAllApplications();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(testUserId);
        verify(applicationRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get application by ID successfully")
    void shouldGetApplicationById() {
        // Given
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));

        // When
        Optional<ApplicationDto> result = applicationService.getApplicationById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getUserId()).isEqualTo(testUserId);
        verify(applicationRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty when application ID not found")
    void shouldReturnEmptyWhenApplicationNotFound() {
        // Given
        when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<ApplicationDto> result = applicationService.getApplicationById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(applicationRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should get applications by user ID")
    void shouldGetApplicationsByUserId() {
        // Given
        List<Application> applications = Arrays.asList(testApplication);
        when(applicationRepository.findByUserId(testUserId)).thenReturn(applications);

        // When
        List<ApplicationDto> result = applicationService.getApplicationsByUserId(testUserId);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(testUserId);
        verify(applicationRepository, times(1)).findByUserId(testUserId);
    }

    @Test
    @DisplayName("Should get applications by status")
    void shouldGetApplicationsByStatus() {
        // Given
        List<Application> applications = Arrays.asList(testApplication);
        when(applicationRepository.findByStatus(Application.ApplicationStatus.PENDING))
                .thenReturn(applications);

        // When
        List<ApplicationDto> result = applicationService.getApplicationsByStatus(
                Application.ApplicationStatus.PENDING);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Application.ApplicationStatus.PENDING);
        verify(applicationRepository, times(1)).findByStatus(Application.ApplicationStatus.PENDING);
    }

    @Test
    @DisplayName("Should create application successfully")
    void shouldCreateApplication() {
        // Given
        when(applicationRepository.save(any(Application.class))).thenReturn(testApplication);

        // When
        ApplicationDto result = applicationService.createApplication(testUserId, testAmount);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(testUserId);
        assertThat(result.getAmount()).isEqualTo(testAmount);
        assertThat(result.getStatus()).isEqualTo(Application.ApplicationStatus.PENDING);
        verify(applicationRepository, times(1)).save(any(Application.class));
    }

    @Test
    @DisplayName("Should update application status to APPROVED")
    void shouldUpdateApplicationStatusToApproved() {
        // Given
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        when(applicationRepository.save(any(Application.class))).thenReturn(testApplication);

        // When
        Optional<ApplicationDto> result = applicationService.updateApplicationStatus(
                1L, Application.ApplicationStatus.APPROVED, "Approved by system");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(Application.ApplicationStatus.APPROVED);
        verify(applicationRepository, times(1)).findById(1L);
        verify(applicationRepository, times(1)).save(any(Application.class));
    }

    @Test
    @DisplayName("Should update application status to REJECTED")
    void shouldUpdateApplicationStatusToRejected() {
        // Given
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        when(applicationRepository.save(any(Application.class))).thenReturn(testApplication);

        // When
        Optional<ApplicationDto> result = applicationService.updateApplicationStatus(
                1L, Application.ApplicationStatus.REJECTED, "Insufficient credit score");

        // Then
        assertThat(result).isPresent();
        verify(applicationRepository, times(1)).save(any(Application.class));
    }

    @Test
    @DisplayName("Should return empty when updating non-existent application")
    void shouldReturnEmptyWhenUpdatingNonExistentApplication() {
        // Given
        when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<ApplicationDto> result = applicationService.updateApplicationStatus(
                999L, Application.ApplicationStatus.APPROVED, "Notes");

        // Then
        assertThat(result).isEmpty();
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    @DisplayName("Should delete application successfully")
    void shouldDeleteApplication() {
        // Given
        when(applicationRepository.existsById(1L)).thenReturn(true);
        doNothing().when(applicationRepository).deleteById(1L);

        // When
        boolean result = applicationService.deleteApplication(1L);

        // Then
        assertThat(result).isTrue();
        verify(applicationRepository, times(1)).existsById(1L);
        verify(applicationRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should return false when deleting non-existent application")
    void shouldReturnFalseWhenDeletingNonExistentApplication() {
        // Given
        when(applicationRepository.existsById(999L)).thenReturn(false);

        // When
        boolean result = applicationService.deleteApplication(999L);

        // Then
        assertThat(result).isFalse();
        verify(applicationRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should get application count by user ID")
    void shouldGetApplicationCountByUserId() {
        // Given
        when(applicationRepository.countByUserId(testUserId)).thenReturn(3L);

        // When
        Long count = applicationService.getApplicationCountByUserId(testUserId);

        // Then
        assertThat(count).isEqualTo(3L);
        verify(applicationRepository, times(1)).countByUserId(testUserId);
    }

    @Test
    @DisplayName("Should return empty list when no applications found for user")
    void shouldReturnEmptyListWhenNoApplicationsForUser() {
        // Given
        when(applicationRepository.findByUserId("unknownUser")).thenReturn(List.of());

        // When
        List<ApplicationDto> result = applicationService.getApplicationsByUserId("unknownUser");

        // Then
        assertThat(result).isEmpty();
        verify(applicationRepository, times(1)).findByUserId("unknownUser");
    }
}
