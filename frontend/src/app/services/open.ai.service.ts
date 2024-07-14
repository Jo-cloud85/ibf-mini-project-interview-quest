import { AuthService } from './firebase.auth.service';
import { HttpClient } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable, catchError, map, switchMap, throwError, timeout } from "rxjs";

@Injectable({providedIn: "root"})
export class OpenAIService{

    private readonly http = inject(HttpClient);
    private readonly authSvc = inject(AuthService);
    private readonly baseUrl = "/api/ai"

    // Post new custom job
    createNewCustomJob(formData: FormData): Observable<any> {
        return this.http
        .post(`${this.baseUrl}/create-job`, formData)
        .pipe(
            catchError(error => {
                console.error('Request error: ', error);
                return throwError(() => new Error(error.message));
            })
        );
    }

    // Post reply to AI (either "Generate answer" or user input)
    requestFeedback(userInput: string, customJobId: string, qnsId: string): Observable<any> {
        return this.http
        .post(`${this.baseUrl}/${customJobId}/${qnsId}/request-feedback`, userInput)
        .pipe(
            catchError(error => {
                console.error('Request error: ', error);
                return throwError(() => new Error(error.message));
            })
        );
    }
}