package com.narendra.handlingdata.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@Entity
@Data
@NoArgsConstructor
@Table(name = "datasets")
public class Dataset{
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(unique = true, nullable = false)
    private String datasetName;
    @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Record> record = new ArrayList<>();

    public Dataset(String datasetName) {
        this.datasetName = datasetName;
    }
}
