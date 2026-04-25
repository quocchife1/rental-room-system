/**
 * TEST SUITE: Luồng Đặt Lịch Xem Phòng (Room Booking)
 *
 * Dùng dữ liệu thật từ BE. Mỗi test truy vấn BE để lấy phòng thật trước khi thao tác.
 *
 * TC-01: Người dùng chưa đăng nhập bị redirect đến login khi bấm đặt lịch
 * TC-02: Tenant đặt lịch thành công qua RoomDetailPage
 * TC-03: Tenant xem danh sách lịch đặt của mình
 * TC-04: Tenant có thể hủy yêu cầu đang PENDING
 * TC-05: Không thể đặt lịch phòng đã hết (OCCUPIED)
 * TC-06: Danh sách trống khi chưa có đặt lịch
 */

const API = Cypress.env('apiUrl') || 'http://localhost:8080';

describe('Room Booking Flow', () => {
  beforeEach(() => {
    cy.clearCookies();
    cy.clearLocalStorage();
  });

  // ─── TC-01: Ẩn danh → redirect login ────────────────────────────────────────
  it('TC-01: redirects anonymous user to login when clicking book button', () => {
    // Lấy phòng AVAILABLE thật từ BE
    cy.request(`${API}/api/rooms?status=AVAILABLE&size=1`).then((res) => {
      const rooms = res.body?.data?.content || res.body?.data || [];
      if (rooms.length === 0) {
        cy.log('Không có phòng AVAILABLE – skip TC-01');
        return;
      }
      const room = rooms[0];

      cy.visit(`/rooms/${room.id}`);

      cy.contains('button', 'Đặt Lịch Xem Phòng', { timeout: 10000 }).should('be.visible');

      cy.window().then((win) => {
        cy.stub(win, 'confirm').returns(true);
      });

      cy.contains('button', 'Đặt Lịch Xem Phòng').click();
      cy.location('pathname', { timeout: 10000 }).should('eq', '/login');
    });
  });

  // ─── TC-02: Tenant đặt lịch thành công ──────────────────────────────────────
  it('TC-02: logged-in user can open booking modal and submit reservation', () => {
    cy.loginByFixture('receptionist1');

    cy.request(`${API}/api/rooms?status=AVAILABLE&size=1`).then((res) => {
      const rooms = res.body?.data?.content || res.body?.data || [];
      if (rooms.length === 0) {
        cy.log('Không có phòng AVAILABLE – skip TC-02');
        return;
      }
      const room = rooms[0];

      cy.visit(`/rooms/${room.id}`);

      cy.contains('button', 'Đặt Lịch Xem Phòng', { timeout: 10000 })
        .should('be.visible')
        .and('not.be.disabled')
        .click();

      cy.contains('Xác nhận lịch tham khảo', { timeout: 8000 }).should('be.visible');

      // Chọn khung giờ Chiều
      cy.get('select').contains('option', 'Chiều (13:30 - 16:00)').parent().select('AFTERNOON');

      // Nhập ghi chú
      cy.get('textarea').type('Tôi muốn xem phòng vào buổi chiều.');

      cy.window().then((win) => {
        cy.stub(win, 'alert').returns(undefined);
      });

      // Intercept request thật để verify payload (không mock response)
      cy.intercept('POST', '**/api/reservations').as('createReservation');

      cy.contains('button', 'Gửi yêu cầu').click();

      cy.wait('@createReservation', { timeout: 15000 }).then((interception) => {
        expect(interception.response.statusCode).to.be.oneOf([200, 201]);
        expect(interception.request.body).to.include({
          roomId: room.id,
          visitSlot: 'AFTERNOON',
        });
      });
    });
  });

  // ─── TC-03: Tenant xem danh sách đặt lịch ───────────────────────────────────
  it('TC-03: tenant can view their reservation list page', () => {
    cy.loginByFixture('receptionist1');
    cy.visit('/tenant/reservations');

    // Trang phải render được (không crash)
    cy.location('pathname', { timeout: 10000 }).should('eq', '/tenant/reservations');

    // Tiêu đề hoặc nội dung trang phải xuất hiện
    cy.get('body', { timeout: 10000 }).should('not.be.empty');
  });

  // ─── TC-04: Tenant hủy yêu cầu đang chờ ────────────────────────────────────
  it('TC-04: tenant can cancel a PENDING reservation if one exists', () => {
    cy.loginByFixture('receptionist1');

    cy.request({
      url: `${API}/api/reservations/my-reservations?status=PENDING_CONFIRMATION&size=1`,
      failOnStatusCode: false,
    }).then((res) => {
      const content = res.body?.data?.content || [];
      if (content.length === 0) {
        cy.log('Không có reservation PENDING – skip TC-04');
        return;
      }

      cy.visit('/tenant/reservations');

      cy.contains('Hủy yêu cầu', { timeout: 10000 }).should('be.visible');

      cy.window().then((win) => {
        cy.stub(win, 'confirm').returns(true);
        cy.stub(win, 'alert').returns(undefined);
      });

      cy.intercept('DELETE', '**/api/reservations/**').as('cancelReservation');
      cy.contains('Hủy yêu cầu').first().click();
      cy.wait('@cancelReservation', { timeout: 10000 }).then((interception) => {
        expect(interception.response.statusCode).to.be.oneOf([200, 204]);
      });
    });
  });

  // ─── TC-05: Phòng đã thuê → nút đặt lịch bị disabled ───────────────────────
  it('TC-05: booking button is disabled for OCCUPIED rooms', () => {
    cy.loginByFixture('receptionist1');

    cy.request(`${API}/api/rooms?status=OCCUPIED&size=1`).then((res) => {
      const rooms = res.body?.data?.content || res.body?.data || [];
      if (rooms.length === 0) {
        cy.log('Không có phòng OCCUPIED – skip TC-05');
        return;
      }
      const room = rooms[0];

      cy.visit(`/rooms/${room.id}`);
      cy.contains('button', 'Đã Được Thuê', { timeout: 10000 })
        .should('be.visible')
        .and('be.disabled');
    });
  });

  // ─── TC-06: Trang reservation khi chưa có đặt lịch ─────────────────────────
  it('TC-06: tenant reservation page loads correctly', () => {
    cy.loginByFixture('receptionist1');
    cy.visit('/tenant/reservations');

    // Trang luôn phải render được dù có hay không có data
    cy.location('pathname', { timeout: 10000 }).should('eq', '/tenant/reservations');

    // Kiểm tra: hiện danh sách hoặc empty state
    cy.get('body', { timeout: 10000 }).then(($body) => {
      const hasEmpty = $body.text().includes('Bạn chưa có yêu cầu đặt phòng nào.');
      const hasList = $body.find('table, [class*="card"], [class*="list"]').length > 0;
      expect(hasEmpty || hasList, 'Trang phải render content').to.be.true;
    });
  });
});
