package com.project.leapwise.data_import;

import com.opencsv.exceptions.CsvValidationException;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.IOException;

public interface DataImport {

    @Transactional
    <T, V> void importData(String filePath, JpaRepository<T, V> repository, Class<T> entityClass) throws IOException, CsvValidationException;
}
