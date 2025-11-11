package com.ucp.moca.service.implement;

import com.ucp.moca.entity.Patient;
import com.ucp.moca.entity.Result;
import com.ucp.moca.service.ExcelService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelServiceImpl implements ExcelService {

    @Override
    public ByteArrayOutputStream generatePatientsExcel(List<Patient> patients) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Pacientes");
        
        // Crear estilos
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        // Crear encabezados
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Cédula", "Nombre Completo", "Fecha de Nacimiento", "Psicólogos Asignados"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Llenar datos
        int rowNum = 1;
        for (Patient patient : patients) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(patient.getDocumentNumber());
            row.createCell(1).setCellValue(patient.getFullName());
            
            if (patient.getBirthDate() != null) {
                row.createCell(2).setCellValue(patient.getBirthDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } else {
                row.createCell(2).setCellValue("");
            }
            
            // Concatenar nombres de psicólogos
            StringBuilder psychologists = new StringBuilder();
            if (patient.getPsychologists() != null && !patient.getPsychologists().isEmpty()) {
                patient.getPsychologists().forEach(psychologist -> {
                    if (psychologists.length() > 0) {
                        psychologists.append(", ");
                    }
                    psychologists.append(psychologist.getFullName());
                });
            }
            row.createCell(3).setCellValue(psychologists.toString());
            
            // Aplicar estilo a todas las celdas de la fila
            for (int i = 0; i < 4; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }
        
        // Autoajustar columnas (solo las 4 columnas que usamos)
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // Asegurar que solo se usen 4 columnas
        for (int i = 4; i < 10; i++) { // Ocultar columnas adicionales del 4 al 9
            sheet.setColumnHidden(i, true);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream;
    }

    @Override
    public ByteArrayOutputStream generateResultsExcel(List<Result> results) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Evaluaciones");
        
        // Crear estilos
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        // Crear encabezados
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Paciente", "Cédula", "Evaluación", "Puntaje Total", "Fecha de Evaluación", "Psicólogo"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Llenar datos
        int rowNum = 1;
        for (Result result : results) {
            Row row = sheet.createRow(rowNum++);
            
            if (result.getPatient() != null) {
                row.createCell(0).setCellValue(result.getPatient().getFullName());
                row.createCell(1).setCellValue(result.getPatient().getDocumentNumber());
            } else {
                row.createCell(0).setCellValue("Sin identificar");
                row.createCell(1).setCellValue("Sin identificar");
            }
            
            if (result.getTest() != null) {
                row.createCell(2).setCellValue(result.getTest().getTitle());
            } else {
                row.createCell(2).setCellValue("Sin identificar");
            }
            
            row.createCell(3).setCellValue(result.getTotalScore() != null ? result.getTotalScore().doubleValue() : 0);
            
            if (result.getEvaluationDate() != null) {
                row.createCell(4).setCellValue(result.getEvaluationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            } else {
                row.createCell(4).setCellValue("");
            }
            
            if (result.getUser() != null) {
                row.createCell(5).setCellValue(result.getUser().getFullName());
            } else {
                row.createCell(5).setCellValue("Sin identificar");
            }
            
            // Aplicar estilo a todas las celdas de la fila
            for (int i = 0; i < 6; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }
        
        // Autoajustar columnas (solo las 6 columnas que usamos)
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // Asegurar que solo se usen 6 columnas
        for (int i = 6; i < 10; i++) { // Ocultar columnas adicionales del 6 al 9
            sheet.setColumnHidden(i, true);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream;
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }
    
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        
        font.setFontHeightInPoints((short) 10);
        
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }
}
