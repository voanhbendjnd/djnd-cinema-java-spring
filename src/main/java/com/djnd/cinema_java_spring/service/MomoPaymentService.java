// package com.djnd.cinema_java_spring.service;

// import org.aspectj.weaver.SignatureUtils;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.http.HttpEntity;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate;

// import com.djnd.cinema_java_spring.domain.entity.Booking;
// import com.djnd.cinema_java_spring.domain.enumeration.RoomType;

// import java.math.BigDecimal;
// import java.text.SimpleDateFormat;
// import java.util.Date;
// import java.util.UUID;

// @Service
// public class MomoPaymentService {
// @Value("${momo.partner-code}")
// private String partnerCode;

// @Value("${momo.access-key}")
// private String accessKey;

// @Value("${momo.secret-key}")
// private String secretKey;

// @Value("${momo.endpoint}")
// private String endpoint;

// @Value("${momo.return-url}")
// private String returnUrl;

// @Value("${momo.notify-url}")
// private String notifyUrl;

// @Value("${momo.request-type}")
// private String requestType;

// public String getPartnerCode() {
// return partnerCode;
// }

// public String getAccessKey() {
// return accessKey;
// }

// public String getSecretKey() {
// return secretKey;
// }

// public String getEndpoint() {
// return endpoint;
// }

// public String getReturnUrl() {
// return returnUrl;
// }

// public String getNotifyUrl() {
// return notifyUrl;
// }

// public String getRequestType() {
// return requestType;
// }

// @Bean
// public RestTemplate restTemplate() {
// return new RestTemplate();
// }

// private static final Logger log =
// LoggerFactory.getLogger(MomoPaymentService.class);

// private final SignatureUtils signatureUtils;
// private final RestTemplate restTemplate;
// private final BookingService bookingService;

// public MomoPaymentService(
// SignatureUtils signatureUtils,
// RestTemplate restTemplate,
// BookingService bookingService) {
// this.signatureUtils = signatureUtils;
// this.restTemplate = restTemplate;
// this.bookingService = bookingService;
// }

// public void createPayment(long amount, String orderInfo) {
// String orderId = UUID.randomUUID().toString().replace("-", "").substring(0,
// 20);
// String requestId = orderId;

// SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
// String expireDateStr = sdf.format(new Date(System.currentTimeMillis() + 15 *
// 60 * 1000));

// log.info("=== Bắt đầu tạo thanh toán MoMo ===");
// log.info("OrderId: {}, Amount: {}", orderId, amount);

// String rawSignature = "accessKey=" + accessKey
// + "&amount=" + amount
// + "&extraData="
// + "&ipnUrl=" + notifyUrl
// + "&orderId=" + orderId
// + "&orderInfo=" + orderInfo
// + "&partnerCode=" + partnerCode
// + "&redirectUrl=" + returnUrl
// + "&requestId=" + requestId
// + "&requestType=" + requestType;

// String signature = signatureUtils.generateSignature(rawSignature, secretKey);

// // MomoRequestDTO requestDTO = MomoRequestDTO.builder()
// // .partnerCode(momoConfig.getPartnerCode())
// // .accessKey(momoConfig.getAccessKey())
// // .requestId(requestId)
// // .amount(String.valueOf(amount))
// // .orderId(orderId)
// // .orderInfo(orderInfo)
// // .redirectUrl(momoConfig.getReturnUrl())
// // .ipnUrl(momoConfig.getNotifyUrl())
// // .extraData("")
// // .requestType(momoConfig.getRequestType())
// // .signature(signature)
// // .expireDate(expireDateStr)
// // .build();

// HttpHeaders headers = new HttpHeaders();
// headers.setContentType(MediaType.APPLICATION_JSON);

// // Thêm dòng này để xem JSON thực gửi đi
// log.debug("ipnUrl trong DTO: {}", requestDTO.getIpnUrl());
// log.debug("redirectUrl trong DTO: {}", requestDTO.getRedirectUrl());
// HttpEntity<MomoRequestDTO> entity = new HttpEntity<>(requestDTO, headers);

// try {
// log.info("Gửi request đến MoMo: {}", momoConfig.getEndpoint());
// ResponseEntity<MomoResponseDTO> response = restTemplate.postForEntity(
// momoConfig.getEndpoint(), entity, MomoResponseDTO.class);
// MomoResponseDTO responseDTO = response.getBody();
// log.info("Response từ MoMo: {}", responseDTO);
// if (responseDTO == null)
// throw new RuntimeException("MoMo trả về response rỗng");
// return responseDTO;

// } catch (Exception e) {
// log.error("Lỗi khi gọi MoMo API: {}", e.getMessage(), e);
// throw new RuntimeException("Không thể kết nối MoMo: " + e.getMessage(), e);
// }
// }

// private String nvl(String s) {
// return s == null ? "" : s;
// }

// public boolean verifyReturnSignature(
// String partnerCode, String requestId, String orderId,
// String amount, String orderInfo, String orderType,
// String transId, String message,
// String responseTime, String resultCode, String payType,
// String extraData, String receivedSignature) {

// String rawSignature = "accessKey=" + momoConfig.getAccessKey()
// + "&amount=" + nvl(amount)
// + "&extraData=" + nvl(extraData)
// + "&message=" + nvl(message)
// + "&orderId=" + nvl(orderId)
// + "&orderInfo=" + nvl(orderInfo)
// + "&orderType=" + nvl(orderType)
// + "&partnerCode=" + nvl(partnerCode)
// + "&payType=" + nvl(payType)
// + "&requestId=" + nvl(requestId)
// + "&responseTime=" + nvl(responseTime)
// + "&resultCode=" + nvl(resultCode)
// + "&transId=" + nvl(transId);

// return signatureUtils.verifySignature(rawSignature, receivedSignature,
// momoConfig.getSecretKey());
// }

// public boolean verifyNotifySignature(
// String partnerCode, String requestId, String orderId,
// String amount, String orderInfo, String orderType,
// String transId, String message,
// String responseTime, String resultCode, String payType,
// String extraData, String receivedSignature) {

// String rawSignature = "accessKey=" + momoConfig.getAccessKey()
// + "&amount=" + nvl(amount)
// + "&extraData=" + nvl(extraData)
// + "&message=" + nvl(message)
// + "&orderId=" + nvl(orderId)
// + "&orderInfo=" + nvl(orderInfo)
// + "&orderType=" + nvl(orderType)
// + "&partnerCode=" + nvl(partnerCode)
// + "&payType=" + nvl(payType)
// + "&requestId=" + nvl(requestId)
// + "&responseTime=" + nvl(responseTime)
// + "&resultCode=" + nvl(resultCode)
// + "&transId=" + nvl(transId);

// return signatureUtils.verifySignature(rawSignature, receivedSignature,
// momoConfig.getSecretKey());
// }

// public MomoResponseDTO payForExtendBooking(Long bookingId, Long timetableId)
// throws Exception {
// Booking booking = bookingService.getBookingById(bookingId);
// if (booking == null) {
// throw new Exception("Booking not exist");
// }

// if (timetableId < 1 || timetableId > 4) {
// throw new Exception("Timetable invalid");
// }

// RoomType roomType = booking.getRoom().getRoomType();
// if (roomType == null) {
// throw new Exception("RoomType invalid");
// }
// if (timetableId == 4) {
// return createPayment(roomType.getOverPrice().longValue(),
// booking.getBookingCode());
// }

// return createPayment(roomType.getBasePrice().longValue(),
// booking.getBookingCode());
// }
// }