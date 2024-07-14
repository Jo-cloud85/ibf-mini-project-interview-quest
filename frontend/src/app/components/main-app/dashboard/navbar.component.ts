import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { JobSummary } from '../../../models/logic.models';
import { getAuth, signOut } from 'firebase/auth';
import { initFlowbite } from 'flowbite';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {
  private readonly formbuilder = inject(FormBuilder);
  private readonly router = inject(Router);

  auth = getAuth(); //alternatively, just use AngularFire

  searchForm !: FormGroup;
  displayedJobs: JobSummary[] = [];
  search_icon: string = "assets/search.svg";
  notification_icon: string = "assets/notification.svg";


  ngOnInit(): void {
    initFlowbite();

    this.searchForm = this.formbuilder.group({
      q: this.formbuilder.control<string>(''), 
    })
  }


  search() {
    const queryParams = { ...this.searchForm.value };
    this.router.navigate(['/interview-quest/overview'], { queryParams });
  }


  processSignOut(): void {
    signOut(this.auth)
      .then(() => {
        // Sign-out successful -> navigate back to sign-in page
        this.router.navigate(['/signin'])
      }).catch((error) => {
        console.log("An unexpected error occur: " + error);
      });
  }
}
