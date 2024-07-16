import { ChangeDetectorRef, Component, ElementRef, Input, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, Subscription } from 'rxjs';
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
  isLoadingCustomJob: boolean = false;

  @Input() dashboardMode=true;
  @Input() formMode=false;
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  typewritingText: string[] = [ 
    "Creating Assistant AI...", 
    "Files uploaded...",
    "Creating vector store...", 
    "Adding uploaded files to vector store...", 
    "Updating Assistant AI...", 
    "Creating thread...", 
    "Assistant AI responding...", 
    "Almost there...", 
    "Almost..."];

  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly jobStore = inject(JobStore);
  private readonly openAISvc = inject(OpenAIService);
  private readonly htmlTitle = inject(Title);
  private readonly changeDetectorRef = inject(ChangeDetectorRef);

  private sub$?: Subscription;
 
  readonly mainApp = inject(MainAppComponent);

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
    this.changeDetectorRef.detectChanges();
    this.startTextAnimation(0);
    
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

  //// Type writing - CSS (each cycle is 7500ms) ////////////////////////////////////////////
  // https://css-tricks.com/snippets/css/typewriter-effect/
  typeWriter(text: string, i: number, fnCallback: () => void): void {
    const element = document.querySelector(".type-writer");
    if (element) {
      if (i < text.length) {
        element.innerHTML = text.substring(0, i + 1) + '<span aria-hidden="true"></span>';
        setTimeout(() => {
          this.typeWriter(text, i + 1, fnCallback);
        }, 75);
      } else if (typeof fnCallback === 'function') {
        setTimeout(fnCallback, 1000);
      }
    }
  }

  startTextAnimation(i: number): void {
    if (typeof this.typewritingText[i] === 'undefined') {
      setTimeout(() => {
        this.startTextAnimation(0);
      }, 7500); 
    } else if (i < this.typewritingText[i].length) {
      this.typeWriter(this.typewritingText[i], 0, () => {
        this.startTextAnimation(i + 1);
      });
    }
  }
}
