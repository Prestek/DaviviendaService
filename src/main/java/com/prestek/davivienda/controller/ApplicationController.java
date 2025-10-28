package com.prestek.davivienda.controller;

import java.util.List;
import java.util.Map;

import com.prestek.davivienda.request.CreateApplicationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prestek.davivienda.dto.ApplicationDto;
import com.prestek.davivienda.model.Application.ApplicationStatus;
import com.prestek.davivienda.service.ApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Applications", description = "Credit application management operations")
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping
    @Operation(summary = "Get all applications", description = "Retrieve a list of all credit applications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved applications",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ApplicationDto.class))))
    })
    public ResponseEntity<List<ApplicationDto>> getAllApplications() {
        log.info("GET /api/applications - Fetching all applications");
        List<ApplicationDto> applications = applicationService.getAllApplications();
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get application by ID", description = "Retrieve a specific application by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApplicationDto.class))),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    public ResponseEntity<ApplicationDto> getApplicationById(
            @Parameter(description = "Application ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("GET /api/applications/{} - Fetching application by id", id);
        return applicationService.getApplicationById(id)
                .map(application -> ResponseEntity.ok(application))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get applications by user ID", description = "Retrieve all applications for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user applications",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ApplicationDto.class))))
    })
    public ResponseEntity<List<ApplicationDto>> getApplicationsByUserId(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable String userId) {
        log.info("GET /api/applications/user/{} - Fetching applications by user id", userId);
        List<ApplicationDto> applications = applicationService.getApplicationsByUserId(userId);
        return ResponseEntity.ok(applications);
    }

//    @GetMapping("/credit-offer/{creditOfferId}")
//    @Operation(summary = "Get applications by credit offer ID", description = "Retrieve all applications for a specific credit offer")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Successfully retrieved credit offer applications",
//                    content = @Content(mediaType = "application/json",
//                            array = @ArraySchema(schema = @Schema(implementation = ApplicationDto.class))))
//    })
//    public ResponseEntity<List<ApplicationDto>> getApplicationsByCreditOfferId(
//            @Parameter(description = "Credit offer ID", required = true, example = "1")
//            @PathVariable Long creditOfferId) {
//        log.info("GET /api/applications/credit-offer/{} - Fetching applications by credit offer id", creditOfferId);
//        List<ApplicationDto> applications = applicationService.getApplicationsByCreditOfferId(creditOfferId);
//        return ResponseEntity.ok(applications);
//    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get applications by status", description = "Retrieve all applications with a specific status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved applications by status",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ApplicationDto.class))))
    })
    public ResponseEntity<List<ApplicationDto>> getApplicationsByStatus(
            @Parameter(description = "Application status", required = true, example = "PENDING")
            @PathVariable ApplicationStatus status) {
        log.info("GET /api/applications/status/{} - Fetching applications by status", status);
        List<ApplicationDto> applications = applicationService.getApplicationsByStatus(status);
        return ResponseEntity.ok(applications);
    }

    @PostMapping
    @Operation(summary = "Create new application", description = "Create a new credit application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Application created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApplicationDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data or referenced entities not found")
    })
    public ResponseEntity<ApplicationDto> createApplication(
            @Parameter(description = "Application request containing userId and creditOfferId", required = true,
                    schema = @Schema(example = "{\"userId\": \"1\", \"amount\": 1000.0}"))
            @RequestBody CreateApplicationRequest request) {
        log.info("POST /api/applications - Creating new application");

        String userId = request.getUserId();
        Double amount = request.getAmount();

        if (userId == null || amount == null) {
            log.error("Missing required fields: userId or creditOfferId");
            return ResponseEntity.badRequest().build();
        }

        try {
            ApplicationDto createdApplication = applicationService.createApplication(userId, amount);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdApplication);
        } catch (IllegalArgumentException e) {
            log.error("Error creating application: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update application status", description = "Update the status of an existing application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application status updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApplicationDto.class))),
            @ApiResponse(responseCode = "404", description = "Application not found"),
            @ApiResponse(responseCode = "400", description = "Invalid status or input data")
    })
    public ResponseEntity<ApplicationDto> updateApplicationStatus(
            @Parameter(description = "Application ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Status update request", required = true,
                    schema = @Schema(example = "{\"status\": \"APPROVED\", \"notes\": \"Application approved after review\"}"))
            @RequestBody Map<String, String> request) {

        log.info("PATCH /api/applications/{}/status - Updating application status", id);

        String statusStr = request.get("status");
        String notes = request.get("notes");

        if (statusStr == null) {
            log.error("Missing required field: status");
            return ResponseEntity.badRequest().build();
        }

        try {
            ApplicationStatus status = ApplicationStatus.valueOf(statusStr);
            return applicationService.updateApplicationStatus(id, status, notes)
                    .map(application -> ResponseEntity.ok(application))
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            log.error("Invalid status value: {}", statusStr);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}/count")
    @Operation(summary = "Get application count by user", description = "Get the total number of applications for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved application count",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"count\": 5}")))
    })
    public ResponseEntity<Map<String, Long>> getApplicationCountByUserId(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable String userId) {
        log.info("GET /api/applications/user/{}/count - Getting application count for user", userId);
        Long count = applicationService.getApplicationCountByUserId(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete application", description = "Remove an application from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Application deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    public ResponseEntity<Void> deleteApplication(
            @Parameter(description = "Application ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("DELETE /api/applications/{} - Deleting application", id);
        boolean deleted = applicationService.deleteApplication(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}