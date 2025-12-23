package com.example.rental.repository;

import com.example.rental.entity.InvoiceDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceDetailRepository extends JpaRepository<InvoiceDetail, Long> {
    // bạn có thể thêm query nếu cần (findByInvoiceId,...)
}
