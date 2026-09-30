package be.pxl.services.employee.controller;

import be.pxl.services.employee.domain.Employee;
import be.pxl.services.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService){
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<Employee> GetAllEmployees(){
        return employeeService.findAll();
    }

    @GetMapping("/{id}")
    public Employee GetEmployee(@PathVariable Long id){
        return employeeService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Employee CreateEmployee(@Valid @RequestBody Employee employee){
        return employeeService.save(employee);
    }

    @PutMapping("/id")
    public Employee UpdateEmployee(@PathVariable Long id, @Valid @RequestBody Employee employee){

        Employee existingEmployee = employeeService.findById(id);

        existingEmployee.setFirstName(employee.getFirstName());
        existingEmployee.setLastName(employee.getLastName());
        existingEmployee.setEmail(employee.getEmail());
        existingEmployee.setDepartmentId(employee.getDepartmentId());
        existingEmployee.setOrganizationId(employee.getOrganizationId());

        return employeeService.save(existingEmployee);
    }

    @DeleteMapping("/id")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void DeleteEmployee(@PathVariable Long id){
        employeeService.delete(id);
    }


}
