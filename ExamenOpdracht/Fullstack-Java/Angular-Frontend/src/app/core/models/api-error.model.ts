import { HttpErrorResponse } from '@angular/common/http';

// Het vaste foutobject dat de backend teruggeeft, bv.
// { "code": "OUT_OF_STOCK", "message": "Onvoldoende arborio voor deze bestelling." }
export interface ApiError {
  status: number;
  code: string;
  message: string;
}

/** Zet elke fout (HTTP of anders) om naar één ApiError, zodat de UI er altijd mee kan werken. */
export function toApiError(error: unknown): ApiError {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return {
        status: 0,
        code: 'NETWORK_ERROR',
        message: 'De server is niet bereikbaar. Probeer het later opnieuw.',
      };
    }
    const body = error.error as Partial<ApiError> | null;
    return {
      status: error.status,
      code: body?.code ?? 'HTTP_' + error.status,
      message: body?.message ?? 'Er ging iets mis (' + error.status + ').',
    };
  }
  return { status: -1, code: 'UNKNOWN', message: 'Er ging iets onverwacht mis.' };
}
