package com.fleman.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

// Matches ConfirmPage.jsx's createBooking(...) payload field-for-field —
// the frontend does not send prices for add-ons, only addonId + quantity;
// the server looks up the current rate and computes every subtotal itself.
public class CreateBookingRequest {
    @NotNull(message = "customerId is required")
    private Long customerId;

    @NotNull(message = "carId is required")
    private Long carId;

    @NotNull(message = "pickupHubId is required")
    private Long pickupHubId;

    private Long dropHubId;

    @NotNull(message = "pickupDatetime is required")
    private LocalDateTime pickupDatetime;

    @NotNull(message = "returnDatetime is required")
    private LocalDateTime returnDatetime;

    private List<AddonLineRequest> addons;

    // Sent by the client for display purposes only — the server recomputes
    // and trusts its own total, never this value, when persisting.
    private Double estimatedAmount;

    private String remarks;

    public CreateBookingRequest() {}

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getCarId() { return carId; }
    public void setCarId(Long carId) { this.carId = carId; }
    public Long getPickupHubId() { return pickupHubId; }
    public void setPickupHubId(Long pickupHubId) { this.pickupHubId = pickupHubId; }
    public Long getDropHubId() { return dropHubId; }
    public void setDropHubId(Long dropHubId) { this.dropHubId = dropHubId; }
    public LocalDateTime getPickupDatetime() { return pickupDatetime; }
    public void setPickupDatetime(LocalDateTime pickupDatetime) { this.pickupDatetime = pickupDatetime; }
    public LocalDateTime getReturnDatetime() { return returnDatetime; }
    public void setReturnDatetime(LocalDateTime returnDatetime) { this.returnDatetime = returnDatetime; }
    public List<AddonLineRequest> getAddons() { return addons; }
    public void setAddons(List<AddonLineRequest> addons) { this.addons = addons; }
    public Double getEstimatedAmount() { return estimatedAmount; }
    public void setEstimatedAmount(Double estimatedAmount) { this.estimatedAmount = estimatedAmount; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
