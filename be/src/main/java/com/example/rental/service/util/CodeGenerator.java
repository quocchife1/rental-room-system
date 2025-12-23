package com.example.rental.service.util; // Tạo thư mục util trong service

import org.springframework.stereotype.Component;

@Component
public class CodeGenerator {

    /**
     * Định dạng mã định danh theo quy tắc [TIỀN_TỐ] + [ID].
     * Ví dụ: Nếu prefix là "NV" và id là 12, trả về "NV0012".
     *
     * @param prefix Tiền tố mã (NV, DT, T, v.v.)
     * @param id ID của Entity vừa được lưu (thường là Long)
     * @return Chuỗi mã định danh đã định dạng
     */
    public String generateCode(String prefix, Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID không được null khi tạo code.");
        }
        
        // Đảm bảo số được đệm bằng các số 0 phía trước (ví dụ: 0001)
        // Dùng String.format để đệm 0, ví dụ: %04d
        // Số 4 ở đây là số chữ số tối thiểu của phần số.
        String numberPart = String.format("%04d", id); 
        
        // Kết hợp tiền tố và phần số
        return prefix + numberPart;
    }
}