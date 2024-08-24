package com.example.mealmate.veiw.signup_fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.mealmate.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import jp.wasabeef.glide.transformations.BlurTransformation;

import android.text.InputType;

import java.util.HashMap;
import java.util.Map;

public class SignUpFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    Map<String, Object> user = new HashMap<>();
    private EditText sginupName;
    private EditText sginupEmail;
    private EditText sginupPassword;
    private EditText sginupconfirmPassword;
    private Button sginup;
    private ImageView togglePasswordVisibility;
    private ImageView toggleConfirmPasswordVisibility;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private final String TAG = "SignUpFragment";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        ImageView imageView = view.findViewById(R.id.blurredImagesignupView);
        Glide.with(this)
                .load(R.drawable.signupscreenbackground)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 1)))
                .into(imageView);

        firebaseAuth = FirebaseAuth.getInstance();
        sginupEmail = view.findViewById(R.id.email_input);
        sginupPassword = view.findViewById(R.id.password_input);
        sginupconfirmPassword = view.findViewById(R.id.confirm_password_input);
        sginup = view.findViewById(R.id.signup_button);
        sginupName = view.findViewById(R.id.name_input);
        togglePasswordVisibility = view.findViewById(R.id.toggle_password_visibility);
        toggleConfirmPasswordVisibility = view.findViewById(R.id.toggle_confirmpassword_visibility);

        sginup.setOnClickListener(v -> {

            String name = sginupName.getText().toString().trim();
            String email = sginupEmail.getText().toString().trim().toLowerCase();
            String password = sginupPassword.getText().toString().trim();
            String confirmPassword = sginupconfirmPassword.getText().toString().trim();

            if (validateInputs(email, password, confirmPassword)) {
                firebaseAuth.createUserWithEmailAndPassword(email.toLowerCase(), password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    user.put("name", name);

                                    String id = firebaseAuth.getCurrentUser().getUid();
                                    db.collection("users").document(id).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot added with ID: " + id);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error adding document", e);
                                        }
                                    });

                                    Toast.makeText(getContext(), R.string.sign_in_successful, Toast.LENGTH_SHORT).show();
                                    // Use a safe way to get the NavController
                                    // Use the correct NavController
                                    NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

                                    navController.navigate(R.id.action_signUpFragment_to_loginFragment);
                                } else {
                                    Toast.makeText(getContext(), getString(R.string.sign_up_failed) + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        togglePasswordVisibility.setOnClickListener(v -> togglePasswordVisibility());
        toggleConfirmPasswordVisibility.setOnClickListener(v -> toggleConfirmPasswordVisibility());
    }

    private boolean validateInputs(String email, String password, String confirmPassword) {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), R.string.please_enter_a_valid_email, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.isEmpty() || !isValidPassword(password)) {
            Toast.makeText(getContext(), R.string.please_enter_a_valid_password, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), R.string.passwords_do_not_match, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isValidPassword(String password) {
        String passwordPattern =
                "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        return password.matches(passwordPattern);
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            sginupPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            togglePasswordVisibility.setImageResource(R.drawable.visiblepass);  // Set the icon for hidden password
        } else {
            sginupPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            togglePasswordVisibility.setImageResource(R.drawable.hidepass);  // Set the icon for visible password
        }
        isPasswordVisible = !isPasswordVisible;
        sginupPassword.setSelection(sginupPassword.getText().length());  // Move cursor to the end
    }

    private void toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            sginupconfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggleConfirmPasswordVisibility.setImageResource(R.drawable.visiblepass);  // Set the icon for hidden password
        } else {
            sginupconfirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleConfirmPasswordVisibility.setImageResource(R.drawable.hidepass);  // Set the icon for visible password
        }
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        sginupconfirmPassword.setSelection(sginupconfirmPassword.getText().length());  // Move cursor to the end
    }
}
