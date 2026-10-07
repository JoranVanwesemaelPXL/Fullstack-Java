package be.pxl.services.organization.controller;

import be.pxl.services.organization.domain.dto.OrganizationRequest;
import be.pxl.services.organization.domain.dto.OrganizationResponse;
import be.pxl.services.organization.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(
            OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationResponse add(
            @Valid @RequestBody OrganizationRequest request) {

        return organizationService.add(request);
    }

    @GetMapping("/{id}")
    public OrganizationResponse findById(
            @PathVariable Long id) {

        return organizationService.findById(id);
    }
}