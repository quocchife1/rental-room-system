describe('Auth E2E', () => {
  beforeEach(() => {
    cy.clearCookies();
    cy.clearLocalStorage();
  });

  it('shows login page', () => {
    cy.visit('/login');
    cy.contains('h2', 'Đăng nhập tài khoản').should('be.visible');
    cy.contains('label', 'Tên đăng nhập').should('be.visible');
    cy.contains('label', 'Mật khẩu').should('be.visible');
  });

  it('rejects invalid credentials', () => {
    cy.visit('/login');

    cy.contains('label', 'Tên đăng nhập').parent().find('input').type('admin');
    cy.contains('label', 'Mật khẩu').parent().find('input').type('wrong-password');
    cy.contains('button', 'Đăng nhập').click();

    cy.location('pathname', { timeout: 10000 }).should('eq', '/login');
    cy.get('.border-red-500').should('be.visible');
    // Verify still on login page
    cy.location('pathname', { timeout: 10000 }).should('eq', '/login');
    
    // Verify error message is displayed
    cy.get('.text-red-700').should('be.visible');
  });

  ['admin', 'accountant', 'maintenance', 'director'].forEach((fixtureKey) => {
    it(`logs in successfully as ${fixtureKey}`, () => {
      cy.loginByFixture(fixtureKey).then((user) => {
        // Sau khi login thật, phải rời khỏi /login
        cy.location('pathname', { timeout: 15000 }).should('not.eq', '/login');

        cy.window().then((win) => {
          const token = win.localStorage.getItem('token');
          const savedUser = JSON.parse(win.localStorage.getItem('user'));

          expect(token, 'localStorage token').to.be.a('string').and.not.be.empty;
          expect(savedUser.username).to.eq(user.username);
          expect(savedUser.role).to.eq(user.role);
        });

        // Điều hướng đến redirectPath đã cấu hình trong fixture
        cy.visit(user.redirectPath);
        cy.location('pathname', { timeout: 15000 }).should('eq', user.redirectPath);
      });
    });
  });
});
