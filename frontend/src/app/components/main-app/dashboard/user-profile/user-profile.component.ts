import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { AuthService } from '../../../../services/firebase.auth.service';
import { HttpErrorResponse } from '@angular/common/http';
import { AngularFireAuth } from '@angular/fire/compat/auth';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrl: './user-profile.component.css'
})
export class UserProfileComponent {

  profileForm!: FormGroup;
  sub$!: Subscription;
  user: any;

  private readonly fb = inject(FormBuilder);
  private readonly authSvc = inject(AuthService);
  private readonly afAuth = inject(AngularFireAuth);


  ngOnInit(): void {
    // Get the current user asynchronously
    this.afAuth.currentUser
    .then((user: any) => {
      if (user && user.email) {
        this.authSvc.getProfile(user.email).subscribe({
          next: (response: any) => {
            this.user = response;
            this.profileForm = this.fb.group({
              displayName: this.fb.control<string>(this.user.displayName, [Validators.required, Validators.minLength(3)]),
              email: this.fb.control<string>({value: this.user.email, disabled: true}),
              photoUrl: this.fb.control<string>(this.user.photoUrl)
            });
          },
          error: (error: HttpErrorResponse) => {
            console.error('Error fetching profile:', error);
          }
        });
      } else {
        console.error('No user is currently signed in.');
      }
    })
    .catch((error) => {
      console.error('Error getting current user:', error);
    });
  }

  updateProfile(): void {
    const displayName = this.profileForm.get('displayName')?.value;
    const photoUrl = this.profileForm.get('photoUrl')?.value;

    this.afAuth.currentUser
    .then((user: any) => {
      if (user && user.email) {
        this.authSvc.updateProfile({email: user.email, displayName, photoUrl}).subscribe({
          next: (response: any) => {
            alert('Profile updated successfully!');
          },
          error: (error: HttpErrorResponse) => {
            console.error('Error updating profile:', error);
          }
        });
      } else {
        console.error('No user is currently signed in.');
      }
    }).catch((error) => {
      console.error('Error getting current user:', error);
    });
  }
}
