describe('Route Guard E2E', () => {
  beforeEach(() => {
    cy.clearCookies();
    cy.clearLocalStorage();
  });

  it('redirects anonymous user to login when opening protected page', () => {
    cy.visit('/admin/dashboard');
    cy.location('pathname', { timeout: 10000 }).should('eq', '/login');
  });

  it('prevents role mismatch access to admin pages', () => {
    cy.loginByFixture('accountant');

    cy.visit('/admin/dashboard');
    cy.location('pathname', { timeout: 10000 }).should('eq', '/');
  });

  it('allows role-matched access to admin pages', () => {
    cy.loginByFixture('admin').then((user) => {
      cy.visit(user.redirectPath);
      cy.location('pathname', { timeout: 10000 }).should('eq', '/admin/dashboard');
    });
  });

  it('logs out and revokes protected access', () => {
    cy.loginByFixture('admin');
    cy.logoutByUi();

    cy.window().then((win) => {
      expect(win.localStorage.getItem('token')).to.eq(null);
      expect(win.localStorage.getItem('user')).to.eq(null);
    });

    cy.visit('/staff/rooms');
    cy.location('pathname', { timeout: 10000 }).should('eq', '/login');
  });
});
