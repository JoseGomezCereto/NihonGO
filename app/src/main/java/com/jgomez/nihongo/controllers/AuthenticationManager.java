package com.jgomez.nihongo.controllers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.jgomez.nihongo.R;

import static android.content.ContentValues.TAG;

public class AuthenticationManager {
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String PREF_FIRST_RUN = "FirstRun";
    private SharedPreferences settings;
    private Context context;

    private FirebaseAuth mAuth;
    private Activity activity;
    private MutableLiveData<Boolean> loginStatus = new MutableLiveData<>();

    public AuthenticationManager(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        this.settings = context.getSharedPreferences(PREFS_NAME, 0);
        this.mAuth = FirebaseAuth.getInstance();
    }

    public GoogleSignInClient getGoogleSignInClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        return GoogleSignIn.getClient(context, gso);
    }

    public boolean isFirstRun() {
        return settings.getBoolean(PREF_FIRST_RUN, true);
    }

    public void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            loginStatus.postValue(true);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(activity, "No ha sido posible iniciar sesión. Por favor, inténtelo de nuevo.", Toast.LENGTH_LONG).show();
                            loginStatus.postValue(false);
                        }
                    }
                });
    }

    public Intent getSignInIntent() {
        GoogleSignInClient client = getGoogleSignInClient();
        return client.getSignInIntent();
    }

    //Método auxiliar para setear una primera run para comprobar que va bien
    public void setFirstRun(boolean isFirstRun) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREF_FIRST_RUN, isFirstRun);
        editor.apply();
    }

    //Método para saber si estamos o no logueados
    public LiveData<Boolean> getLoginStatus() {
        return loginStatus;
    }
}