// package com.djnd.cinema_java_spring.service.producer;

// import org.springframework.kafka.core.KafkaTemplate;
// import org.springframework.stereotype.Service;

// import com.djnd.cinema_java_spring.service.dto.TicketMailEvent;

// import lombok.AccessLevel;
// import lombok.RequiredArgsConstructor;
// import lombok.experimental.FieldDefaults;

// @Service
// @RequiredArgsConstructor
// @FieldDefaults(level = AccessLevel.PRIVATE)
// public class TicketEventProducer {
// final KafkaTemplate<String, Object> kafkaTemplate;
// public static final String TICKET_SEND_MAIL_TOPIC = "cinema-ticket-mail";

// public void sendTicketMailEvent(TicketMailEvent event) {
// kafkaTemplate.send(TICKET_SEND_MAIL_TOPIC, event);
// }
// }
