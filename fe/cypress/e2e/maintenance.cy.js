/**
 * TEST SUITE: Quản lý Bảo trì (Maintenance)
 *
 * Dùng dữ liệu thật từ BE. Không mock API maintenance.
 *
 * TC-43: MAINTENANCE xem danh sách yêu cầu bảo trì
 * TC-44: Lọc theo tab "Chờ xử lý"
 * TC-45: Tìm kiếm theo phòng
 * TC-46: Cập nhật trạng thái PENDING → IN_PROGRESS
 * TC-47: Cập nhật trạng thái IN_PROGRESS → COMPLETED
 * TC-48: Lập hóa đơn khi yêu cầu đã COMPLETED
 * TC-49: Xem chi tiết yêu cầu (modal)
 * TC-50: Tenant gửi yêu cầu bảo trì mới
 * TC-51: Tenant xem lịch sử yêu cầu bảo trì
 * TC-52: ACCOUNTANT không truy cập /staff/maintenance/board
 */

const API = Cypress.env('apiUrl') || 'http://localhost:8080';

describe('Maintenance Management Flow', () => {
  beforeEach(() => {
    cy.clearCookies();
    cy.clearLocalStorage();
  });

  // TC-43: MAINTENANCE xem danh sách yêu cầu
  it('TC-43: MAINTENANCE role can view maintenance board', () => {
    cy.loginByFixture('maintenance');
    cy.visit('/staff/maintenance/board');

    cy.contains('h1', 'Quản Lý Bảo Trì', { timeout: 15000 }).should('be.visible');
    cy.get('body').should('not.contain', 'Lỗi');
  });

  // TC-44: Tab lọc "Chờ xử lý"
  it('TC-44: filter tab "Chờ xử lý" works without error', () => {
    cy.loginByFixture('maintenance');
    cy.visit('/staff/maintenance/board');

    cy.contains('h1', 'Quản Lý Bảo Trì', { timeout: 15000 }).should('be.visible');
    cy.contains('button', 'Chờ xử lý').click();

    // Sau khi click tab, chỉ có PENDING được hiển thị
    // Nếu không có PENDING nào thì không nên có row IN_PROGRESS hoặc COMPLETED
    cy.contains('Đang xử lý').should('not.exist');
    cy.contains('Hoàn thành').should('not.exist');
  });

  // TC-45: Tìm kiếm theo phòng
  it('TC-45: search by room number filters results', () => {
    cy.loginByFixture('maintenance');
    cy.visit('/staff/maintenance/board');

    cy.contains('h1', 'Quản Lý Bảo Trì', { timeout: 15000 }).should('be.visible');

    // Lấy item thật để lấy số phòng
    cy.request({
      url: `${API}/api/maintenance/board`,
      failOnStatusCode: false,
    }).then((res) => {
      const items = res.body?.data || [];
      if (items.length === 0) {
        cy.log('Không có maintenance request – skip TC-45');
        return;
      }
      const roomNum = items[0].roomNumber;

      cy.get('input[placeholder*="Tìm kiếm"]').type(roomNum);

      // Kết quả phải chứa đúng số phòng này
      cy.contains(roomNum).should('be.visible');
    });
  });

  // TC-46: Cập nhật trạng thái PENDING → IN_PROGRESS
  it('TC-46: MAINTENANCE can update status from PENDING to IN_PROGRESS', () => {
    cy.loginByFixture('maintenance');

    cy.request({
      url: `${API}/api/maintenance/board`,
      failOnStatusCode: false,
    }).then((res) => {
      const items = res.body?.data || [];
      const pending = items.find((i) => i.status === 'PENDING');
      if (!pending) {
        cy.log('Không có PENDING request – skip TC-46');
        return;
      }

      cy.intercept('PUT', `**/api/maintenance/${pending.id}/status`).as('updateStatus');

      cy.visit('/staff/maintenance/board');
      cy.contains('h1', 'Quản Lý Bảo Trì', { timeout: 15000 }).should('be.visible');

      cy.contains(pending.requestCode, { timeout: 10000 })
        .closest('tr')
        .find('select')
        .select('IN_PROGRESS');

      cy.wait('@updateStatus', { timeout: 10000 }).then((interception) => {
        expect(interception.response.statusCode).to.be.oneOf([200, 204]);
      });
    });
  });

  // TC-47: Cập nhật trạng thái IN_PROGRESS → COMPLETED
  it('TC-47: MAINTENANCE can mark a request as COMPLETED', () => {
    cy.loginByFixture('maintenance');

    cy.request({
      url: `${API}/api/maintenance/board`,
      failOnStatusCode: false,
    }).then((res) => {
      const items = res.body?.data || [];
      const inProgress = items.find((i) => i.status === 'IN_PROGRESS');
      if (!inProgress) {
        cy.log('Không có IN_PROGRESS request – skip TC-47');
        return;
      }

      cy.intercept('PUT', `**/api/maintenance/${inProgress.id}/status`).as('updateStatus');

      cy.visit('/staff/maintenance/board');
      cy.contains('h1', 'Quản Lý Bảo Trì', { timeout: 15000 }).should('be.visible');

      cy.contains(inProgress.requestCode, { timeout: 10000 })
        .closest('tr')
        .find('select')
        .select('COMPLETED');

      cy.wait('@updateStatus', { timeout: 10000 }).then((interception) => {
        expect(interception.response.statusCode).to.be.oneOf([200, 204]);
      });
    });
  });

  // TC-48: Lập hóa đơn khi yêu cầu COMPLETED và chưa có hóa đơn
  it('TC-48: can create invoice for a COMPLETED maintenance request', () => {
    cy.loginByFixture('maintenance');

    cy.request({
      url: `${API}/api/maintenance/board`,
      failOnStatusCode: false,
    }).then((res) => {
      const items = res.body?.data || [];
      const completed = items.find((i) => i.status === 'COMPLETED' && !i.invoiceId);
      if (!completed) {
        cy.log('Không có COMPLETED request chưa có invoice – skip TC-48');
        return;
      }

      cy.intercept('POST', `**/api/maintenance/${completed.id}/invoice`).as('createMaintenanceInvoice');

      cy.visit('/staff/maintenance/board');
      cy.contains('h1', 'Quản Lý Bảo Trì', { timeout: 15000 }).should('be.visible');

      cy.contains(completed.requestCode, { timeout: 10000 })
        .closest('tr')
        .find('button[title="Lập hóa đơn"]')
        .click();

      cy.contains('Lập hóa đơn lỗi người thuê', { timeout: 8000 }).should('be.visible');

      const cost = completed.cost || 100000;
      cy.get('input[type="number"]').clear().type(cost.toString());
      cy.contains('button', 'Xác nhận tạo').click();

      cy.wait('@createMaintenanceInvoice', { timeout: 10000 }).then((interception) => {
        expect(interception.response.statusCode).to.be.oneOf([200, 201]);
      });
    });
  });

  // TC-49: Xem chi tiết yêu cầu (modal)
  it('TC-49: clicking detail icon opens detail modal', () => {
    cy.loginByFixture('maintenance');

    cy.request({
      url: `${API}/api/maintenance/board`,
      failOnStatusCode: false,
    }).then((res) => {
      const items = res.body?.data || [];
      if (items.length === 0) {
        cy.log('Không có maintenance request – skip TC-49');
        return;
      }
      const item = items[0];

      cy.visit('/staff/maintenance/board');
      cy.contains('h1', 'Quản Lý Bảo Trì', { timeout: 15000 }).should('be.visible');

      cy.contains(item.requestCode, { timeout: 10000 })
        .closest('tr')
        .find('button[title="Xem chi tiết"]')
        .click();

      cy.contains('Chi tiết yêu cầu', { timeout: 8000 }).should('be.visible');
      cy.contains(item.description).should('be.visible');

      cy.contains('button', 'Đóng lại').click();
      cy.contains('Chi tiết yêu cầu').should('not.exist');
    });
  });

  // TC-50: Tenant gửi yêu cầu bảo trì mới
  it('TC-50: user can submit a new maintenance request if they have an active contract', () => {
    cy.loginByFixture('receptionist1');

    cy.intercept('POST', '**/api/maintenance').as('createMaintenance');

    cy.visit('/tenant/maintenance');

    // Nếu có nút tạo yêu cầu → điền form và submit
    cy.get('body', { timeout: 15000 }).then(($body) => {
      if (!$body.text().includes('Tạo yêu cầu mới')) {
        cy.log('Không có nút tạo yêu cầu (chưa có hợp đồng active) – skip TC-50');
        return;
      }

      cy.contains('button', 'Tạo yêu cầu mới').click();
      cy.contains('Thông tin sự cố', { timeout: 8000 }).should('be.visible');

      cy.get('textarea[placeholder*="Mô tả chi tiết"]').type('Điều hòa bị hỏng, không tắt được.');

      cy.window().then((win) => {
        cy.stub(win, 'alert').returns(undefined);
      });

      cy.contains('button', 'Gửi yêu cầu').click();

      cy.wait('@createMaintenance', { timeout: 15000 }).then((interception) => {
        expect(interception.response.statusCode).to.be.oneOf([200, 201]);
      });
    });
  });

  // TC-51: Tenant xem lịch sử yêu cầu
  it('TC-51: tenant can view their maintenance request history page', () => {
    cy.loginByFixture('receptionist1');
    cy.visit('/tenant/maintenance');

    cy.location('pathname', { timeout: 10000 }).should('eq', '/tenant/maintenance');
    cy.get('body').should('not.contain', 'Lỗi');
  });

  // TC-52: ACCOUNTANT không truy cập /staff/maintenance/board
  it('TC-52: ACCOUNTANT is redirected away from maintenance board', () => {
    cy.loginByFixture('accountant');
    cy.visit('/staff/maintenance/board');

    cy.location('pathname', { timeout: 10000 }).should('eq', '/');
  });
});
