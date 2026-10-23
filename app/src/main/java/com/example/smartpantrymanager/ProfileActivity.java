
package com.example.smartpantrymanager;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.content.SharedPreferences;
import android.util.Patterns;

public class ProfileActivity extends Activity {

    private static final String PREFS_NAME =
            "smart_pantry_settings";

    private static final String KEY_PROFILE_NAME =
            "profile_full_name";

    private static final String KEY_PROFILE_EMAIL =
            "profile_email";

    private EditText nameField;
    private EditText emailField;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(
                PREFS_NAME,
                MODE_PRIVATE
        );

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);

        TextView title = new TextView(this);
        title.setText("My Profile");
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 24);
        layout.addView(title);

        TextView description = new TextView(this);
        description.setText(
                "Manage your personal information."
        );
        description.setTextSize(16);
        description.setPadding(0, 0, 0, 24);
        layout.addView(description);

        TextView nameLabel = new TextView(this);
        nameLabel.setText("Full Name");
        nameLabel.setTextSize(16);
        layout.addView(nameLabel);

        nameField = new EditText(this);
        nameField.setHint("Enter your full name");
        nameField.setSingleLine(true);
        nameField.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT
                        | android.text.InputType
                        .TYPE_TEXT_FLAG_CAP_WORDS
        );

        layout.addView(
                nameField,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView emailLabel = new TextView(this);
        emailLabel.setText("Email Address");
        emailLabel.setTextSize(16);
        emailLabel.setPadding(0, 16, 0, 0);
        layout.addView(emailLabel);

        emailField = new EditText(this);
        emailField.setHint("Enter your email address");
        emailField.setSingleLine(true);
        emailField.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT
                        | android.text.InputType
                        .TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        );

        layout.addView(
                emailField,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        loadProfile();

        Button saveButton = new Button(this);
        saveButton.setText("Save Profile");
        saveButton.setOnClickListener(
                view -> saveProfile()
        );
        layout.addView(saveButton);

        Button backButton = new Button(this);
        backButton.setText("Back to Settings");
        backButton.setOnClickListener(view -> finish());
        layout.addView(backButton);

        setContentView(layout);
    }

    private void loadProfile() {
        String savedName = preferences.getString(
                KEY_PROFILE_NAME,
                ""
        );

        String savedEmail = preferences.getString(
                KEY_PROFILE_EMAIL,
                ""
        );

        nameField.setText(savedName);
        emailField.setText(savedEmail);
    }

    private void saveProfile() {
        String fullName = nameField.getText()
                .toString()
                .trim();

        String email = emailField.getText()
                .toString()
                .trim();

        if (fullName.isEmpty()) {
            nameField.setError(
                    "Please enter your full name"
            );
            nameField.requestFocus();
            return;
        }

        if (email.isEmpty()
                || !Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            emailField.setError(
                    "Please enter a valid email address"
            );
            emailField.requestFocus();
            return;
        }

        boolean saved = preferences.edit()
                .putString(KEY_PROFILE_NAME, fullName)
                .putString(KEY_PROFILE_EMAIL, email)
                .commit();

        if (saved) {
            Toast.makeText(
                    this,
                    "Profile saved successfully",
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Toast.makeText(
                    this,
                    "Could not save profile",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}
