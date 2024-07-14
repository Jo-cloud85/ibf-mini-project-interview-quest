import { Component, Input, OnInit, inject } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Router } from '@angular/router';

@Component({
  selector: 'app-job-form-00',
  templateUrl: './job-form-00.component.html',
  styleUrl: './job-form-00.component.css'
})
export class JobForm00Component implements OnInit {

  private readonly htmlTitle = inject(Title);

  ngOnInit(): void {
    this.htmlTitle.setTitle('InterviewQuest | Create Job');
  }
}
