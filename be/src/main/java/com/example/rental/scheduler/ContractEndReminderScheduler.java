package com.example.rental.scheduler;

import com.example.rental.entity.Contract;
import com.example.rental.entity.ContractStatus;
import com.example.rental.repository.ContractRepository;
import com.example.rental.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ContractEndReminderScheduler {

    private final ContractRepository contractRepository;
    private final EmailService emailService;

    // 1 tháng + 1 tuần = 37 ngày
    private static final long REMINDER_DAYS_BEFORE_END = 37L;

    // Chạy mỗi ngày lúc 08:00 sáng
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void remindBeforeContractEnd() {
        LocalDate today = LocalDate.now();
        LocalDate targetEndDate = today.plusDays(REMINDER_DAYS_BEFORE_END);

        List<Contract> contracts = contractRepository.findByStatusAndEndDateAndEndReminderSentFalse(
                ContractStatus.ACTIVE,
                targetEndDate
        );

        if (contracts == null || contracts.isEmpty()) {
            return;
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("vi", "VN"));

        for (Contract c : contracts) {
            if (c.getTenant() == null || c.getTenant().getEmail() == null || c.getTenant().getEmail().isBlank()) {
                c.setEndReminderSent(true);
                continue;
            }

            String tenantName = c.getTenant().getFullName() != null ? c.getTenant().getFullName() : "bạn";
            String endDateStr = c.getEndDate() != null ? c.getEndDate().format(fmt) : "";

            String subject = "Nhắc nhở sắp hết hạn hợp đồng";
            String text = "Xin chào " + tenantName + ",\n\n"
                    + "Hợp đồng thuê phòng của bạn sẽ hết hạn vào ngày " + endDateStr + ".\n"
                    + "Vui lòng chủ động chuẩn bị thủ tục trả phòng/hoàn trả hiện trạng và gửi yêu cầu trả phòng trước thời hạn theo quy định hợp đồng.\n\n"
                    + "Thông tin: Chi nhánh " + safe(c.getBranchCode()) + ", Phòng " + safe(c.getRoomNumber()) + ".\n\n"
                    + "Trân trọng.";

            emailService.sendSimpleMessage(c.getTenant().getEmail(), subject, text);
            c.setEndReminderSent(true);
        }

        contractRepository.saveAll(contracts);
    }

    private static String safe(String v) {
        return v == null ? "" : v;
    }
}
