import { Component, inject } from '@angular/core';
import { QnsThreadService } from '../../../../services/qns.thread.service';
import { ActivatedRoute, Router } from '@angular/router';
import { JobSummary } from '../../../../models/logic.models';
import { HttpErrorResponse } from '@angular/common/http';
import { JobSummaryService } from '../../../../services/job.summary.service';

@Component({
  selector: 'app-archive',
  templateUrl: './archive.component.html',
  styleUrl: './archive.component.css'
})
export class ArchiveComponent {

}
