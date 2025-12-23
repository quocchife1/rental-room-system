package com.example.rental.utils;

import com.example.rental.dto.contract.ContractCreateRequest;
import com.example.rental.entity.Branch;
import com.example.rental.entity.Contract;
import com.example.rental.entity.Room;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class ContractDocxGenerator {

    private static final String TEMPLATE_PATH = "src/main/resources/templates/hop-dong-thue-nha-tro.docx";
    private static final String OUTPUT_DIR = "uploads/generated_contracts/";

    public String generateContractFile(Contract contract, ContractCreateRequest request) throws IOException {
        Path outputDir = Paths.get(OUTPUT_DIR);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        try (InputStream is = new FileInputStream(TEMPLATE_PATH);
             XWPFDocument document = new XWPFDocument(is)) {

            replacePlaceholders(document, contract, request);

            String fileName = "contract_" + contract.getId() + ".docx";
            Path filePath = outputDir.resolve(fileName);

            try (FileOutputStream out = new FileOutputStream(filePath.toFile())) {
                document.write(out);
            }

            return "/" + OUTPUT_DIR + fileName;
        }
    }

    private void replacePlaceholders(XWPFDocument doc, Contract contract, ContractCreateRequest request) {
        Branch branch = contract.getRoom().getBranch();
        Room room = contract.getRoom();

        DecimalFormat moneyFormat = new DecimalFormat("#,###");
        String priceFormatted = moneyFormat.format(room.getPrice());
        String depositFormatted = moneyFormat.format(contract.getDeposit());

        String priceInWords = NumberToVietnameseWords.convert(room.getPrice().longValue());
        String depositInWords = NumberToVietnameseWords.convert(contract.getDeposit().longValue());

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("vi", "VN"));
        String startDateFormatted = contract.getStartDate().format(dateFormatter);
        String endDateFormatted = contract.getEndDate() != null ? contract.getEndDate().format(dateFormatter) : "";

        // Map placeholder -> value
        Map<String, String> values = new LinkedHashMap<>();
        values.put("{{TEN_NGUOI_THUE}}", request.getTenantFullName());
        values.put("{{CCCD}}", request.getTenantCccd());
        values.put("{{SDT}}", request.getTenantPhoneNumber());
        values.put("{{EMAIL}}", request.getTenantEmail());
        values.put("{{DIACHI}}", request.getTenantAddress());
        values.put("{{ROOM_NUMBER}}", contract.getRoomNumber());
        values.put("{{BRANCH_CODE}}", contract.getBranchCode());
        values.put("{{BRANCH_ADDRESS}}", branch.getAddress());
        values.put("{{PRICE}}", priceFormatted);
        values.put("{{PRICE_WORDS}}", priceInWords);
        values.put("{{DEPOSIT}}", depositFormatted);
        values.put("{{DEPOSIT_WORDS}}", depositInWords);
        values.put("{{START_DATE}}", startDateFormatted);
        values.put("{{END_DATE}}", endDateFormatted);

        for (XWPFParagraph paragraph : doc.getParagraphs()) {
            String fullText = paragraph.getText();
            if (fullText == null || fullText.isEmpty()) continue;

            boolean containsPlaceholder = values.keySet().stream().anyMatch(fullText::contains);
            if (!containsPlaceholder) continue;

            // Xóa các run cũ
            int runCount = paragraph.getRuns().size();
            for (int i = runCount - 1; i >= 0; i--) {
                paragraph.removeRun(i);
            }

            // Thay thế và bọc phần dữ liệu in đậm
            String replaced = fullText;
            for (Map.Entry<String, String> entry : values.entrySet()) {
                if (replaced.contains(entry.getKey())) {
                    replaced = replaced.replace(entry.getKey(), "§" + entry.getValue() + "§");
                }
            }

            String[] parts = replaced.split("§");
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                if (part.isEmpty()) continue;

                XWPFRun newRun = paragraph.createRun();
                newRun.setText(part);
                if (i % 2 == 1) { // phần dữ liệu
                    newRun.setBold(true);
                }
            }
        }
    }
}
