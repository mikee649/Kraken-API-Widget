package com.example.michaelrokas.cryptowidget;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.disclaimer)                  TextView disclaimer;
    @BindView(R.id.api_key_field)               EditText keyField;
    @BindView(R.id.private_key_field)           EditText privateKeyField;
    @BindView(R.id.save_btn)                    Button saveBtn;
    @BindView(R.id.show_time_checkbox)          CheckBox showTime;

    SharedPreferences sharedPref;
    SettingsObject currentSettings;
    SettingsObject newSettings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        sharedPref =getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        currentSettings = getSettings();
        newSettings = new SettingsObject();

        keyField.setText(currentSettings.getKey());
        privateKeyField.setText(currentSettings.getPrivateKey());
        showTime.setChecked(currentSettings.isShowLastRefreshTime());

        disclaimer.setText(Html.fromHtml(getString(R.string.disclaimer)));

        keyField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                validateSaveButton();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        privateKeyField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                validateSaveButton();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        showTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateSaveButton();
            }
        });
    }

    private void validateSaveButton(){
        boolean keysFilled = !keyField.getText().toString().equals("")
                && !privateKeyField.getText().toString().equals("");

        newSettings = new SettingsObject(keyField.getText().toString(),
                privateKeyField.getText().toString(),
                showTime.isChecked());

        if(keysFilled && !newSettings.equals(currentSettings)){
            saveBtn.setClickable(true);
            saveBtn.setBackgroundResource(R.color.colorAccent);
        } else {
            saveBtn.setClickable(false);
            saveBtn.setBackgroundResource(R.color.disabled);
        }
    }

    @OnClick(R.id.save_btn)
    protected void saveSettings(){
        String json = new Gson().toJson(newSettings);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("settings",json);
        editor.commit();

        currentSettings = getSettings();
        newSettings = new SettingsObject();

        CryptoWidgetProvider.sendRefreshBroadcast(this);

        validateSaveButton();
    }

    private SettingsObject getSettings(){
        String json = sharedPref.getString("settings", "{}");
        return new Gson().fromJson(json,SettingsObject.class);
    }
}
