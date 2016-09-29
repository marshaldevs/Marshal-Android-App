package com.basmapp.marshal.services;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.ui.MainActivity;
import com.basmapp.marshal.ui.utils.NotificationUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class FcmIntentService extends FirebaseMessagingService {

    SharedPreferences mSharedPreferences;

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);
        Log.i("FCM", "onMessageReceived from:" + message.getFrom());
        Map data = message.getData();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        NotificationUtils mNotificationUtils = new NotificationUtils(this);
//
//        Uri ringtoneUri = mNotificationUtils.getRingtoneUri();
//        long[] vibrate = mNotificationUtils.getVibrate();
//        int lightColor = mNotificationUtils.getLightColor();
//
//        long[] vibrate = new long[]{0};
//        if (sharedPreferences.getBoolean(Constants.PREF_NOTIFICATIONS_NEW_MESSAGE, false)) {
//            vibrate = new long[]{0,1000};
//        }

//        String ringtonePref = sharedPreferences.getString(Constants.PREF_NOTIFICATIONS_NEW_RINGTONE, null);
//        Uri ringtoneUri;
//        if (ringtonePref != null) {
//            ringtoneUri = Uri.parse(ringtonePref);
//        } else {
//            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        }

//        int lightColor = Color.parseColor(sharedPreferences
//                .getString(Constants.PREF_NOTIFICATIONS_COLOR, "#FFFFFF"));

        /********************************************/

        PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        String type = (String) data.get("type");

        switch (type) {
            case "commands":
                String[] commands = (String[]) data.get("commands");
                if (commands != null) {
                    executeCommands(commands);
                }
                break;
            case "notification":
                // Get Title
                String title = (String) data.get("title");
                if (title == null) title = getString(R.string.app_name);
                // Get Content
                String content = (String) data.get("content");
                // Get PhotoUrl
                String imageUrl = (String) data.get("imageUrl");

                if (content != null) {
                    if (imageUrl != null) {
                        // Show Picture Notification
                        new NotificationUtils.GeneratePictureStyleNotification(this, content,
                                imageUrl, notifyPendingIntent);
                    } else {
                        // Show Basic Notification
                        mNotificationUtils.notify(title, content, notifyPendingIntent);
                    }
                }

                break;
            default:
                break;
        }
        /********************************************/
//        String message = data.getString("message");
//        if (message != null &&
//                message.equals("set-registration-state=false")) {
//            FcmRegistrationService.setDeviceRegistrationState(this, false);
//        } else if (message != null && message.equals("data-update-true")) {
//            Log.i("FCM", "data-update-true");
//            UpdateIntentService.startUpdateData(this);
//        } else if (message != null && message.equals("reset-fcm-registration-pref")) {
//            mSharedPreferences.edit().putBoolean(Constants.PREF_IS_DEVICE_REGISTERED, false).apply();
//        } else if (message != null && message.equals("show-must-update-dialog")) {
//            mSharedPreferences.edit().putBoolean(Constants.PREF_MUST_UPDATE, true).apply();
//        } else if (message != null && message.contains("show-picture-style-notification")
//                && mSharedPreferences.getBoolean("notifications_new_message", true)) {
//            String[] separated = message.split(";");
//
//            PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
//                    MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
//
//            new NotificationUtils.GeneratePictureStyleNotification(this, separated[1].trim(), separated[2].trim(),
//                    vibrate, ringtoneUri, lightColor, notifyPendingIntent).execute();
//        } else {
//            if (mSharedPreferences.getBoolean(Constants.PREF_NOTIFY_NEW_MESSAGE, true)) {
//                PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
//                        MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
//
//                mNotificationUtils.notify(message, notifyPendingIntent);
//            }
//        }
    }

    private void executeCommands(String[] commands) {
        for (String command : commands) {
            executeCommand(command);
        }
    }

    private void executeCommand(String command) {
        switch (command) {
            case "set-registration-state?false":
                boolean state = Boolean.valueOf(command.split("\\?")[1]);
                FcmRegistrationService.setDeviceRegistrationState(this, state);
                break;
            case "start-data-update":
                UpdateIntentService.startUpdateData(this);
                break;
            case "set-must-update?true":
                boolean value = Boolean.valueOf(command.split("\\?")[1]);
                mSharedPreferences.edit().putBoolean(Constants.PREF_MUST_UPDATE, value).apply();
                break;
            default:
                break;
        }
    }
}