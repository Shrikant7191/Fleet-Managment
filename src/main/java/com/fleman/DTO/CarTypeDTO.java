package com.fleman.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CarTypeDTO {

    private Long carTypeId;
    private String carTypeName;
    private BigDecimal dailyRate;
    private BigDecimal wklyRate;
    private BigDecimal mnthRate;
    private LocalDate rateValidFrom;
    private LocalDate rateValidTo;
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