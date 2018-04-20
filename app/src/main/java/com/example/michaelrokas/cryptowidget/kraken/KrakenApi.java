package com.example.michaelrokas.cryptowidget.kraken;

import android.support.annotation.Nullable;

import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by michaelrokas on 2018-04-17.
 */

public class KrakenApi {
    private Retrofit retrofit;
    private KrakenService service;

    private String apiKey;
    private String privateKey;

    public interface KrakenService {
        @FormUrlEncoded
        @POST("TradeBalance")
        Call<TradeBalanceResponse> getTradeBalance(
                @Header("API-Key") String apiKey,
                @Header("API-Sign") String apiSign,
                @Field("nonce") String nonce,
                @Field("asset") String asset);
    }

    public interface TradeBalanceCallback{
        void onResponse(TradeBalanceResponse response);
        void onFailure(Throwable t);
    }

    public KrakenApi(String apiKey, String privateKey){
        this.apiKey = apiKey;
        this.privateKey = privateKey;

        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.kraken.com/0/private/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(KrakenService.class);
    }

    public void fetchTradeBalance(final TradeBalanceCallback callback){
        String nonce = String.valueOf(System.currentTimeMillis());
        String postBody = "nonce=" + nonce + "&asset=ZCAD";

        String sign = calculateSignature("/0/private/TradeBalance", nonce, postBody);

        Call<TradeBalanceResponse> call = service.getTradeBalance(apiKey, sign, nonce, "ZCAD");
        call.enqueue(new Callback<TradeBalanceResponse>() {
            @Override
            public void onResponse(@Nullable Call<TradeBalanceResponse> call, @Nullable Response<TradeBalanceResponse> response) {
                callback.onResponse(response.body());
            }

            @Override
            public void onFailure(Call<TradeBalanceResponse> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    private String calculateSignature(String path, String nonce, String postBody) {
        String signature = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((nonce + postBody).getBytes());
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(Base64.getDecoder().decode(privateKey.getBytes()), "HmacSHA512"));
            mac.update(path.getBytes());
            signature = new String(Base64.getEncoder().encode(mac.doFinal(md.digest())));
        } catch(Exception e) {}
        return signature;
    }
}
