package com.example.rental.scheduler;

import com.example.rental.entity.Contract;
import com.example.rental.entity.ContractStatus;
import com.example.rental.entity.Invoice;
import com.example.rental.entity.InvoiceDetail;
import com.example.rental.entity.ServiceItem;
import com.example.rental.service.ContractService;
import com.example.rental.service.InvoiceService;
import com.example.rental.service.ServiceItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class AutoBillingScheduler {

    private final ContractService contractService;
    private final ServiceItemService serviceItemService;
    private final InvoiceService invoiceService;

    // run at 2:00 AM on the 1st of each month
    @Scheduled(cron = "0 0 2 1 * ?")
    public void generateMonthlyInvoices(){
        List<Contract> activeContracts = contractService.findAll().stream()
            .filter(c -> c.getStatus() == ContractStatus.ACTIVE)
            .collect(Collectors.toList());
        List<ServiceItem> allServices = serviceItemService.getAll();

        for(Contract c : activeContracts){
            try{
                // compute base rent
                BigDecimal base = (c.getRoom() != null && c.getRoom().getPrice() != null) ? c.getRoom().getPrice() : BigDecimal.ZERO;
                Invoice invoice = new Invoice();
                invoice.setContract(c);
                invoice.setDueDate(LocalDate.now().plusDays(7));
                invoice.setStatus(com.example.rental.entity.InvoiceStatus.UNPAID);
                invoice.setAmount(base);

                // create details: rent
                InvoiceDetail rentDetail = new InvoiceDetail();
                rentDetail.setDescription("Tiền phòng tháng " + LocalDate.now().getMonthValue());
                rentDetail.setQuantity(1);
                rentDetail.setUnitPrice(base);
                rentDetail.setAmount(base);

                invoice.setDetails(new ArrayList<>(java.util.List.of(rentDetail)));

                // attach monthly services if any (for demo include all active monthly services)
                for(ServiceItem si : allServices){
                    if("MONTHLY".equalsIgnoreCase(si.getBillingFrequency()) && si.isActive()){
                        InvoiceDetail d = new InvoiceDetail();
                        d.setDescription(si.getName());
                        d.setQuantity(1);
                        d.setUnitPrice(si.getUnitPrice());
                        d.setAmount(si.getUnitPrice());
                        invoice.getDetails().add(d);
                        invoice.setAmount(invoice.getAmount().add(si.getUnitPrice()));
                    }
                }

                // create via InvoiceService so it maps to DTOs and persists
                com.example.rental.dto.invoice.InvoiceRequest req = new com.example.rental.dto.invoice.InvoiceRequest();
                req.setContractId(c.getId());
                req.setDueDate(invoice.getDueDate());
                // map details
                java.util.List<com.example.rental.dto.invoice.InvoiceDetailRequest> detailReqs = new java.util.ArrayList<>();
                for(InvoiceDetail d : invoice.getDetails()){
                    com.example.rental.dto.invoice.InvoiceDetailRequest dr = new com.example.rental.dto.invoice.InvoiceDetailRequest();
                    dr.setDescription(d.getDescription());
                    dr.setUnitPrice(d.getUnitPrice());
                    dr.setQuantity(d.getQuantity());
                    detailReqs.add(dr);
                }
                req.setDetails(detailReqs);
                invoiceService.create(req);
            }catch(Exception ex){
                // log and continue
                System.err.println("Auto-billing failed for contract " + c.getId() + ": " + ex.getMessage());
            }
        }
    }
}
