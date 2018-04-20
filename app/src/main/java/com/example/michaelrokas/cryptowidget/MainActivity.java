package com.example.michaelrokas.cryptowidget;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.michaelrokas.cryptowidget.googleMobileVision.BarcodeGraphicTracker;
import com.example.michaelrokas.cryptowidget.googleMobileVision.CameraFragment;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements BarcodeGraphicTracker.BarcodeUpdateListener{

    @BindView(R.id.disclaimer)                  TextView disclaimer;
    @BindView(R.id.api_key_field)               EditText keyField;
    @BindView(R.id.private_key_field)           EditText privateKeyField;
    @BindView(R.id.save_btn)                    Button saveBtn;
    @BindView(R.id.show_time_checkbox)          CheckBox showTime;
    @BindView(R.id.camera_view_holder)          RelativeLayout cameraViewHolder;

    SharedPreferences sharedPref;
    SettingsObject currentSettings;
    SettingsObject newSettings;

    boolean cameraOpen = false;
    CameraFragment cameraFragment;
    FragmentTransaction fragmentTransaction;

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

        cameraFragment = new CameraFragment();

        fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.camera_view_holder, cameraFragment, "camera");
        fragmentTransaction.commit();
        cameraOpen = true;
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

    @Override
    public void onBarcodeDetected(Barcode barcode) {
        Log.d("Barcode",barcode.displayValue);
        closeCamera();
    }

    public void closeCamera(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(cameraOpen) {
                    ((RelativeLayout)findViewById(R.id.camera_view_holder)).removeAllViews();
                }
                cameraOpen = false;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if(cameraFragment != null)
            cameraFragment.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }
}
