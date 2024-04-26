package com.jgomez.nihongo;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.jgomez.nihongo.controllers.AuthenticationManager;
import com.jgomez.nihongo.controllers.NetworkMonitor;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {
    private CircularProgressIndicator progressBar;
    private Button loginButton;
    private AuthenticationManager authManager;
    private static final int RC_SIGN_IN = 9001;
    private NetworkMonitor networkMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar);
        loginButton = findViewById(R.id.loginButton);

        authManager = new AuthenticationManager(this, this);
        networkMonitor = new NetworkMonitor(this);
        networkMonitor.registerNetworkCallback();



        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.show();
                loginButton.setVisibility(View.GONE);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (networkMonitor.isConnected()) {
                            if (authManager.isFirstRun()) {
                                // Loguear con Google por coacción
                                loginWithGoogle();
                                authManager.setFirstRun(false);
                            } else {
                                // Intentar loguear con Google (cacheando si es posible)
                                loginWithGoogle();
                            }
                        } else {
                            if (authManager.isFirstRun()) {
                                // Caso 4: NO hay conexión y ES la primera ejecución
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showConnectionErrorDialog();
                                    }
                                });
                            } else {
                                // Caso 3: NO hay conexión y NO es la primera ejecución
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showOfflineAlertDialog();
                                    }
                                });
                            }
                        }
                    }
                }).start();
            }
        });
    }

    private void loginWithGoogle() {
        Intent signInIntent = authManager.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        // Si no peta, significará que se ha logueado correctamente.
    }

    private void showConnectionErrorDialog() {
        // Mostrar un AlertDialog indicando que se necesita conexión la primera vez
        // Este método podría mostrar un mensaje de error más específico
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error de conexión");
        builder.setMessage("Se requiere conexión a internet la primera vez que inicies sesión.");
        builder.setPositiveButton("Aceptar", null);
        builder.show();
    }

    private void showOfflineAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sin conexión")
                .setMessage("¿Iniciar sin conexión? (Algunas funcionalidades podrían no estar disponibles y el contenido estar desactualizado).")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadResoruces();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FFB300")); // colorAlertIcon
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#3949AB")); // colorAlertButtons
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            boolean loginSuccessful = false;
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                authManager.firebaseAuthWithGoogle(account.getIdToken());
                loginSuccessful = true;
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(MainActivity.this, "No ha sido posible iniciar sesión. Por favor, inténtelo de nuevo.", Toast.LENGTH_LONG).show();
                progressBar.hide();
                loginButton.setVisibility(View.VISIBLE);
                loginSuccessful = false;
            }
        }
    }

    public void loadResoruces(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Cargar los recursos: Se pone a "pensar" y cuando termine, navega.
                progressBar.show();
                loginButton.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "¡Bienvenido! Por favor, espere mientras se cargan los recursos.", Toast.LENGTH_LONG).show();
                // TODO NAVEGACION A CURSOS
            }
        });
    }
}