import { Injectable } from "@angular/core";
import { ComponentStore } from "@ngrx/component-store";
import { JobSummaryService } from "../services/job.summary.service";    

interface CustomJobSlice {
    jobForm: any;
    companyForm: any;
    uploadForm: File[];
}

const initialState: CustomJobSlice = {
    jobForm: null,
    companyForm: null,
    uploadForm: []
};
  

@Injectable({providedIn: 'root'})
export class JobStore extends ComponentStore<CustomJobSlice> {

    constructor(private jobSummarySrv: JobSummaryService) {
        super(initialState);
    
        // Log state changes
        this.state$.subscribe(state => console.log('JobStore State:', state));
    }

    readonly jobForm$ = this.select(state => state.jobForm);
    readonly companyForm$ = this.select(state => state.companyForm);
    readonly uploadForm$ = this.select(state => state.uploadForm);

    readonly setJobForm = this.updater((state, jobForm: any) => ({ ...state, jobForm }));
    readonly setCompanyForm = this.updater((state, companyForm: any) => ({ ...state, companyForm }));
    readonly setUploadForm = this.updater((state, uploadForm: File[]) => ({ ...state, uploadForm }));
}