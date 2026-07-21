package com.djnd.cinema_java_spring.service;

import com.djnd.cinema_java_spring.service.dto.SeatMaintenanceMailDTO;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;
import java.util.Locale;
@Service
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j

public class NotificationAsyncService {
    final MailService mailService;
    final JavaMailSender javaMailSender;
    final MessageSource messageSource;
    final SpringTemplateEngine springTemplateEngine;
    @Transactional
    @Async("notificationSeatMaintenanceExecutor")
    public void sendMailSeatMaintenanceForCustomer(List<SeatMaintenanceMailDTO> infoSeatMaintenanceAndEmailCustomers){
        infoSeatMaintenanceAndEmailCustomers.forEach(seatMaintenanceMailDTO -> {
            try{
                this.sendMailSeatMaintenanceForCustomerAlreadyHasTicket(seatMaintenanceMailDTO);
            }
            catch (Exception e){
                log.error(e.getMessage());
            }
        });
    }

    private void sendMailSeatMaintenanceForCustomerAlreadyHasTicket(SeatMaintenanceMailDTO seatMaintenanceMailDTO) {
        Locale locale = Locale.forLanguageTag("en");
        Context context = new Context(locale);
        context.setVariable("movieName", seatMaintenanceMailDTO.getMovieTitle());
        context.setVariable("startTime", seatMaintenanceMailDTO.getStartDateTime());
        context.setVariable("positionSeatMaintenance", seatMaintenanceMailDTO.getPositionSeatMaintenance());
        String content = springTemplateEngine.process("mail/seatMaintenanceEmail", context);
        String subject = messageSource.getMessage("email.seat.maintenance.notice", null, locale);
        mailService.sendEmailSync(seatMaintenanceMailDTO.getEmailCustomerImpact(), subject, content, false, true);
    }
}
