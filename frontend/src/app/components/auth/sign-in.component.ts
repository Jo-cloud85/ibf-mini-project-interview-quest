import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Title } from '@angular/platform-browser';
import { AuthService } from '../../services/firebase.auth.service';
import { GoogleAuthProvider } from 'firebase/auth';
import { AngularFireAuth } from '@angular/fire/compat/auth';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { FirebaseError } from 'firebase/app';

@Component({
  selector: 'app-sign-in',
  templateUrl: './sign-in.component.html',
  styleUrl: './sign-in.component.css'
})
export class SignInComponent {

  signInForm!: FormGroup;
  passwordVisible: boolean = false;
  interviewquest_logo: string = "assets/logo-dark.svg";
  signInAttempted: boolean = false;
  signInFailed: boolean = false;
  errorMessage = "";
  resetPasswordForm!: FormGroup;
  resetPassword: string = '';

  googleAuthProvider = new GoogleAuthProvider();

  private readonly fb = inject(FormBuilder);
  private readonly htmlTitle = inject(Title);
  private readonly authSvc = inject(AuthService);
  private readonly router = inject(Router);
  private readonly afAuth = inject(AngularFireAuth);
 

  //// Initialization ////////////////////////////////////////////////////////////////////////
  ngOnInit(): void {
    this.htmlTitle.setTitle('InterviewQuest | Sign In');
    this.authSvc.onAuthStateChanged();

    this.signInForm = this.fb.group({
      email: this.fb.control<string>('', [Validators.required, Validators.email]),
      password: this.fb.control<string>('', [Validators.required])
    })

    this.resetPasswordForm = this.fb.group({
      email: this.fb.control<string>('', [Validators.required, Validators.email]),
    })    
  }


  //// User sign in via Email/Password ///////////////////////////////////////////////////////
  // https://firebase.google.com/docs/auth/web/password-auth
  processEmailPasswordSignIn() {
    this.signInAttempted = true;

    if (this.signInForm.valid) {
      const email = this.signInForm.get('email')?.value;
      const password = this.signInForm.get('password')?.value;

      this.authSvc.signIn(email, password)
        .then((response) => {
          const customToken = response.custom_token;
          return this.authSvc.signInWithCustomToken(customToken);
        })
        .then(() => {
          this.router.navigate(['/interview-quest']);
        })
        .catch((error: HttpErrorResponse) => {
          this.signInFailed = true;
          console.error('HttpError during sign in:', error);
          this.errorMessage = error.message;
          alert(this.errorMessage);
        });
    }
  }


  //// User resets password //////////////////////////////////////////////////////////////////
  onResetPassword() {
    if (this.resetPasswordForm.valid) {
      const email = this.resetPasswordForm.get('email')?.value;
      this.afAuth.sendPasswordResetEmail(email)
        .then(() => {
          this.resetPasswordForm.reset();
          this.errorMessage = '';
          this.closeForgetPasswordModal();
          alert('Password reset email sent successfully. Please check your inbox.');
        })
        .catch((error) => {
          console.error('Error sending password reset email:', error);
          this.errorMessage = 'An error occurred while sending the password reset email. Please try again.';
        });
    }
  }

  
  //// User sign in via Google (similar to sign-in) ////////////////////////////////////////////
  // https://firebase.google.com/docs/auth/web/google-signin#handle_the_sign-in_flow_with_the_firebase_sdk
  processGoogleSignIn() {
    this.googleAuthProvider.addScope('https://www.googleapis.com/auth/contacts.readonly');

    this.signInAttempted = true;

    this.afAuth.signInWithPopup(this.googleAuthProvider)
      .then((result: any) => {
        const credential = GoogleAuthProvider.credentialFromResult(result);
        const token = credential?.accessToken;
        const user = result.user;
        if (user) {
          console.log('User signed in via Google.');
          this.router.navigate(['/interview-quest']);
        }
      })
      .catch((error: FirebaseError) => {
        console.log(error);
        console.log(GoogleAuthProvider.credentialFromError(error)); // The AuthCredential type that was used
      })
  }

  
  //// Helper methods ////////////////////////////////////////////////////////////////////////
  // Open the modal
  openForgetPasswordModal() {
    document.getElementById('forgetPasswordModal')?.classList.remove('hidden');
  }

  // Close the modal
  closeForgetPasswordModal() {
    document.getElementById('forgetPasswordModal')?.classList.add('hidden');
  }

  // Toggle password visisbility
  togglePasswordVisibility(): void {
    this.passwordVisible = !this.passwordVisible;
  }
}


/**
 * You can choose to use "firebase/auth" or "@angular/fire/compat/auth". Both syntaxes slightly differs.
 * The latter is specifically by Angular to provides Angular-specific bindings for Firebase, making it 
 * easier to integrate Firebase services within Angular applications.
 */