/*
 * Copyright (C) 2015 P100 OG, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shiftconnects.android.push.receiver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.TextUtils;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.shiftconnects.android.push.service.GcmIntentService;


/**
 * This {@code WakefulBroadcastReceiver} takes care of creating and managing a
 * partial wake lock for your app. It passes off the work of processing the GCM
 * message to an {@code IntentService}, while ensuring that the device does not
 * go back to sleep in the transition. The {@code IntentService} calls
 * {@code GcmBroadcastReceiver.completeWakefulIntent()} when it is ready to
 * release the wake lock.
 */

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    public static interface Callbacks {
        void onReceivedRegistrationId(String registrationId);
    }

    private static final String ACTION_REGISTRATION = "com.google.android.c2dm.intent.REGISTRATION";
    private static final String EXTRA_REGISTRATION_ID = "registration_id";

    private static Callbacks sCallbacks;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (TextUtils.equals(ACTION_REGISTRATION, action)) {
            final String registrationId = intent.getStringExtra(EXTRA_REGISTRATION_ID);
            if (!TextUtils.isEmpty(registrationId)) {
                if (sCallbacks != null) {
                    sCallbacks.onReceivedRegistrationId(registrationId);
                }
            }
        } else {
            final String messageType = GoogleCloudMessaging.getInstance(context).getMessageType(intent);
            if (TextUtils.equals(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE, messageType)) {
                intent.setAction(GcmIntentService.ACTION_MESSAGE);
            } else if (TextUtils.equals(GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR, messageType)) {
                intent.setAction(GcmIntentService.ACTION_SEND_ERROR);
            } else if (TextUtils.equals(GoogleCloudMessaging.MESSAGE_TYPE_DELETED, messageType)) {
                intent.setAction(GcmIntentService.ACTION_DELETED);
            }
            intent.setComponent(null);
            startWakefulService(context, intent);
        }
        if (isOrderedBroadcast()) {
            setResultCode(Activity.RESULT_OK);
        }
    }

    public static void registerCallbacks(Callbacks callbacks) {
        sCallbacks = callbacks;
    }

    public static void unregisterCallbacks() {
        sCallbacks = null;
    }
}