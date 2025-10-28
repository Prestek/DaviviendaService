package com.prestek.davivienda.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.prestek.FinancialEntityCore.dto.ApplicationDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.prestek.FinancialEntityCore.model.Application;
import com.prestek.FinancialEntityCore.model.Application.*;
import com.prestek.davivienda.repository.ApplicationRepository;
//import com.prestek.coltefinanciera.repository.CreditOfferRepository;
//import com.prestek.coltefinanciera.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
//    private final UserRepository userRepository;
//    private final CreditOfferRepository creditOfferRepository;

    public List<ApplicationDto> getAllApplications() {
        log.info("Fetching all applications");
        return applicationRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<ApplicationDto> getApplicationById(Long id) {
        log.info("Fetching application with id: {}", id);
        return applicationRepository.findById(id)
                .map(this::convertToDto);
    }

    public List<ApplicationDto> getApplicationsByUserId(String userId) {
        log.info("Fetching applications for user id: {}", userId);
        return applicationRepository.findByUserId(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

//    public List<ApplicationDto> getApplicationsByCreditOfferId(Long creditOfferId) {
//        log.info("Fetching applications for credit offer id: {}", creditOfferId);
//        return applicationRepository.findByCreditOfferId(creditOfferId)
//                .stream()
//                .map(this::convertToDto)
//                .collect(Collectors.toList());
//    }

    public List<ApplicationDto> getApplicationsByStatus(ApplicationStatus status) {
        log.info("Fetching applications with status: {}", status);
        return applicationRepository.findByStatus(status)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ApplicationDto createApplication(String userId, Double amount) {
        log.info("Creating new application for user {} and credit amount {}", userId, amount);

//        var user = userRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

//        var creditOffer = creditOfferRepository.findById(creditOfferId)
//                .orElseThrow(() -> new IllegalArgumentException("Credit offer not found with id: " + creditOfferId));

//        if (!creditOffer.getIsActive()) {
//            throw new IllegalArgumentException("Credit offer is not active");
//        }

        Application application = Application.builder()
                .userId(userId)
//                .creditOffer(creditOffer)
                .amount(amount)
                .status(ApplicationStatus.PENDING)
                .applicationDate(LocalDateTime.now())
                .build();

        Application savedApplication = applicationRepository.save(application);
        log.info("Application created successfully with id: {}", savedApplication.getId());

        return convertToDto(savedApplication);
    }

    public Optional<ApplicationDto> updateApplicationStatus(Long id, ApplicationStatus newStatus, String notes) {
        log.info("Updating application {} status to: {}", id, newStatus);

        return applicationRepository.findById(id)
                .map(application -> {
                    ApplicationStatus oldStatus = application.getStatus();
                    application.setStatus(newStatus);
                    application.setNotes(notes);

                    // Set review date when moving to UNDER_REVIEW
                    if (newStatus == ApplicationStatus.UNDER_REVIEW && oldStatus != ApplicationStatus.UNDER_REVIEW) {
                        application.setReviewDate(LocalDateTime.now());
                    }

                    // Set approval date when approved
                    if (newStatus == ApplicationStatus.APPROVED && oldStatus != ApplicationStatus.APPROVED) {
                        application.setApprovalDate(LocalDateTime.now());
                    }

                    // Set rejection reason when rejected
                    if (newStatus == ApplicationStatus.REJECTED) {
                        application.setRejectionReason(notes);
                    }

                    Application updatedApplication = applicationRepository.save(application);
                    log.info("Application status updated successfully for id: {}", updatedApplication.getId());
                    return convertToDto(updatedApplication);
                });
    }

    public boolean deleteApplication(Long id) {
        log.info("Deleting application with id: {}", id);

        if (applicationRepository.existsById(id)) {
            applicationRepository.deleteById(id);
            log.info("Application deleted successfully with id: {}", id);
            return true;
        }

        log.warn("Application not found for deletion with id: {}", id);
        return false;
    }

    public Long getApplicationCountByUserId(String userId) {
        log.info("Getting application count for user id: {}", userId);
        return applicationRepository.countByUserId(userId);
    }

    private ApplicationDto convertToDto(Application application) {
//        String userFullName = application.getUser() != null
//                ? application.getUser().getFirstName() + " " + application.getUser().getLastName()
//                : null;
//
//        String creditOfferDescription = application.getCreditOffer() != null
//                ? application.getCreditOffer().getDescription()
//                : null;

        return ApplicationDto.builder()
                .id(application.getId())
                .status(application.getStatus())
                .applicationDate(application.getApplicationDate())
                .reviewDate(application.getReviewDate())
                .approvalDate(application.getApprovalDate())
                .notes(application.getNotes())
                .rejectionReason(application.getRejectionReason())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .userId(application.getUserId())
                .amount(application.getAmount())
//                .creditOfferId(application.getCreditOffer() != null ? application.getCreditOffer().getId() : null)
//                .userFullName(userFullName)
//                .creditOfferDescription(creditOfferDescription)
                .build();
    }
}