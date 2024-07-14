import { Component, inject } from '@angular/core';
import { Subscription } from 'rxjs';
import { Question } from '../../../../models/logic.models';
import { ActivatedRoute, Router } from '@angular/router';
import { JobSummaryService } from '../../../../services/job.summary.service';
import { QnsThreadService } from '../../../../services/qns.thread.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-question-thread',
  templateUrl: './question-thread.component.html',
  styleUrl: './question-thread.component.css'
})
export class QuestionThreadComponent {
  customJobId !: string;
  qnsId !: string;
  allSubThreads !: any[];
  show = false;
  question!: Question;
  subQnsById$!: Subscription;
  subQnsThreadById$ !: Subscription;

  aiFeedback: string = '';
  aiGeneratedAnswer: string = '';

  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly jobSummSvc = inject(JobSummaryService);
  private readonly qnsThreadSvc = inject(QnsThreadService);

  ngOnInit(): void {
    this.customJobId = this.activatedRoute.snapshot.params['custom-job-id'];
    this.qnsId = this.activatedRoute.snapshot.params['question-id'];

    this.subQnsById$ = this.qnsThreadSvc
      .getQuestionByQnsId(this.customJobId, this.qnsId)
      .subscribe((qns: any) => {
        if (qns) {
          this.question = qns;
        }
    });

    this.jobSummSvc.getJobSummaryByCustomJobId(this.customJobId)
      .then((result: any) => { 
        this.subQnsThreadById$ = this.qnsThreadSvc
          .getAttemptedQuestionThreadByQnsId(this.customJobId, this.qnsId, result.firebaseThreadKey)
          .subscribe((result: any) => {
            this.allSubThreads = result; 
          });
      })
      .catch((error: HttpErrorResponse) => {
        alert('An error occurred while fetching the job summary:\n' + error);
      });
  }

  toggleAccordion(): void {
    this.show = !this.show;
  }

  backToAllAttemptedQuestions() {
    this.router.navigate(['/interview-quest/archive', this.customJobId])
  }

  ngOnDestroy(): void {
    if (this.subQnsById$) {
      this.subQnsById$.unsubscribe();
    }
  }
}
