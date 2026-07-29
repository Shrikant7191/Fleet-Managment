package com.fleman.dto;

import java.time.LocalDate;

public class CarTypeDTO {
    private Long carTypeId;
    private String carTypeName;
    private Double dailyRate;
    private Double weeklyRate;
    private Double monthlyRate;
    private LocalDate rateValidFrom;
    private LocalDate rateValidTo;
    private String imagePath;

    public CarTypeDTO() {}

    public Long getCarTypeId() { return carTypeId; }
    public void setCarTypeId(Long carTypeId) { this.carTypeId = carTypeId; }
    public String getCarTypeName() { return carTypeName; }
    public void setCarTypeName(String carTypeName) { this.carTypeName = carTypeName; }
    public Double getDailyRate() { return dailyRate; }
    public void setDailyRate(Double dailyRate) { this.dailyRate = dailyRate; }
    public Double getWeeklyRate() { return weeklyRate; }
    public void setWeeklyRate(Double weeklyRate) { this.weeklyRate = weeklyRate; }
    public Double getMonthlyRate() { return monthlyRate; }
    public void setMonthlyRate(Double monthlyRate) { this.monthlyRate = monthlyRate; }
    public LocalDate getRateValidFrom() { return rateValidFrom; }
    public void setRateValidFrom(LocalDate rateValidFrom) { this.rateValidFrom = rateValidFrom; }
    public LocalDate getRateValidTo() { return rateValidTo; }
    public void setRateValidTo(LocalDate rateValidTo) { this.rateValidTo = rateValidTo; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}
