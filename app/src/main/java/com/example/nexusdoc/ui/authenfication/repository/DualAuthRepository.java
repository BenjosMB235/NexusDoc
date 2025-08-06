package com.example.nexusdoc.ui.authenfication.repository;

import android.content.Context;
import android.util.Log;

import com.example.nexusdoc.ui.authenfication.model.AuthLoginRequest;
import com.example.nexusdoc.ui.authenfication.model.AuthRegistrationRequest;
import com.example.nexusdoc.ui.authenfication.model.AuthResult;

public class DualAuthRepository {
    private static final String TAG = "DualAuthRepository";

    private final FirebaseAuthRepository firebaseRepo;
    private final SupabaseAuthRepository supabaseRepo;

    public DualAuthRepository(Context context) {
        this.firebaseRepo = new FirebaseAuthRepository(context);
        this.supabaseRepo = new SupabaseAuthRepository(context);
    }

    public interface DualAuthCallback {
        void onSuccess(AuthResult.AuthProvider successfulProvider);
        void onError(String errorMessage);
        void onPartialSuccess(AuthResult.AuthProvider successfulProvider, String failedProviderError);
    }

    public void signUp(AuthRegistrationRequest request, DualAuthCallback callback) {
        // État de l'inscription
        final boolean[] firebaseSuccess = {false};
        final boolean[] supabaseSuccess = {false};
        final String[] firebaseError = {null};
        final String[] supabaseError = {null};
        final boolean[] callbackCalled = {false};

        // Inscription Firebase
        firebaseRepo.signUp(request, new FirebaseAuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                firebaseSuccess[0] = true;
                checkBothResults();
            }

            @Override
            public void onError(String errorMessage) {
                firebaseError[0] = errorMessage;
                checkBothResults();
            }

            private void checkBothResults() {
                // Attendre que les deux soient terminés
                if (firebaseError[0] == null && supabaseError[0] == null &&
                        (!firebaseSuccess[0] || !supabaseSuccess[0])) {
                    return; // Attendre la fin des deux
                }

                synchronized (callbackCalled) {
                    if (callbackCalled[0]) return;
                    callbackCalled[0] = true;

                    if (firebaseSuccess[0] && supabaseSuccess[0]) {
                        callback.onSuccess(AuthResult.AuthProvider.BOTH);
                    } else if (firebaseSuccess[0] && supabaseError[0] != null) {
                        callback.onPartialSuccess(AuthResult.AuthProvider.FIREBASE, supabaseError[0]);
                    } else if (supabaseSuccess[0] && firebaseError[0] != null) {
                        callback.onPartialSuccess(AuthResult.AuthProvider.SUPABASE, firebaseError[0]);
                    } else {
                        callback.onError("Firebase: " + firebaseError[0] + "\nSupabase: " + supabaseError[0]);
                    }
                }
            }
        });

        // Inscription Supabase
        supabaseRepo.signUp(request, new SupabaseAuthRepository.SupabaseAuthCallback() {
            @Override
            public void onSuccess(String accessToken, String refreshToken) {
                supabaseSuccess[0] = true;
                checkBothResults();
            }

            @Override
            public void onError(String errorMessage) {
                supabaseError[0] = errorMessage;
                checkBothResults();
            }

            private void checkBothResults() {
                // Même logique que Firebase
                if (firebaseError[0] == null && supabaseError[0] == null &&
                        (!firebaseSuccess[0] || !supabaseSuccess[0])) {
                    return;
                }

                synchronized (callbackCalled) {
                    if (callbackCalled[0]) return;
                    callbackCalled[0] = true;

                    if (firebaseSuccess[0] && supabaseSuccess[0]) {
                        callback.onSuccess(AuthResult.AuthProvider.BOTH);
                    } else if (firebaseSuccess[0] && supabaseError[0] != null) {
                        callback.onPartialSuccess(AuthResult.AuthProvider.FIREBASE, supabaseError[0]);
                    } else if (supabaseSuccess[0] && firebaseError[0] != null) {
                        callback.onPartialSuccess(AuthResult.AuthProvider.SUPABASE, firebaseError[0]);
                    } else {
                        callback.onError("Firebase: " + firebaseError[0] + "\nSupabase: " + supabaseError[0]);
                    }
                }
            }
        });
    }

    public void signIn(AuthLoginRequest request, DualAuthCallback callback) {
        // Logique similaire pour la connexion
        final boolean[] firebaseSuccess = {false};
        final boolean[] supabaseSuccess = {false};
        final String[] firebaseError = {null};
        final String[] supabaseError = {null};
        final boolean[] callbackCalled = {false};

        firebaseRepo.signIn(request, new FirebaseAuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                firebaseSuccess[0] = true;
                checkResults();
            }

            @Override
            public void onError(String errorMessage) {
                firebaseError[0] = errorMessage;
                checkResults();
            }

            private void checkResults() {
                if (firebaseError[0] == null && supabaseError[0] == null &&
                        (!firebaseSuccess[0] || !supabaseSuccess[0])) {
                    return;
                }

                synchronized (callbackCalled) {
                    if (callbackCalled[0]) return;
                    callbackCalled[0] = true;

                    if (firebaseSuccess[0] && supabaseSuccess[0]) {
                        callback.onSuccess(AuthResult.AuthProvider.BOTH);
                    } else if (firebaseSuccess[0]) {
                        callback.onPartialSuccess(AuthResult.AuthProvider.FIREBASE, supabaseError[0]);
                    } else if (supabaseSuccess[0]) {
                        callback.onPartialSuccess(AuthResult.AuthProvider.SUPABASE, firebaseError[0]);
                    } else {
                        callback.onError("Firebase: " + firebaseError[0] + "\nSupabase: " + supabaseError[0]);
                    }
                }
            }
        });

        supabaseRepo.signIn(request, new SupabaseAuthRepository.SupabaseAuthCallback() {
            @Override
            public void onSuccess(String accessToken, String refreshToken) {
                supabaseSuccess[0] = true;
                checkResults();
            }

            @Override
            public void onError(String errorMessage) {
                supabaseError[0] = errorMessage;
                checkResults();
            }

            private void checkResults() {
                if (firebaseError[0] == null && supabaseError[0] == null &&
                        (!firebaseSuccess[0] || !supabaseSuccess[0])) {
                    return;
                }

                synchronized (callbackCalled) {
                    if (callbackCalled[0]) return;
                    callbackCalled[0] = true;

                    if (firebaseSuccess[0] && supabaseSuccess[0]) {
                        callback.onSuccess(AuthResult.AuthProvider.BOTH);
                    } else if (firebaseSuccess[0]) {
                        callback.onPartialSuccess(AuthResult.AuthProvider.FIREBASE, supabaseError[0]);
                    } else if (supabaseSuccess[0]) {
                        callback.onPartialSuccess(AuthResult.AuthProvider.SUPABASE, firebaseError[0]);
                    } else {
                        callback.onError("Firebase: " + firebaseError[0] + "\nSupabase: " + supabaseError[0]);
                    }
                }
            }
        });
    }

    public void signOut(DualAuthCallback callback) {
        firebaseRepo.signOut();
        supabaseRepo.signOut(new SupabaseAuthRepository.SupabaseAuthCallback() {
            @Override
            public void onSuccess(String accessToken, String refreshToken) {
                callback.onSuccess(AuthResult.AuthProvider.BOTH);
            }

            @Override
            public void onError(String errorMessage) {
                // Même si Supabase échoue, Firebase est déconnecté
                callback.onPartialSuccess(AuthResult.AuthProvider.FIREBASE, errorMessage);
            }
        });
    }

    public void sendEmailVerification(FirebaseAuthRepository.AuthCallback callback) {
        firebaseRepo.sendEmailVerification(callback);
    }

    public void resetPassword(String email, FirebaseAuthRepository.AuthCallback callback) {
        firebaseRepo.resetPassword(email, callback);
    }

    public void cleanup() {
        firebaseRepo.cleanup();
    }
}