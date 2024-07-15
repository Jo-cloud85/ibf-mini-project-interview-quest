import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { getAuth, onAuthStateChanged, signOut } from 'firebase/auth';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {

  interviewquest_logo: string = "assets/logo-dark.svg"; 
  overview_icon: string = "assets/overview.svg";
  archive_icon: string = "assets/archive.svg";
  schedule_icon: string = "assets/schedule.png";
  profile_icon: string = "assets/profile.png";
  faq_icon: string = "assets/faq.png";
  signout_icon: string = "assets/signout.png";

  private readonly router = inject(Router);

  auth = getAuth();

  ngOnInit(): void {
    onAuthStateChanged(this.auth, (user) => {
      if (user) {
        console.log('User is signed in:', user);
      } else {
        console.log('User is signed out.');
      }
    });
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
