package com.fleman.repository;

import com.fleman.entity.BookingHeader;
import com.fleman.entity.BookingHeader.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingHeaderRepository extends JpaRepository<BookingHeader, Long> {

    Optional<BookingHeader> findByConfirmationNoIgnoreCase(String confirmationNo);

    List<BookingHeader> findByCustomerIdAndBookingStatusNotOrderByBookingDateDesc(
            Long customerId, BookingStatus excludedStatus);
}
