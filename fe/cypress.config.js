import { defineConfig } from 'cypress';

const baseUrl = process.env.CYPRESS_BASE_URL || 'http://localhost:3000';
const apiUrl = process.env.CYPRESS_API_URL || 'http://localhost:8080';

export default defineConfig({
  e2e: {
    baseUrl,
    specPattern: 'cypress/e2e/**/*.cy.js',
    supportFile: 'cypress/support/e2e.js',
    viewportWidth: 1280,
    viewportHeight: 720,
    video: true,
    screenshotOnRunFailure: true,
    defaultCommandTimeout: 8000,
    requestTimeout: 10000,
    retries: {
      runMode: 1,
      openMode: 0,
    },
    env: {
      apiUrl,
    },
    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
  },
});