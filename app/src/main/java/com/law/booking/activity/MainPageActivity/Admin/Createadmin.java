package com.law.booking.activity.MainPageActivity.Admin;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.law.booking.R;
import com.law.booking.activity.Application.TinkerApplications;
import com.law.booking.activity.tools.Utils.Agreement_content;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.HashMap;
import java.util.Map;

public class Createadmin extends AppCompatActivity {
    AppCompatButton back;
    private EditText nameEditText, emailEditText, usernameEditText, passwordEditText, confirmPasswordEditText;
    private EditText phoneEditText;
    private AppCompatButton registerButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private boolean eye1check = false;
    private boolean eye1check2 = false;
    private ImageView password_eye1, password_eye2;
    private CheckBox agreement_cb;
    private boolean isAgreed = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createadmin);
        back = findViewById(R.id.back);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        changeStatusBarColor(getResources().getColor(R.color.purple_theme2));
        nameEditText = findViewById(R.id.name);
        emailEditText = findViewById(R.id.email);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        agreement_cb = findViewById(R.id.agreement_cb);
        password_eye1 = findViewById(R.id.password_eye1);
        password_eye2 = findViewById(R.id.password_eye2);
        phoneEditText = findViewById(R.id.Phone);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        registerButton = findViewById(R.id.registered);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Lawyer");

        agreement_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isAgreed = b;
                if(isAgreed){
                    showdagreementdialog();
                }
            }
        });


        findViewById(R.id.password_eye1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eye1check = !eye1check;

                if (eye1check) {
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    password_eye1.setImageResource(R.mipmap.eye_open);
                } else {
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    password_eye1.setImageResource(R.mipmap.eye_close);
                }
                passwordEditText.setSelection(passwordEditText.getText().length());
            }
        });

        findViewById(R.id.password_eye2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eye1check2 = !eye1check2;

                if (eye1check2) {
                    confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    password_eye2.setImageResource(R.mipmap.eye_open);
                } else {
                    confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    password_eye2.setImageResource(R.mipmap.eye_close);
                }
                confirmPasswordEditText.setSelection(passwordEditText.getText().length());
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TinkerApplications app = (TinkerApplications) Createadmin.this.getApplication();
                app.clearUserData(false,Createadmin.this);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void showdagreementdialog() {
        DialogPlus dialog = DialogPlus.newDialog(Createadmin.this)
                .setContentHolder(new ViewHolder(R.layout.agreement_layout))
                .setContentWidth(ViewGroup.LayoutParams.MATCH_PARENT)
                .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setGravity(Gravity.CENTER)
                .setCancelable(false)
                .create();
        View dialogView = dialog.getHolderView();
        Button agreed = dialogView.findViewById(R.id.agreed);
        Button ignore = dialogView.findViewById(R.id.ignore);

        TextView introductionContent = dialogView.findViewById(R.id.introductionContent);
        TextView Acceptance_content = dialogView.findViewById(R.id.Acceptance_content);
        TextView Responsibilities_content = dialogView.findViewById(R.id.Responsibilities_content);
        TextView Conduct_content =  dialogView.findViewById(R.id.Conduct_content);
        TextView User_Conduct_content = dialogView.findViewById(R.id.User_Conduct_content);
        TextView Data_Protection_content = dialogView.findViewById(R.id.Data_Protection_content);
        TextView Intellectual_content = dialogView.findViewById(R.id.Intellectual_content);
        TextView Liability_content = dialogView.findViewById(R.id.Liability_content);
        TextView Services_content = dialogView.findViewById(R.id.Services_content);
        TextView Amendments_content = dialogView.findViewById(R.id.Amendments_content);
        TextView Governing_content = dialogView.findViewById(R.id.Governing_content);
        TextView Contact_content = dialogView.findViewById(R.id.Contact_content);

        introductionContent.setText(Agreement_content.Introduction_content);
        Acceptance_content.setText(Agreement_content.Acceptance_content);
        Responsibilities_content.setText(Agreement_content.Responsibilities_content);
        Conduct_content.setText(Agreement_content.Conduct_content);
        User_Conduct_content.setText(Agreement_content.User_Conduct_content);
        Data_Protection_content.setText(Agreement_content.Data_Protection_content);
        Intellectual_content.setText(Agreement_content.Intellectual_content);
        Liability_content.setText(Agreement_content.Liability_content);
        Services_content.setText(Agreement_content.Services_content);
        Amendments_content.setText(Agreement_content.Amendments_content);
        Governing_content.setText(Agreement_content.Governing_content);
        Contact_content.setText(Agreement_content.Contact_content);
        agreed.setOnClickListener(view -> dialog.dismiss());
        ignore.setOnClickListener(view ->cancelAgree(dialog));
        dialog.show();
    }

    private void cancelAgree(DialogPlus dialog) {
        dialog.dismiss();
        agreement_cb.setChecked(false);
        isAgreed = false;
    }

    private void changeStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }

    private void registerUser() {
        final String name = nameEditText.getText().toString().trim();
        final String phone = phoneEditText.getText().toString().trim();
        final String email = emailEditText.getText().toString().trim();
        final String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if(!isAgreed){
            Toast.makeText(getApplicationContext(),"Please agree Terms and Conditions.",Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            phoneEditText.setError("Please enter your phone number");
            phoneEditText.requestFocus();
            return;
        }

        // Validate the input fields
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Please enter your full name");
            nameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Please enter your email");
            emailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Please enter a username");
            usernameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Please enter a password");
            passwordEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.setError("Please confirm your password");
            confirmPasswordEditText.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Set display name for the user
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();
                            user.updateProfile(profileUpdates);

                            // Send email verification
                            sendEmailVerification(user);

                            // Save user details to Realtime Database
                            saveUserDetailsToDatabase(user.getUid(), name, email, username,phone);

                            Toast.makeText(getApplicationContext(), "Registration successful. Please check your email for verification. "+email, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Registration failed"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void saveUserDetailsToDatabase(String uid, String name, String email, String username, String phone) {
        DatabaseReference userRef = mDatabase.child(uid);
        userRef.child("name").setValue(name);
        userRef.child("email").setValue(email);
        userRef.child("username").setValue(username);
        userRef.child("phone").setValue(phone); // Save phone number

        Map<String, Object> updates = new HashMap<>();
        boolean isVerify = false;
        updates.put("isVerify", isVerify);
        savedLawsettings(updates,uid);

    }
    private void savedLawsettings(Map<String, Object> updates,String key) {
        if (key != null) {
            DatabaseReference adminRef = FirebaseDatabase.getInstance()
                    .getReference("Lawyer")
                    .child(key);
            adminRef.updateChildren(updates)
                    .addOnSuccessListener(unused -> {
                        Log.e("Saveddatafromservice", "Success: ");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Saveddatafromservice", "Failed to save data: " + e.getMessage());
                    });
        }
    }

    private void sendEmailVerification(final FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("EmailVerification", "Email sent.");
                        } else {
                            Log.e("EmailVerification", "Failed to send verification email.", task.getException());
                        }
                    }
                });

    }
}
