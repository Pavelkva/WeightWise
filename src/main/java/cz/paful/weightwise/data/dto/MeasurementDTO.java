package cz.paful.weightwise.data.dto;

import cz.paful.weightwise.data.jpa.Measurement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;

public class MeasurementDTO implements Comparable<MeasurementDTO> {

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

    public MeasurementDTO() {
    }

    public MeasurementDTO(Measurement measurement) {
        setDate(measurement.getDate());
        setTime(measurement.getTime());
        setWeightKg(measurement.getWeightKg());
        setBmi(measurement.getBmi());
        setBodyFatProcentage(measurement.getBodyFatProcentage());
        setWaterProcentage(measurement.getWaterProcentage());
        setMuscleMassProcentage(measurement.getMuscleMassProcentage());
        setBonesKg(measurement.getBonesKg());
        setComment(measurement.getComment());
        setMedication(measurement.getMedication());
    }

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

    public void setWeightKg(float weightKg) {
        this.weightKg = weightKg;
    }

    public float getBmi() {
        return bmi;
    }

    public void setBmi(float bmi) {
        this.bmi = bmi;
    }

    public float getMuscleMassProcentage() {
        return muscleMassProcentage;
    }

    public void setMuscleMassProcentage(float muscleMassProcentage) {
        this.muscleMassProcentage = muscleMassProcentage;
    }

    public float getWaterProcentage() {
        return waterProcentage;
    }

    public void setWaterProcentage(float waterProcentage) {
        this.waterProcentage = waterProcentage;
    }

    public float getBodyFatProcentage() {
        return bodyFatProcentage;
    }

    public void setBodyFatProcentage(float bodyFatProcentage) {
        this.bodyFatProcentage = bodyFatProcentage;
    }

    public float getBonesKg() {
        return bonesKg;
    }

    public void setBonesKg(float bonesKg) {
        this.bonesKg = bonesKg;
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

    public float getMuscleMassKG() {
        return round(weightKg * (muscleMassProcentage / 100));
    }

    public float getBodyFatKG() {
        return round(weightKg * (bodyFatProcentage / 100));
    }

    public float getWaterKG() {
        return round(weightKg * (waterProcentage / 100));
    }

    private float round(float number) {
        BigDecimal bd = new BigDecimal(Float.toString(number));
        return bd.setScale(2, RoundingMode.HALF_UP).floatValue();
    }

    public Measurement getMeasurement() {
        Measurement measurement = new Measurement();
        measurement.setDate(getDate());
        measurement.setTime(getTime());
        measurement.setWeightKg(getWeightKg());
        measurement.setBmi(getBmi());
        measurement.setBodyFatProcentage(getBodyFatProcentage());
        measurement.setWaterProcentage(getWaterProcentage());
        measurement.setMuscleMassProcentage(getMuscleMassProcentage());
        measurement.setBonesKg(getBonesKg());
        measurement.setComment(getComment());
        measurement.setMedication(getMedication());

        return measurement;
    }

    @Override
    public int compareTo(MeasurementDTO o) {
        return getDate().compareTo(o.getDate());
    }
}
