package com.example.michaelrokas.cryptowidget;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.MotionEvent;
import android.widget.TextView;

import com.example.michaelrokas.cryptowidget.Kraken.KrakenApi;
import com.example.michaelrokas.cryptowidget.Kraken.TradeBalanceResponse;

import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((TextView)findViewById(R.id.disclaimer)).setText(Html.fromHtml(getString(R.string.disclaimer)));
    }
}
