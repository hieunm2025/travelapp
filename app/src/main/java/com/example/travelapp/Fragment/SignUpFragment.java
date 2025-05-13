package com.example.travelapp.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.example.travelapp.Activity.MainActivity;
import com.example.travelapp.Domain.UserDomain;
import com.example.travelapp.R;
import com.example.travelapp.SharedPrefsHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpFragment extends Fragment {

    private FirebaseAuth mAuth;
    private SharedPrefsHelper prefsHelper;
    private EditText nameEditText, lastNameEditText, phoneEditText, ageEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private CheckBox termsCheckBox;
    private ProgressBar progressBar;
    private DatabaseReference dbRef;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        mAuth = FirebaseAuth.getInstance();
        prefsHelper = new SharedPrefsHelper(requireContext());
        dbRef = FirebaseDatabase.getInstance().getReference();

        nameEditText = view.findViewById(R.id.nameEditText);
        lastNameEditText = view.findViewById(R.id.lastNameEditText);
        phoneEditText = view.findViewById(R.id.phoneEditText);
        ageEditText = view.findViewById(R.id.ageEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText);
        termsCheckBox = view.findViewById(R.id.termsCheckBox);
        Button signUpButton = view.findViewById(R.id.signUpButton);
        TextView loginTextView = view.findViewById(R.id.loginTextView);
        progressBar = view.findViewById(R.id.progressBar);

        signUpButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String lastName = lastNameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String ageStr = ageEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            if (name.isEmpty() || lastName.isEmpty() || phone.isEmpty() || ageStr.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            int age;
            try {
                age = Integer.parseInt(ageStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Age must be a number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!termsCheckBox.isChecked()) {
                Toast.makeText(getContext(), "Please accept terms and conditions", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String fullName = name + " " + lastName;
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(fullName)
                                        .build();

                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(updateTask -> {
                                            if (updateTask.isSuccessful()) {
                                                String userId = user.getUid();
                                                UserDomain userDomain = new UserDomain(fullName, email, phone,age);

                                                dbRef.child("users").child(userId).setValue(userDomain)
                                                        .addOnSuccessListener(aVoid -> {
                                                            progressBar.setVisibility(View.GONE);
                                                            prefsHelper.setLoggedIn(true);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            progressBar.setVisibility(View.GONE);
                                                            Toast.makeText(getContext(), "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        });
                                            } else {
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(getContext(), "Failed to set display name: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Sign-up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        loginTextView.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).loadFragment(new LoginFragment());
        });

        return view;
    }
}