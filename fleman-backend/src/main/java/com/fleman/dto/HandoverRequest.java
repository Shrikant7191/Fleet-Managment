package com.fleman.dto;

public class HandoverRequest {
    private Integer fuelStatus;
    private String notes;

    public HandoverRequest() {}

    public Integer getFuelStatus() { return fuelStatus; }
    public void setFuelStatus(Integer fuelStatus) { this.fuelStatus = fuelStatus; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
