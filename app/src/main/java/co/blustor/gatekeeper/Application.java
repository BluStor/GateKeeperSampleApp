package co.blustor.gatekeeper;

import android.content.Context;

public class Application extends android.app.Application {
    public static final String TAG = Application.class.getSimpleName();

    private static Context context;

    public void onCreate() {
        super.onCreate();
        Application.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return Application.context;
    }
}
