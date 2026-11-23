// Eenvoudige mock-backend voor de Angular-frontend, zonder extra packages.
// Start met:  npm run mock   → http://localhost:3000/api
// Wordt vervangen door de Spring Cloud-gateway (http://localhost:8080/api) zodra die draait.
import { createServer } from 'node:http';

const PORT = 3000;

const recipes = [
  {
    id: 1, name: 'Romige champignonrisotto', week: '2026-W48',
    description: 'Arborio met kastanjechampignons, parmezaan en verse tijm.',
    prepTimeMinutes: 35, servings: 2, allergens: ['melk', 'selderij'], imageUrl: null,
    ingredients: [
      { ingredientId: 1, name: 'arborio', quantity: 90, unit: 'g' },
      { ingredientId: 2, name: 'champignons', quantity: 125, unit: 'g' },
      { ingredientId: 3, name: 'parmezaan', quantity: 20, unit: 'g' },
    ],
  },
  {
    id: 2, name: 'Thaise groene curry met kip', week: '2026-W48',
    description: 'Kip, kokosmelk, sperziebonen en jasmijnrijst.',
    prepTimeMinutes: 25, servings: 2, allergens: ['vis', 'soja'], imageUrl: null,
    ingredients: [
      { ingredientId: 4, name: 'kipfilet', quantity: 125, unit: 'g' },
      { ingredientId: 5, name: 'kokosmelk', quantity: 100, unit: 'ml' },
      { ingredientId: 6, name: 'jasmijnrijst', quantity: 75, unit: 'g' },
    ],
  },
  {
    id: 3, name: 'Volkoren pasta pesto', week: '2026-W48',
    description: 'Met kerstomaatjes, rucola en geroosterde pijnboompitten.',
    prepTimeMinutes: 20, servings: 2, allergens: ['gluten', 'melk', 'noten'], imageUrl: null,
    ingredients: [
      { ingredientId: 7, name: 'volkoren penne', quantity: 90, unit: 'g' },
      { ingredientId: 8, name: 'basilicumpesto', quantity: 30, unit: 'g' },
    ],
  },
  {
    id: 4, name: 'Zalm met zoete aardappel', week: '2026-W48',
    description: 'Ovenzalm, zoete-aardappelpartjes en citroen-yoghurtsaus.',
    prepTimeMinutes: 30, servings: 2, allergens: ['vis', 'melk'], imageUrl: null,
    ingredients: [
      { ingredientId: 9, name: 'zalmfilet', quantity: 120, unit: 'g' },
      { ingredientId: 10, name: 'zoete aardappel', quantity: 200, unit: 'g' },
    ],
  },
  {
    id: 5, name: 'Linzendahl met naan', week: '2026-W48',
    description: 'Rode linzen, tomaat, spinazie en warme kruiden.',
    prepTimeMinutes: 30, servings: 4, allergens: ['gluten', 'mosterd'], imageUrl: null,
    ingredients: [
      { ingredientId: 11, name: 'rode linzen', quantity: 70, unit: 'g' },
      { ingredientId: 12, name: 'naan', quantity: 1, unit: 'stuk' },
    ],
  },
  {
    id: 6, name: 'Buddha bowl met falafel', week: '2026-W48',
    description: 'Quinoa, hummus, geroosterde groenten en granaatappel.',
    prepTimeMinutes: 25, servings: 2, allergens: ['sesam'], imageUrl: null,
    ingredients: [
      { ingredientId: 13, name: 'quinoa', quantity: 70, unit: 'g' },
      { ingredientId: 14, name: 'falafel', quantity: 4, unit: 'stuk' },
    ],
  },
];

function send(res, status, body) {
  res.writeHead(status, {
    'Content-Type': 'application/json',
    'Access-Control-Allow-Origin': 'http://localhost:4200',
    'Access-Control-Allow-Headers': 'Content-Type, X-User, X-Role',
    'Access-Control-Allow-Methods': 'GET, POST, PUT, PATCH, DELETE, OPTIONS',
  });
  res.end(body === undefined ? '' : JSON.stringify(body));
}

const server = createServer((req, res) => {
  if (req.method === 'OPTIONS') return send(res, 204);

  const url = new URL(req.url, `http://${req.headers.host}`);
  console.log(`${req.method} ${url.pathname}  user=${req.headers['x-user'] ?? '-'} role=${req.headers['x-role'] ?? '-'}`);

  // US1 — weekmenu
  if (req.method === 'GET' && url.pathname === '/api/recipes') {
    return send(res, 200, recipes);
  }
  const match = url.pathname.match(/^\/api\/recipes\/(\d+)$/);
  if (req.method === 'GET' && match) {
    const recipe = recipes.find((r) => r.id === Number(match[1]));
    return recipe
      ? send(res, 200, recipe)
      : send(res, 404, { code: 'RECIPE_NOT_FOUND', message: `Recept ${match[1]} bestaat niet.` });
  }

  send(res, 404, { code: 'NOT_FOUND', message: `Onbekend pad: ${url.pathname}` });
});

server.listen(PORT, () => console.log(`Mock-backend draait op http://localhost:${PORT}/api`));
