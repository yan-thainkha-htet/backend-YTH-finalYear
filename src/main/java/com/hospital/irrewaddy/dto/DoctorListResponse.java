package com.hospital.irrewaddy.dto;

import java.math.BigDecimal;

public class DoctorListResponse {
    private Long id;
    private String fullName;
    private String specialization;
    private String departmentName;
    private BigDecimal consultationFee;
    private Integer experienceYears;
    private Float rating;

    // Constructors
    public DoctorListResponse() {
    }

    public DoctorListResponse(Long id, String fullName, String specialization, String departmentName,
                              BigDecimal consultationFee, Integer experienceYears, Float rating) {
        this.id = id;
        this.fullName = fullName;
        this.specialization = specialization;
        this.departmentName = departmentName;
        this.consultationFee = consultationFee;
        this.experienceYears = experienceYears;
        this.rating = rating;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }
}