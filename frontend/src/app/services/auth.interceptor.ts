import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable, from, switchMap } from "rxjs";
import { AuthService } from "./firebase.auth.service";

/* Intercept all httpClient requests to the backend to make sure each request 
is authenticated first. */

@Injectable({providedIn: 'root'})
export class AuthInterceptor implements HttpInterceptor {

	private readonly authSvc = inject(AuthService);

	intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
		return from(this.authSvc.getIdToken())
		.pipe(
			switchMap(token => {
				if (token) {
					const clonedReq = req.clone({
						headers: req.headers.set('Authorization', `Bearer ${token}`)
					});
					return next.handle(clonedReq);
				} else {
					return next.handle(req);
				}
			})
		);
	}
}