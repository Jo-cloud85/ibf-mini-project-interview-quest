import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { passwordValidator } from '../../custom.validators';
import { Title } from '@angular/platform-browser';

import {
  GoogleAuthProvider,
  UserCredential,
  getAuth,
  sendEmailVerification,
  signInWithCustomToken,
  signInWithPopup
} from 'firebase/auth';

import { AuthService } from '../../services/firebase.auth.service';
import { Subscription } from 'rxjs';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { FirebaseError } from 'firebase/app';

@Component({
  selector: 'app-sign-up',
  templateUrl: './sign-up.component.html',
  styleUrl: './sign-up.component.css'
})
export class SignUpComponent {

  /* Firebase app already initialized in main.ts file */

  signUpForm!: FormGroup;
  passwordVisible: boolean = false;
  interviewquest_logo: string = "assets/logo-dark.svg";
  signUpAttempted: boolean = false;
  signUpFailed: boolean = false;
  errorMessage = "";

  auth = getAuth();
  googleAuthProvider = new GoogleAuthProvider();
  sub$!: Subscription;

  typewritingText: string[] = [ "Personalized experience.", "Real-time feedback.", "Powered by AI."];

  private readonly fb = inject(FormBuilder);
  private readonly htmlTitle = inject(Title);
  private readonly authSvc = inject(AuthService);
  private readonly router = inject(Router);

  //// Initialization ////////////////////////////////////////////////////////////////////////
  ngOnInit(): void {
    this.htmlTitle.setTitle('InterviewQuest | Sign Up');

    this.startTextAnimation(0);

    this.signUpForm = this.fb.group({
      name: this.fb.group({
        firstName: this.fb.control<string>('', [Validators.required, Validators.minLength(3)]),
        lastName: this.fb.control<string>('', [Validators.required, Validators.minLength(3)])
      }),
      email: this.fb.control<string>('', [Validators.required, Validators.email]),
      password: this.fb.control<string>('', [Validators.required, passwordValidator])
    })

    this.authSvc.onAuthStateChanged();
  }


  //// User sign up via Email/Password ////////////////////////////////////////////////////////
  // https://firebase.google.com/docs/auth/web/password-auth
  processEmailPasswordSignUp(): void {
    this.signUpAttempted = true;

    const firstName = this.signUpForm.get('name.firstName')?.value;
    const lastName = this.signUpForm.get('name.lastName')?.value;
    const email = this.signUpForm.get('email')?.value;
    const password = this.signUpForm.get('password')?.value;

    // Send details to backend and subscribe to response
    // https://firebase.google.com/docs/auth/web/password-auth
    this.sub$ = this.authSvc.signUp(firstName, lastName, email, password).subscribe({
      next: (response) => {
        const customToken = response.custom_token;
        signInWithCustomToken(this.auth, customToken)
          .then(() => {
            console.log('User signed in via email and password with custom token');
            sendEmailVerification(this.auth.currentUser!);
          })
          .then(() => {
            console.log('Email verification sent.');
            alert('A verification email has been sent to your email address. Please verify your email to complete the registration.');
            this.router.navigate(['/interview-quest']);
          })
          .catch((error: HttpErrorResponse) => {
            this.signUpFailed = true;
            console.error('Error signing in with custom token:', error);
          });
      },
      error: (error: FirebaseError) => {
        this.signUpFailed = true;
        console.error('Error during sign up:', error);
        if (error.code === "400") {
          alert('Looks like this email has been taken. Please try with a different email account.');
        }
      },
      complete: () => {
        console.log('Sign up process completed!');
      }
    });
  }


  //// User sign up via Google ////////////////////////////////////////////////////////////////
  // https://firebase.google.com/docs/auth/web/google-signin#handle_the_sign-in_flow_with_the_firebase_sdk
  processGoogleSignUp() {
    this.googleAuthProvider.addScope('https://www.googleapis.com/auth/contacts.readonly');
    
    this.signUpAttempted = true;
    
    signInWithPopup(this.auth, this.googleAuthProvider)
      .then((result: any) => {
        const credential = GoogleAuthProvider.credentialFromResult(result);
        const token = credential?.accessToken;
        const user = result.user;

        if (user) {
          const { email, displayName } = user;

          if (!email || !displayName) {
            return Promise.reject('Email or displayName is missing.');
          }

          const [ firstName, lastName ] = displayName ? displayName.split(' ') : ['', ''];

          return this.authSvc.googleSignUp(firstName, lastName, email)
            .then((response: any) => {
              const customToken = response.custom_token;
              signInWithCustomToken(this.auth, customToken);
            })
            .then(() => {
              console.log('User signed in via Google with custom token.');
              sendEmailVerification(this.auth.currentUser!);
            })
            .then(() => {
              console.log('Email verification sent.');
              alert('A verification email has been sent to your email address. Please verify your email to complete the registration.');
              this.router.navigate(['/interview-quest']);
            })
        } else {
          return Promise.reject('User information is missing.');
        }
      })
      .catch((error: any) => {
        console.error('Error during Google sign up process:', error);
        this.signUpFailed = true;
      });
  }



  ngOnDestroy(): void {
    if (this.sub$) {
      this.sub$.unsubscribe();
    }
  }


  //// Type writing - CSS (each cycle is 7500ms) ////////////////////////////////////////////
  // https://css-tricks.com/snippets/css/typewriter-effect/
  typeWriter(text: string, i: number, fnCallback: () => void): void {
    const element = document.querySelector(".type-writer");
    if (element) {
      if (i < text.length) {
        element.innerHTML = text.substring(0, i + 1) + '<span aria-hidden="true"></span>';
        setTimeout(() => {
          this.typeWriter(text, i + 1, fnCallback);
        }, 50);
      } else if (typeof fnCallback === 'function') {
        setTimeout(fnCallback, 700);
      }
    }
  }

  startTextAnimation(i: number): void {
    if (typeof this.typewritingText[i] === 'undefined') {
      setTimeout(() => {
        this.startTextAnimation(0);
      }, 7500); 
    } else if (i < this.typewritingText[i].length) {
      this.typeWriter(this.typewritingText[i], 0, () => {
        this.startTextAnimation(i + 1);
      });
    }
  }

  togglePasswordVisibility(): void {
    this.passwordVisible = !this.passwordVisible;
  }
}

/**
 * Not using createUserWithEmailAndPassword(this.auth, email, password) as this 
 * creates account w/o backend. We need the backend to check if email is unique.
 */