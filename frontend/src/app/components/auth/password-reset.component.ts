import { Component, inject } from '@angular/core';
import { AngularFireAuth } from '@angular/fire/compat/auth';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-password-reset',
  templateUrl: './password-reset.component.html',
  styleUrl: './password-reset.component.css'
})
export class PasswordResetComponent {

  passwordResetForm!: FormGroup;
  successMessage: string = '';
  errorMessage: string = '';

  private readonly fb = inject(FormBuilder);
  private readonly afAuth =  inject(AngularFireAuth);

  ngOnInit(): void {
    this.passwordResetForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onResetPassword() {
    if (this.passwordResetForm.valid) {
      const email = this.passwordResetForm.get('email')?.value;
      
      this.afAuth.sendPasswordResetEmail(email)
        .then(() => {
          this.successMessage = 'Password reset email sent successfully. Please check your inbox.';
          this.passwordResetForm.reset();
          // Optionally, redirect the user
          // this.router.navigate(['/some-page']);
        })
        .catch((error) => {
          console.error('Error sending password reset email:', error);
          this.errorMessage = 'An error occurred while sending the password reset email. Please try again.';
        });
    }
  }
}
