package com.example.telemoney;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword, etConfirmPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.et_email_register);
        etPassword = findViewById(R.id.et_password_register);
        MaterialButton btnRegister = findViewById(R.id.btn_register);

        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(authResult -> {
                        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, VistainicialActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

    }
}