package com.example.michaelrokas.cryptowidget;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.MotionEvent;
import android.widget.TextView;

import com.example.michaelrokas.cryptowidget.Kraken.KrakenApi;
import com.example.michaelrokas.cryptowidget.Kraken.TradeBalanceResponse;

import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    String key = "api key here";
    String secret = "private key here";

    public static Response<TradeBalanceResponse> response;

    KrakenApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        api = new KrakenApi(key, secret);
        api.fetchTradeBalance(this);
    }

    public void set(final String str){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SpannableString ss1=new SpannableString(str);
                ss1.setSpan(new RelativeSizeSpan(0.75f), str.indexOf('.'),str.length(), 0); // set size
                ((TextView)findViewById(R.id.text)).setText(ss1);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP) {
            ((TextView) findViewById(R.id.text)).setText("");
            api.fetchTradeBalance(this);
        }
        return super.onTouchEvent(event);
    }
}
