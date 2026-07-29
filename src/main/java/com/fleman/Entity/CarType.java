package com.fleman.Entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "car_type_master")
public class CarType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cartype_id")
    private Long carTypeId;

    @Column(name = "car_type_name", nullable = false, unique = true)
    private String carTypeName;

    @Column(name = "daily_rate", nullable = false)
    private BigDecimal dailyRate;

    @Column(name = "wkly_rate")
    private BigDecimal wklyRate;

    @Column(name = "mnth_rate")
    private BigDecimal mnthRate;

    @Column(name = "rate_valid_from")
    private LocalDate rateValidFrom;

    @Column(name = "rate_valid_to")
    private LocalDate rateValidTo;

    @Column(name = "image_path")
    private String imagePath;

    // Getters and setters
    public Long getCarTypeId() { return carTypeId; }
    public void setCarTypeId(Long carTypeId) { this.carTypeId = carTypeId; }
    public String getCarTypeName() { return carTypeName; }
    public void setCarTypeName(String carTypeName) { this.carTypeName = carTypeName; }
    public BigDecimal getDailyRate() { return dailyRate; }
    public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }
    public BigDecimal getWklyRate() { return wklyRate; }
    public void setWklyRate(BigDecimal wklyRate) { this.wklyRate = wklyRate; }
    public BigDecimal getMnthRate() { return mnthRate; }
    public void setMnthRate(BigDecimal mnthRate) { this.mnthRate = mnthRate; }
    public LocalDate getRateValidFrom() { return rateValidFrom; }
    public void setRateValidFrom(LocalDate rateValidFrom) { this.rateValidFrom = rateValidFrom; }
    public LocalDate getRateValidTo() { return rateValidTo; }
    public void setRateValidTo(LocalDate rateValidTo) { this.rateValidTo = rateValidTo; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}