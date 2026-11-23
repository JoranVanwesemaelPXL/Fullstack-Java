export type Role = 'KLANT' | 'MEDEWERKER' | 'CHAUFFEUR';

export const ROLES: readonly Role[] = ['KLANT', 'MEDEWERKER', 'CHAUFFEUR'];

export interface User {
  username: string;
  role: Role;
}
