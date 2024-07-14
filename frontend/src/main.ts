import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';

// Firebase app initialization
import { initializeApp } from 'firebase/app';

const firebaseConfig = {
  apiKey: "AIzaSyDRiurP6EfCnWfuQJYTKg5twqfxBAcJQYI",
  authDomain: "interviewquest-b0b2e.firebaseapp.com",
  databaseURL: "https://interviewquest-b0b2e-default-rtdb.asia-southeast1.firebasedatabase.app",
  projectId: "interviewquest-b0b2e",
  storageBucket: "interviewquest-b0b2e.appspot.com",
  messagingSenderId: "35471201213",
  appId: "1:35471201213:web:c4468b22a8384f91a438d4"
};

initializeApp(firebaseConfig);

platformBrowserDynamic().bootstrapModule(AppModule)
  .catch(err => console.error(err));
