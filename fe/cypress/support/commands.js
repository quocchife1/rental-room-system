const API_URL = Cypress.env('apiUrl') || 'http://localhost:8080';

/**
 * loginByFixture: Login thật qua UI (không mock API).
 * Đọc username/password từ fixtures/users.json, submit form login thật.
 *
 * Ví dụ: cy.loginByFixture('manager1')
 */
Cypress.Commands.add('loginByFixture', (fixtureKey) => {
  return cy.fixture('users').then((users) => {
    const user = users[fixtureKey];

    if (!user) {
      throw new Error(
        `Fixture user not found: "${fixtureKey}". Available keys: ${Object.keys(users).join(', ')}`
      );
    }

    // Đăng nhập thật qua UI – KHÔNG dùng intercept mock
    cy.visit('/login');
    cy.contains('label', 'Tên đăng nhập').parent().find('input').clear().type(user.username);
    cy.contains('label', 'Mật khẩu').parent().find('input').clear().type(user.password);
    cy.contains('button', 'Đăng nhập').click();

    // Đợi redirect rời khỏi /login
    cy.location('pathname', { timeout: 20000 }).should('not.eq', '/login');

    return cy.wrap(user, { log: false });
  });
});

/**
 * loginByApi: Login nhanh qua cy.request (không cần UI), inject token thật vào localStorage.
 * Dùng cho các test không kiểm tra luồng login, chỉ cần bypass auth nhanh.
 *
 * Ví dụ: cy.loginByApi('manager1')
 */
Cypress.Commands.add('loginByApi', (fixtureKey) => {
  return cy.fixture('users').then((users) => {
    const user = users[fixtureKey];
    if (!user) throw new Error(`Fixture user not found: "${fixtureKey}"`);

    return cy
      .request({
        method: 'POST',
        url: `${API_URL}/api/auth/login`,
        body: { username: user.username, password: user.password },
        failOnStatusCode: true,
      })
      .then((response) => {
        // BE trả về { data: { accessToken, tokenType, id, username, role, ... } }
        const payload = response.body?.data || response.body;
        const token = payload.accessToken;

        const authPayload = {
          token,
          username: payload.username,
          fullName: payload.fullName || payload.username,
          role: payload.role,
          id: payload.id,
        };

        // Visit trang trống để có window trước khi set localStorage
        cy.visit('/login');
        cy.window().then((win) => {
          win.localStorage.setItem('token', token);
          win.localStorage.setItem('user', JSON.stringify(authPayload));
        });

        // Điều hướng đến app (thoát khỏi login)
        cy.visit(user.redirectPath || '/');
        cy.location('pathname', { timeout: 20000 }).should('not.eq', '/login');

        return cy.wrap({ ...user, ...authPayload }, { log: false });
      });
  });
});

/**
 * loginByLocalStorage: Alias của loginByApi để tương thích với code cũ.
 */
Cypress.Commands.add('loginByLocalStorage', (fixtureKey) => {
  return cy.loginByApi(fixtureKey);
});

Cypress.Commands.add('logoutByUi', () => {
  cy.window().then((win) => {
    const rawUser = win.localStorage.getItem('user');
    const user = rawUser ? JSON.parse(rawUser) : null;

    if (user?.username) {
      cy.contains('span', user.username).should('be.visible').click();
      return;
    }

    cy.get('header button').first().click();
  });

  cy.contains('button', 'Đăng xuất').click();
  cy.location('pathname', { timeout: 10000 }).should('eq', '/login');
});
