package com.djnd.cinema_java_spring.domain.enumeration;

public enum ShowtimeStatus {
    PLANNING, // 1. Đang lên kế hoạch (Mới tạo nháp, chưa mở bán vé ngoài Frontend)
    ACTIVE, // 2. Đang mở bán (Khách hàng trên bắt đầu thấy và đặt vé được)
    CANCELLED, // 3. Đã hủy (Ví dụ: Phòng chiếu bị sự cố kỹ thuật, phải hủy suất và hoàn tiền)
    COMPLETED // 4. Đã chiếu xong (Hệ thống tự động chuyển trạng thái khi hết giờ để đóng sổ)
}
