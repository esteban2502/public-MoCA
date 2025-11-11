package com.ucp.moca.controller;

import com.ucp.moca.dto.ResultRequest;
import com.ucp.moca.entity.Result;
import com.ucp.moca.entity.UserEntity;
import com.ucp.moca.service.ResultService;
import com.ucp.moca.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("/result/v1")
public class ResultController {

    @Autowired
    private ResultService resultService;

    @Autowired
    private ExcelService excelService;

    @GetMapping
    public ResponseEntity<List<Result>> getAllResults() {
        try {
            List<Result> results = resultService.getAll();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/my-results")
    public ResponseEntity<List<Result>> getMyResults() {
        try {
            UserEntity currentUser = getCurrentUser();
            List<Result> results = resultService.getByUserId(currentUser.getId());
            return ResponseEntity.ok(results);
        } catch (RuntimeException e) {
            System.err.println("Error obteniendo usuario autenticado: " + e.getMessage());
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result> getResultById(@PathVariable Long id) {
        try {
            Result result = resultService.getById(id);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/test/{testId}")
    public ResponseEntity<List<Result>> getResultsByTestId(@PathVariable Long testId) {
        try {
            List<Result> results = resultService.getAllByTestId(testId);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Void> createResult(@RequestBody ResultRequest resultRequest) {
        try {
            resultService.createFromRequest(resultRequest);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            System.err.println("Error creando resultado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateResult(@PathVariable Long id, @RequestBody Result result) {
        try {
            resultService.update(id, result);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResult(@PathVariable Long id) {
        try {
            resultService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Result>> getResultsByPatient(@PathVariable Long patientId) {
        try {
            UserEntity currentUser = getCurrentUser();
            System.out.println("🔍 Obteniendo evaluaciones para paciente ID: " + patientId + " por psicólogo: " + currentUser.getFullName());
            
            List<Result> results = resultService.getByUserId(currentUser.getId());
            
            // Filtrar solo las evaluaciones del paciente específico
            List<Result> patientResults = results.stream()
                .filter(result -> result.getPatient() != null && result.getPatient().getId().equals(patientId))
                .collect(java.util.stream.Collectors.toList());
            
            System.out.println("📊 Evaluaciones encontradas para el paciente: " + patientResults.size());
            return ResponseEntity.ok(patientResults);
        } catch (RuntimeException e) {
            System.err.println("❌ Error obteniendo evaluaciones del paciente: " + e.getMessage());
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            System.err.println("❌ Error interno obteniendo evaluaciones del paciente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/export/excel")
    public ResponseEntity<ByteArrayResource> exportResultsToExcel() {
        try {
            UserEntity currentUser = getCurrentUser();
            System.out.println("📊 Exportando evaluaciones a Excel para psicólogo: " + currentUser.getFullName());

            List<Result> results = resultService.getByUserId(currentUser.getId());
            System.out.println("📋 Evaluaciones a exportar: " + results.size());

            ByteArrayOutputStream excelStream = excelService.generateResultsExcel(results);
            ByteArrayResource resource = new ByteArrayResource(excelStream.toByteArray());

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "evaluaciones_" + currentUser.getFullName().replace(" ", "_") + "_" + timestamp + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(resource.contentLength())
                    .body(resource);

        } catch (IOException e) {
            System.err.println("❌ Error generando Excel de evaluaciones: " + e.getMessage());
            return ResponseEntity.status(500).build();
        } catch (RuntimeException e) {
            System.err.println("❌ Error obteniendo usuario autenticado: " + e.getMessage());
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping("/export/excel/patient/{patientId}")
    public ResponseEntity<ByteArrayResource> exportPatientResultsToExcel(@PathVariable Long patientId) {
        try {
            UserEntity currentUser = getCurrentUser();
            System.out.println("📊 Exportando historial de evaluaciones para paciente ID: " + patientId + " por psicólogo: " + currentUser.getFullName());

            List<Result> results = resultService.getByUserId(currentUser.getId());

            // Filtrar solo las evaluaciones del paciente específico
            List<Result> patientResults = results.stream()
                .filter(result -> result.getPatient() != null && result.getPatient().getId().equals(patientId))
                .collect(java.util.stream.Collectors.toList());

            System.out.println("📋 Evaluaciones del paciente a exportar: " + patientResults.size());

            ByteArrayOutputStream excelStream = excelService.generateResultsExcel(patientResults);
            ByteArrayResource resource = new ByteArrayResource(excelStream.toByteArray());

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "historial_paciente_" + patientId + "_" + timestamp + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(resource.contentLength())
                    .body(resource);

        } catch (IOException e) {
            System.err.println("❌ Error generando Excel de historial del paciente: " + e.getMessage());
            return ResponseEntity.status(500).build();
        } catch (RuntimeException e) {
            System.err.println("❌ Error obteniendo usuario autenticado: " + e.getMessage());
            return ResponseEntity.status(401).build();
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
