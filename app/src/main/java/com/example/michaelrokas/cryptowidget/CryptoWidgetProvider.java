package com.example.michaelrokas.cryptowidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.example.michaelrokas.cryptowidget.kraken.KrakenApi;
import com.example.michaelrokas.cryptowidget.kraken.TradeBalanceResponse;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by michaelrokas on 2018-04-16.
 */

public class CryptoWidgetProvider extends AppWidgetProvider {

    final private String TAG = "Widget";

    private String key;
    private String privateKey;
    private boolean showLastRefresh;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if(intent.getBooleanExtra("refresh", false)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(), CryptoWidgetProvider.class.getName());
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        getSettings(context);

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
                    Log.d(TAG, "Widget Balance - " + response.getResult().getEquivalentBalance());
                else
                    Log.e(TAG, response.getError()[0]);
                setupViewsSuccess(response, context, appWidgetManager, appWidgetIds);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG,t.toString());
                setupViewsSuccess(null, context, appWidgetManager, appWidgetIds);
            }
        };

        //make api call
        KrakenApi api = new KrakenApi(key,privateKey);
        api.fetchTradeBalance(callbcack);
    }

    private void setupViewsSuccess(TradeBalanceResponse response, Context context,
                                   AppWidgetManager appWidgetManager, int[] appWidgetIds){

        SpannableString formatedBalance;
        if(response == null){
            formatedBalance = new SpannableString(":-(");
        } else if(response.getResult() != null){
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

            if(showLastRefresh)
                views.setViewVisibility(R.id.last_update_time, View.VISIBLE);
            else
                views.setViewVisibility(R.id.last_update_time, View.GONE);

            //set last update time

            views.setTextViewText(R.id.last_update_time, getTime());

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

    public static void sendRefreshBroadcast(Context context) {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.setComponent(new ComponentName(context, CryptoWidgetProvider.class));
        intent.putExtra("refresh",true);
        context.sendBroadcast(intent);
    }

    private String getTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
        String currentTime = sdf.format(new Date());

        if(currentTime.charAt(0) == '0')
            currentTime = currentTime.substring(1);

        return  currentTime;
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

    private void getSettings(Context context){
        SharedPreferences sharedPref =context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        String json = sharedPref.getString("settings", "{}");
        SettingsObject settings = new Gson().fromJson(json,SettingsObject.class);

        key = settings.getKey();
        privateKey = settings.getPrivateKey();
        showLastRefresh = settings.isShowLastRefreshTime();
    }
}
