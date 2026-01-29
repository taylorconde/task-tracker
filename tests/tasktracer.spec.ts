const { test, expect } = require('@playwright/test');

test.beforeEach(async ({ page }) => {
  await page.goto('http://localhost:8080');
});

test('Deve adicionar uma nova tarefa (Modo Compatibilidade)', async ({ page }) => {
  const nomeTarefa = `Teste ${Date.now()}`;

  // 1. Cria a tarefa
  await page.fill('#taskInput', nomeTarefa);
  await page.click('#addTaskBtn');

  // 2. Espera o input de cima limpar (garante que o JS rodou)
  await expect(page.locator('#taskInput')).toBeEmpty();

  // 3. Espera a lista ter pelo menos 1 item (para não dar erro de lista vazia)
  await expect(page.locator('#taskList input').first()).toBeVisible();

  // 4. A MÁGICA DO MACGYVER ($$eval):
  // Entra no navegador e varre todos os inputs da lista, extraindo o valor real (.value)
  // Isso ignora se é atributo ou propriedade. É JavaScript puro rodando no Chrome.
  const valoresEncontrados = await page.$$eval('#taskList input', (elementos) => {
    return elementos.map(input => input.value);
  });

  console.log('Tarefas na tela:', valoresEncontrados);

  // 5. Validação
  // Verifica se o nosso texto está no array de textos que trouxemos do navegador
  expect(valoresEncontrados).toContain(nomeTarefa);
});

// CENÁRIO 2: História do TDD (Compatibilidade Total)
test.fail('Deve filtrar tarefas: Tarefas "A Fazer" não devem aparecer no filtro "Concluída"', async ({ page }) => {

  const tarefaPendente = `Pendente ${Date.now()}`;

  // 1. Cria a tarefa
  await page.fill('#taskInput', tarefaPendente);
  await page.click('#addTaskBtn');

  // Espera limpar
  await expect(page.locator('#taskInput')).toBeEmpty();

  // 2. Aplica o filtro
  await page.check('#done');

  // 3. VALIDAÇÃO COM $$eval (A mesma técnica que funcionou)
  // Buscamos TODOS os textos que estão na tela agora
  const tarefasVisiveis = await page.$$eval('#taskList input', (elementos) => {
    // Filtra apenas os que estão visíveis (caso o CSS esconda algum)
    return elementos
      .filter(el => el.offsetParent !== null)
      .map(el => el.value);
  });

  console.log('Tarefas visíveis após filtro:', tarefasVisiveis);

  // Como o filtro NÃO funciona, a tarefa pendente AINDA vai estar lá.
  // O expect vai falhar (porque pedimos .not.toContain).
  // O test.fail() vai capturar e dar SUCESSO.
  expect(tarefasVisiveis).not.toContain(tarefaPendente);
});