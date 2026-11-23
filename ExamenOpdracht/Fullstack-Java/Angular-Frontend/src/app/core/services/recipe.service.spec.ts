import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { Recipe } from '../models/recipe.model';
import { RecipeService } from './recipe.service';

describe('RecipeService', () => {
  let service: RecipeService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(RecipeService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('haalt het weekmenu op via GET /recipes', () => {
    const mock: Recipe[] = [
      {
        id: 1, name: 'Risotto', description: '', prepTimeMinutes: 30, servings: 2,
        allergens: ['melk'], ingredients: [], imageUrl: null, week: '2026-W48',
      },
    ];
    let result: Recipe[] | undefined;

    service.getWeekMenu().subscribe((r) => (result = r));

    const req = http.expectOne(`${environment.apiUrl}/recipes`);
    expect(req.request.method).toBe('GET');
    req.flush(mock);
    expect(result).toEqual(mock);
  });
});
