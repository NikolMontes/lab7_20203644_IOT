package com.example.telemoney;

import static com.facebook.FacebookSdk.getApplicationContext;

import android.app.Application;
import android.os.Bundle;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

public class TelemoneyApp extends Application {

    @Override
    public  void onCreate() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        super.onCreate();
    }
}
