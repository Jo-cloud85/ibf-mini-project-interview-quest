import { Component, Input, inject } from '@angular/core';
import { JobSummaryService } from '../../../../services/job.summary.service';
import { ActivatedRoute, Router } from '@angular/router';
import { JobSummary } from '../../../../models/logic.models';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-all-job-summary',
  templateUrl: './all-job-summary.component.html',
  styleUrl: './all-job-summary.component.css'
})
export class AllJobSummaryComponent {

  private readonly jobSummSvc = inject(JobSummaryService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly router = inject(Router);

  queryParams !: { q: string };
  jobSummListP$ !: Promise<JobSummary[]>;
  allJobs: JobSummary[] = []; 

  ngOnInit(): void {
    this.activatedRoute.queryParams.subscribe(params => {
      this.queryParams = { q: params['q'] };
      // Have to call inside once the params are updated else it will use the initial queryParams value
      this.loadAllJobSummary(); 
    });
  }

  loadAllJobSummary(): void {
    this.jobSummListP$ = this.jobSummSvc.getAllJobSummaryByUserIdByQuery(this.queryParams.q || '')
      .then((result: JobSummary[]) => {
        this.allJobs = result;
        return result; 
      })
      .catch((error: HttpErrorResponse) => {
        console.error('Error fetching jobs:', error);
        return []; // Return an empty array in case of error to match the expected type
      });
  }

  retrieveAllQns(customJobId: string) {
    this.router.navigate(['/interview-quest/overview', customJobId])
  }
}
