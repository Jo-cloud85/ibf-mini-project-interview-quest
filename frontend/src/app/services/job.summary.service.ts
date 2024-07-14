import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { firstValueFrom } from "rxjs";
import { JobSummary } from "../models/logic.models";

@Injectable({providedIn: "root"})
export class JobSummaryService {
    
    private readonly http = inject(HttpClient);
    private readonly baseUrl = "/api/jobs";


    // Get all job summary - where query is optional
    getAllJobSummaryByUserIdByQuery(search: string): Promise<JobSummary[]> {
        const query = new HttpParams().set('q', search)
            
        return firstValueFrom(
            this.http.get<JobSummary[]>(
                `${this.baseUrl}/all-custom-jobs`, { params: query, observe: 'response' }
            )
        )
        .then((response) => {
            if (response.status === 204) { // I.e. no content
                return [];
            } else if (response.status === 200) { 
                return response.body || [];
            } else {
                console.error('Unexpected status code:', response.status);
                return [];
            }
        })
        .catch((error: any) => {
            console.error('An error occurred:', error);
            return []; // Return an empty array to ensure the function resolves
        });
    }


    // Get all job summary - where isAttempted is true
    getAllAttemptedJobSummaryByUserId(): Promise<JobSummary[]> {    
        return firstValueFrom(
            this.http.get<JobSummary[]>(
                `${this.baseUrl}/all-custom-jobs-attempted`, { observe: 'response' }
            )
        )
        .then((response) => {
            if (response.status === 204) { // I.e. no content
                return [];
            } else if (response.status === 200) { 
                return response.body || [];
            } else {
                console.error('Unexpected status code:', response.status);
                return [];
            }
        })
        .catch((error: any) => {
            console.error('An error occurred:', error);
            return []; // Return an empty array to ensure the function resolves
        });
    }


    // Get job summary by id -> then pass the thread id etc to get from MySQL
    getJobSummaryByCustomJobId(customJobId: string): Promise<JobSummary> {
        return firstValueFrom(
            this.http.get<JobSummary>(
                `${this.baseUrl}/custom-job/${customJobId}`)
        )
        .then((result: JobSummary) => result)
        .catch((error: any) => {
            console.error('An error occurred:', error);
            // Optionally, handle the error (e.g., show a notification to the user)
            throw error; // Re-throw the error to propagate it
        });
    }


    // Once user has attempted at least 1 interview qns, isAttempted will be set to true
    updateJobSummaryById(customJobId: string, isAttempted: boolean): Promise<any> {
        const data = { isAttempted };
        return firstValueFrom(
            this.http.put(`${this.baseUrl}/custom-job/${customJobId}`, data)
        );
    }
}