import { Component, inject } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { JobStore } from '../../../stores/job.store';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-job-form-02',
  templateUrl: './job-form-02.component.html',
  styleUrl: './job-form-02.component.css'
})
export class JobForm02Component {

  // Cannot use localStorage to store as it cannot stringify files (upload section)

  companyForm !: FormGroup;

  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly customJobStore = inject(JobStore);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly htmlTitle = inject(Title);

  ngOnInit(): void {
    this.htmlTitle.setTitle('InterviewQuest | Create Job');
    
    this.companyForm = this.fb.group({
      companyName: this.fb.control<string>(''),
      companyDetails: this.fb.control<string>('')
    });
  }

  onSubmit(): void {
    if (this.companyForm.valid) {
      this.customJobStore.setCompanyForm(this.companyForm.value);
      this.router.navigate(['../03'], { relativeTo: this.activatedRoute });
    }
  }
}
