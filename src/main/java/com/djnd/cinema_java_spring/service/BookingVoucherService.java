package com.djnd.cinema_java_spring.service;

import com.djnd.cinema_java_spring.domain.entity.Booking;
import com.djnd.cinema_java_spring.domain.entity.BookingVoucher;
import com.djnd.cinema_java_spring.domain.entity.Promotion;
import com.djnd.cinema_java_spring.repository.BookingVoucherRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class BookingVoucherService {

    final BookingVoucherRepository bookingVoucherRepository;
    @Transactional
    public void saveBookingVoucher(Booking booking, Promotion voucher,Double discountPercent, BigDecimal discountAmount ,BigDecimal finalTotalAmount) {
        BookingVoucher  bookingVoucher = new BookingVoucher();
        bookingVoucher.setBooking(booking);
        bookingVoucher.setDiscountAmount(discountAmount);
        bookingVoucher.setDiscountPercentage(discountPercent);
        bookingVoucher.setFinalAmount(finalTotalAmount);
        bookingVoucher.setVoucher(voucher);
        bookingVoucherRepository.save(bookingVoucher);
    }
}
