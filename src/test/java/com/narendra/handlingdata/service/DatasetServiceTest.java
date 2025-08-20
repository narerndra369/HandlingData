package com.narendra.handlingdata.service;

import com.narendra.handlingdata.model.Dataset;
import com.narendra.handlingdata.model.Record;
import com.narendra.handlingdata.repository.DatasetRepository;
import com.narendra.handlingdata.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatasetServiceTest {

    @Mock
    private DatasetRepository datasetRepository;

    @Mock
    private RecordRepository recordRepository;

    @InjectMocks
    private DatasetService datasetService;

    private Dataset mockDataset;

    @BeforeEach
    void setUp() {
        mockDataset = new Dataset("TestDataset");
        mockDataset.setId(1L);
    }

    @Test
    void insertRecord_shouldCreateRecordAndUseExistingDataset_whenDatasetExists() {
        String datasetName = "ExistingDataset";
        Map<String, Object> data = Map.of("name", "Jane Doe", "age", 30, "department", "HR");

        Dataset existingDataset = new Dataset(datasetName);
        Record recordToSave = new Record();
        recordToSave.setName("Jane Doe");
        recordToSave.setAge(30);
        recordToSave.setDepartment("HR");
        recordToSave.setDataset(existingDataset);

        when(datasetRepository.findByDatasetName(datasetName)).thenReturn(Optional.of(existingDataset));
        when(recordRepository.save(any(Record.class))).thenReturn(recordToSave);

        Record result = datasetService.insertRecord(datasetName, data);
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Jane Doe");
        assertThat(result.getDataset().getDatasetName()).isEqualTo(datasetName);

        verify(datasetRepository, times(1)).findByDatasetName(datasetName);
        verify(datasetRepository, never()).save(any(Dataset.class)); // Crucial check
        verify(recordRepository, times(1)).save(any(Record.class));
    }

    @Test
    void insertRecord_shouldCreateRecordAndNewDataset_whenDatasetDoesNotExist() {
        // ARRANGE
        String datasetName = "NewDataset";
        Map<String, Object> data = Map.of("name", "John Smith", "age", 45, "department", "Engineering");

        Dataset newDataset = new Dataset(datasetName);
        Record recordToSave = new Record(); // The object created inside the method
        recordToSave.setName("John Smith");
        recordToSave.setDataset(newDataset);

        when(datasetRepository.findByDatasetName(datasetName)).thenReturn(Optional.empty());
        when(datasetRepository.save(any(Dataset.class))).thenReturn(newDataset);
        when(recordRepository.save(any(Record.class))).thenReturn(recordToSave);


        Record result = datasetService.insertRecord(datasetName, data);


        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("John Smith");
        assertThat(result.getDataset().getDatasetName()).isEqualTo(datasetName);


        ArgumentCaptor<Dataset> datasetCaptor = ArgumentCaptor.forClass(Dataset.class);


        verify(datasetRepository, times(1)).findByDatasetName(datasetName);
        verify(datasetRepository, times(1)).save(datasetCaptor.capture()); // Crucial check
        verify(recordRepository, times(1)).save(any(Record.class));

        assertThat(datasetCaptor.getValue().getDatasetName()).isEqualTo(datasetName);
    }

    //--- Tests for getSortedRecords ---

    @Test
    void getSortedRecords_shouldReturnSortedRecords_whenDatasetExists() {
        // ARRANGE
        when(datasetRepository.findByDatasetName("TestDataset")).thenReturn(Optional.of(mockDataset));

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);

        // ACT
        datasetService.getSortedRecords("TestDataset", "age", "desc");

        // ASSERT
        verify(recordRepository).findByDataset(eq(mockDataset), sortCaptor.capture());
        Sort capturedSort = sortCaptor.getValue();

        assertThat(capturedSort.getOrderFor("age").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getSortedRecords_shouldThrowException_whenDatasetDoesNotExist() {
        // ARRANGE
        when(datasetRepository.findByDatasetName("NonExistent")).thenReturn(Optional.empty());

        // ACT & ASSERT
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            datasetService.getSortedRecords("NonExistent", "name", "asc");
        });

        assertThat(exception.getMessage()).isEqualTo("Dataset not found: NonExistent");
        verify(recordRepository, never()).findByDataset(any(), any());
    }

    //--- Tests for getGroupedRecords ---

    @Test
    void getGroupedRecords_shouldReturnGroupedData_whenGroupingByValidField() {
        // ARRANGE
        Record engRecord1 = new Record("Narendra", 30, "Engineering", mockDataset);
        Record engRecord2 = new Record("Raj", 32, "Engineering", mockDataset);
        Record hrRecord = new Record("Sita", 28, "HR", mockDataset);
        List<Record> allRecords = List.of(engRecord1, engRecord2, hrRecord);

        when(datasetRepository.findByDatasetName("TestDataset")).thenReturn(Optional.of(mockDataset));
        when(recordRepository.findByDataset(mockDataset)).thenReturn(allRecords);

        // ACT
        Map<String, List<Record>> result = datasetService.getGroupedRecords("TestDataset", "department");

        // ASSERT
        assertThat(result).hasSize(2);
        assertThat(result.get("Engineering")).hasSize(2).containsExactlyInAnyOrder(engRecord1, engRecord2);
        assertThat(result.get("HR")).hasSize(1).containsExactly(hrRecord);
    }

    @Test
    void getGroupedRecords_shouldThrowException_whenGroupingByInvalidField() {
        // ARRANGE
        String invalidGroupBy = "location";

        // ACT & ASSERT
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            datasetService.getGroupedRecords("TestDataset", invalidGroupBy);
        });

        assertThat(exception.getMessage()).isEqualTo("Can only group by 'name', 'age', or 'department'");
        // Ensure we fail fast before hitting the database
        verify(datasetRepository, never()).findByDatasetName(any());
    }

    @Test
    void getGroupedRecords_shouldThrowException_whenDatasetDoesNotExist() {
        // ARRANGE
        when(datasetRepository.findByDatasetName("NonExistent")).thenReturn(Optional.empty());

        // ACT & ASSERT
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            datasetService.getGroupedRecords("NonExistent", "name");
        });

        assertThat(exception.getMessage()).isEqualTo("Dataset not found: NonExistent");
    }
}