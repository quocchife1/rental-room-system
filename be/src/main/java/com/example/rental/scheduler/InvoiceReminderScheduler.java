package com.example.rental.scheduler;

import com.example.rental.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvoiceReminderScheduler {

    private final InvoiceService invoiceService;

    // Chạy mỗi ngày lúc 08:00 sáng
    @Scheduled(cron = "0 0 8 * * *")
    public void dailyReminder() {
        invoiceService.checkAndSendDueReminders();
    }
}
