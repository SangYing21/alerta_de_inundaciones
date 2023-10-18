package com.example.alerta_de_inundaciones;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;
    private static final String TAG = "GoogleActivity";
 MaterialButton mSignInButtonGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSignInButtonGoogle = findViewById(R.id.btnGoogle);

        /* Auth Google*/

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mSignInButtonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        /*____________________________________________________________________________-*/
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    ActivityResultContract<Intent, GoogleSignInAccount> signInContract = new ActivityResultContract<Intent, GoogleSignInAccount>() {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Intent input) {
            return input;
        }

        @Override
        public GoogleSignInAccount parseResult(int resultCode, @Nullable Intent intent) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
            if (task.isSuccessful()) {
                return task.getResult();
            } else {
                return null;
            }
        }
    };

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    private ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(signInContract, result -> {
        if (result != null) {
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount account = result;
            //Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
            firebaseAuthWithGoogle(account.getIdToken());
        } else {
            // Google Sign In failed, update UI appropriately
            Log.w(TAG, "Google sign in failed");
            Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
        }
    });


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            irHome();
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                          //  mTextViewRespuesta.setText(task.getException().toString());
                            updateUI(null);
                        }
                    }
                });
    }




    private void updateUI(FirebaseUser user) {
        user = mAuth.getCurrentUser();
        if (user != null){
            irHome();
        }
    }

    private void irHome() {
        Intent intent = new Intent(MainActivity.this, inicio.class);
        startActivity(intent);
        finish();
    }

}