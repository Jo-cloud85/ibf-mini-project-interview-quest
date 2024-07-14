import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { JobSummary } from '../../../../models/logic.models';
import { JobSummaryService } from '../../../../services/job.summary.service';
import { QnsThreadService } from '../../../../services/qns.thread.service';

@Component({
  selector: 'app-all-attempted-job-summary',
  templateUrl: './all-attempted-job-summary.component.html',
  styleUrl: './all-attempted-job-summary.component.css'
})
export class AllAttemptedJobSummaryComponent {
  private readonly jobSummSvc = inject(JobSummaryService);
  private readonly qnsThreadSvc = inject(QnsThreadService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly router = inject(Router);

  attemptedJobsSummListP$ !: Promise<JobSummary[]>;
  allAttemptedJobs: JobSummary[] = []; 

  ngOnInit(): void {
    this.loadAllAttemptedJobSummary(); 
  }

  loadAllAttemptedJobSummary(): void {
    this.attemptedJobsSummListP$ = this.jobSummSvc.getAllAttemptedJobSummaryByUserId()
      .then((result: JobSummary[]) => {
        this.allAttemptedJobs = result;
        return result; 
      })
      .catch((error: HttpErrorResponse) => {
        console.error('Error fetching jobs:', error);
        return []; // Return an empty array in case of error to match the expected type
      });
  }

  retrieveAllAttemptedQns(customJobId: string) {
    this.router.navigate(['/interview-quest/archive', customJobId])
  }
}
