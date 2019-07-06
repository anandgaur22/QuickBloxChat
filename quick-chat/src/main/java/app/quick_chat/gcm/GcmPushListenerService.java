package app.quick_chat.gcm;


import android.os.Bundle;
import android.util.Log;

import java.util.logging.Logger;

import app.quick_chat.R;
import app.quick_chat.ui.activity.SplashChatActivity;
import app.quick_chat.utils.NotificationUtils;
import app.quick_chat.utils.ResourceUtils;

public class GcmPushListenerService extends CoreGcmPushListenerService {
    private static final int NOTIFICATION_ID = 1;

    @Override
    protected void showNotification(String message) {
        NotificationUtils.showNotification(this, SplashChatActivity.class,
                ResourceUtils.getString(R.string.notification_title), message,
                R.mipmap.ic_launcher, NOTIFICATION_ID);
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);

        String message = data.getString("message");
        String customParameter1 = data.getString("custom_parameter_1");
        String customParameter2 = data.getString("custom_parameter_2");

        Log.e("MESS  ",message);
    }
}