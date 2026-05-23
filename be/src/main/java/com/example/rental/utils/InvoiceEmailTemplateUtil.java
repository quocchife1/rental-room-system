package com.example.rental.utils;

import com.example.rental.entity.Invoice;
import com.example.rental.entity.Tenant;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class InvoiceEmailTemplateUtil {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getInstance(Locale.of("vi", "VN"));

    public static String buildSettlementInvoiceEmail(Invoice invoice, Tenant tenant) {
        return """
                <html><body>
                <p>Xin chào <b>%s</b>,</p>
                <p>Biên bản kiểm tra phòng đã được hoàn tất và hệ thống đã tạo hóa đơn tất toán <b>#%d</b> cho hợp đồng <b>#%d</b>.</p>
                <p><b>Số tiền cần thanh toán:</b> %s VNĐ</p>
                <p><b>Hạn thanh toán:</b> %s</p>
                <p>Quý khách vui lòng hoàn tất thanh toán trong vòng 3 ngày. Sau khi thanh toán thành công, hợp đồng sẽ tự động kết thúc và chúng tôi sẽ gửi email xác nhận ngay.</p>
                <p>Xin cảm ơn Quý khách đã đồng hành cùng chúng tôi. Sự tin tưởng của Quý khách là điều chúng tôi luôn trân trọng.</p>
                </body></html>
                """.formatted(
                tenant.getFullName(),
                invoice.getId(),
                invoice.getContract().getId(),
                CURRENCY_FORMAT.format(invoice.getAmount()),
                invoice.getDueDate().format(DATE_FORMAT)
        );
    }

    public static String buildReminderEmail(Invoice invoice, Tenant tenant) {
        return """
                <html><body>
                <p>Xin chào <b>%s</b>,</p>
                <p>Đây là email nhắc thanh toán cho hóa đơn <b>#%d</b> của hợp đồng <b>#%d</b>.</p>
                <p><b>Số tiền:</b> %s VNĐ</p>
                <p><b>Ngày đến hạn:</b> %s</p>
                <p>Hạn thanh toán của bạn còn rất gần. Nếu có thể, vui lòng hoàn tất trong hôm nay để tránh phát sinh trạng thái quá hạn.</p>
                <p>Chúng tôi cảm ơn bạn đã hợp tác và luôn sẵn sàng hỗ trợ khi bạn cần.</p>
                </body></html>
                """.formatted(
                tenant.getFullName(),
                invoice.getId(),
                invoice.getContract().getId(),
                CURRENCY_FORMAT.format(invoice.getAmount()),
                invoice.getDueDate().format(DATE_FORMAT)
        );
    }

    public static String buildOverdueEmail(Invoice invoice, Tenant tenant) {
        return """
                <html><body>
                <p>Xin chào <b>%s</b>,</p>
                <p>Hóa đơn <b>#%d</b> của bạn đã <span style='color:red'><b>QUÁ HẠN</b></span>.</p>
                <p><b>Số tiền cần thanh toán:</b> %s VNĐ</p>
                <p><b>Ngày đến hạn:</b> %s</p>
                <p>Vui lòng thanh toán sớm nhất có thể để tránh ảnh hưởng đến quá trình bàn giao phòng và các phát sinh không mong muốn.</p>
                <p>Xin cảm ơn bạn đã phối hợp.</p>
                </body></html>
                """.formatted(
                tenant.getFullName(),
                invoice.getId(),
                CURRENCY_FORMAT.format(invoice.getAmount()),
                invoice.getDueDate().format(DATE_FORMAT)
        );
    }

    public static String buildPaymentSuccessEmail(Invoice invoice, Tenant tenant) {
        return """
                <html><body>
                <p>Xin chào <b>%s</b>,</p>
                <p>Hệ thống đã ghi nhận bạn thanh toán thành công hóa đơn <b>#%d</b>.</p>
                <p><b>Số tiền:</b> %s VNĐ</p>
                <p><b>Ngày thanh toán:</b> %s</p>
                <p>Chúng tôi trân trọng cảm ơn bạn đã hoàn tất nghĩa vụ thanh toán đúng hạn.</p>
                <p>Rất vui được đồng hành cùng bạn trong suốt thời gian sử dụng dịch vụ. Chúc bạn thật nhiều sức khỏe và thuận lợi.</p>
                </body></html>
                """.formatted(
                tenant.getFullName(),
                invoice.getId(),
                CURRENCY_FORMAT.format(invoice.getAmount()),
                invoice.getPaidDate() != null ? invoice.getPaidDate().format(DATE_FORMAT) : "Hôm nay"
        );
    }

    public static String buildNewInvoiceEmail(Invoice invoice, Tenant tenant) {
        return """
                <html><body>
                <p>Xin chào <b>%s</b>,</p>
                <p>Bạn có một hóa đơn mới <b>#%d</b> cho hợp đồng <b>#%d</b>.</p>
                <p><b>Số tiền:</b> %s VNĐ</p>
                <p><b>Ngày đến hạn:</b> %s</p>
                <p>Vui lòng thanh toán đúng hạn để đảm bảo dịch vụ liên tục.</p>
                </body></html>
                """.formatted(
                tenant.getFullName(),
                invoice.getId(),
                invoice.getContract().getId(),
                CURRENCY_FORMAT.format(invoice.getAmount()),
                invoice.getDueDate().format(DATE_FORMAT)
        );
    }
}
