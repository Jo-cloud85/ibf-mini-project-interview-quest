import { Component, OnDestroy, inject } from '@angular/core';
import { Question } from '../../../../models/logic.models';
import { ActivatedRoute, Router } from '@angular/router';
import { QnsThreadService } from '../../../../services/qns.thread.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { OpenAIService } from '../../../../services/open.ai.service';
import { HttpErrorResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { JobSummaryService } from '../../../../services/job.summary.service';

@Component({
  selector: 'app-question',
  templateUrl: './question.component.html',
  styleUrl: './question.component.css'
})
export class QuestionComponent implements OnDestroy {
  question!: Question;
  showAnswerApproach = false;
  userAnswer = '';
  customJobId !: string;
  qnsId !: string;
  answerForm !: FormGroup;
  aiFeedback: string = '';
  aiGeneratedAnswer: string = '';
  isLoadingAnswer: boolean = false;
  isLoadingFeedback: boolean = false;

  subQnsById$!: Subscription;
  subFeedback$!: Subscription;
  subAnswer$!: Subscription;

  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly jobSummSvc = inject(JobSummaryService);
  private readonly qnsThreadSvc = inject(QnsThreadService);
  private readonly openAiThreadSvc = inject(OpenAIService);
  private readonly fb = inject(FormBuilder);

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

    this.answerForm = this.fb.group({
      userAnswer: this.fb.control<string>('', [Validators.required])
    });
  }

  toggleAccordion(): void {
    this.showAnswerApproach = !this.showAnswerApproach;
  }

  // Scenario 1 - User writes his/her answer
  onSubmit() {
    if (this.answerForm.valid) {
      const userInput = this.answerForm.get('userAnswer')?.value;
      this.isLoadingFeedback = true;

      this.subFeedback$ = this.openAiThreadSvc
        .requestFeedback(userInput, this.customJobId, this.qnsId)
        .subscribe({
          next: (result: any) => {
            // format response for UI purposes
            const strippedString = result.ai_response
              .replace(/^\{\s*"feedback":\s*"/, '')
              .replace(/"\s*\}$/, '')
              .replace(/(\d+\.\s\*\*[^\*\*]+?\*\*)/g, '<br/><strong>$1</strong>')
              .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
              .replace(/(\d+)\./g, '<br/>$1.')
            // console.log('AI Feedback:', strippedString);
            this.aiFeedback = strippedString;

            // update MySQL that this question has been attempted by user
            this.jobSummSvc.updateJobSummaryById(this.customJobId, true)
              .then(response => {
                console.log('Update successful:', response);
                this.isLoadingFeedback = false;
              })
              .catch(error => {
                console.error('Update failed:', error);
                this.isLoadingFeedback = false;
              });
            this.isLoadingFeedback = false; 
          },
          error: (error: HttpErrorResponse) => {
            console.error('Error:', error);
            this.isLoadingFeedback = false; 
          },
          complete: () => console.log("Generate feedback completed!")
        })
    }
  }

  // Scenario 2 - User ask AI to generate answer
  generateAnswer() {
    const generateAnswer = "Generate Answer";
    this.isLoadingAnswer = true;

    this.subAnswer$ = this.openAiThreadSvc
      .requestFeedback(generateAnswer, this.customJobId, this.qnsId)
      .subscribe({
        next: (result: any) => {
          const strippedString = result.ai_response
            .replace(/^\{\s*"suggested_answer":\s*"/, '')
            .replace(/"\s*\}$/, '')
            .replace(/(\d+\.\s\*\*[^\*\*]+?\*\*)/g, '<br/><strong>$1</strong>')
            .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
            .replace(/(\d+)\./g, '<br/>$1.')
          // console.log('AI Answer:', strippedString);
          this.aiGeneratedAnswer = strippedString;
          this.answerForm.patchValue({ userAnswer: result.generatedAnswer });
          this.isLoadingAnswer = false; 
        },
        error: (error: HttpErrorResponse) => {
          console.error('Error:', error);
          this.isLoadingAnswer = false; 
        },
        complete: () => console.log("Generate answer completed!")
      })
  }
 
  backToAllQuestions() {
    this.router.navigate(['/interview-quest/overview', this.customJobId])
  }

  ngOnDestroy(): void {
    if(this.subQnsById$) {
      this.subQnsById$.unsubscribe();
    }

    if(this.subAnswer$) {
      this.subAnswer$.unsubscribe();
    }

    if(this.subFeedback$) {
      this.subFeedback$.unsubscribe();
    }
  }
}
