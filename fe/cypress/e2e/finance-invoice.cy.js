/**
 * TEST SUITE: Quản lý Tài chính & Hóa đơn (Finance / Accountant)
 *
 * Dùng dữ liệu thật từ BE. Không mock API invoices/reports.
 *
 * TC-35: ACCOUNTANT truy cập trang tài chính
 * TC-36: Hiển thị danh sách hóa đơn với thống kê
 * TC-37: Lọc hóa đơn theo trạng thái UNPAID
 * TC-38: ACCOUNTANT xác nhận thu tiền mặt từ hóa đơn cụ thể
 * TC-39: ADMIN tạo hóa đơn tháng
 * TC-40: MANAGER không thấy nút "Tạo hóa đơn tháng này"
 * TC-41: Xem chi tiết hóa đơn (expand row)
 * TC-42: Xác nhận MANAGER truy cập /staff/finance/invoices
 */

const API = Cypress.env('apiUrl') || 'http://localhost:8080';

describe('Finance & Invoice Management Flow (Accountant / Admin)', () => {
  beforeEach(() => {
    cy.clearCookies();
    cy.clearLocalStorage();
  });

  // TC-35: ACCOUNTANT truy cập trang tài chính
  it('TC-35: ACCOUNTANT can access finance page', () => {
    cy.loginByFixture('accountant');
    cy.visit('/staff/finance/invoices');

    cy.location('pathname', { timeout: 10000 }).should('eq', '/staff/finance/invoices');
    cy.contains('h1', 'Quản lý Tài chính', { timeout: 15000 }).should('be.visible');
  });

  // TC-36: Hiển thị danh sách hóa đơn với thống kê KPI
  it('TC-36: invoice list page renders KPI status badges', () => {
    cy.loginByFixture('accountant');
    cy.visit('/staff/finance/invoices');

    cy.contains('h1', 'Quản lý Tài chính', { timeout: 15000 }).should('be.visible');

    // KPI badges luôn hiển thị dù không có dữ liệu
    cy.contains('Chưa thanh toán').should('be.visible');
    cy.contains('Đã thanh toán').should('be.visible');
    cy.contains('Quá hạn').should('be.visible');
  });

  // TC-37: Lọc hóa đơn theo trạng thái
  it('TC-37: filter by status UNPAID calls API correctly', () => {
    cy.loginByFixture('accountant');
    cy.visit('/staff/finance/invoices');

    cy.contains('h1', 'Quản lý Tài chính', { timeout: 15000 }).should('be.visible');

    cy.intercept('GET', '**/api/invoices**').as('getInvoices');

    cy.get('select').contains('option', 'Chưa thanh toán').parent().select('UNPAID');

    cy.wait('@getInvoices', { timeout: 10000 }).then((interception) => {
      expect(interception.response.statusCode).to.eq(200);
    });
  });

  // TC-38: ACCOUNTANT xác nhận thu tiền mặt
  it('TC-38: ACCOUNTANT can look up an invoice by ID and confirm cash collection', () => {
    cy.loginByFixture('accountant');

    // Lấy hóa đơn UNPAID thật từ BE
    cy.request({
      url: `${API}/api/invoices?status=UNPAID&size=1`,
      failOnStatusCode: false,
    }).then((res) => {
      const content = res.body?.data?.content || [];
      if (content.length === 0) {
        cy.log('Không có hóa đơn UNPAID – skip TC-38');
        return;
      }
      const invoice = content[0];

      cy.intercept('PUT', `**/api/invoices/${invoice.id}/pay**`).as('payInvoice');

      cy.visit('/staff/finance/invoices');
      cy.contains('h1', 'Quản lý Tài chính', { timeout: 15000 }).should('be.visible');

      // Nhập ID hóa đơn vào ô tra cứu
      cy.get('input[placeholder*="Nhập mã hóa đơn"]').type(invoice.id.toString());
      cy.contains('button', 'Tìm hóa đơn').click();

      // Thông tin hóa đơn xuất hiện
      cy.contains(`#${invoice.id}`, { timeout: 10000 }).should('be.visible');

      cy.contains('button', 'Xác nhận đã thu tiền mặt').click();

      cy.wait('@payInvoice', { timeout: 10000 }).then((interception) => {
        expect(interception.response.statusCode).to.be.oneOf([200, 204]);
      });
    });
  });

  // TC-39: ADMIN tạo hóa đơn tháng
  it('TC-39: ADMIN can generate monthly invoices', () => {
    cy.loginByFixture('admin');
    cy.visit('/staff/finance/invoices');

    cy.contains('h1', 'Quản lý Tài chính', { timeout: 15000 }).should('be.visible');
    cy.contains('button', 'Tạo hóa đơn tháng này').should('be.visible');

    cy.intercept('POST', '**/api/invoices/generate-monthly').as('generateMonthly');

    cy.window().then((win) => {
      cy.stub(win, 'alert').returns(undefined);
    });

    cy.contains('button', 'Tạo hóa đơn tháng này').click();

    cy.wait('@generateMonthly', { timeout: 15000 }).then((interception) => {
      expect(interception.response.statusCode).to.be.oneOf([200, 201]);
    });
  });

  // TC-40: MANAGER không thấy nút "Tạo hóa đơn tháng này"
  it('TC-40: MANAGER does not see generate invoice button', () => {
    cy.loginByFixture('manager1');
    cy.visit('/staff/finance/invoices');

    cy.contains('h1', 'Quản lý Tài chính', { timeout: 15000 }).should('be.visible');
    cy.contains('button', 'Tạo hóa đơn tháng này').should('not.exist');
  });

  // TC-41: Expand row để xem chi tiết hóa đơn
  it('TC-41: clicking invoice row expands to show line items', () => {
    cy.loginByFixture('accountant');
    cy.visit('/staff/finance/invoices');

    cy.contains('h1', 'Quản lý Tài chính', { timeout: 15000 }).should('be.visible');

    // Lấy hóa đơn thật
    cy.request({
      url: `${API}/api/invoices?size=1`,
      failOnStatusCode: false,
    }).then((res) => {
      const content = res.body?.data?.content || [];
      if (content.length === 0) {
        cy.log('Không có hóa đơn nào – skip TC-41');
        return;
      }
      const invoice = content[0];

      cy.intercept('GET', `**/api/invoices/${invoice.id}`).as('getInvoiceDetail');

      cy.contains('td', `#${invoice.id}`, { timeout: 10000 }).click();

      cy.wait('@getInvoiceDetail', { timeout: 10000 }).then((interception) => {
        expect(interception.response.statusCode).to.eq(200);
      });

      cy.contains('Chi tiết hóa đơn', { timeout: 8000 }).should('be.visible');
    });
  });

  // TC-42: MANAGER truy cập /staff/finance/invoices
  it('TC-42: MANAGER can access finance invoices page', () => {
    cy.loginByFixture('manager1');
    cy.visit('/staff/finance/invoices');

    cy.location('pathname').should('eq', '/staff/finance/invoices');
    cy.contains('h1', 'Quản lý Tài chính', { timeout: 15000 }).should('be.visible');
  });
});
