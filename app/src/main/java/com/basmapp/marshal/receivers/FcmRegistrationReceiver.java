package com.basmapp.marshal.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.basmapp.marshal.interfaces.FcmReceiverListener;

public class FcmRegistrationReceiver extends BroadcastReceiver {
    public static final String EXTRA_RESULT = "extra_result";
    public static final String ACTION_RESULT = "com.basmapp.marshal.services.action.FCM_REGISTRATION_RESULT";
    private FcmReceiverListener callback;

    public FcmRegistrationReceiver() {
    }

    public FcmRegistrationReceiver(FcmReceiverListener callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("FCM BR", "onReceive");
        boolean result = intent.getBooleanExtra(EXTRA_RESULT, false);
        if (callback != null) {
            callback.onFinish(result);
//        } else {
//            publishResultToUI(result, context);
        }
    }
//
//    private void publishResultToUI(boolean result, final Context context) {
//        Handler handler = new Handler(context.getMainLooper());
//        if (result) {
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(context, context.getResources().getString(R.string.fcm_settings_change_success), Toast.LENGTH_SHORT).show();
//                }
//            });
//        } else {
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(context, context.getResources().getString(R.string.fcm_settings_change_failed), Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    }
}
