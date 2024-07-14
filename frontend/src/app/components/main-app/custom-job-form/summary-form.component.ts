import { Component, ElementRef, Input, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, Subscription } from 'rxjs';
import { JobSummaryService } from '../../../services/job.summary.service';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { JobStore } from '../../../stores/job.store';
import { MainAppComponent } from '../main-app.component';
import { OpenAIService } from '../../../services/open.ai.service';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-summary-form',
  templateUrl: './summary-form.component.html',
  styleUrl: './summary-form.component.css'
})
export class SummaryFormComponent implements OnInit, OnDestroy {

  // Cannot use localStorage to store as it cannot stringify files

  jobForm: any;
  companyForm: any;
  uploadForm: any[] = [];
  isSubmitted = false;

  @Input() dashboardMode=true;
  @Input() formMode=false;
  
  private sub$?: Subscription;

  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly jobStore = inject(JobStore);
  private readonly openAISvc = inject(OpenAIService);
  private readonly htmlTitle = inject(Title);
 
  readonly mainApp = inject(MainAppComponent);

  isLoadingCustomJob: boolean = false;

  ngOnInit(): void {
    this.htmlTitle.setTitle('InterviewQuest | Create Job');
    
    this.jobStore.jobForm$.subscribe(data => this.jobForm = data);
    this.jobStore.companyForm$.subscribe(data => this.companyForm = data);
    this.jobStore.uploadForm$.subscribe(data => this.uploadForm = data);
  }

  createDoc(data: any): FormGroup {
    return this.fb.group({
      file: [data.file, Validators.required]
    });
  }

  onFileChange(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.uploadForm.push(file);
      this.jobStore.setUploadForm(this.uploadForm);
    }
  }

  saveChanges(): void {
    const formData = new FormData();

    // Append jobForm fields to formData
    for (const key in this.jobForm) {
      if (this.jobForm.hasOwnProperty(key)) {
        formData.append(key, this.jobForm[key]);
      }
    }

    // Append companyForm fields to formData
    for (const key in this.companyForm) {
      if (this.companyForm.hasOwnProperty(key)) {
        formData.append(key, this.companyForm[key]);
      }
    }

    // Append files to formData
    this.uploadForm.forEach((file: File) => {
      formData.append('documents', file);
    });

    // formData.forEach((value, key) => {
    //   console.log(`${key}:`, value);
    // });

    this.isSubmitted = true;
    this.formMode=false;

    this.isLoadingCustomJob = true;

    // This response will take time
    this.sub$ = this.openAISvc.createNewCustomJob(formData).subscribe({
      next: (result: any) => {
        this.isLoadingCustomJob = false;
        console.log(result.message)
      },
      error: (error: HttpErrorResponse) => {
        this.isLoadingCustomJob = false;
        console.error(">>> Error in creating new custom job (from frontend): ", error);
      },
      complete: () => console.log(">>> Creating custom job completed!")
    });
  }

  switchToDashboard() {
    this.mainApp.switchToDashboard();
  }

  removeFile(index: number): void {
    this.uploadForm.splice(index, 1);
    this.jobStore.setUploadForm(this.uploadForm);
  }

  addFile(): void {
    this.fileInput.nativeElement.click();
  }

  canDeactivate(): Observable<boolean> | Promise<boolean> | boolean {
    return confirm('You have unsaved changes! Do you really want to leave?');
  }

  onBack(): void {
    this.router.navigate(['/interview-quest']);
  }

  ngOnDestroy(): void {
    this.sub$?.unsubscribe();
  }
}
