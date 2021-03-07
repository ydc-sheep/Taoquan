package com.csuft.taoquan.base;

import android.app.Application;
import android.content.Context;

import com.zkx.doctor.PageDetector;

public class BaseApplication extends Application {

    private static Context appContext;


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        PageDetector.instance().init(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getBaseContext();
    }


    public static Context getAppContext() {
        return appContext;
    }
}
