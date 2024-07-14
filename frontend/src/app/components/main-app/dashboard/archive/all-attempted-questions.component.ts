import { Component, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { JobSummary, Question } from '../../../../models/logic.models';
import { JobSummaryService } from '../../../../services/job.summary.service';
import { QnsThreadService } from '../../../../services/qns.thread.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-all-attempted-questions',
  templateUrl: './all-attempted-questions.component.html',
  styleUrl: './all-attempted-questions.component.css'
})
export class AllAttemptedQuestionsComponent {

  private readonly jobSummSvc = inject(JobSummaryService);
  private readonly qnsThreadSvc = inject(QnsThreadService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly router = inject(Router);

  customJobId!: string;
  firebaseThreadKey!: string;
  questions:Question[] = [];
  sub$ !: Subscription;


  ngOnInit(): void {
    this.activatedRoute.params.subscribe(params => {
      this.customJobId = params['custom-job-id'];
      this.retrieveJobSummaryByCustomJobId(this.customJobId);
    });
  }


  retrieveJobSummaryByCustomJobId(customJobId: string) {
    this.jobSummSvc.getJobSummaryByCustomJobId(customJobId)
      .then((result: JobSummary) => {
        this.retrieveAllAttemptedQuestions(customJobId, result.firebaseThreadKey);
      })
      .catch((error: HttpErrorResponse) => {
        alert('An error occurred while fetching the attempted job summary:\n' + error);
      });
  } 


  retrieveAllAttemptedQuestions(customJobId: string, firebaseThreadKey: string) {
    this.sub$ = this.qnsThreadSvc.getAllAttemptedQuestions(customJobId, firebaseThreadKey)
      .subscribe({
        next: (result: any) => {
          this.questions = result;
        },
        error: (error: HttpErrorResponse) => console.log(">>> Error: ", error),
        complete: () => console.log("Retrieve attempted questions completed")
      })
  }


  goToAttemptedQuestionThread(qnsId: string) {
    this.router.navigate(['/interview-quest/archive/', this.customJobId, qnsId])
  }


  ngOnDestroy(): void {
    if (this.sub$) {
      this.sub$.unsubscribe();
    }
  }
}
