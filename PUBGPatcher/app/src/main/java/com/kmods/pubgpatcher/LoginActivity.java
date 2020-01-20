package com.kmods.pubgpatcher;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    private final String USER = "USER";
    private final String PASS = "PASS";

    private EditText mUsername;
    private EditText mPass;
    private Prefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);

        prefs = Prefs.with(this);

        mUsername = findViewById(R.id.username);
        mUsername.setText(prefs.read(USER, ""));

        mPass = findViewById(R.id.password);
        mPass.setText(prefs.read(PASS, ""));

        Button mLogin = findViewById(R.id.login);
        mLogin.setOnClickListener(v -> {
            if (!mUsername.getText().toString().isEmpty() || !mPass.getText().toString().isEmpty()) {
                prefs.write(USER, mUsername.getText().toString());
                prefs.write(PASS, mPass.getText().toString());
                new Auth(LoginActivity.this).execute(mUsername.getText().toString(), mPass.getText().toString());
            } else {
                Toast.makeText(LoginActivity.this, "Please Enter Username And Password!", Toast.LENGTH_LONG).show();
            }
        });
    }
}
