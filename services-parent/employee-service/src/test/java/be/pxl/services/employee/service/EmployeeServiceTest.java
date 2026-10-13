package be.pxl.services.employee.service;

import be.pxl.services.employee.domain.Employee;
import be.pxl.services.employee.domain.dto.EmployeeRequest;
import be.pxl.services.employee.domain.dto.EmployeeResponse;
import be.pxl.services.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void save_createsAndReturnsEmployeeResponse() {
        EmployeeRequest request = new EmployeeRequest();
        request.setFirstName("Jasper");
        request.setLastName("Peeters");
        request.setEmail("JP@gmail.com");
        request.setDepartmentId(1L);
        request.setOrganizationId(1L);

        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeResponse result = employeeService.save(request);

        assertThat(result.getFirstName()).isEqualTo("Jasper");
        assertThat(result.getLastName()).isEqualTo("Peeters");
        assertThat(result.getEmail()).isEqualTo("JP@gmail.com");
        assertThat(result.getDepartmentId()).isEqualTo(1L);
        assertThat(result.getOrganizationId()).isEqualTo(1L);

        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void findById_returnsEmployeeWhenFound() {
        Employee employee = createEmployee();

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        EmployeeResponse result = employeeService.findById(1L);

        assertThat(result.getFirstName()).isEqualTo("Jasper");
        assertThat(result.getLastName()).isEqualTo("Peeters");
        assertThat(result.getEmail()).isEqualTo("JP@gmail.com");
        assertThat(result.getDepartmentId()).isEqualTo(1L);
        assertThat(result.getOrganizationId()).isEqualTo(1L);

        verify(employeeRepository).findById(1L);
    }

    @Test
    void findById_throwsExceptionWhenEmployeeDoesNotExist() {
        when(employeeRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.findById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Employee not found");

        verify(employeeRepository).findById(99L);
    }

    @Test
    void findAll_returnsEmployeeResponses() {
        Employee employee = createEmployee();

        when(employeeRepository.findAll())
                .thenReturn(List.of(employee));

        List<EmployeeResponse> result = employeeService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getFirstName()).isEqualTo("Jasper");
        assertThat(result.getFirst().getEmail()).isEqualTo("JP@gmail.com");

        verify(employeeRepository).findAll();
    }

    @Test
    void findByDepartment_returnsEmployeesForDepartment() {
        Employee employee = createEmployee();

        when(employeeRepository.findByDepartmentId(1L))
                .thenReturn(List.of(employee));

        List<EmployeeResponse> result =
                employeeService.findByDepartment(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDepartmentId()).isEqualTo(1L);
        assertThat(result.get(0).getFirstName()).isEqualTo("Jasper");

        verify(employeeRepository).findByDepartmentId(1L);
    }

    @Test
    void findByOrganization_returnsEmployeesForOrganization() {
        Employee employee = createEmployee();

        when(employeeRepository.findByOrganizationId(1L))
                .thenReturn(List.of(employee));

        List<EmployeeResponse> result =
                employeeService.findByOrganization(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrganizationId()).isEqualTo(1L);
        assertThat(result.get(0).getFirstName()).isEqualTo("Jasper");

        verify(employeeRepository).findByOrganizationId(1L);
    }

    private Employee createEmployee() {
        return new Employee(
                "Jasper",
                "Peeters",
                "JP@gmail.com",
                1L,
                1L
        );
    }
}
