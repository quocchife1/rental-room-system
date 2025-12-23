package com.example.rental.utils;

import com.example.rental.entity.Contract;
import com.example.rental.entity.PaymentMethod;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class DepositDocxGenerator {

    private static final String OUTPUT_DIR = "uploads/deposit_docs/";

    public static class Result {
        private final String invoicePath;
        private final String receiptPath;

        public Result(String invoicePath, String receiptPath) {
            this.invoicePath = invoicePath;
            this.receiptPath = receiptPath;
        }

        public String getInvoicePath() {
            return invoicePath;
        }

        public String getReceiptPath() {
            return receiptPath;
        }
    }

    public static class TransferInfo {
        private final String receiverName;
        private final String receiverPhone;
        private final String receiverQrUrl;

        public TransferInfo(String receiverName, String receiverPhone, String receiverQrUrl) {
            this.receiverName = receiverName;
            this.receiverPhone = receiverPhone;
            this.receiverQrUrl = receiverQrUrl;
        }

        public String getReceiverName() {
            return receiverName;
        }

        public String getReceiverPhone() {
            return receiverPhone;
        }

        public String getReceiverQrUrl() {
            return receiverQrUrl;
        }
    }

    public Result generate(Contract contract, BigDecimal amount, PaymentMethod method, String reference, TransferInfo transferInfo) throws IOException {
        Path outputDir = Paths.get(OUTPUT_DIR);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        String invoiceName = "deposit_invoice_contract_" + contract.getId() + ".docx";
        String receiptName = "deposit_receipt_contract_" + contract.getId() + ".docx";

        Path invoiceFile = outputDir.resolve(invoiceName);
        Path receiptFile = outputDir.resolve(receiptName);

        writeInvoice(contract, amount, method, reference, transferInfo, invoiceFile);
        writeReceipt(contract, amount, method, reference, transferInfo, receiptFile);

        return new Result("/" + OUTPUT_DIR + invoiceName, "/" + OUTPUT_DIR + receiptName);
    }

    private void writeInvoice(Contract contract, BigDecimal amount, PaymentMethod method, String reference, TransferInfo transferInfo, Path filePath) throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            title(doc, "HÓA ĐƠN TIỀN CỌC");

            bodyLine(doc, "Mã hợp đồng", String.valueOf(contract.getId()));
            bodyLine(doc, "Chi nhánh", safe(contract.getBranchCode()));
            bodyLine(doc, "Phòng", safe(contract.getRoomNumber()));
            bodyLine(doc, "Người thuê", safe(contract.getTenant() != null ? contract.getTenant().getFullName() : null));
            bodyLine(doc, "Email", safe(contract.getTenant() != null ? contract.getTenant().getEmail() : null));
            bodyLine(doc, "Số điện thoại", safe(contract.getTenant() != null ? contract.getTenant().getPhoneNumber() : null));

            bodyLine(doc, "Số tiền cọc", formatMoney(amount));
            bodyLine(doc, "Phương thức", method != null ? method.name() : "");
            bodyLine(doc, "Mã tham chiếu", safe(reference));
            bodyLine(doc, "Ngày thanh toán", nowVi());

            if (method == PaymentMethod.BANK_TRANSFER) {
                spacer(doc);
                paragraph(doc, "Thông tin chuyển khoản (MoMo):");
                bodyLine(doc, "Tên người nhận", safe(transferInfo != null ? transferInfo.getReceiverName() : null));
                bodyLine(doc, "Số điện thoại", safe(transferInfo != null ? transferInfo.getReceiverPhone() : null));
                if (transferInfo != null && transferInfo.getReceiverQrUrl() != null && !transferInfo.getReceiverQrUrl().isBlank()) {
                    bodyLine(doc, "QR", transferInfo.getReceiverQrUrl());
                }
                paragraph(doc, "Nội dung chuyển khoản nên ghi rõ mã hợp đồng hoặc mã tham chiếu.");
            }

            spacer(doc);
            paragraph(doc, "Xác nhận nhân viên thu/nhận tiền cọc.");

            try (FileOutputStream out = new FileOutputStream(filePath.toFile())) {
                doc.write(out);
            }
        }
    }

    private void writeReceipt(Contract contract, BigDecimal amount, PaymentMethod method, String reference, TransferInfo transferInfo, Path filePath) throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            title(doc, "BIÊN BẢN NHẬN TIỀN CỌC");

            bodyLine(doc, "Mã hợp đồng", String.valueOf(contract.getId()));
            bodyLine(doc, "Chi nhánh", safe(contract.getBranchCode()));
            bodyLine(doc, "Phòng", safe(contract.getRoomNumber()));
            bodyLine(doc, "Người thuê", safe(contract.getTenant() != null ? contract.getTenant().getFullName() : null));
            bodyLine(doc, "Số tiền cọc đã nhận", formatMoney(amount));
            bodyLine(doc, "Phương thức", method != null ? method.name() : "");
            bodyLine(doc, "Mã tham chiếu", safe(reference));
            bodyLine(doc, "Ngày nhận", nowVi());

            if (method == PaymentMethod.BANK_TRANSFER) {
                spacer(doc);
                paragraph(doc, "Thông tin chuyển khoản (MoMo):");
                bodyLine(doc, "Tên người nhận", safe(transferInfo != null ? transferInfo.getReceiverName() : null));
                bodyLine(doc, "Số điện thoại", safe(transferInfo != null ? transferInfo.getReceiverPhone() : null));
            }

            spacer(doc);
            paragraph(doc, "Điều kiện hoàn trả tiền cọc:");
            bullet(doc, "Ở hết thời hạn hợp đồng: Ví dụ ký 1 năm nhưng ở 6 tháng dọn đi thì 99% sẽ mất cọc.");
            bullet(doc, "Thông báo trước (Notice): Phải báo cho chủ nhà trước khi chuyển đi (thường là 15 hoặc 30 ngày tùy hợp đồng). Dù hết hạn hợp đồng mà không báo trước, bạn vẫn có thể bị trừ tiền.");
            bullet(doc, "Hiện trạng phòng: Phòng không bị hư hỏng, tường không bị vẽ bậy, đinh không đóng lung tung (trừ hao mòn tự nhiên).");

            spacer(doc);
            paragraph(doc, "Người thuê xác nhận đã đọc và hiểu các điều kiện trên.");

            try (FileOutputStream out = new FileOutputStream(filePath.toFile())) {
                doc.write(out);
            }
        }
    }

    private static void title(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r = p.createRun();
        r.setBold(true);
        r.setFontSize(16);
        r.setText(text);
    }

    private static void spacer(XWPFDocument doc) {
        doc.createParagraph();
    }

    private static void paragraph(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(text);
    }

    private static void bodyLine(XWPFDocument doc, String label, String value) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r1 = p.createRun();
        r1.setBold(true);
        r1.setText(label + ": ");
        XWPFRun r2 = p.createRun();
        r2.setText(value != null ? value : "");
    }

    private static void bullet(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        p.setNumID(null);
        XWPFRun r = p.createRun();
        r.setText("- " + text);
    }

    private static String safe(String v) {
        return v == null ? "" : v;
    }

    private static String formatMoney(BigDecimal amount) {
        if (amount == null) return "";
        DecimalFormat fmt = new DecimalFormat("#,###");
        return fmt.format(amount) + " VND";
    }

    private static String nowVi() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("vi-VN"));
        return LocalDateTime.now().format(f);
    }
}
