package com.hospital.irrewaddy.service;

import com.hospital.irrewaddy.dto.CreateDepartmentRequest;
import com.hospital.irrewaddy.dto.DepartmentResponse;
import com.hospital.irrewaddy.model.Department;
import com.hospital.irrewaddy.model.Doctor;
import com.hospital.irrewaddy.repository.DepartmentRepository;
import com.hospital.irrewaddy.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        // Check if department name already exists
        if (departmentRepository.existsByName(request.getName())) {
            throw new RuntimeException("Department with this name already exists");
        }

        // Create department
        Department department = new Department();
        department.setName(request.getName().trim());
        department.setDescription(request.getDescription());
        department.setIsActive(request.getIsActive());

        // Set department head if provided
        if (request.getDepartmentHeadId() != null) {
            Doctor departmentHead = doctorRepository.findById(request.getDepartmentHeadId())
                    .orElseThrow(() -> new RuntimeException("Doctor not found for department head"));
            department.setDepartmentHead(departmentHead);
        }

        // Save department
        department = departmentRepository.save(department);

        return convertToResponse(department, "Department created successfully");
    }

    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(dept -> convertToResponse(dept, null))
                .collect(Collectors.toList());
    }

    public List<DepartmentResponse> getActiveDepartments() {
        return departmentRepository.findAll().stream()
                .filter(Department::getIsActive)
                .map(dept -> convertToResponse(dept, null))
                .collect(Collectors.toList());
    }

    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        return convertToResponse(department, null);
    }

    public DepartmentResponse getDepartmentByName(String name) {
        Department department = departmentRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        return convertToResponse(department, null);
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long id, CreateDepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        // Check if new name conflicts with existing department
        if (!department.getName().equals(request.getName())) {
            if (departmentRepository.existsByName(request.getName())) {
                throw new RuntimeException("Department with this name already exists");
            }
            department.setName(request.getName().trim());
        }

        department.setDescription(request.getDescription());
        department.setIsActive(request.getIsActive());

        // Update department head if provided
        if (request.getDepartmentHeadId() != null) {
            Doctor departmentHead = doctorRepository.findById(request.getDepartmentHeadId())
                    .orElseThrow(() -> new RuntimeException("Doctor not found for department head"));
            department.setDepartmentHead(departmentHead);
        } else {
            department.setDepartmentHead(null);
        }

        department = departmentRepository.save(department);

        return convertToResponse(department, "Department updated successfully");
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        // Check if department has doctors
        if (department.getDoctors() != null && !department.getDoctors().isEmpty()) {
            throw new RuntimeException("Cannot delete department with assigned doctors. Please reassign doctors first.");
        }

        // Soft delete by setting isActive to false
        department.setIsActive(false);
        departmentRepository.save(department);
    }

    @Transactional
    public void hardDeleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        // Check if department has doctors
        if (department.getDoctors() != null && !department.getDoctors().isEmpty()) {
            throw new RuntimeException("Cannot delete department with assigned doctors. Please reassign doctors first.");
        }

        // Hard delete
        departmentRepository.delete(department);
    }

    // Helper method to convert Department to Response
    private DepartmentResponse convertToResponse(Department department, String message) {
        DepartmentResponse response = new DepartmentResponse();
        response.setId(department.getId());
        response.setName(department.getName());
        response.setDescription(department.getDescription());
        response.setIsActive(department.getIsActive());
        response.setCreatedAt(department.getCreatedAt());

        // Set department head info if exists
        if (department.getDepartmentHead() != null) {
            response.setDepartmentHeadId(department.getDepartmentHead().getId());
            response.setDepartmentHeadName(department.getDepartmentHead().getUser().getFullName());
        }

        // Count total doctors in department
        if (department.getDoctors() != null) {
            response.setTotalDoctors(department.getDoctors().size());
        } else {
            response.setTotalDoctors(0);
        }

        response.setMessage(message);
        return response;
    }
}