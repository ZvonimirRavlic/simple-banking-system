package com.project.leapwise.data_import.impl;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.leapwise.ServiceProperties;
import com.project.leapwise.data_import.DataImport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class CSVDataImportImpl implements DataImport {

    private final ServiceProperties serviceProperties;

    @Override
    public <T, V> void importData(final String filePath, final JpaRepository<T, V> repository, final Class<T> entityClass) throws IOException {

        long startTime = System.currentTimeMillis();

        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

        ExecutorService executorService = Executors.newFixedThreadPool(serviceProperties.getDataGenerationProperties().getThreadPoolSize());

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            List<String> lines = new ArrayList<>();
            String line;
            String[] headers = reader.readLine().split(serviceProperties.getDataGenerationProperties().getDataSeparator());
            Runnable mapToEntityAndSave = () -> {
                List<String> chunk = new ArrayList<>(lines);
                Runnable task = () -> {
                    try {
                        List<T> list = chunk.stream().map(e -> {
                            String[] splitLine = e.split(serviceProperties.getDataGenerationProperties().getDataSeparator());
                            Map<String, String> map = arrayToMap(headers, splitLine);
                            return mapper.convertValue(map, entityClass);
                        }).toList();
                        repository.saveAll(list);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                };
                executorService.submit(task);
            };
            while ((line = reader.readLine()) != null) {
                lines.add(line);
                if (lines.size() >= serviceProperties.getDataGenerationProperties().getBatchSize()) {
                    mapToEntityAndSave.run();
                    lines.clear();
                }
            }
            if (!lines.isEmpty()) {
                mapToEntityAndSave.run();
            }
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        log.info("Data insert execution time: {} ms", duration);
    }

    private Map<String, String> arrayToMap(String[] keys, String[] values) {
        if (keys.length != values.length) {
            throw new IllegalArgumentException("Keys and values arrays must have the same length.");
        }
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }
}
