import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CanComponentDeactivate } from '../../../guards/guard';
import { Observable } from 'rxjs';
import { JobStore } from '../../../stores/job.store';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-job-form-01',
  templateUrl: './job-form-01.component.html',
  styleUrl: './job-form-01.component.css'
})
export class JobForm01Component implements OnInit, CanComponentDeactivate{

  // Cannot use localStorage to store as it cannot stringify files (upload section)

  jobForm !: FormGroup;

  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly customJobStore = inject(JobStore);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly htmlTitle = inject(Title);

  ngOnInit(): void {
    this.htmlTitle.setTitle('InterviewQuest | Create Job');

    this.jobForm = this.fb.group({
      jobTitle: this.fb.control<string>('', [Validators.required, Validators.minLength(5)]),
      jobDescription: this.fb.control<string>('', [Validators.required, Validators.minLength(50)]),
      jobLevel: this.fb.control<string>('', Validators.required)
    })
  }

  canDeactivate(): Observable<boolean> | Promise<boolean> | boolean {
    return confirm('You have unsaved changes! Do you really want to leave?');
  }

  onSubmit(): void {
    if (this.jobForm.valid) {
      this.customJobStore.setJobForm(this.jobForm.value);
      this.router.navigate(['../02'], { relativeTo: this.activatedRoute });
    }
  }

  onBack(): void {
    this.router.navigate(['../'], { relativeTo: this.activatedRoute });
  }
}
