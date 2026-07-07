// package com.djnd.cinema_java_spring.service.consumer;

// import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.stereotype.Service;

// import com.djnd.cinema_java_spring.config.GroupConstants;
// import com.djnd.cinema_java_spring.service.MailService;
// import com.djnd.cinema_java_spring.service.dto.TicketMailEvent;
// import com.djnd.cinema_java_spring.service.producer.TicketEventProducer;

// import lombok.AccessLevel;
// import lombok.RequiredArgsConstructor;
// import lombok.experimental.FieldDefaults;

// @Service
// @RequiredArgsConstructor
// @FieldDefaults(level = AccessLevel.PRIVATE)

// public class TicketEventConsumer {
// final MailService mailService;

// @KafkaListener(topics = TicketEventProducer.TICKET_SEND_MAIL_TOPIC, groupId =
// GroupConstants.GROUP_MAIL)
// public void consumeTicketMail(TicketMailEvent event) {
// mailService.sendTicketMailBooking(event);
// }
// }
