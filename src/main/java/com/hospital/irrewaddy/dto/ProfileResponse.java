package com.hospital.irrewaddy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hospital.irrewaddy.model.Patient;
import com.hospital.irrewaddy.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileResponse {
    private Long id;

    private String fullName;

    private String username;

    private String email;

    private String phone;

    private User.Gender gender;

    private User.UserRole role;

    private Boolean isActive = true;

    private LocalDate dateOfBirth;

    private String address;

    private DoctorResponse doctorInfo;

    private AdminResponse adminInfo;

    private ReceptionistResponse receptionistInfo;

    private PatientResponse patientInfo;
}
