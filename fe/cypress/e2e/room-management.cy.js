/**
 * TEST SUITE: Quản lý Phòng (Room Management)
 *
 * Dùng dữ liệu thật từ BE. Không mock API rooms/branches.
 *
 * TC-25: Hiển thị danh sách phòng
 * TC-26: Lọc theo trạng thái
 * TC-27: ADMIN/DIRECTOR thấy nút "Tạo phòng"
 * TC-28: RECEPTIONIST không thấy nút "Tạo phòng"
 * TC-29: Mở modal tạo phòng và submit thành công
 * TC-30: Validation khi thiếu chi nhánh/số phòng
 * TC-31: Cập nhật trạng thái phòng AVAILABLE → MAINTENANCE
 * TC-32: Mở modal chỉnh sửa phòng (edit description)
 * TC-33: Role MAINTENANCE truy cập /staff/rooms được
 * TC-34: ACCOUNTANT không truy cập /staff/rooms
 */

const API = Cypress.env('apiUrl') || 'http://localhost:8080';

describe('Room Management Flow', () => {
  beforeEach(() => {
    cy.clearCookies();
    cy.clearLocalStorage();
  });

  // TC-25: Hiển thị danh sách phòng
  it('TC-25: displays room list page correctly', () => {
    cy.loginByFixture('manager1');
    cy.visit('/staff/rooms');

    cy.contains('h1', 'Quản lý phòng', { timeout: 15000 }).should('be.visible');
    // Trang phải load data thật (không lỗi 4xx)
    cy.get('body').should('not.contain', 'Lỗi');
  });

  // TC-26: Lọc theo trạng thái
  it('TC-26: filter by status AVAILABLE hides non-matching rooms', () => {
    cy.loginByFixture('manager1');
    cy.visit('/staff/rooms');

    cy.contains('h1', 'Quản lý phòng', { timeout: 15000 }).should('be.visible');

    // Chọn lọc "Phòng trống"
    cy.get('select').contains('option', 'Phòng trống').parent().select('AVAILABLE');

    // Sau khi lọc, không có phòng OCCUPIED hiển thị badge "Đang thuê"
    cy.contains('Đang thuê').should('not.exist');
  });

  // TC-27: ADMIN thấy nút "Tạo phòng"
  it('TC-27: ADMIN sees "Tạo phòng" button', () => {
    cy.loginByFixture('admin');
    cy.visit('/staff/rooms');

    cy.contains('h1', 'Quản lý phòng', { timeout: 15000 }).should('be.visible');
    cy.contains('button', 'Tạo phòng').should('be.visible');
  });

  // TC-28: RECEPTIONIST không thấy nút "Tạo phòng"
  it('TC-28: RECEPTIONIST does not see "Tạo phòng" button', () => {
    cy.loginByFixture('receptionist1');
    cy.visit('/staff/rooms');

    cy.contains('h1', 'Quản lý phòng', { timeout: 15000 }).should('be.visible');
    cy.contains('button', 'Tạo phòng').should('not.exist');
  });

  // TC-29: ADMIN tạo phòng mới thành công
  it('TC-29: ADMIN can create a new room via modal', () => {
    cy.loginByFixture('admin');
    cy.visit('/staff/rooms');

    cy.contains('h1', 'Quản lý phòng', { timeout: 15000 }).should('be.visible');

    // Lấy branch thật từ BE để chọn
    cy.request(`${API}/api/branches`).then((res) => {
      const branches = res.body?.data || [];
      if (branches.length === 0) {
        cy.log('Không có branch trong DB – skip TC-29');
        return;
      }

      // Số phòng ngẫu nhiên để tránh trùng
      const roomNumber = `CY${Date.now().toString().slice(-5)}`;

      cy.intercept('POST', '**/api/rooms').as('createRoom');

      cy.contains('button', 'Tạo phòng').click();
      cy.contains('h2', 'Tạo phòng', { timeout: 8000 }).should('be.visible');

      // Số phòng
      cy.contains('label', 'Số phòng').parent().find('input').clear().type(roomNumber);
      // Diện tích
      cy.contains('label', 'Diện tích (m²)').parent().find('input').clear().type('30');
      // Giá
      cy.contains('label', 'Giá (đ/tháng)').parent().find('input').clear().type('3000000');

      cy.contains('button', 'Tạo phòng').last().click();

      cy.wait('@createRoom', { timeout: 15000 }).then((interception) => {
        expect(interception.response.statusCode).to.be.oneOf([200, 201]);
        expect(interception.request.body.roomNumber).to.eq(roomNumber);
      });
    });
  });

  // TC-30: Validation - thiếu số phòng
  it('TC-30: shows error when roomNumber is empty', () => {
    cy.loginByFixture('admin');
    cy.visit('/staff/rooms');

    cy.contains('h1', 'Quản lý phòng', { timeout: 15000 }).should('be.visible');
    cy.contains('button', 'Tạo phòng').click();
    cy.contains('h2', 'Tạo phòng', { timeout: 8000 }).should('be.visible');

    // Submit mà không nhập số phòng
    cy.contains('button', 'Tạo phòng').last().click();

    // Phải hiện thông báo lỗi trong modal
    cy.contains('Vui lòng chọn chi nhánh và nhập số phòng', { timeout: 5000 }).should('be.visible');
  });

  // TC-31: Cập nhật trạng thái phòng AVAILABLE → MAINTENANCE
  it('TC-31: MANAGER can update room status to MAINTENANCE', () => {
    cy.loginByFixture('manager1');
    cy.visit('/staff/rooms');

    cy.contains('h1', 'Quản lý phòng', { timeout: 15000 }).should('be.visible');

    // Tìm phòng AVAILABLE từ BE
    cy.request(`${API}/api/rooms?status=AVAILABLE&size=1`).then((res) => {
      const rooms = res.body?.data?.content || res.body?.data || [];
      if (rooms.length === 0) {
        cy.log('Không có phòng AVAILABLE – skip TC-31');
        return;
      }
      const room = rooms[0];

      cy.intercept('PUT', `**/api/rooms/${room.id}/status`).as('updateStatus');

      // Tìm card phòng và bấm "Bảo trì"
      cy.contains(room.roomNumber)
        .closest('[class*="rounded-2xl"]')
        .contains('button', 'Bảo trì')
        .click();

      cy.wait('@updateStatus', { timeout: 10000 }).then((interception) => {
        expect(interception.response.statusCode).to.be.oneOf([200, 204]);
      });
    });
  });

  // TC-32: Mở modal chỉnh sửa phòng
  it('TC-32: can open edit modal and update description', () => {
    cy.loginByFixture('manager1');
    cy.visit('/staff/rooms');

    cy.contains('h1', 'Quản lý phòng', { timeout: 15000 }).should('be.visible');

    cy.request(`${API}/api/rooms?size=1`).then((res) => {
      const rooms = res.body?.data?.content || res.body?.data || [];
      if (rooms.length === 0) {
        cy.log('Không có phòng nào – skip TC-32');
        return;
      }
      const room = rooms[0];

      cy.intercept('PUT', `**/api/rooms/${room.id}/description`).as('updateDesc');

      cy.contains(room.roomNumber)
        .closest('[class*="rounded-2xl"]')
        .find('button[title="Chỉnh sửa"]')
        .click();

      // Modal edit phải xuất hiện
      cy.contains(room.roomCode || room.roomNumber, { timeout: 8000 }).should('be.visible');

      cy.get('textarea[placeholder*="Nhập mô tả"]').clear().type('Phòng rộng rãi, có điều hòa.');
      cy.contains('button', 'Lưu thay đổi').click();

      cy.wait('@updateDesc', { timeout: 10000 }).then((interception) => {
        expect(interception.response.statusCode).to.be.oneOf([200, 204]);
        expect(interception.request.body.description).to.eq('Phòng rộng rãi, có điều hòa.');
      });
    });
  });

  // TC-33: MAINTENANCE role truy cập /staff/rooms
  it('TC-33: MAINTENANCE role can access /staff/rooms', () => {
    cy.loginByFixture('maintenance');
    cy.visit('/staff/rooms');

    cy.location('pathname', { timeout: 10000 }).should('eq', '/staff/rooms');
    cy.contains('h1', 'Quản lý phòng', { timeout: 15000 }).should('be.visible');
  });

  // TC-34: ACCOUNTANT không truy cập /staff/rooms
  it('TC-34: ACCOUNTANT is redirected away from /staff/rooms', () => {
    cy.loginByFixture('accountant');
    cy.visit('/staff/rooms');

    cy.location('pathname', { timeout: 10000 }).should('eq', '/');
  });
});
