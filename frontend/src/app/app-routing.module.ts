import { QuestionThreadComponent } from './components/main-app/dashboard/archive/question-thread.component';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LandingPageComponent } from './components/landing-page/landing-page.component';
import { MainAppComponent } from './components/main-app/main-app.component';
import { SignInComponent } from './components/auth/sign-in.component';
import { SignUpComponent } from './components/auth/sign-up.component';
import { JobForm00Component} from './components/main-app/custom-job-form/job-form-00.component';
import { JobForm01Component } from './components/main-app/custom-job-form/job-form-01.component';
import { JobForm02Component } from './components/main-app/custom-job-form/job-form-02.component';
import { JobForm03Component } from './components/main-app/custom-job-form/job-form-03.component';
import { ArchiveComponent } from './components/main-app/dashboard/archive/archive.component';
import { DashboardComponent } from './components/main-app/dashboard/dashboard.component';
import { SummaryFormComponent } from './components/main-app/custom-job-form/summary-form.component';
import { OverviewComponent } from './components/main-app/dashboard/overview/overview.component';
import { CustomJobFormComponent } from './components/main-app/custom-job-form/custom-job-form.component';
import { UserProfileComponent } from './components/main-app/dashboard/user-profile/user-profile.component';
import { QuestionComponent } from './components/main-app/dashboard/overview/question.component';
import { AllQuestionsComponent } from './components/main-app/dashboard/overview/all-questions.component';
import { AllJobSummaryComponent } from './components/main-app/dashboard/overview/all-job-summary.component';
import { ScheduleComponent } from './components/main-app/dashboard/schedule/schedule.component';
import { AuthGuard } from './guards/auth.guard';
import { canDeactivateGuard } from './guards/guard';
import { FaqsComponent } from './components/main-app/dashboard/faqs/faqs.component';
import { AllAttemptedJobSummaryComponent } from './components/main-app/dashboard/archive/all-attempted-job-summary.component';
import { AllAttemptedQuestionsComponent } from './components/main-app/dashboard/archive/all-attempted-questions.component';

const routes: Routes = [
  { path: '', component: LandingPageComponent },
  { path: 'signin', component: SignInComponent },
  { path: 'signup', component: SignUpComponent },
  {
    path: 'interview-quest', 
    component: MainAppComponent,
    canActivate: [AuthGuard],
    canActivateChild: [AuthGuard],
    children: [
      // Define child routes for dashboard
      { 
        path: '', 
        component: DashboardComponent,
        children: [
          { path: '', redirectTo: 'overview', pathMatch: 'full' },
          { 
            path: 'overview', 
            component: OverviewComponent,
            children: [
              { path: '', component: AllJobSummaryComponent },
              { path: ':custom-job-id', component: AllQuestionsComponent },
              { path: ':custom-job-id/:question-id', component: QuestionComponent },
            ]
          },
          { path: 'archive', 
            component: ArchiveComponent,
            children: [
              { path: '', component: AllAttemptedJobSummaryComponent },
              { path: ':custom-job-id', component: AllAttemptedQuestionsComponent },
              { path: ':custom-job-id/:question-id', component: QuestionThreadComponent},
            ]
          },
          { path: 'profile', component: UserProfileComponent },
          { path: 'schedule', component: ScheduleComponent },
          { path: 'faqs', component: FaqsComponent },
        ]
      },
      // Define child routes for custom-job-form
      {
        path: 'custom-job-form', 
        component: CustomJobFormComponent,
        children: [
          { path: '', component: JobForm00Component },
          { path: '01', component: JobForm01Component, canDeactivate: [canDeactivateGuard] },
          { path: '02', component: JobForm02Component },
          { path: '03', component: JobForm03Component },
          { path: 'summary', component: SummaryFormComponent},
        ]
      }
    ]
  },
  { path: "**", redirectTo: "/", pathMatch: "full" }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
