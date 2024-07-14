import { HttpClient } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import {firstValueFrom } from "rxjs";

@Injectable({providedIn: 'root'})
export class GoogleAuthService {

    private readonly http = inject(HttpClient);

    getAuthorizationUrl(): Promise<any> {
        console.log("Calling google auth...")
        return firstValueFrom(this.http.get<string>("/api/google/authorize-url"));
    }
}