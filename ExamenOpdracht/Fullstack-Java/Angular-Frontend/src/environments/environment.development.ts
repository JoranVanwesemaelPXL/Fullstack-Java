// Development-configuratie (ng serve).
// Zolang de echte backend nog niet via de gateway bereikbaar is, draait de
// mock-backend op poort 3000 (npm run mock). Daarna: 'http://localhost:8080/api'.
export const environment = {
  production: false,
  apiUrl: 'http://localhost:3000/api',
};
