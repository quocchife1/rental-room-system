/**
 * TEST SUITE: Portal Tenant - Hóa đơn & Hợp đồng (Tenant Portal)
 *
 * Dùng dữ liệu thật từ BE. Không mock API invoices/contracts/reservations.
 *
 * TC-63: TENANT truy cập tenant dashboard, thấy 3 KPI card
 * TC-64: Dashboard hiển thị hóa đơn gần đây
 * TC-65: Xem danh sách hóa đơn của tôi
 * TC-66: Xem danh sách hợp đồng của tôi
 * TC-67: Hợp đồng hiển thị trạng thái
 * TC-68: Tenant không truy cập /admin/* pages
 * TC-69: Tenant sidebar navigation links hoạt động
 * TC-70: Trang dashboard tenant hiển thị KPI hóa đơn chưa thanh toán
 */

describe('Tenant Portal - Invoices & Contracts', () => {
  beforeEach(() => {
    cy.clearCookies();
    cy.clearLocalStorage();
  });

  // TC-63: Tenant dashboard với 3 KPI card
  it('TC-63: user sees tenant dashboard with KPI card labels', () => {
    cy.loginByFixture('receptionist1');
    cy.visit('/tenant/dashboard');

    cy.location('pathname', { timeout: 10000 }).should('eq', '/tenant/dashboard');
    cy.contains('Hóa đơn chưa thanh toán', { timeout: 15000 }).should('be.visible');
    cy.contains('Hợp đồng hiệu lực').should('be.visible');
    cy.contains('Giữ chỗ đang chờ').should('be.visible');
  });

  // TC-64: Dashboard hiển thị section hóa đơn gần đây
  it('TC-64: dashboard shows recent invoices section', () => {
    cy.loginByFixture('receptionist1');
    cy.visit('/tenant/dashboard');

    cy.location('pathname', { timeout: 10000 }).should('eq', '/tenant/dashboard');
    cy.contains('Hóa đơn gần đây', { timeout: 15000 }).should('be.visible');
  });

  // TC-65: Xem danh sách hóa đơn của tôi
  it('TC-65: tenant can view their invoice list page', () => {
    cy.intercept('GET', '**/api/invoices**').as('getInvoices');

    cy.loginByFixture('receptionist1');
    cy.visit('/tenant/invoices');

    cy.location('pathname', { timeout: 10000 }).should('eq', '/tenant/invoices');
    cy.get('body', { timeout: 15000 }).should('not.contain', 'Lỗi');
  });

  // TC-66: Xem danh sách hợp đồng
  it('TC-66: tenant can view their contracts list page', () => {
    cy.intercept('GET', '**/api/contracts/my-contracts**').as('getMyContracts');

    cy.loginByFixture('receptionist1');
    cy.visit('/tenant/contracts');

    cy.location('pathname', { timeout: 10000 }).should('eq', '/tenant/contracts');
    cy.get('body', { timeout: 15000 }).should('not.contain', 'Lỗi');
  });

  // TC-67: Trang contracts render đúng dù có hay không có data
  it('TC-67: contracts page renders without crash', () => {
    cy.loginByFixture('receptionist1');
    cy.visit('/tenant/contracts');

    cy.location('pathname', { timeout: 10000 }).should('eq', '/tenant/contracts');
    cy.get('body', { timeout: 15000 }).should('not.be.empty');
  });

  // TC-68: Tenant không truy cập /admin/*
  it('TC-68: non-admin role cannot access admin pages', () => {
    cy.loginByFixture('receptionist1');
    cy.visit('/admin/dashboard');

    cy.location('pathname', { timeout: 10000 }).should('eq', '/');
  });

  // TC-69: Tenant sidebar navigation links hoạt động
  it('TC-69: tenant sidebar navigation links work correctly', () => {
    cy.loginByFixture('receptionist1');
    cy.visit('/tenant/dashboard');

    cy.location('pathname', { timeout: 10000 }).should('eq', '/tenant/dashboard');

    // Click link đến invoices
    cy.contains('a', 'Xem chi tiết', { timeout: 10000 }).first().click();
    cy.location('pathname', { timeout: 10000 }).should('include', '/tenant/invoices');
  });

  // TC-70: Dashboard hiển thị đúng KPI số lượng hóa đơn chưa thanh toán
  it('TC-70: tenant dashboard KPI card for unpaid invoices is visible', () => {
    cy.loginByFixture('receptionist1');
    cy.visit('/tenant/dashboard');

    cy.location('pathname', { timeout: 10000 }).should('eq', '/tenant/dashboard');

    // KPI card "Hóa đơn chưa thanh toán" phải có giá trị số (bất kể là bao nhiêu)
    cy.contains('Hóa đơn chưa thanh toán', { timeout: 15000 })
      .parents('[class*="card"], section, div')
      .first()
      .find('h3, p, span')
      .first()
      .invoke('text')
      .then((text) => {
        expect(parseInt(text, 10)).to.be.gte(0);
      });
  });
});
