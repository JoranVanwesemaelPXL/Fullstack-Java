package be.pxl.services.organization.service;

import be.pxl.services.organization.domain.Organization;
import be.pxl.services.organization.domain.dto.OrganizationRequest;
import be.pxl.services.organization.domain.dto.OrganizationResponse;
import be.pxl.services.organization.repository.OrganizationRepository;
import org.springframework.stereotype.Service;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public OrganizationResponse add(OrganizationRequest request) {

        Organization organization =
                new Organization(request.getName());

        return toResponse(
                organizationRepository.save(organization)
        );
    }

    public OrganizationResponse findById(Long id) {

        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Organization not found"));

        return toResponse(organization);
    }

    private OrganizationResponse toResponse(Organization organization) {

        OrganizationResponse response = new OrganizationResponse();

        response.setId(organization.getId());
        response.setName(organization.getName());

        return response;
    }
}