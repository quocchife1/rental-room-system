package com.example.rental.security;

import com.example.rental.entity.AuditAction;

import java.lang.annotation.*;

/**
 * Annotation để tự động ghi nhận các hành động vào AuditLog
 * Chỉ cần thêm @Audited trên các method muốn ghi nhật ký
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audited {
    /**
     * Hành động được ghi nhận
     */
    AuditAction action();
    
    /**
     * Loại đối tượng bị ảnh hưởng (VD: CONTRACT, INVOICE, TENANT)
     */
    String targetType();
    
    /**
     * Mô tả hành động (tuỳ chọn)
     */
    String description() default "";
}
