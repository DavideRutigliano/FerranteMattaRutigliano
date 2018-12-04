package com.github.ferrantemattarutigliano.software.server.model.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ferrantemattarutigliano.software.server.model.entity.Individual;

import javax.validation.constraints.NotNull;
import java.util.Date;

public class HealthDataDTO {
    @NotNull
    private long id;

    @NotNull
    private String name;

    @NotNull
    private String value;

    @NotNull
    private Date timestamp;


    @DTO(IndividualDTO.class)
    private Individual individual;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Individual getIndividual() {
        return individual;
    }

    public void setIndividual(Individual individual) {
        this.individual = individual;
    }
}
