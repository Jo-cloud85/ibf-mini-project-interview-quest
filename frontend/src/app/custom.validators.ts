import { AbstractControl, ValidationErrors } from "@angular/forms";

export function futureDateValidator(control: AbstractControl): ValidationErrors | null {
    const selectedDate = new Date(control.value);
    const now = new Date();
    return selectedDate > now ? null : { pastDate: true };
}

export function passwordValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (!value) {
      return null;
    }
    const hasNumber = /\d/.test(value);
    const hasMinLength = value.length >= 8;
    if (!hasNumber || !hasMinLength) {
      return { passwordValidator: true };
    }
    return null;
}