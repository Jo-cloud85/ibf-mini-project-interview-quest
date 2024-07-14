enum JobLevel {
    Internship = "INTERNSHIP",
    Entry = "ENTRY",
    Junior = "JUNIOR",
    Mid = "MID",
    Senior = "SENIOR",
    Others = "OTHERS"
}

export interface JobSummary {
    customJobId: string //id of the job created
    assistantId: string
    threadId: string
    firebaseThreadKey: string
    title: string
    level: string
    createdTime: Date
    isAttempted: boolean
}

export interface JobDetails {
    customJobId: number //id of the job created
    createdDate: Date
    jobTitle: string
    jobDescription: string
    jobLevel: JobLevel //enum
    companyName?: string //as input from user is optional
    companyDescription?: string //as input from user is optional
    documentUrls: string[];
}

export interface Question {
    questionId: string
    questionType: string
    question: string
    answerApproach: string
}

export interface QuestionSummary {
    questionId: string
    questionType: string
    question: string
}

export interface QuestionSubThread {
    questionId: string;
    questionType: string;
    question: string;
    threadContents: ThreadContent[]
}

export interface ThreadContent {
    timestamp: Date;
    role: string;
    content: string;
}

export interface Schedule {
    scheduleId: string
    title: string
    description: string
    startDateTime: Date
    endDateTime: Date
    email: string // required as login email may not be a gmail
    url: string
}

