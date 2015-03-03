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

package com.shiftconnects.android.push.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.shiftconnects.android.push.receiver.GcmBroadcastReceiver;

/**
 * This intent service will be started by the {@link com.shiftconnects.android.push.receiver.GcmBroadcastReceiver}
 * when a new GCM messages had been delivered.
 */
public abstract class GcmIntentService extends IntentService {

    public static final String ACTION_MESSAGE = "com.shiftconnects.android.push.action.MESSAGE";
    public static final String ACTION_SEND_ERROR = "com.shiftconnects.android.push.action.SEND_ERROR";
    public static final String ACTION_DELETED = "com.shiftconnects.android.push.action.DELETED";

    private static final String TAG = GcmIntentService.class.getSimpleName();
    private static boolean DEBUG = false;

    public GcmIntentService() {
        super(GcmIntentService.class.getName());
    }

    @Override protected void onHandleIntent(Intent intent) {
        if (DEBUG) {
            Log.d(TAG, "onHandleIntent(" + intent + ")");
        }
        final String action = intent.getAction();
        if (TextUtils.equals(ACTION_MESSAGE, action)) {
            handleMessage(intent.getExtras());
        } else if (TextUtils.equals(ACTION_SEND_ERROR, action)) {
            handleSendErrorMessage(intent.getExtras());
        } else if (TextUtils.equals(ACTION_DELETED, action)) {
            handleDeletedMessage(intent.getExtras());
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    /**
     * Called when a send error message has been received
     * @param extras - the bundle of extras
     */
    public abstract void handleSendErrorMessage(Bundle extras);

    /**
     * Called when a deleted message has been received
     * @param extras - the bundle of extras
     */
    public abstract void handleDeletedMessage(Bundle extras);

    /**
     * Called when a new message has been received
     * @param extras - the bundle of extras
     */
    public abstract void handleMessage(Bundle extras);
}
