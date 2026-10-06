package be.pxl.services.department.domain.dto;

import jakarta.validation.constraints.NotBlank;

public class DepartmentRequest {

    @NotBlank
    private String name;

    private Long organizationId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }
}

