import { Component } from '@angular/core';
import { SpinnerService } from '../services/Spinner.service';

@Component({
  selector: 'app-spinner',
  templateUrl: './spinner.component.html',
  styleUrls: ['./spinner.component.css']
})
export class SpinnerComponent {
  isSpinnerVisible = this.spinnerService.spinnerState$;

  constructor(private spinnerService: SpinnerService) {}
}
