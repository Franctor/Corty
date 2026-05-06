import { test, expect } from '@playwright/test';

test('has title', async ({ page }) => {
  await page.goto('/');

  // La página de login tiene h1 con el nombre de la app
  expect(await page.locator('h1').innerText()).toContain('Corty');
});
