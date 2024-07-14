import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-main-app',
  templateUrl: './main-app.component.html',
  styleUrl: './main-app.component.css'
})
export class MainAppComponent {
  // default
  formMode = false;
  dashboardMode = true;

  private readonly router = inject(Router);

  switchToDashboard() {
    this.formMode = false;
    this.dashboardMode = true;
    this.router.navigate(['/interview-quest']);
  }

  switchToForm() {
    this.formMode = true;
    this.dashboardMode = false;
  }
}
