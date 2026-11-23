// Productie-configuratie (ng build).
// De Angular-app praat altijd met de API-gateway van de Spring Cloud-backend.
export const environment = {
  production: true,
  apiUrl: 'http://localhost:8080/api',
};
