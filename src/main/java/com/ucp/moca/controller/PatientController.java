package com.ucp.moca.controller;

import com.ucp.moca.entity.Patient;
import com.ucp.moca.entity.UserEntity;
import com.ucp.moca.repository.PatientRepository;
import com.ucp.moca.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/patients/v1")
public class PatientController {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ExcelService excelService;

    @GetMapping
    public ResponseEntity<List<Patient>> getAllPatients() {
        List<Patient> patients = patientRepository.findAll();
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/my-patients")
    public ResponseEntity<List<Patient>> getMyPatients() {
        try {
            UserEntity currentUser = getCurrentUser();
            System.out.println("Buscando pacientes para psicólogo: " + currentUser.getFullName() + " (ID: " + currentUser.getId() + ")");
            
            List<Patient> patients = patientRepository.findByPsychologistsId(currentUser.getId());
            System.out.println("Pacientes encontrados: " + patients.size());
            
            // Debug: mostrar detalles de cada paciente encontrado
            for (Patient patient : patients) {
                System.out.println("  - Paciente: " + patient.getFullName() + 
                                 " (ID: " + patient.getId() + 
                                 ", Psicólogos: " + patient.getPsychologists().size() + ")");
            }
            
            return ResponseEntity.ok(patients);
        } catch (RuntimeException e) {
            System.err.println("Error obteniendo usuario autenticado: " + e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/cedula/{idNumber}")
    public ResponseEntity<Patient> getPatientByCedula(@PathVariable String idNumber) {
        Optional<Patient> patient = patientRepository.findByDocumentNumber(idNumber);
        return patient.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/register")
    public ResponseEntity<Patient> registerPatient(@RequestBody Patient patient) {
        // Los pacientes se registran sin psicólogos asignados
        // Los psicólogos se asignan cuando evalúan al paciente
        Patient saved = patientRepository.save(patient);
        System.out.println("Paciente registrado: " + saved.getFullName());
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/debug-all-patients")
    public ResponseEntity<String> debugAllPatients() {
        try {
            UserEntity currentUser = getCurrentUser();
            List<Patient> allPatients = patientRepository.findAll();
            
            StringBuilder debug = new StringBuilder();
            debug.append("Usuario actual: ").append(currentUser.getFullName()).append(" (ID: ").append(currentUser.getId()).append(")\n");
            debug.append("Total pacientes en BD: ").append(allPatients.size()).append("\n\n");
            
            for (Patient patient : allPatients) {
                debug.append("Paciente: ").append(patient.getFullName())
                     .append(" (ID: ").append(patient.getId())
                     .append(", Psicólogos: ").append(patient.getPsychologists().size()).append(")\n");
                
                for (UserEntity psychologist : patient.getPsychologists()) {
                    debug.append("  - Psicólogo: ").append(psychologist.getFullName())
                         .append(" (ID: ").append(psychologist.getId()).append(")\n");
                }
            }
            
            System.out.println("Debug todos los pacientes:\n" + debug.toString());
            return ResponseEntity.ok(debug.toString());
        } catch (RuntimeException e) {
            System.err.println("Error en debug: " + e.getMessage());
            return ResponseEntity.status(500).body("Error en debug: " + e.getMessage());
        }
    }

    @GetMapping("/export/excel")
    public ResponseEntity<ByteArrayResource> exportPatientsToExcel() {
        try {
            UserEntity currentUser = getCurrentUser();
            System.out.println("Exportando pacientes a Excel para psicólogo: " + currentUser.getFullName());

            List<Patient> patients = patientRepository.findByPsychologistsId(currentUser.getId());
            System.out.println("Pacientes a exportar: " + patients.size());

            ByteArrayOutputStream excelStream = excelService.generatePatientsExcel(patients);
            ByteArrayResource resource = new ByteArrayResource(excelStream.toByteArray());

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "pacientes_" + currentUser.getFullName().replace(" ", "_") + "_" + timestamp + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(resource.contentLength())
                    .body(resource);

        } catch (IOException e) {
            System.err.println("Error generando Excel de pacientes: " + e.getMessage());
            return ResponseEntity.status(500).build();
        } catch (RuntimeException e) {
            System.err.println("Error obteniendo usuario autenticado: " + e.getMessage());
            return ResponseEntity.status(401).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(@PathVariable Long id, @RequestBody Patient patient) {
        try {
            UserEntity currentUser = getCurrentUser();
            System.out.println("✏️ Actualizando paciente ID: " + id + " por psicólogo: " + currentUser.getFullName());

            // Verificar que el paciente existe y está asociado al psicólogo actual
            Optional<Patient> existingPatient = patientRepository.findById(id);
            if (existingPatient.isEmpty()) {
                System.err.println("❌ Paciente no encontrado con ID: " + id);
                return ResponseEntity.notFound().build();
            }

            Patient patientToUpdate = existingPatient.get();
            boolean isAssociated = patientToUpdate.getPsychologists().stream()
                .anyMatch(psychologist -> psychologist.getId().equals(currentUser.getId()));

            if (!isAssociated) {
                System.err.println("❌ El paciente no está asociado al psicólogo actual");
                return ResponseEntity.status(403).build();
            }

            // Actualizar solo los campos permitidos
            patientToUpdate.setFullName(patient.getFullName());
            patientToUpdate.setBirthDate(patient.getBirthDate());
            // No permitir cambiar la cédula por seguridad

            Patient updatedPatient = patientRepository.save(patientToUpdate);
            System.out.println("✅ Paciente actualizado exitosamente: " + updatedPatient.getFullName());

            return ResponseEntity.ok(updatedPatient);

        } catch (RuntimeException e) {
            System.err.println("❌ Error actualizando paciente: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    private UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserEntity) {
            return (UserEntity) authentication.getPrincipal();
        }
        throw new RuntimeException("Usuario no autenticado");
    }
}
