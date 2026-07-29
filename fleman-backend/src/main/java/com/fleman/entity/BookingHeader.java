package com.fleman.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// customerId, carId, pickupHubId, dropHubId are all plain logical-reference
// columns — no @ManyToOne, no database foreign keys anywhere in this project.
@Entity
@Table(name = "booking_headers")
public class BookingHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @Column(nullable = false, unique = true)
    private String confirmationNo;

    @Column(nullable = false)
    private LocalDateTime bookingDate;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long carId;

    @Column(nullable = false)
    private Long pickupHubId;

    @Column(nullable = false)
    private Long dropHubId;

    @Column(nullable = false)
    private LocalDateTime pickupDatetime;

    @Column(nullable = false)
    private LocalDateTime returnDatetime;

    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;

    private Double estimatedAmount;

    @Column(length = 255)
    private String remarks;

    public BookingHeader() {}

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getConfirmationNo() { return confirmationNo; }
    public void setConfirmationNo(String confirmationNo) { this.confirmationNo = confirmationNo; }
    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }
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
    public BookingStatus getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(BookingStatus bookingStatus) { this.bookingStatus = bookingStatus; }
    public Double getEstimatedAmount() { return estimatedAmount; }
    public void setEstimatedAmount(Double estimatedAmount) { this.estimatedAmount = estimatedAmount; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public enum BookingStatus { PENDING, CONFIRMED, ONGOING, COMPLETED, CANCELLED }
}
