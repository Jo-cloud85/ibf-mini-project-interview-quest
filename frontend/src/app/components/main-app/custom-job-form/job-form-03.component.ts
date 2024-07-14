import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { JobStore } from '../../../stores/job.store';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-job-form-03',
  templateUrl: './job-form-03.component.html',
  styleUrls: ['./job-form-03.component.css']
})
export class JobForm03Component implements OnInit {

  uploadForm!: FormGroup;
  files: File[] = [];

  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly customJobStore = inject(JobStore);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly htmlTitle = inject(Title);

  ngOnInit(): void {
    this.htmlTitle.setTitle('InterviewQuest | Create Job');

    this.uploadForm = this.fb.group({
      documents: [null, Validators.required]
    });
  }

  onFileChange(event: any): void {
    if (event.target.files.length > 0) {
      this.files = Array.from(event.target.files);
      this.uploadForm.patchValue({
        documents: this.files
      });
      this.uploadForm.get('documents')?.updateValueAndValidity();
    }
  }

  onSubmit(): void {
    if (this.uploadForm.valid) {
      console.log(this.files);
      this.customJobStore.setUploadForm(this.files);
      this.router.navigate(['../summary'], { relativeTo: this.activatedRoute });
    }
  }
}
