import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-new-order',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './new-order.html'
})
export class NewOrder {}