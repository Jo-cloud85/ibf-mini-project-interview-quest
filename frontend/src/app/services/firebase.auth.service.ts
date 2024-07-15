import { HttpClient, HttpErrorResponse, HttpHeaders } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { FirebaseError } from "@angular/fire/app";
import { AngularFireAuth } from '@angular/fire/compat/auth';
import firebase from 'firebase/compat/app';
import { BehaviorSubject, Observable, firstValueFrom, from, map } from "rxjs";

@Injectable({providedIn: 'root'})
export class AuthService {

    private readonly http = inject(HttpClient);
    private readonly afAuth = inject(AngularFireAuth);

    signInAttempted: boolean = false;
    isAuthenticated: boolean = false;

    signUp(firstName: string, lastName: string, email: string, password: string): Observable<any> {
        const headers = new HttpHeaders().set('Content-Type', 'application/json')

        return this.http.post<{ token: string }>(
            '/api/auth/signup', 
            { firstName, lastName, email, password },
            { headers }
        )
    }

    googleSignUp(firstName: string, lastName: string, email: string): Promise<any> {
        const headers = new HttpHeaders().set('Content-Type', 'application/json')

        return firstValueFrom(this.http.post<{ token: string }>(
            '/api/auth/google-signup', 
            { firstName, lastName, email },
            { headers }
        ))
    }

    
    signIn(email: string, password: string): Promise<any> {
        return this.afAuth.signInWithEmailAndPassword(email, password)
            .then((userCredential) => {
                userCredential.user?.getIdToken()
            })
            .then((idToken: any) => {
                const headers = new HttpHeaders()
                    .set('Content-Type', 'application/json')
                    .set('Authorization', `Bearer ${idToken}`);
                return firstValueFrom(this.http.post<{ custom_token: string }>(
                    '/api/auth/signin',
                    { email, password },
                    { headers }
                ));
            })
            .catch((error: FirebaseError) => {
                if (error.code === 'auth/user-not-found') {
                    throw new Error('Email not registered. Please sign up.');
                } else if (error.code === 'auth/wrong-password') {
                    throw new Error('Incorrect password. Please try again.');
                } else if (error.code === 'auth/invalid-credential') {
                    throw new Error('Invalid email & password. Please try again.');
                } else {
                    throw new Error('An error occurred during sign in. Please try again later.');
                }
            });
    }


    onAuthStateChanged(): void {
        this.afAuth.onAuthStateChanged((user) => {
            if (user) {
                console.log('User is signed in:', user);
                if (!user.emailVerified) {
                console.log('Email not verified.');
                }
            } else {
            console.log('User is signed out.');
            }
        })
    }


    signInWithCustomToken(token: string): Observable<firebase.auth.UserCredential> {
        return from(this.afAuth.signInWithCustomToken(token));
    }


    sendEmailVerification(): Observable<void> {
        return from(this.afAuth.currentUser.then(user => user!.sendEmailVerification()));
    }


    getIdToken(): Observable<string | null> {
        return this.afAuth.idToken;
    }


    isSignedIn(): Observable<boolean> {
        return this.afAuth.authState
            .pipe(
                map(user => !!user)
            );
    }

    signOut(): Promise<void> {
        return this.afAuth.signOut()
          .then(() => {
            this.isAuthenticated = false;
          });
    }

    getProfile(email: string): Observable<any> {
        return this.http.get<any>(`/api/auth/profile?email=${email}`);
    }

    updateProfile(profileData: any): Observable<any> {
        return this.http.put<any>(`/api/auth/profile`, profileData);
    }
}