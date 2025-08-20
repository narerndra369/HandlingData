package com.narendra.handlingdata.repository;

import com.narendra.handlingdata.model.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.narendra.handlingdata.model.Record;
@Repository
public interface RecordRepository extends JpaRepository<Record, Long> {

    List<Record> findByDataset(Dataset dataset);

    List<Record> findByDataset(Dataset dataset, Sort sort);
}