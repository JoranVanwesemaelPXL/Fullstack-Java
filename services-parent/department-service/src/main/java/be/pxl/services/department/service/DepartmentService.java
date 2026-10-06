package be.pxl.services.department.service;

import be.pxl.services.department.domain.Department;
import be.pxl.services.department.domain.dto.DepartmentRequest;
import be.pxl.services.department.domain.dto.DepartmentResponse;
import be.pxl.services.department.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public DepartmentResponse add(DepartmentRequest request) {

        Department department = new Department(
                request.getName(),
                request.getOrganizationId()
        );

        return toResponse(departmentRepository.save(department));
    }

    public DepartmentResponse findById(Long id) {

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        return toResponse(department);
    }

    public List<DepartmentResponse> findAll() {

        return departmentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<DepartmentResponse> findByOrganization(Long organizationId) {

        return departmentRepository.findByOrganizationId(organizationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private DepartmentResponse toResponse(Department department) {

        DepartmentResponse response = new DepartmentResponse();

        response.setId(department.getId());
        response.setName(department.getName());
        response.setOrganizationId(department.getOrganizationId());

        return response;
    }
}