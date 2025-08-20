package com.narendra.handlingdata.service;

import com.narendra.handlingdata.model.Dataset;
import com.narendra.handlingdata.model.Record;
import com.narendra.handlingdata.repository.DatasetRepository;
import com.narendra.handlingdata.repository.RecordRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DatasetService {
    private final DatasetRepository datasetRepository;
    private final RecordRepository recordRepository;

    @Transactional
    public Record insertRecord(String datasetName, Map<String, Object> data) {
        Dataset dataset = datasetRepository.findByDatasetName(datasetName)
                .orElseGet(() -> datasetRepository.save(new Dataset(datasetName)));
        Record record = new Record();
        record.setName((String)data.get("name"));
        record.setAge((Integer)data.get("age"));
        record.setDepartment((String)data.get("department"));
        record.setDataset(dataset);
        return recordRepository.save(record);
    }

    private Dataset getDatasetByName(String datasetName) {
        return datasetRepository.findByDatasetName(datasetName)
                .orElseThrow(() -> new IllegalArgumentException("Dataset not found: " + datasetName));
    }

    public List<Record> getSortedRecords(String datasetName,String sortBy,String order){
        Dataset dataset = getDatasetByName(datasetName);
        Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);
        return recordRepository.findByDataset(dataset, sort);
    }
    public Map<String, List<Record>> getGroupedRecords(String datasetName, String groupBy) {
        // For this model, we can only group by 'name', 'age', or 'department'
        if (!List.of("name", "age", "department").contains(groupBy)) {
            throw new IllegalArgumentException("Can only group by 'name', 'age', or 'department'");
        }
        Dataset dataset = getDatasetByName(datasetName);
        List<Record> records = recordRepository.findByDataset(dataset);

        return records.stream().collect(Collectors.groupingBy(record -> {
            return switch (groupBy) {
                case "name" -> record.getName();
                case "age" -> String.valueOf(record.getAge());
                case "department" -> record.getDepartment();
                default -> "";
            };
        }));
    }
}
