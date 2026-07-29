package com.fleman.service;

import com.fleman.dto.BookingResponseDTO;
import com.fleman.dto.CarTypeAvailabilityDTO;
import com.fleman.dto.HandoverRequest;
import com.fleman.dto.InvoiceResponseDTO;
import com.fleman.dto.ProcessReturnRequest;

import java.util.List;

public interface StaffService {
    BookingResponseDTO handoverVehicle(String confirmationNo, HandoverRequest request);
    InvoiceResponseDTO processReturn(String confirmationNo, ProcessReturnRequest request);
    List<BookingResponseDTO> getAllBookings();
    List<CarTypeAvailabilityDTO> getDashboard(Long hubId);
}
