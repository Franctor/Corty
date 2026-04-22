import {
    AbstractControl,
    ValidationErrors,
    ValidatorFn,
} from '@angular/forms';

// ============================================================
// CORTY — Custom Validators
// ============================================================

export class CortyValidators {

    // ------------------------------------------------------------
    // GENERIC
    // ------------------------------------------------------------

    /**
      * The field cannot be just blank spaces.
      * For username, name, etc.
     */
    static noWhitespace(control: AbstractControl): ValidationErrors | null {
        const value = control.value as string;
        return (!value || value.trim().length > 0) ? null : { noWhitespace: true };
    }

    /**
     * Strong pass:
     * - Minimum 8 characters
      * - At least one uppercase letter
      * - At least one number
      * - At least one special character (!@#$%^&*...)
     */
    static strongPassword(control: AbstractControl): ValidationErrors | null {
        const value = control.value as string;
        const errors: Record<string, boolean> = {};

        if (value) {
            if (value.length < 8) errors['minLength'] = true;
            if (!/[A-Z]/.test(value)) errors['noUppercase'] = true;
            if (!/[0-9]/.test(value)) errors['noNumber'] = true;
            if (!/[!@#$%^&*(),.?":{}|<>]/.test(value)) errors['noSpecialChar'] = true;
        }

        return Object.keys(errors).length > 0 ? { strongPassword: errors } : null;
    }

    /**
     * Group validator: confirm that two fields match.
     * Apply to the FormGroup, not the individual control.
     *
     */
    static passwordMatch(
        passwordField: string,
        confirmField: string
    ): ValidatorFn {
        return (group: AbstractControl): ValidationErrors | null => {
            const password = group.get(passwordField)?.value;
            const confirm = group.get(confirmField)?.value;
            if (!password || !confirm) return null;
            return password === confirm ? null : { passwordMatch: true };
        };
    }

    // ------------------------------------------------------------
    // SPAIN
    // ------------------------------------------------------------

    /**
      * Valid Spanish phone number.
      * Accepts: 6XX, 7XX (mobile) and 8XX, 9XX (landline)
      * With or without the +34 or 0034 country code
    */
    static phoneEs(control: AbstractControl): ValidationErrors | null {
        const value = (control.value as string)?.replace(/\s/g, '');
        const regex = /^(\+34|0034)?[6789]\d{8}$/;
        return (!value || regex.test(value)) ? null : { phoneEs: true };
    }

    /**
        * Valid Spanish DNI/NIE.
        * DNI: 8 digits + letter (12345678Z)
        * NIE: X/Y/Z + 7 digits + letter (X1234567L)
    */
    static DNI(control: AbstractControl): ValidationErrors | null {
        const value = (control.value as string)?.toUpperCase().trim();
        const dniLetters = 'TRWAGMYFPDXBNJZSQVHLCKE';

        const dniRegex = /^(\d{8})([A-Z])$/;
        const dniMatch = value ? value.match(dniRegex) : null;

        const nieRegex = /^([XYZ])(\d{7})([A-Z])$/;
        const nieMatch = value && !dniMatch ? value.match(nieRegex) : null;

        let result: ValidationErrors | null;
        if (!value) {
            result = null;
        } else if (dniMatch) {
            const expectedLetter = dniLetters[parseInt(dniMatch[1]) % 23];
            result = dniMatch[2] === expectedLetter ? null : { dni: true };
        } else if (nieMatch) {
            const niePrefix: Record<string, string> = { X: '0', Y: '1', Z: '2' };
            const nieNumber = parseInt(niePrefix[nieMatch[1]] + nieMatch[2]);
            const expectedLetter = dniLetters[nieNumber % 23];
            result = nieMatch[3] === expectedLetter ? null : { dni: true };
        } else {
            result = { dni: true };
        }
        return result;
    }

    /**
        * The user must be of legal age (>= 18 years).
        * The control must have a value of type Date or ISO string.
    */
    static minAge(minYears: number): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            const birthDate = control.value ? new Date(control.value) : null;
            let result: ValidationErrors | null = null;
            if (birthDate) {
                const today = new Date();
                const age = today.getFullYear() - birthDate.getFullYear();
                const monthDiff = today.getMonth() - birthDate.getMonth();
                const dayDiff = today.getDate() - birthDate.getDate();
                const realAge = monthDiff < 0 || (monthDiff === 0 && dayDiff < 0) ? age - 1 : age;
                result = realAge >= minYears ? null : { minAge: { required: minYears, actual: realAge } };
            }
            return result;
        };
    }
}

// ==============================================================
// ERROR MESSAGES — helper to display in templates
// ===========================================================
export const CortyValidatorMessages: Record<string, string> = {
    required: 'Este campo es obligatorio',
    minlength: 'Demasiado corto',
    maxlength: 'Demasiado largo',
    email: 'Introduce un email válido',
    noWhitespace: 'No puede contener solo espacios',
    phoneEs: 'Introduce un teléfono español válido',
    dni: 'Introduce un DNI/NIE válido',
    minAge: 'No supera la edad necesaria',
    passwordMatch: 'Las contraseñas no coinciden',
    strongPassword: 'La contraseña no es suficientemente segura',
};

/**
* Returns the first error message from a control.
*/
export function getFirstError(control: AbstractControl): string | null {
  const errors = control.errors;
  let message: string | null = null;
  if (errors) {
    if (errors['required'])       message = 'Este campo es obligatorio';
    else if (errors['email'])     message = 'El formato del email no es válido';
    else if (errors['minlength']) message = `Mínimo ${errors['minlength'].requiredLength} caracteres`;
    else if (errors['maxlength']) message = `Máximo ${errors['maxlength'].requiredLength} caracteres`;
    else if (errors['noWhitespace'])  message = 'No puede contener espacios';
    else if (errors['phoneNumber'])   message = 'El formato del teléfono no es válido';
    else if (errors['minAge'])        message = `Debes tener al menos ${errors['minAge'].required} años para registrarte`;
    else if (errors['strongPassword']) message = 'La contraseña no cumple los requisitos';
    else                               message = 'Campo no válido';
  }
  return message;
}