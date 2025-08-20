package com.narendra.handlingdata.controller;

import com.narendra.handlingdata.model.Dataset;
import com.narendra.handlingdata.model.Record;
import com.narendra.handlingdata.service.DatasetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dataset")
@RequiredArgsConstructor
public class DatasetController {
    private final DatasetService datasetService;

    @PostMapping("/{datasetName}/record")
    public ResponseEntity<Map<String, Object>> addRecord(@PathVariable String datasetName, @RequestBody Map<String, Object> data){
        Record savedRecord = datasetService.insertRecord(datasetName, data);
        Map<String, Object> response = Map.of(
                "message", "Record added successfully",
                "dataset", savedRecord.getDataset().getDatasetName(),
                "recordId", savedRecord.getId()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @GetMapping("/{datasetName}/query")
    public ResponseEntity<?> queryDataset(
            @PathVariable String datasetName,
            @RequestParam(required = false) String groupBy,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String order) {

        if (groupBy != null) {
            Map<String, List<Record>> groupedData = datasetService.getGroupedRecords(datasetName, groupBy);
            return ResponseEntity.ok(Map.of("groupedRecords", groupedData));
        }

        if (sortBy != null) {
            List<Record> sortedData = datasetService.getSortedRecords(datasetName, sortBy, order);
            return ResponseEntity.ok(Map.of("sortedRecords", sortedData));
        }

        return ResponseEntity.badRequest().body(Map.of("error", "Either 'groupBy' or 'sortBy' query parameter must be provided."));
    }
}
