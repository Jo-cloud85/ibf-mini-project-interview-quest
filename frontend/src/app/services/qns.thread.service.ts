import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable, map, tap} from "rxjs";
import { Question } from "../models/logic.models";

@Injectable({providedIn: "root"})
export class QnsThreadService {

    private readonly http = inject(HttpClient);
    private readonly baseUrl = "/api/qnsthread";

    // For Overview
    // Get all interview questions 
    getAllQuestions(customJobId: string, firebaseThreadKey: string): Observable<Question[]> {

        const query = new HttpParams().set('firebaseThreadKey', firebaseThreadKey);

        return this.http.get<any>(`${this.baseUrl}/${customJobId}/all-questions`, { params: query })
            .pipe(
                map((response: any) => {
                    return response.all_questions.map((q: any) => ({
                        questionId: q.questionId,
                        questionType: q.questionType,
                        question: q.question,
                        answerApproach: q.answerApproach
                    }));
                })
        );
    }


    // For Overview
    // Get interview question by ids
    getQuestionByQnsId(customJobId: string, qnsId: string): Observable<any> {
        return this.http.get<any>(`${this.baseUrl}/${customJobId}/${qnsId}`)
            .pipe(
                map((response: any) => {
                    return {
                        questionId: response.questionId,
                        questionType: response.questionType,
                        question: response.question,
                        answerApproach: response.answerApproach
                    };
                })
            );
    }


    // For Archive
    // Get all attempted interview questions 
    getAllAttemptedQuestions(customJobId: string, firebaseThreadKey: string): Observable<Question[]> {
        const query = new HttpParams().set('firebaseThreadKey', firebaseThreadKey);

        return this.http.get<any>(
                `${this.baseUrl}/${customJobId}/all-attempted-questions`, 
                { params: query }
            )
            .pipe(
                map((response: any) => {
                    return response.all_questionSummaries.map((q: any) => ({
                        questionId: q.questionId,
                        questionType: q.questionType,
                        question: q.question,
                    }));
                })
            );
    }


    // For Archive
    // Get interview question thread by ids
    getAttemptedQuestionThreadByQnsId(customJobId: string, qnsId: string, firebaseThreadKey: string): Observable<any> {
        const query = new HttpParams().set('firebaseThreadKey', firebaseThreadKey);
    
        return this.http.get<any>(
            `${this.baseUrl}/${customJobId}/${qnsId}/question-thread`, 
            { params: query }
        ).pipe(
            map((response: any) => {
                return response.all_threadContents
                    .map((q: any) => {
                        if (q.role === 'assistant') {
                            // Strip off the prefix if it starts with "suggested_answer": " or "feedback": "
                            let content = q.content;
                            if (content.startsWith('{\n  \"suggested_answer')) {
                                content = content
                                    .replace(/^\{\s*"suggested_answer":\s*"/, '')
                                    .replace(/"\s*\}$/, '')
                                    .replace(/(\d+\.\s\*\*[^\*\*]+?\*\*)/g, '<br/><strong>$1</strong>')
                                    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
                                    .replace(/(\d+)\./g, '<br/>$1.')
                            } else if (content.startsWith('{\n  \"feedback')) {
                                content = content
                                    .replace(/^\{\s*"feedback":\s*"/, '')
                                    .replace(/"\s*\}$/, '')
                                    .replace(/(\d+\.\s\*\*[^\*\*]+?\*\*)/g, '<br/><strong>$1</strong>')
                                    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
                                    .replace(/(\d+)\./g, '<br/>$1.')
                            }
                            return {
                                role: q.role,
                                content: content
                            }; 
                        } else {
                            return {
                                role: q.role,
                                content: q.content
                            };
                        }
                    });
            })
        );
    }
}