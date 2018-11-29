package com.hps.esecure.yourbank.settings;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.hps.esecure.yourbank.R;

import java.util.HashSet;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {

    private EditText oobverifyurlEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        oobverifyurlEditText = (EditText) findViewById(R.id.oobverifyurl);

        initializeUI();

        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setPreferences();

            }
        });
    }


    private void setPreferences() {
        SettingsPreferenceHelper.setOobVerifyUrl(this, oobverifyurlEditText.getText().toString());

        finish();
    }

    private void initializeUI() {
        oobverifyurlEditText.setText(SettingsPreferenceHelper.getOobVerifyUrl(this));

    }

}
