import { HttpClient } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";

@Injectable({providedIn: 'root'})
export class GoogleCalendarService {

    private readonly http = inject(HttpClient);

    createSchedule(formData: any): Observable<any> {
        return this.http.post<any>("/api/calendar/create-schedule", formData);
    }

    getAllSchedules(): Observable<any> {
        return this.http.get("/api/calendar/all-schedule")
    }

    deleteSchedule(scheduleId: string): Observable<any> {
        return this.http.delete(`/api/calendar/all-schedule/${scheduleId}/delete`);
    }
}