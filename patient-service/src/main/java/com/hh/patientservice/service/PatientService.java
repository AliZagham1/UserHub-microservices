package com.hh.patientservice.service;

import billing.BillingServiceGrpc;
import com.hh.patientservice.EmailAlreadyExistsException;
import com.hh.patientservice.PatientNotFoundException;
import com.hh.patientservice.dto.PatientRequestDTO;
import com.hh.patientservice.dto.PatientResponseDTO;
import com.hh.patientservice.grpc.BillingServiceGrpcClient;
import com.hh.patientservice.kafka.kafkaProducer;
import com.hh.patientservice.mapper.PatientMapper;
import com.hh.patientservice.model.Patient;
import com.hh.patientservice.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.hh.patientservice.mapper.PatientMapper.toDTO;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final kafkaProducer kafkaProducer;

    public PatientService(PatientRepository patientRepository, BillingServiceGrpcClient billingServiceGrpcClient, kafkaProducer kafkaProducer) {

        this.patientRepository =  patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }

    public List<PatientResponseDTO> getPatients() {
        List<Patient> patients= patientRepository.findAll();
        List<PatientResponseDTO> patientResponseDTOs = patients.stream().map(patient -> PatientMapper.toDTO(patient)).toList();
        return patientResponseDTOs;
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException("A patient with this email" + "already exists" + patientRequestDTO.getEmail());
        }
        Patient newPatient = patientRepository.save(PatientMapper.toModel(patientRequestDTO));

        billingServiceGrpcClient.createBillingAccount(newPatient.getId().toString(), newPatient.getName(), newPatient.getEmail());
        kafkaProducer.sendEvent(newPatient);
        return PatientMapper.toDTO(newPatient);
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO) {
        Patient patient = patientRepository.findById(id).orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: "+ id));
        if (!patient.getEmail().equalsIgnoreCase(patientRequestDTO.getEmail())
                && patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException("A patient with this email" + "already exists" + patientRequestDTO.getEmail());
        }

        patient.setName(patientRequestDTO.getName());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));
        Patient updatedPatient = patientRepository.save(patient);

        return PatientMapper.toDTO(updatedPatient);


    }

    public void deletePatient(UUID id) {
        patientRepository.deleteById(id);
    }


}
