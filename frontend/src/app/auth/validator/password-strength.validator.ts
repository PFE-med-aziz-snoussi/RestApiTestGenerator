import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function passwordStrengthValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;

    if (!value) {
      return null;
    }

    const hasUpperCase = /[A-Z]/.test(value);
    const hasLowerCase = /[a-z]/.test(value);
    const hasNumeric = /[0-9]/.test(value);
    const isValidLength = value.length >= 6;

    const passwordValid = hasUpperCase && hasLowerCase && hasNumeric  && isValidLength;

    if (!passwordValid) {
      return { passwordStrength: 'Le mot de passe doit contenir  une lettre majuscule, une lettre minuscule, un chiffre.' };
    }

    return null;
  };
}
