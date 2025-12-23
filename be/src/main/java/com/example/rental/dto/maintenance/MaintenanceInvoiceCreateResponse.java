package com.example.rental.dto.maintenance;

import com.example.rental.dto.invoice.InvoiceResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MaintenanceInvoiceCreateResponse {
    private MaintenanceResponse maintenance;
    private InvoiceResponse invoice;
}
