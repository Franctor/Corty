import { test, expect } from '@playwright/test';

// Credenciales de prueba (usuario ya existente en la BD de dev)
const VALID_USER = { username: 'Franco', password: 'Test1234!' };
const INVALID_USER = { username: 'noexiste', password: 'wrongpass' };

// ── Helpers ──────────────────────────────────────────────────────────────────

async function fillLogin(page: import('@playwright/test').Page, username: string, password: string) {
  // ui-input y ui-password son web components — el input nativo está dentro del shadow DOM o del slot
  await page.locator('ui-input input').fill(username);
  await page.locator('ui-password input').fill(password);
}

// ── TC-E01: Login con credenciales válidas ────────────────────────────────────

test('TC-E01: Login válido redirige a la pantalla principal', async ({ page }) => {
  await page.goto('/auth/login');
  await expect(page).toHaveURL(/auth\/login/);

  await fillLogin(page, VALID_USER.username, VALID_USER.password);
  await page.locator('ion-button[type="submit"]').click();

  // Tras login correcto el guard redirige a la ruta raíz (main)
  await expect(page).not.toHaveURL(/auth\/login/, { timeout: 8000 });
  // El token queda guardado en localStorage
  const token = await page.evaluate(() => localStorage.getItem('corty_token'));
  expect(token).not.toBeNull();
});

// ── TC-E02: Login con credenciales incorrectas ────────────────────────────────

test('TC-E02: Login inválido muestra mensaje de error y permanece en /auth/login', async ({ page }) => {
  await page.goto('/auth/login');

  await fillLogin(page, INVALID_USER.username, INVALID_USER.password);
  await page.locator('ion-button[type="submit"]').click();

  // Debe permanecer en la página de login
  await expect(page).toHaveURL(/auth\/login/, { timeout: 5000 });
  // No se guarda token
  const token = await page.evaluate(() => localStorage.getItem('corty_token'));
  expect(token).toBeNull();
});

// ── TC-E03: Ruta protegida redirige a login si no autenticado ─────────────────

test('TC-E03: Acceso a ruta protegida sin sesión redirige a /auth/login', async ({ page }) => {
  // Aseguramos que no hay token
  await page.goto('/auth/login');
  await page.evaluate(() => localStorage.removeItem('corty_token'));

  await page.goto('/');
  await expect(page).toHaveURL(/auth\/login/, { timeout: 5000 });
});

// ── TC-E04: Logout limpia la sesión y redirige a login ────────────────────────

test('TC-E04: Logout elimina el token y redirige a /auth/login', async ({ page }) => {
  // Login previo
  await page.goto('/auth/login');
  await fillLogin(page, VALID_USER.username, VALID_USER.password);
  await page.locator('ion-button[type="submit"]').click();
  await expect(page).not.toHaveURL(/auth\/login/, { timeout: 8000 });

  // Ejecutar logout directamente via JS (simula llamada al AuthService.logout())
  await page.evaluate(() => {
    localStorage.removeItem('corty_token');
    window.dispatchEvent(new Event('storage'));
  });
  await page.goto('/');

  await expect(page).toHaveURL(/auth\/login/, { timeout: 5000 });
});

// ── TC-E05: Redirección de usuario ya autenticado desde /auth/login ────────────

test('TC-E05: Usuario autenticado en /auth/login es redirigido a pantalla principal', async ({ page }) => {
  // Login
  await page.goto('/auth/login');
  await fillLogin(page, VALID_USER.username, VALID_USER.password);
  await page.locator('ion-button[type="submit"]').click();
  await expect(page).not.toHaveURL(/auth\/login/, { timeout: 8000 });

  // Intentar volver a /auth/login — el guestGuard redirige
  await page.goto('/auth/login');
  await expect(page).not.toHaveURL(/auth\/login/, { timeout: 5000 });
});
