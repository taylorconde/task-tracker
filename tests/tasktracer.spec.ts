import { test, expect, Page } from '@playwright/test';

test.describe.configure({ mode: 'serial' });
test.beforeEach(async ({ page }: { page: Page }) => {

  await page.goto('http://localhost:8080');

  await page.evaluate(() => {
    window.localStorage.clear();
  })
});

test('Deve adicionar uma nova tarefa no Kanban', async ({ page }: { page: Page }) => {

  const nomeTarefa = `Nova Task ${Date.now()}`;
  const cards = page.locator('#TODO .task-card');
  const cardsBefore = await cards.count();

  // 1. Clicar no botão de adicionar nova tarefa da coluna "A fazer "
  await page.click('.kanban-column:has(#TODO) >> .btn-add-task');
  // 2. Preencher a textarea que ficou visível
  await page.fill('.kanban-column:has(#TODO) >> .form-control', nomeTarefa);
  // 3. Disparar o 'blur' (clicar fora ou Enter) para salvar.
  await page.dispatchEvent('.kanban-column:has(#TODO) >> .form-control', 'blur');

  // 4. Validação: O card com o texto deve aparecer.
  await expect(cards.last()).toContainText(nomeTarefa);
  await expect(cards).toHaveCount(cardsBefore + 1);
});

test('Deve mover uma tarefa de "A fazer" para "Em Progresso"', async ({ page }: { page: Page }) => {

  // 1. Criar a tarefa na coluna inicial
  const nomeTarefa = `Task Movel ${Date.now()}`;
  await page.click('.kanban-column:has(#TODO) >> .btn-add-task');
  await page.fill('.kanban-column:has(#TODO) >> .form-control', nomeTarefa);
  await page.dispatchEvent('.kanban-column:has(#TODO) >> .form-control', 'blur');

  // 2. Definir origem e destino
  const origem = page.locator('#TODO .task-card').filter({ hasText: nomeTarefa });
  const destino = page.locator('#IN_PROGRESS'); // Agora esta área é grande!

  await expect(origem.last()).toBeVisible();

  // A. Pegamos a posição exata (BOX)
  const boxOrigem = await origem.boundingBox();
  const boxDestino = await destino.boundingBox();

  if (boxOrigem && boxDestino) {
    // 1. Move para o topo
    await page.mouse.move(boxOrigem.x + boxOrigem.width / 2, boxOrigem.y + 15);
    await page.mouse.down();

    // 2. O "EMPURRÃO": Move só 5 pixels para baixo para o navegador entender que começou um arraste
    await page.mouse.move(boxOrigem.x + boxOrigem.width / 2, boxOrigem.y + 20, { steps: 5 });
    await page.waitForTimeout(200);

    // 3. A VIAGEM: Move para o destino
    await page.mouse.move(boxDestino.x + boxDestino.width / 2, boxDestino.y + boxDestino.height / 2, { steps: 40 });

    // 4. Pausa antes de soltar (crucial para o drop ser aceito)
    await page.waitForTimeout(200);
    await page.mouse.up();
  }

  // 4. Validação
  // Procuramos o card especificamente dentro da coluna de destino
  const cardNoDestino = page.locator('#IN_PROGRESS .task-card').filter({ hasText: nomeTarefa });
  await expect(cardNoDestino).toBeVisible();
});

test('Deve deletar uma tarefa', async ({ page }) => {
  // 1. Setup: Criar uma tarefa para ser deletada
  const nomeTarefa = `Task Delete ${Date.now()}`;
  await page.click('.kanban-column:has(#TODO) >> .btn-add-task');
  await page.fill('.kanban-column:has(#TODO) >> .form-control', nomeTarefa);
  await page.dispatchEvent('.kanban-column:has(#TODO) >> .form-control', 'blur');

  // 2. Interação
  const card = page.locator('#TODO .task-card').filter({ hasText: nomeTarefa });
  const botaoDeletar = card.locator('.delete-btn');
  const eventoConsole = page.waitForEvent('console', msg => msg.text().includes('Task deletada com sucesso!'));

  await card.hover();
  await expect(botaoDeletar).toBeVisible();
  // Em vez de .click({ force: true }), usamos isso:
  await botaoDeletar.dispatchEvent('click');
  // 3. Validação: O cartão não deve mais existir
  await expect(card).not.toBeVisible();
});
