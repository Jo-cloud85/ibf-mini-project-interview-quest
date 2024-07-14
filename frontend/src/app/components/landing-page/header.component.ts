import { Component, Input, Output } from '@angular/core';
import { NavigationItem } from '../../models/ui.model';
import { Subject } from 'rxjs';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  
  interviewquest: string = "assets/logo-dark.svg";

  @Input() openNavigation: boolean = false;
  @Input() navigation: NavigationItem[] = [];
  @Input() pathname: { hash: string } = { hash: '' };

  @Output() toggleNavigationEvent = new Subject<void>();

  handleClick(event: Event): void {
    event.preventDefault();
    this.toggleNavigationEvent.next();
  }
}
