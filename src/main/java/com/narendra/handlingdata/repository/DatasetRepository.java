package com.narendra.handlingdata.repository;

import com.narendra.handlingdata.model.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DatasetRepository extends JpaRepository<Dataset, Integer> {
    Optional<Dataset> findByDatasetName(String datasetName);
}
