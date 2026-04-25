/**
 * TEST SUITE: Dashboard Giám đốc / Admin (Director Dashboard)
 *
 * Dùng dữ liệu thật từ BE. Không mock API dashboard/users/branches.
 *
 * TC-53: DIRECTOR truy cập /admin/dashboard
 * TC-54: Dashboard hiển thị 4 KPI card
 * TC-55: Bấm "Làm mới dữ liệu" gọi lại API
 * TC-56: Chuyển tab biểu đồ "Theo chi nhánh"
 * TC-57: Quản lý nhân viên hiển thị danh sách
 * TC-58: Quản lý chi nhánh hiển thị
 * TC-59: Cấu hình hệ thống
 * TC-60: Nhật ký kiểm toán hiển thị
 * TC-61: MANAGER không truy cập /admin/dashboard
 * TC-62: DIRECTOR redirect sang /admin/dashboard khi vào redirectPath
 */

describe('Director / Admin Dashboard Flow', () => {
  beforeEach(() => {
    cy.clearCookies();
    cy.clearLocalStorage();
  });

  // TC-53: DIRECTOR truy cập dashboard
  it('TC-53: DIRECTOR can access /admin/dashboard', () => {
    cy.intercept('GET', '**/api/dashboard**').as('getDashboard');
    cy.loginByFixture('director');
    cy.visit('/admin/dashboard');
    cy.wait('@getDashboard', { timeout: 15000 });

    cy.location('pathname', { timeout: 10000 }).should('eq', '/admin/dashboard');
    cy.contains('h1', 'Tổng quan Giám đốc', { timeout: 15000 }).should('be.visible');
  });

  // TC-54: Dashboard hiển thị 4 KPI card
  it('TC-54: dashboard shows 4 KPI card labels', () => {
    cy.intercept('GET', '**/api/dashboard**').as('getDashboard');
    cy.loginByFixture('director');
    cy.visit('/admin/dashboard');
    cy.wait('@getDashboard', { timeout: 15000 });

    cy.contains('Doanh thu tháng này').should('be.visible');
    cy.contains('Tỷ lệ lấp đầy').should('be.visible');
    cy.contains('Tổng công nợ').should('be.visible');
    cy.contains('Bảo trì chờ xử lý').should('be.visible');
  });

  // TC-55: Bấm "Làm mới dữ liệu"
  it('TC-55: refresh button reloads dashboard data from real BE', () => {
    cy.intercept('GET', '**/api/dashboard**').as('getDashboard');
    cy.loginByFixture('director');
    cy.visit('/admin/dashboard');
    cy.wait('@getDashboard', { timeout: 15000 });

    cy.contains('button', 'Làm mới dữ liệu').click();

    cy.wait('@getDashboard', { timeout: 10000 }).then((interception) => {
      expect(interception.response.statusCode).to.eq(200);
    });
  });

  // TC-56: Chuyển tab biểu đồ sang "Theo chi nhánh"
  it('TC-56: can switch revenue chart to branch view', () => {
    cy.intercept('GET', '**/api/dashboard**').as('getDashboard');
    cy.loginByFixture('director');
    cy.visit('/admin/dashboard');
    cy.wait('@getDashboard', { timeout: 15000 });

    cy.contains('button', 'Theo chi nhánh').click();

    // Bảng/biểu đồ theo chi nhánh phải xuất hiện (kiểm tra không crash)
    cy.get('body').should('not.contain', 'Lỗi');
  });

  // TC-57: Quản lý nhân viên
  it('TC-57: ADMIN can view user/employee management page', () => {
    cy.intercept('GET', '**/api/admin/users**').as('getUsers');
    cy.loginByFixture('admin');
    cy.visit('/admin/users');
    cy.wait('@getUsers', { timeout: 15000 });

    cy.location('pathname').should('eq', '/admin/users');
    cy.get('body', { timeout: 15000 }).should('not.contain', 'Lỗi');
  });

  // TC-58: Quản lý chi nhánh
  it('TC-58: ADMIN can view branch management page', () => {
    cy.intercept('GET', '**/api/branches**').as('getBranches');
    cy.loginByFixture('admin');
    cy.visit('/admin/branches');
    cy.wait('@getBranches', { timeout: 15000 });

    cy.location('pathname').should('eq', '/admin/branches');
    cy.get('body', { timeout: 15000 }).should('not.contain', 'Lỗi');
  });

  // TC-59: Cấu hình hệ thống
  it('TC-59: ADMIN can access system configuration page', () => {
    cy.intercept('GET', '**/api/system-config**').as('getSystemConfig');
    cy.loginByFixture('admin');
    cy.visit('/admin/config');
    cy.wait('@getSystemConfig', { timeout: 15000 });

    cy.location('pathname').should('eq', '/admin/config');
    cy.get('body').should('not.be.empty');
  });

  // TC-60: Nhật ký kiểm toán
  it('TC-60: ADMIN can view audit logs page', () => {
    cy.intercept('GET', '**/api/audit-logs**').as('getAuditLogs');
    cy.loginByFixture('admin');
    cy.visit('/admin/audit-logs');
    cy.wait('@getAuditLogs', { timeout: 15000 });

    cy.location('pathname').should('eq', '/admin/audit-logs');
    cy.get('body').should('not.be.empty');
  });

  // TC-61: MANAGER không truy cập /admin/dashboard
  it('TC-61: MANAGER is redirected away from /admin/dashboard', () => {
    cy.loginByFixture('manager1');
    cy.visit('/admin/dashboard');

    cy.location('pathname', { timeout: 10000 }).should('eq', '/');
  });

  // TC-62: DIRECTOR redirect về /admin/dashboard khi vào redirectPath
  it('TC-62: DIRECTOR is redirected to /admin/dashboard after login', () => {
    cy.intercept('GET', '**/api/dashboard**').as('getDashboard');
    cy.loginByFixture('director').then((user) => {
      cy.visit(user.redirectPath);
      cy.wait('@getDashboard', { timeout: 15000 });
      cy.location('pathname', { timeout: 10000 }).should('eq', '/admin/dashboard');
    });
  });
});
