package com.drassapps.ourwall;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    // MARK - CLASS

    private FirebaseAuth mAuth;
    private Common ui = new Common();

    // MARK - UI

    private RelativeLayout main;
    private EditText emailText, passwordText;

    // MARL - LIFE CYCLE

    @Override
    protected void onStart(){
        super.onStart();
        FirebaseApp.initializeApp(this);
    }

    // MARK - MAIN

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        emailText = findViewById(R.id.main_emailText);
        passwordText = findViewById(R.id.main_passwordText);
        TextView loginTv = findViewById(R.id.main_loginText);
        TextView logoTv = findViewById(R.id.main_logoN1);
        TextView newAccountTv = findViewById(R.id.main_newAccountext);

        RelativeLayout register = findViewById(R.id.main_register);
        RelativeLayout login = findViewById(R.id.main_login);
        RelativeLayout dummy = findViewById(R.id.main_dummy);
        main = findViewById(R.id.main_mainLayout);
        dummy.requestFocus();

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/roboto.ttf");

        emailText.setTypeface(typeface);
        passwordText.setTypeface(typeface);
        loginTv.setTypeface(typeface);
        newAccountTv.setTypeface(typeface);
        logoTv.setTypeface(typeface);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = emailText.getText().toString();
                String password = passwordText.getText().toString();

                if(validEmail(email) && password.length() > 5){
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(MainActivity.this,
                                    new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            FirebaseUser user =
                                                    FirebaseAuth.getInstance().getCurrentUser();

                                            if (user != null) { updateUI(user); }
                                            if (!task.isSuccessful()) {
                                                ui.setSnackBar(main, getString(R.string.err_login));
                                            }
                                        }
                                    });
                }else{ ui.setSnackBar(main, getString(R.string.err_login)); }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailText.getText().toString();
                String password = passwordText.getText().toString();

                if (validEmail(email) && password.length() > 5){
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(MainActivity.this,
                                    new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {

                                            FirebaseUser user =
                                                    FirebaseAuth.getInstance().getCurrentUser();

                                            if (user != null) { updateUI(user); }
                                            if (!task.isSuccessful()) {
                                                ui.setSnackBar(main, getString(R.string.err_user));
                                            }
                                        }
                            });
                } else {
                    setSnackBarWithButton(main,getString(R.string.err_login));
                }
            }
        });
    }

    // MARK - ACTIONS

    public void showText(View v) { passwordText.setInputType(InputType.TYPE_CLASS_TEXT); }

    // MARK - METHODS

    // Update UI
    private void updateUI(FirebaseUser user) {
        Intent i = new Intent(MainActivity.this, UserActivity.class);
        i.putExtra("email",user.getEmail());
        startActivity(i);
    }
    // Allow to manage user email
    private boolean validEmail(String correo) {
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            emailText.setError(getString(R.string.err_email));
            return false;
        } else { emailText.setError(null); }

        return true;
    }
    // Display custom snackBar
    private void setSnackBarWithButton(View coordinatorLayout, String snackTitle) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, snackTitle, Snackbar.LENGTH_SHORT);
        snackbar.setAction(getString(R.string.dialog_title), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        new ContextThemeWrapper(MainActivity.this,R.style.AlertDialogCustom));
                builder.setMessage(getString(R.string.dialog_message1))
                        .setTitle(getString(R.string.dialog_title))
                        .setPositiveButton(getString(R.string.dialog_button),
                                new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) { }
                        });
                builder.setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }
        });
        snackbar.show();
        View view = snackbar.getView();
        TextView txtv = view.findViewById(android.support.design.R.id.snackbar_text);
        txtv.setGravity(Gravity.CENTER_HORIZONTAL);
    }
}