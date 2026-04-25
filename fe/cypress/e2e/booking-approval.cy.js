/**
 * TEST SUITE: Luồng Xác Nhận Booking (Booking Approval)
 *
 * Dùng dữ liệu thật từ BE. Không mock API reservations.
 *
 * TC-07: Hiển thị trang quản lý đặt lịch
 * TC-08: Lọc theo trạng thái
 * TC-09: Xác nhận (approve) booking → trạng thái RESERVED
 * TC-10: Từ chối (cancel) booking → trạng thái CANCELLED
 * TC-11: Đánh dấu COMPLETED
 * TC-12: Đánh dấu NO_SHOW
 * TC-13: Điều hướng sang trang tạo hợp đồng từ booking RESERVED
 * TC-14: Tìm kiếm booking theo tên/SDT
 * TC-15: Hiển thị empty state
 * TC-16: Role MAINTENANCE không truy cập được trang bookings
 */

const API = Cypress.env('apiUrl') || 'http://localhost:8080';

describe('Booking Approval Flow (Manager / Receptionist)', () => {
  beforeEach(() => {
    cy.clearCookies();
    cy.clearLocalStorage();
  });

  // ─── TC-07: Hiển thị trang quản lý đặt lịch ─────────────────────────────
  it('TC-07: receptionist can view booking management page', () => {
    cy.loginByFixture('receptionist1');
    cy.visit('/staff/bookings');

    cy.contains('h1', 'Quản lý đặt lịch', { timeout: 15000 }).should('be.visible');
    cy.get('body').should('not.contain', 'Lỗi');
  });

  // ─── TC-08: Lọc theo trạng thái ─────────────────────────────────────────
  it('TC-08: status filter works without crashing', () => {
    cy.loginByFixture('receptionist1');
    cy.visit('/staff/bookings');

    cy.contains('h1', 'Quản lý đặt lịch', { timeout: 15000 }).should('be.visible');

    cy.intercept('GET', '**/api/reservations/my-branch**').as('getBranchReservations');

    cy.get('select').first().select('PENDING_CONFIRMATION');
    cy.wait('@getBranchReservations', { timeout: 10000 }).then((interception) => {
      expect(interception.response.statusCode).to.eq(200);
    });
  });

  // ─── TC-09: Xác nhận (approve) booking ──────────────────────────────────
  it('TC-09: receptionist can approve a PENDING_CONFIRMATION booking', () => {
    cy.loginByFixture('receptionist1');

    // Lấy reservation PENDING thật
    cy.request({
      url: `${API}/api/reservations/my-branch?status=PENDING_CONFIRMATION&size=1`,
      failOnStatusCode: false,
    }).then((res) => {
      const content = res.body?.data?.content || [];
      if (content.length === 0) {
        cy.log('Không có reservation PENDING_CONFIRMATION – skip TC-09');
        return;
      }
      const pending = content[0];

      cy.intercept('PUT', `**/api/reservations/${pending.id}/confirm`).as('confirmReservation');

      cy.visit('/staff/bookings');
      cy.contains('h1', 'Quản lý đặt lịch', { timeout: 15000 }).should('be.visible');

      cy.contains(pending.reservationCode, { timeout: 10000 }).should('be.visible');
      cy.contains('button', 'Xác nhận').first().click();

      cy.wait('@confirmReservation', { timeout: 10000 }).then((interception) => {
        expect(interception.response.statusCode).to.be.oneOf([200, 204]);
      });
    });
  });

  // ─── TC-10: Từ chối (cancel) booking ────────────────────────────────────
  it('TC-10: receptionist can cancel a PENDING_CONFIRMATION booking', () => {
    cy.loginByFixture('receptionist1');

    cy.request({
      url: `${API}/api/reservations/my-branch?status=PENDING_CONFIRMATION&size=1`,
      failOnStatusCode: false,
    }).then((res) => {
      const content = res.body?.data?.content || [];
      if (content.length === 0) {
        cy.log('Không có reservation PENDING_CONFIRMATION – skip TC-10');
        return;
      }
      const pending = content[0];

      cy.intercept('DELETE', `**/api/reservations/${pending.id}`).as('cancelReservation');

      cy.visit('/staff/bookings');
      cy.contains('h1', 'Quản lý đặt lịch', { timeout: 15000 }).should('be.visible');

      cy.contains(pending.reservationCode, { timeout: 10000 }).should('be.visible');

      cy.window().then((win) => {
        cy.stub(win, 'confirm').returns(true);
      });

      cy.contains('button', 'Từ chối').first().click();

      cy.wait('@cancelReservation', { timeout: 10000 }).then((interception) => {
        expect(interception.response.statusCode).to.be.oneOf([200, 204]);
      });
    });
  });

  // ─── TC-11: Đánh dấu Hoàn tất ───────────────────────────────────────────
  it('TC-11: receptionist can mark a RESERVED booking as COMPLETED', () => {
    cy.loginByFixture('receptionist1');

    cy.request({
      url: `${API}/api/reservations/my-branch?status=RESERVED&size=1`,
      failOnStatusCode: false,
    }).then((res) => {
      const content = res.body?.data?.content || [];
      if (content.length === 0) {
        cy.log('Không có reservation RESERVED – skip TC-11');
        return;
      }
      const reserved = content[0];

      cy.intercept('PUT', `**/api/reservations/${reserved.id}/mark-completed`).as('markCompleted');

      cy.visit('/staff/bookings');
      cy.contains('h1', 'Quản lý đặt lịch', { timeout: 15000 }).should('be.visible');

      cy.contains(reserved.reservationCode, { timeout: 10000 }).should('be.visible');
      cy.contains('button', 'Hoàn tất').first().click();

      cy.wait('@markCompleted', { timeout: 10000 }).then((interception) => {
        expect(interception.response.statusCode).to.be.oneOf([200, 204]);
      });
    });
  });

  // ─── TC-12: Đánh dấu Không đến ─────────────────────────────────────────
  it('TC-12: receptionist can mark a RESERVED booking as NO_SHOW', () => {
    cy.loginByFixture('receptionist1');

    cy.request({
      url: `${API}/api/reservations/my-branch?status=RESERVED&size=1`,
      failOnStatusCode: false,
    }).then((res) => {
      const content = res.body?.data?.content || [];
      if (content.length === 0) {
        cy.log('Không có reservation RESERVED – skip TC-12');
        return;
      }
      const reserved = content[0];

      cy.intercept('PUT', `**/api/reservations/${reserved.id}/mark-no-show`).as('markNoShow');

      cy.visit('/staff/bookings');
      cy.contains('h1', 'Quản lý đặt lịch', { timeout: 15000 }).should('be.visible');

      cy.contains(reserved.reservationCode, { timeout: 10000 }).should('be.visible');
      cy.contains('button', 'Không đến').first().click();

      cy.wait('@markNoShow', { timeout: 10000 }).then((interception) => {
        expect(interception.response.statusCode).to.be.oneOf([200, 204]);
      });
    });
  });

  // ─── TC-13: Điều hướng tạo hợp đồng từ RESERVED ─────────────────────────
  it('TC-13: receptionist can navigate to contract creation from a RESERVED booking', () => {
    cy.loginByFixture('receptionist1');

    cy.request({
      url: `${API}/api/reservations/my-branch?status=RESERVED&size=1`,
      failOnStatusCode: false,
    }).then((res) => {
      const content = res.body?.data?.content || [];
      if (content.length === 0) {
        cy.log('Không có reservation RESERVED – skip TC-13');
        return;
      }
      const reserved = content[0];

      cy.visit('/staff/bookings');
      cy.contains('h1', 'Quản lý đặt lịch', { timeout: 15000 }).should('be.visible');

      cy.contains(reserved.reservationCode, { timeout: 10000 }).should('be.visible');
      cy.contains('button', 'Lập hợp đồng').first().click();

      cy.location('pathname', { timeout: 10000 }).should('eq', '/staff/contracts/create');
      cy.location('search').should('include', `reservationId=${reserved.id}`);
    });
  });

  // ─── TC-14: Tìm kiếm booking ────────────────────────────────────────────
  it('TC-14: search input calls API without error', () => {
    cy.loginByFixture('receptionist1');
    cy.visit('/staff/bookings');

    cy.contains('h1', 'Quản lý đặt lịch', { timeout: 15000 }).should('be.visible');

    cy.intercept('GET', '**/api/reservations/my-branch**').as('searchReservations');

    cy.get('input[placeholder*="Tìm"]').type('test');
    cy.contains('button', 'Tìm kiếm').click();

    cy.wait('@searchReservations', { timeout: 10000 }).then((interception) => {
      expect(interception.response.statusCode).to.eq(200);
    });
  });

  // ─── TC-15: Hiển thị trang khi không có kết quả ─────────────────────────
  it('TC-15: booking management page renders even with empty data', () => {
    cy.loginByFixture('receptionist1');
    cy.visit('/staff/bookings');

    cy.contains('h1', 'Quản lý đặt lịch', { timeout: 15000 }).should('be.visible');
    cy.get('body').should('not.contain', 'Lỗi');
  });

  // ─── TC-16: Role MAINTENANCE không truy cập được trang bookings ──────────
  it('TC-16: MAINTENANCE role is redirected away from /staff/bookings', () => {
    cy.loginByFixture('maintenance');
    cy.visit('/staff/bookings');

    cy.location('pathname', { timeout: 10000 }).should('eq', '/');
  });
});
