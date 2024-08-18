package com.ritikprajapati.todoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private Button loginBtn, registerBtn;
    private EditText name_ET, email_ET, password_ET;
    ProgressBar progressBar;

    private String name, email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        loginBtn = findViewById(R.id.loginBtn);
        name_ET = findViewById(R.id.name_ET);
        email_ET = findViewById(R.id.email_ET);
        password_ET = findViewById(R.id.password_ET);
        progressBar = findViewById(R.id.progress_bar);

        registerBtn = findViewById(R.id.registerBtn);

        loginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        registerBtn.setOnClickListener(v -> {
            hideKeyboard(RegisterActivity.this, getCurrentFocus());
            name = name_ET.getText().toString();
            email = email_ET.getText().toString();
            password = password_ET.getText().toString();

            if (validate(v)) {
                registerUser(v);
            }
        });
    }

    private void registerUser(View view) {
        progressBar.setVisibility(View.VISIBLE);
        HashMap<String, String> params = new HashMap<>();
        params.put("username", name);
        params.put("email", email);
        params.put("password", password);

        String apiKey = "http://192.168.98.168/api/todo/auth/register"; // Replace with your actual server URL

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                apiKey,
                new JSONObject(params),
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            String token = response.getString("token");
                            Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.GONE);
                }, error -> {
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                try {
                    String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                    JSONObject obj = new JSONObject(res);
                    Toast.makeText(RegisterActivity.this, obj.getString("msg"), Toast.LENGTH_SHORT).show();
                } catch (JSONException | UnsupportedEncodingException je) {
                    je.printStackTrace();
                }
            } else {
                Toast.makeText(RegisterActivity.this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
            }
            progressBar.setVisibility(View.GONE);
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        int socketTime = 3000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTime,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    public boolean validate(View view) {
        boolean isValid;

        if (!TextUtils.isEmpty(name)) {
            if (!TextUtils.isEmpty(email)) {
                if (!TextUtils.isEmpty(password)) {
                    isValid = true;
                } else {
                    Snackbar.make(view, "Please enter password", Snackbar.LENGTH_SHORT).show();
                    isValid = false;
                }
            } else {
                Snackbar.make(view, "Please enter email", Snackbar.LENGTH_SHORT).show();
                isValid = false;
            }
        } else {
            Snackbar.make(view, "Please enter name", Snackbar.LENGTH_SHORT).show();
            isValid = false;
        }
        return isValid;
    }

    public void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
