package com.prestek.davivienda.dto;

import java.time.LocalDateTime;

import com.prestek.davivienda.model.Application.ApplicationStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Credit application data transfer object")
public class ApplicationDto {

    @Schema(description = "Application unique identifier", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Application status", example = "PENDING")
    private ApplicationStatus status;

    @Schema(description = "Application submission date", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime applicationDate;

    @Schema(description = "Application review date", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime reviewDate;

    @Schema(description = "Application approval date", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime approvalDate;

    @Schema(description = "Application notes", example = "Additional documentation provided")
    private String notes;

    @Schema(description = "Rejection reason if applicable", example = "Insufficient income")
    private String rejectionReason;

    @Schema(description = "Application amount", example = "5000.00")
    private Double amount;

    @Schema(description = "Application creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Application last update timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    @Schema(description = "ID of the user who submitted the application", example = "1")
    private String userId;

    @Schema(description = "ID of the credit offer associated with the application", example = "1")
    private Long creditOfferId;

    @Schema(description = "Full name of the user who submitted the application", example = "John Doe", accessMode = Schema.AccessMode.READ_ONLY)
    private String userFullName;

    @Schema(description = "Description of the associated credit offer", example = "Personal loan with competitive rates", accessMode = Schema.AccessMode.READ_ONLY)
    private String creditOfferDescription;
}