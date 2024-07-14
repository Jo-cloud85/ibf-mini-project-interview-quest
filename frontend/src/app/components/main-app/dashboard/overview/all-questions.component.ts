import { QnsThreadService } from './../../../../services/qns.thread.service';
import { Question, JobSummary } from './../../../../models/logic.models';
import { Component, OnDestroy, inject } from '@angular/core';
import { JobSummaryService } from '../../../../services/job.summary.service';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-all-questions',
  templateUrl: './all-questions.component.html',
  styleUrl: './all-questions.component.css'
})
export class AllQuestionsComponent implements OnDestroy {

  private readonly jobSummSvc = inject(JobSummaryService);
  private readonly qnsThreadSvc = inject(QnsThreadService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly router = inject(Router);

  customJobId!: string;
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
        this.retrieveAllQuestions(customJobId, result.firebaseThreadKey);
      })
      .catch((error: HttpErrorResponse) => {
        alert('An error occurred while fetching the job summary:\n' + error);
      });
  } 


  retrieveAllQuestions(customJobId: string, firebaseThreadKey: string) {
    this.sub$ = this.qnsThreadSvc.getAllQuestions(customJobId, firebaseThreadKey)
      .subscribe({
        next: (result: any) => {
          this.questions = result;
        },
        error: (error: HttpErrorResponse) => console.log(">>> Error parsing AI response: ", error),
        complete: () => console.log("Retrieve questions completed")
      })
  }


  goToQuestion(qnsId: string) {
    this.router.navigate(['/interview-quest/overview/', this.customJobId, qnsId])
  }


  ngOnDestroy(): void {
    if (this.sub$) {
      this.sub$.unsubscribe();
    }
  }
}

