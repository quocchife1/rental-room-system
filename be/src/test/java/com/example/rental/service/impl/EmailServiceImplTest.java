package com.example.rental.service.impl;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailServiceImpl Tests")
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    @DisplayName("Send Simple Message - Success")
    void sendSimpleMessage_Success() {
        emailService.sendSimpleMessage("test@example.com", "Subject", "Hello Text");

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getTo()).containsExactly("test@example.com");
        assertThat(sentMessage.getSubject()).isEqualTo("Subject");
        assertThat(sentMessage.getText()).isEqualTo("Hello Text");
    }

    @Test
    @DisplayName("Send Simple Message - Skip if 'to' is blank")
    void sendSimpleMessage_SkipIfBlank() {
        emailService.sendSimpleMessage("", "Subject", "Text");
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Send HTML Message - Success")
    void sendHtmlMessage_Success() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendHtmlMessage("user@test.com", "HTML Subject", "<h1>Hello</h1>");

        // Verify mailSender đã được gọi để send
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Send HTML Message - Handle Exception Gracefully")
    void sendHtmlMessage_HandlesException() {
        // Cố tình gây lỗi khi tạo MimeMessage
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mail server down"));

        // Phương thức của bạn có try-catch nên sẽ không văng lỗi ra ngoài
        emailService.sendHtmlMessage("user@test.com", "Subject", "Content");

        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
