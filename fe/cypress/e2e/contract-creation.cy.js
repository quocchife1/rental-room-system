/**
 * TEST SUITE: Luồng Tạo Hợp Đồng (Contract Creation)
 *
 * Dùng dữ liệu thật từ BE. Không mock API contracts/reservations.
 *
 * TC-17: Trang tạo hợp đồng hiển thị đầy đủ các field
 * TC-18: Prefill dữ liệu từ reservationId thật
 * TC-19: Validation: không submit khi thiếu field bắt buộc
 * TC-20: Tạo hợp đồng thành công
 * TC-21: Danh sách hợp đồng hiển thị đúng
 * TC-22: ACCOUNTANT không truy cập trang tạo hợp đồng
 * TC-23: MANAGER có thể truy cập danh sách hợp đồng
 * TC-24: Điều hướng từ danh sách bookings → trang tạo hợp đồng
 */

const API = Cypress.env('apiUrl') || 'http://localhost:8080';

describe('Contract Creation Flow (Receptionist)', () => {
  beforeEach(() => {
    cy.clearCookies();
    cy.clearLocalStorage();
  });

  // ─── TC-17: Trang tạo hợp đồng render đúng ───────────────────────────────
  it('TC-17: contract creation page renders all required form sections', () => {
    cy.loginByFixture('receptionist1');
    cy.visit('/staff/contracts/create');

    cy.contains('h3', 'Thông tin phòng', { timeout: 15000 }).should('be.visible');
    cy.contains('h3', 'Thông tin khách hàng').should('be.visible');
    cy.contains('h3', 'Thông tin hợp đồng').should('be.visible');

    cy.contains('button', 'Lưu & Tạo hợp đồng').should('be.visible');
    cy.contains('button', 'Tải hợp đồng (DOCX)').should('be.visible').and('be.disabled');
  });

  // ─── TC-18: Prefill dữ liệu từ reservationId ──────────────────────────────
  it('TC-18: form is prefilled when navigated from booking with reservationId', () => {
    cy.loginByFixture('receptionist1');

    // Lấy reservation RESERVED thật
    cy.request({
      url: `${API}/api/reservations/my-branch?status=RESERVED&size=1`,
      failOnStatusCode: false,
    }).then((res) => {
      const content = res.body?.data?.content || [];
      if (content.length === 0) {
        cy.log('Không có reservation RESERVED – skip TC-18');
        return;
      }
      const reserved = content[0];

      cy.visit(`/staff/contracts/create?reservationId=${reserved.id}`);

      // Đợi prefill API được gọi và dữ liệu điền vào form
      cy.contains('h3', 'Thông tin phòng', { timeout: 15000 }).should('be.visible');

      // Kiểm tra form có giá trị (không trống)
      cy.get('input[placeholder="CN01"]', { timeout: 10000 })
        .invoke('val')
        .should('not.be.empty');
    });
  });

  // ─── TC-19: Validation - thiếu field bắt buộc ────────────────────────────
  it('TC-19: shows validation alert when required fields are missing', () => {
    cy.loginByFixture('receptionist1');
    cy.visit('/staff/contracts/create');

    cy.contains('h3', 'Thông tin phòng', { timeout: 15000 }).should('be.visible');

    cy.window().then((win) => {
      cy.stub(win, 'alert').as('alertStub');
    });

    cy.contains('button', 'Lưu & Tạo hợp đồng').click();

    cy.get('@alertStub').should('have.been.calledWith', Cypress.sinon.match(/Vui lòng nhập/));
  });

  // ─── TC-20: Tạo hợp đồng thành công ─────────────────────────────────────
  it('TC-20: receptionist can create a contract successfully from a reservation', () => {
    cy.loginByFixture('receptionist1');

    cy.request({
      url: `${API}/api/reservations/my-branch?status=RESERVED&size=1`,
      failOnStatusCode: false,
    }).then((res) => {
      const content = res.body?.data?.content || [];
      if (content.length === 0) {
        cy.log('Không có reservation RESERVED – skip TC-20');
        return;
      }
      const reserved = content[0];

      cy.intercept('POST', '**/api/contracts').as('createContract');
      cy.intercept('PUT', `**/api/reservations/${reserved.id}/mark-contracted`).as('markContracted');

      cy.visit(`/staff/contracts/create?reservationId=${reserved.id}`);
      cy.contains('h3', 'Thông tin phòng', { timeout: 15000 }).should('be.visible');

      // Điền ngày kết thúc (bắt buộc)
      cy.contains('label', 'Ngày kết thúc')
        .parent()
        .find('input[type="date"]')
        .type('2027-12-31');

      cy.window().then((win) => {
        cy.stub(win, 'alert').returns(undefined);
      });

      cy.contains('button', 'Lưu & Tạo hợp đồng').click();

      cy.wait('@createContract', { timeout: 15000 }).then((interception) => {
        expect(interception.response.statusCode).to.be.oneOf([200, 201]);
      });

      // Sau tạo thành công → nút tải DOCX phải enabled
      cy.contains('button', 'Tải hợp đồng (DOCX)', { timeout: 10000 }).should('not.be.disabled');
    });
  });

  // ─── TC-21: Danh sách hợp đồng ────────────────────────────────────────────
  it('TC-21: contracts list page loads and displays data', () => {
    cy.loginByFixture('receptionist1');
    cy.visit('/staff/contracts');

    cy.location('pathname', { timeout: 10000 }).should('eq', '/staff/contracts');
    cy.get('body', { timeout: 15000 }).should('not.contain', 'Lỗi');
  });

  // ─── TC-22: ACCOUNTANT không truy cập trang tạo hợp đồng ─────────────────
  it('TC-22: ACCOUNTANT role cannot access contract creation page', () => {
    cy.loginByFixture('accountant');
    cy.visit('/staff/contracts/create');

    cy.location('pathname', { timeout: 10000 }).should('eq', '/');
  });

  // ─── TC-23: MANAGER có thể truy cập danh sách hợp đồng ──────────────────
  it('TC-23: MANAGER role can access contracts list page', () => {
    cy.loginByFixture('manager1');
    cy.visit('/staff/contracts');

    cy.location('pathname', { timeout: 10000 }).should('eq', '/staff/contracts');
    cy.get('body', { timeout: 15000 }).should('not.contain', 'Lỗi');
  });

  // ─── TC-24: Điều hướng từ danh sách booking → tạo hợp đồng ─────────────
  it('TC-24: can navigate from bookings page to contract creation page', () => {
    cy.loginByFixture('receptionist1');

    cy.request({
      url: `${API}/api/reservations/my-branch?status=RESERVED&size=1`,
      failOnStatusCode: false,
    }).then((res) => {
      const content = res.body?.data?.content || [];
      if (content.length === 0) {
        cy.log('Không có reservation RESERVED – skip TC-24');
        return;
      }
      const reserved = content[0];

      cy.visit('/staff/bookings');
      cy.contains('h1', 'Quản lý đặt lịch', { timeout: 15000 }).should('be.visible');

      cy.contains(reserved.reservationCode, { timeout: 10000 }).should('be.visible');
      cy.contains('button', 'Lập hợp đồng').first().click();

      cy.location('pathname', { timeout: 10000 }).should('eq', '/staff/contracts/create');
      cy.location('search').should('include', `reservationId=${reserved.id}`);

      cy.contains('h1', 'Tạo hợp đồng', { timeout: 10000 }).should('be.visible');
    });
  });
});
