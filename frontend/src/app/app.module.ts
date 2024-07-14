import { NgModule, isDevMode } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';

import { DashboardComponent } from './components/main-app/dashboard/dashboard.component';
import { SidebarComponent } from './components/main-app/dashboard/sidebar.component';
import { HeaderComponent } from './components/landing-page/header.component';
import { HeroComponent } from './components/landing-page/hero.component';
import { FaqsComponent } from './components/main-app/dashboard/faqs/faqs.component';

import { SignInComponent } from './components/auth/sign-in.component';
import { SignUpComponent } from './components/auth/sign-up.component';
import { MainAppComponent } from './components/main-app/main-app.component';
import { LandingPageComponent } from './components/landing-page/landing-page.component';
import { ArchiveComponent } from './components/main-app/dashboard/archive/archive.component';
import { JobForm00Component } from './components/main-app/custom-job-form/job-form-00.component';
import { JobForm01Component } from './components/main-app/custom-job-form/job-form-01.component';
import { JobForm02Component } from './components/main-app/custom-job-form/job-form-02.component';
import { JobForm03Component } from './components/main-app/custom-job-form/job-form-03.component';
import { SummaryFormComponent } from './components/main-app/custom-job-form/summary-form.component';
import { OverviewComponent } from './components/main-app/dashboard/overview/overview.component';
import { CustomJobFormComponent } from './components/main-app/custom-job-form/custom-job-form.component';
import { UserProfileComponent } from './components/main-app/dashboard/user-profile/user-profile.component';
import { QuestionComponent } from './components/main-app/dashboard/overview/question.component';
import { AllQuestionsComponent } from './components/main-app/dashboard/overview/all-questions.component';
import { AllJobSummaryComponent } from './components/main-app/dashboard/overview/all-job-summary.component';
import { ScheduleComponent } from './components/main-app/dashboard/schedule/schedule.component';
import { AngularFireModule } from '@angular/fire/compat';
import { AngularFireAuthModule } from '@angular/fire/compat/auth';
import { AuthInterceptor } from './services/auth.interceptor';
import { NavbarComponent } from './components/main-app/dashboard/navbar.component';
import { PasswordResetComponent } from './components/auth/password-reset.component';
import { AllAttemptedQuestionsComponent } from './components/main-app/dashboard/archive/all-attempted-questions.component';
import { QuestionThreadComponent } from './components/main-app/dashboard/archive/question-thread.component';
import { AllAttemptedJobSummaryComponent } from './components/main-app/dashboard/archive/all-attempted-job-summary.component';
import { ServiceWorkerModule } from '@angular/service-worker';

const firebaseConfig = {
  apiKey: "AIzaSyDRiurP6EfCnWfuQJYTKg5twqfxBAcJQYI",
  authDomain: "interviewquest-b0b2e.firebaseapp.com",
  databaseURL: "https://interviewquest-b0b2e-default-rtdb.asia-southeast1.firebasedatabase.app",
  projectId: "interviewquest-b0b2e",
  storageBucket: "interviewquest-b0b2e.appspot.com",
  messagingSenderId: "35471201213",
  appId: "1:35471201213:web:c4468b22a8384f91a438d4"
};

@NgModule({
  declarations: [
    AppComponent,
    LandingPageComponent,
    HeaderComponent,
    HeroComponent,
    MainAppComponent,
    SignInComponent,
    SignUpComponent,
    DashboardComponent,
    SidebarComponent,
    ArchiveComponent,
    JobForm00Component,
    JobForm01Component,
    JobForm02Component,
    JobForm03Component,
    SummaryFormComponent,
    CustomJobFormComponent,
    OverviewComponent,
    QuestionComponent,
    AllQuestionsComponent,
    AllJobSummaryComponent,
    ScheduleComponent,
    UserProfileComponent,
    FaqsComponent,
    NavbarComponent,
    PasswordResetComponent,
    AllAttemptedQuestionsComponent,
    QuestionThreadComponent,
    AllAttemptedJobSummaryComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    CommonModule,
    ReactiveFormsModule,
    HttpClientModule,
    FormsModule,
    AngularFireModule.initializeApp(firebaseConfig),
    AngularFireAuthModule,
    ServiceWorkerModule.register('reg_sw.js', {
      enabled: !isDevMode(),
      registrationStrategy: 'registerWhenStable:30000'
    }),
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
