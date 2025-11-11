package com.ucp.moca.service;

import com.ucp.moca.entity.Patient;
import com.ucp.moca.entity.Result;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public interface ExcelService {
    
    /**
     * Genera un archivo Excel con la lista de pacientes
     * @param patients Lista de pacientes
     * @return ByteArrayOutputStream con el archivo Excel
     * @throws IOException
     */
    ByteArrayOutputStream generatePatientsExcel(List<Patient> patients) throws IOException;
    
    /**
     * Genera un archivo Excel con la lista de evaluaciones
     * @param results Lista de evaluaciones
     * @return ByteArrayOutputStream con el archivo Excel
     * @throws IOException
     */
    ByteArrayOutputStream generateResultsExcel(List<Result> results) throws IOException;
}
