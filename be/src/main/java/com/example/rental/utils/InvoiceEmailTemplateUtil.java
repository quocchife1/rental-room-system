package com.example.rental.utils;

import com.example.rental.entity.Invoice;
import com.example.rental.entity.Tenant;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class InvoiceEmailTemplateUtil {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getInstance(new Locale("vi", "VN"));

    public static String buildReminderEmail(Invoice invoice, Tenant tenant) {
        return """
                <html><body>
                <p>Xin chào <b>%s</b>,</p>
                <p>Bạn có một hóa đơn <b>#%d</b> cho hợp đồng <b>#%d</b>.</p>
                <p><b>Số tiền:</b> %s VNĐ</p>
                <p><b>Ngày đến hạn:</b> %s</p>
                <p>Vui lòng thanh toán trước hạn để tránh gián đoạn dịch vụ.</p>
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
                <p>Vui lòng thanh toán ngay để tránh phí phạt và gián đoạn dịch vụ.</p>
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
                <p>Bạn đã thanh toán thành công hóa đơn <b>#%d</b>.</p>
                <p><b>Số tiền:</b> %s VNĐ</p>
                <p><b>Ngày thanh toán:</b> %s</p>
                <p>Cảm ơn bạn đã sử dụng dịch vụ.</p>
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
