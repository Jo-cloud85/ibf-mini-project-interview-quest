package ibf_tfip.mini_project.backend.services;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.firebase.FirebaseApp;

import ibf_tfip.mini_project.backend.models.MainThread.QuestionSubThread;
import ibf_tfip.mini_project.backend.models.MainThread.ThreadContent;
import ibf_tfip.mini_project.backend.models.Question;
import ibf_tfip.mini_project.backend.models.QuestionSummary;
import ibf_tfip.mini_project.backend.repositories.FirebaseRepo;

@Service
public class FirebaseService {

    @Autowired
    private FirebaseRepo firebaseRepo;

    public FirebaseService(FirebaseApp firebaseApp) {}
    
    //// Realtime Database ///////////////////////////////////////////////////////////////
    // Firebase RealTime Database - Thread
    public String saveFirstThreadToFB(
        String userId, List<ThreadContent> firstSubThread, String customJobId, String assistantId, String threadId) throws IOException {

        return firebaseRepo.saveFirstSubThreadToFB(userId, firstSubThread, customJobId, assistantId, threadId);
    }


    // Firebase RealTime Database - Thread
    public List<ThreadContent> getFirstSubThreadFrFB(
        String userId,
        String key) throws IOException, ExecutionException, InterruptedException, TimeoutException {

        return firebaseRepo.getFirstSubThreadFrFB(userId, key);
    }


    // Firebase RealTime Database - Thread
    public void saveQnsThreadToFB(
        String userId, 
        QuestionSubThread qnsThread, String key) {
        
        firebaseRepo.saveQuestionSubThreadToFB(userId, qnsThread, key);
    }


    // Firebase RealTime Database - Thread
    public List<QuestionSummary> getListOfQuestionSummaryFrFB(
        String userId,
        String key) throws IOException, ExecutionException, InterruptedException, TimeoutException {

        return firebaseRepo.getListOfQuestionSummaryFrFB(userId, key);
    }


    // Firebase RealTime Database - Thread
    public List<ThreadContent> getQuestionSubThreadContentsByQnsIdFrFB(
        String userId,
        String key,
        String qnsId) throws IOException, ExecutionException, InterruptedException, TimeoutException {

        return firebaseRepo.getQuestionSubThreadContentsByQnsIdFrFB(userId, key, qnsId);
    }


    //// Firestore ////////////////////////////////////////////////////////////////////////
    // Firebase Firestore - Interview Qns
    public void saveInterviewQnsToFS(
        String userId,
        List<Question> qnsList, 
        String customJobId) throws InterruptedException, ExecutionException {

        firebaseRepo.saveInterviewQnsToFS(userId, qnsList, customJobId);
    }


    // Firebase Firestore - Interview Qns
    public List<Question> getAllInterviewQnsFrFS(
        String userId,
        String customJobId) throws InterruptedException, ExecutionException {
        
        return firebaseRepo.getAllInterviewQnsFrFS(userId, customJobId);
    }


    // Firebase Firestore - Interview Qns
    public Question getInterviewQnsByQnsIdFrFS(
        String userId, 
        String customJobId, 
        String qnsId) throws InterruptedException, ExecutionException {
        
        return firebaseRepo.getInterviewQnsByQnsIdFrFS(userId, customJobId, qnsId);
    }
}
