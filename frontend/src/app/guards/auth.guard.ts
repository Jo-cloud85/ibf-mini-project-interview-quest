import { Injectable, inject } from "@angular/core";
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot } from "@angular/router";
import { AuthService } from "../services/firebase.auth.service";
import { Observable, map } from "rxjs";

@Injectable({ providedIn: 'root' })
class PermissionsService {
  
    private readonly authSvc = inject(AuthService);
    private readonly router = inject(Router);
  
    canActivate(next: ActivatedRouteSnapshot, state: RouterStateSnapshot)
        : Observable<boolean> | Promise<boolean> | boolean {
        return this.checkLogin();
    }
  

    canActivateChild(next: ActivatedRouteSnapshot, state: RouterStateSnapshot)
        : Observable<boolean> | Promise<boolean> | boolean {
        return this.checkLogin();
    }
  
    private checkLogin(): Observable<boolean> | Promise<boolean> | boolean {
      return this.authSvc.isSignedIn()
        .pipe(
            map(isLoggedIn => {
                if (isLoggedIn) {
                    return true;
                } else {
                    this.router.navigate(['/signin']);
                    return false;
                }
            })
        );
    }
}

export const AuthGuard: 
    CanActivateFn = (next: ActivatedRouteSnapshot, state: RouterStateSnapshot)
        : boolean| Observable<boolean> | Promise<boolean> => {
    return inject(PermissionsService).canActivate(next, state);
}