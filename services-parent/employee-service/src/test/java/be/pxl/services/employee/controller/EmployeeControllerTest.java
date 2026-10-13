package be.pxl.services.employee.controller;

import be.pxl.services.employee.domain.dto.EmployeeRequest;
import be.pxl.services.employee.domain.dto.EmployeeResponse;
import be.pxl.services.employee.service.EmployeeService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    private EmployeeResponse createResponse() {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(1L);
        response.setFirstName("Jasper");
        response.setLastName("Peeters");
        response.setEmail("JP@gmail.com");
        response.setDepartmentId(1L);
        response.setOrganizationId(1L);
        return response;
    }

    @Test
    void add_returns201AndEmployeeResponse() throws Exception {
        EmployeeResponse response = createResponse();

        when(employeeService.save(any(EmployeeRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Jasper",
                                  "lastName": "Peeters",
                                  "email": "JP@gmail.com",
                                  "departmentId": 1,
                                  "organizationId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Jasper"))
                .andExpect(jsonPath("$.lastName").value("Peeters"))
                .andExpect(jsonPath("$.email").value("JP@gmail.com"));

        verify(employeeService).save(any(EmployeeRequest.class));
    }

    @Test
    void findById_returnsEmployee() throws Exception {
        when(employeeService.findById(1L))
                .thenReturn(createResponse());

        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Jasper"))
                .andExpect(jsonPath("$.email").value("JP@gmail.com"));

        verify(employeeService).findById(1L);
    }

    @Test
    void findAll_returnsEmployeeList() throws Exception {
        when(employeeService.findAll())
                .thenReturn(List.of(createResponse()));

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Jasper"));

        verify(employeeService).findAll();
    }

    @Test
    void findByDepartment_returnsEmployeesForDepartment() throws Exception {
        when(employeeService.findByDepartment(1L))
                .thenReturn(List.of(createResponse()));

        mockMvc.perform(get("/employees/department/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].departmentId").value(1));

        verify(employeeService).findByDepartment(1L);
    }

    @Test
    void findByOrganization_returnsEmployeesForOrganization() throws Exception {
        when(employeeService.findByOrganization(1L))
                .thenReturn(List.of(createResponse()));

        mockMvc.perform(get("/employees/organization/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].organizationId").value(1));

        verify(employeeService).findByOrganization(1L);
    }
}
