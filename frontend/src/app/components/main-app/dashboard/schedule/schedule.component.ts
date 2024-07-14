import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { GoogleCalendarService } from './../../../../services/google.calendar.service';
import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { Frequency, RRule } from 'rrule';
import { futureDateValidator } from '../../../../custom.validators';
import { Subscription } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { GoogleAuthService } from '../../../../services/google.auth.service';

@Component({
  selector: 'app-schedule',
  templateUrl: './schedule.component.html',
  styleUrl: './schedule.component.css'
})
export class ScheduleComponent implements OnInit, OnDestroy {

  // Make sure npm install rrule - https://jkbrzt.github.io/rrule/

  scheduleForm!: FormGroup;
  schedules: any[] | null = null;
  showSchedules: boolean = false;
  selectedSchedule !: any;

  subCreate$ !: Subscription;
  subGetAll$ !: Subscription;
  subDelete$ !: Subscription;

  private readonly fb = inject(FormBuilder);
  private readonly googleCalendarSvc = inject(GoogleCalendarService);
  private readonly googleAuthSvc = inject(GoogleAuthService);

  //// Initialization //////////////////////////////////////////////////////////////
  ngOnInit(): void {
    this.initializeForm();
    this.handleOAuthCallback();
  }

  private initializeForm(): void {
    this.scheduleForm = this.fb.group({
      title: this.fb.control<string>('', [Validators.required, Validators.minLength(3)]),
      description: this.fb.control<string>(''),
      startDateTime: this.fb.control<string>('', [Validators.required, futureDateValidator]),
      duration: this.fb.control<string>('', [Validators.required]),
      count: this.fb.control<string>('', [Validators.required]),
      interval: this.fb.control<string>('', [Validators.required]),
      email: this.fb.control<string>('', [Validators.email])
    });
  }
  
  private handleOAuthCallback(): void {
    const urlParams = new URLSearchParams(window.location.search);
    const success = urlParams.get('success');

    if (success) {
      // Retrieve form data from localStorage
      const formData = JSON.parse(localStorage.getItem('scheduleFormData') || '{}');

      if (Object.keys(formData).length) {
        this.subCreate$ = this.googleCalendarSvc.createSchedule(formData)
          .subscribe({
            next: (response: any) => {
              console.log('Schedule created:', response.event_link);
              localStorage.removeItem('scheduleFormData'); // Clear stored formData
            },
            error: (error: HttpErrorResponse) => {
              console.error('An unexpected error occurred:', error.message);
            },
            complete: () => {
              console.log('Retrieve all schedules completed.');
            }
          });
      }
    }
  }


  //// Create Schedule /////////////////////////////////////////////////////////////
  createSchedule(): void {
    const formValues = this.scheduleForm.value;
    const startDateTime = new Date(formValues.startDateTime);
    const duration = parseFloat(formValues.duration);
    const endDateTime = new Date(startDateTime.getTime() + duration * 60 * 60 * 1000);

    const rule = new RRule({
      freq: this.getCountFrequency(formValues.count),
      count: formValues.interval,
    });

    const formData = {
      title: formValues.title,
      description: formValues.description,
      startDateTime: startDateTime.toISOString(),
      endDateTime: endDateTime.toISOString(),
      rruleStr: rule.toString(),
      email: formValues.email 
    };

    // Save form data to local storage
    localStorage.setItem('scheduleFormData', JSON.stringify(formData));

    // Ensure authorization first
    this.googleAuthSvc.getAuthorizationUrl()
      .then((response: any) => {
        window.location.href = response.url; // Redirect the user to the authorization URL
      })
      .catch((error: any) => {
        console.error('Error fetching authorization URL:', error);
      });
  }

  // Helper method for Create
  getCountFrequency(count: string): Frequency {
    switch (count) {
      case 'daily':
        return RRule.DAILY;
      case 'weekly':
        return RRule.WEEKLY;
      case 'monthly':
        return RRule.MONTHLY;
      default:
        return RRule.DAILY;
    }
  }


  //// Read All Schedules ////////////////////////////////////////////////////////
  getAllSchedules(): void {
    this.showSchedules = true;
    this.subGetAll$ = this.googleCalendarSvc.getAllSchedules()
      .subscribe({
        next: (response: any) => this.schedules = response.length ? response : null,
        error: (error: HttpErrorResponse) => {
          if (error.status === 404) {
            this.schedules = null;
          } else {
            console.error('An unexpected error occurred:', error.message);
          }
        }
      });
  }


  //// Delete Schedule ////////////////////////////////////////////////////////////
  deleteSchedule(scheduleId: string): void {
    this.subDelete$ = this.googleCalendarSvc.deleteSchedule(scheduleId)
      .subscribe({
        next: (response: any) => {
          console.log('Schedule deleted:', response.success_message);
          this.getAllSchedules();
        },
        error: (error: HttpErrorResponse) => {
          console.log(error);
        }
      })
  }


  // Helper method used in HTML
  getDurationInHours(startDateTimeStr: string, endDateTimeStr: string): number {
    const startDateTime = new Date(startDateTimeStr).getTime();
    const endDateTime = new Date(endDateTimeStr).getTime();
    const durationInMillis = endDateTime - startDateTime;
    const durationInHours = durationInMillis / (1000 * 60 * 60);
    return durationInHours;
  }


  ngOnDestroy() {
    if (this.subCreate$) this.subCreate$.unsubscribe();
    if (this.subGetAll$) this.subGetAll$.unsubscribe();
    if (this.subDelete$) this.subDelete$.unsubscribe();
  }
}
