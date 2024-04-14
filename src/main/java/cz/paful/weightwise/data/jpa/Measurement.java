package cz.paful.weightwise.data.jpa;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table
public class Measurement {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE)
    private Long id;
    @ManyToOne
    @JoinColumn(name="user_weight_id", nullable=false)
    private UserWeight userWeight;
    private LocalDate date;
    private LocalTime time;
    private float weightKg;
    private float bmi;
    private float bodyFatProcentage;
    private float waterProcentage;
    private float muscleMassProcentage;
    private float bonesKg;
    private String comment;
    private String medication;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public float getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(float weightKG) {
        this.weightKg = weightKG;
    }

    public float getBmi() {
        return bmi;
    }

    public void setBmi(float bmi) {
        this.bmi = bmi;
    }

    public float getBodyFatProcentage() {
        return bodyFatProcentage;
    }

    public void setBodyFatProcentage(float bodyFatProcentage) {
        this.bodyFatProcentage = bodyFatProcentage;
    }

    public float getWaterProcentage() {
        return waterProcentage;
    }

    public void setWaterProcentage(float waterProcentage) {
        this.waterProcentage = waterProcentage;
    }

    public float getMuscleMassProcentage() {
        return muscleMassProcentage;
    }

    public void setMuscleMassProcentage(float muscleMassProcentage) {
        this.muscleMassProcentage = muscleMassProcentage;
    }

    public float getBonesKg() {
        return bonesKg;
    }

    public void setBonesKg(float bonesKG) {
        this.bonesKg = bonesKG;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getMedication() {
        return medication;
    }

    public void setMedication(String medication) {
        this.medication = medication;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public UserWeight getUserWeight() {
        return userWeight;
    }

    public void setUserWeight(UserWeight userWeight) {
        this.userWeight = userWeight;
    }
}
