package com.example.michaelrokas.cryptowidget;

import com.google.gson.annotations.Expose;

/**
 * Created by michaelrokas on 2018-04-18.
 */

public class SettingsObject{
    @Expose
    private String key;
    @Expose
    private String privateKey;
    @Expose
    private boolean showLastRefreshTime;

    public SettingsObject(){

    }

    public SettingsObject(String key, String privateKey, boolean showLastRefreshTime) {
        this.key = key;
        this.privateKey = privateKey;
        this.showLastRefreshTime = showLastRefreshTime;
    }

    public String getKey() {
        return key;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public boolean isShowLastRefreshTime() {
        return showLastRefreshTime;
    }

    public boolean equals(SettingsObject other){
        return key.equals(other.getKey())
                && privateKey.equals(other.getPrivateKey())
                && showLastRefreshTime == other.isShowLastRefreshTime();

    }
}
