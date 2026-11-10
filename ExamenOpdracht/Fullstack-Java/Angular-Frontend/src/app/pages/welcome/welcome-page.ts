import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-welcome-page',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './welcome-page.html'
})
export class WelcomePage {

}