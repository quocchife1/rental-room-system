# MoMo Payment for Tenant Invoices (A→Z)

Tài liệu này mô tả cách hệ thống hiện tại triển khai thanh toán MoMo cho **hóa đơn của người thuê (tenant)**, gồm luồng request/response, cấu hình cần có trong `application.properties`, và checklist deploy sang môi trường khác.

## 1) Tổng quan kiến trúc

- **Khởi tạo thanh toán (create payment)**: Backend tạo `orderId`, ký `signature`, gọi MoMo `/create` để lấy `payUrl`.
- **User thanh toán trên MoMo**: Browser được redirect sang `payUrl`.
- **Xác nhận thanh toán (không cần IPN/ngrok cho invoice)**: MoMo redirect về backend `GET /api/momo/return`, backend gọi MoMo `/query` để xác nhận.
- **Cập nhật hóa đơn**: Backend mark invoice = `PAID` khi `resultCode == 0`.

> Lưu ý:
> - Với **tenant invoice chạy local/dev**, hệ thống dùng **return-url + query** nên KHÔNG cần public IPN/ngrok.
> - Với **production**, nên bật IPN public để chống mất trạng thái khi user đóng trình duyệt.

## 2) Endpoint liên quan

### 2.1 Khởi tạo thanh toán MoMo cho hóa đơn

- Endpoint: `POST /api/invoices/{id}/pay?direct=false`
- Auth: `TENANT` (hoặc role admin/director/accountant nhưng accountant bị chặn khi `direct=false`)
- Response (payload):
  - `payUrl`: URL MoMo để redirect
  - `orderId`: mã order nội bộ

Nếu `direct=true` thì đây là xác nhận thu tiền mặt (staff) và response là `InvoiceResponse`.

### 2.2 Return handler (MoMo redirect về backend)

- Endpoint: `GET /api/momo/return?orderId=...&returnUrl=...`
- Public: không cần JWT.
- Backend sẽ gọi MoMo `/query` để xác nhận rồi redirect về `returnUrl` (frontend).

### 2.3 IPN handler (MoMo gọi về)

- Endpoint: `POST /api/momo/ipn-handler` (public)
- Nhận payload JSON hoặc form-urlencoded.
- Backend xác thực chữ ký và cập nhật invoice.

## 3) Cấu hình cần thiết trong application.properties

Các key MoMo đang dùng trong backend:

```properties
# MoMo config
momo.partner-code=...
momo.access-key=...
momo.secret-key=...

# Base endpoint (sandbox/prod)
# Sandbox:
# momo.end-point=https://test-payment.momo.vn/v2/gateway/api
momo.end-point=...

# IPN URL (dành cho flow cần IPN, ví dụ partner). Invoice tenant local/dev không phụ thuộc IPN.
momo.ipn-url=https://<your-domain>/api/momo/ipn-handler

# requestType theo MoMo (tùy hợp đồng với MoMo)
momo.request-type=payWithATM

# default redirect url (một số flow khác có thể dùng). Với invoice, backend sẽ override thành /api/momo/return.
momo.redirect-url=http://localhost:3000/tenant/invoices

# Frontend base URL (backend dùng để build redirectUrl)
app.frontend.base-url=http://localhost:3000
```

## 4) Luồng request/response chi tiết (Invoice)

### Bước 1 — Tenant bấm “Thanh toán ngay”

FE gọi:

`POST /api/invoices/{invoiceId}/pay?direct=false`

Backend thực hiện:

- Validate tenant sở hữu invoice.
- Tạo:
  - `orderId = "INV-" + invoiceId + "-" + UUID`
  - `orderInfo = "Thanh toan hoa don #" + invoiceId`
  - `extraData = Base64("INVOICE:" + invoiceId)`
  - `redirectUrl = <app.frontend.base-url> + /tenant/invoices`
- Gọi MoMo create payment (qua `MomoServiceImpl.createATMPayment(...)`) để lấy `payUrl`.
- Lưu bản ghi `Payment` status `PENDING` (best-effort).
- Trả về `{ payUrl, orderId }`.

### Bước 2 — FE redirect sang payUrl

Frontend nhận `payUrl` và chuyển hướng browser tới MoMo.

### Bước 3 — MoMo redirect về backend return URL (không cần ngrok)

MoMo redirect browser về:

`GET /api/momo/return?orderId=...&returnUrl=<frontend>`

Backend thực hiện:

- Gọi MoMo `/query` với `orderId`.
- Nếu `resultCode == 0` ⇒ mark invoice `PAID`.
- Redirect tiếp về frontend `returnUrl` kèm `?momo=success|failed&orderId=...`.

### Bước 4 — Frontend refetch invoice

FE page `MyInvoices`:

- Hiển thị alert theo `momo=success|failed`.
- Refetch `/api/invoices/my-invoices` sau ~1.2s để thấy trạng thái `PAID`.

## 5) Checklist deploy sang hệ thống khác

### 5.1 Chuẩn bị MoMo credentials

- `partnerCode`
- `accessKey`
- `secretKey`
- Chọn môi trường:
  - Sandbox: `https://test-payment.momo.vn/v2/gateway/api`
  - Production: theo domain MoMo cung cấp

### 5.2 Không dùng ngrok cho invoice tenant

- Không cần public IPN.
- Chỉ cần browser redirect về backend local được: `http://localhost:8080/api/momo/return`.
- Backend chỉ cần outbound được tới MoMo để gọi `/query`.

### 5.3 Cấu hình redirectUrl

- `app.frontend.base-url` phải là domain public của FE.
- FE route `/tenant/invoices` phải truy cập được từ internet.

### 5.4 Network / Firewall

- Backend phải outbound được tới MoMo `momo.end-point`.
- MoMo inbound được tới `momo.ipn-url`.

### 5.5 Test end-to-end

- Tenant mở trang hóa đơn → bấm “Thanh toán ngay”.
- Backend trả `payUrl` → browser redirect sang MoMo.
- Thanh toán thành công → kiểm tra log backend nhận IPN.
- Reload danh sách hóa đơn → invoice chuyển `PAID`.

## 6) Troubleshooting nhanh

- **Thanh toán thành công nhưng invoice vẫn UNPAID**:
  - `momo.ipn-url` không public / sai domain / ngrok hết hạn.
  - Signature verify fail (check log backend trong `MomoServiceImpl`).
  - InvoiceId không parse được từ `extraData`/`orderId`.

- **Không lấy được payUrl**:
  - Sai `accessKey/secretKey/partnerCode`.
  - Sai `momo.end-point` (sandbox/prod).
  - Payload ký sai thứ tự field (check `rawSignature` log).
