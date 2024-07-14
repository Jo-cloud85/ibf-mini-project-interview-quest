import { RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';

export interface CanComponentDeactivate {
  canDeactivate: () => Observable<boolean> | Promise<boolean> | boolean;
}

export function canDeactivateGuard(
  component: CanComponentDeactivate,
  nextState?: RouterStateSnapshot
): Observable<boolean> | Promise<boolean> | boolean {
  if (nextState && nextState.url === '/interview-quest/custom-job-form') {
    // Navigating back to '/interview-quest/custom-job-form'
    return component.canDeactivate ? component.canDeactivate() : true;
  }
  return true;  // Allow navigation otherwise
}