package com.example.michaelrokas.cryptowidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.example.michaelrokas.cryptowidget.Kraken.KrakenApi;
import com.example.michaelrokas.cryptowidget.Kraken.TradeBalanceResponse;

/**
 * Created by michaelrokas on 2018-04-16.
 */

public class CryptoWidgetProvider extends AppWidgetProvider {

    String key = "api key here";
    String secret = "private key here";


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        //Todo: only do this if the action is AppWidgetManager.ACTION_APPWIDGET_UPDATE
        if(intent.getBooleanExtra("refresh", false)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(), CryptoWidgetProvider.class.getName());
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        //show loading spinner
        final int N = appWidgetIds.length;
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setViewVisibility(R.id.refresh, View.GONE);
            views.setViewVisibility(R.id.progress_bar, View.VISIBLE);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

        //setup callback
        KrakenApi.TradeBalanceCallback callbcack = new KrakenApi.TradeBalanceCallback(){
            @Override
            public void onResponse(TradeBalanceResponse response) {
                if(response.getResult() != null)
                    Log.d("Widget Balance", response.getResult().getEquivalentBalance());
                else
                    Log.e("error", response.getError()[0]);
                setupViews(response, context, appWidgetManager, appWidgetIds);
            }

            @Override
            public void onFailure() {
                //Todo: implement onFailure
            }
        };

        //make api call
        KrakenApi api = new KrakenApi(key,secret);
        api.fetchTradeBalance(callbcack);
    }

    private void setupViews(TradeBalanceResponse response, Context context,
                            AppWidgetManager appWidgetManager, int[] appWidgetIds){

        SpannableString formatedBalance;
        if(response.getResult() != null){
            formatedBalance = getFormatedBalance(response.getResult().getEquivalentBalance());
        } else {
            formatedBalance = new SpannableString(":-(");
        }

        final int N = appWidgetIds.length;
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            //show refresh button
            views.setViewVisibility(R.id.progress_bar, View.GONE);
            views.setViewVisibility(R.id.refresh, View.VISIBLE);

            //create intent for settings activity
            Intent settingsIntent = new Intent(context, MainActivity.class);
            PendingIntent settingsPendingIntent = PendingIntent.getActivity(context, 0, settingsIntent, 0);
            views.setOnClickPendingIntent(R.id.settings, settingsPendingIntent);

            //create refresh intent
            Intent refreshIntent = new Intent(context, CryptoWidgetProvider.class);
            refreshIntent.putExtra("refresh",true);
            refreshIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context,0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.refresh,refreshPendingIntent);

            views.setTextViewText(R.id.value, formatedBalance);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private SpannableString getFormatedBalance(String balance){
        float balanceFloat = Float.parseFloat(balance);
        balanceFloat = Math.round(balanceFloat*100)/100f;
        String balanceString = "$" + balanceFloat;

        if(balanceString.length()-balanceString.indexOf('.') <= 2)
            balanceString += '0';

        SpannableString balanceFormated =new SpannableString(balanceString);
        balanceFormated.setSpan(new RelativeSizeSpan(0.75f),
                balanceString.indexOf('.'),balanceString.length(), 0);

        return balanceFormated;
    }
}
