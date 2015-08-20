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

package com.shiftconnects.android.push.util;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.util.Random;

/**
* AsyncTask which handles retrieving a GCM registration id with Google Cloud Messaging using exponential
* back-off
*/
public class GcmRegistrationAsyncTask extends AsyncTask<Void, Void, String> {

    public interface GcmRegistrationCallbacks {

        /**
         * Registration with GCM was successful.
         * @param gcmRegistrationId - the registration id registered with GCM
         */
        void onRegistrationSuccessful(String gcmRegistrationId);

        /**
         * Registration with GCM failed
         */
        void onRegistrationFailed();
    }

    private static final String TAG = GcmRegistrationAsyncTask.class.getSimpleName();
    private static final boolean DEBUG = false;

    private static final int MAX_ATTEMPTS = 5;
    private static final int BACK_OFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();

    private InstanceID instanceID;
    private String gcmSenderId;
    private GcmRegistrationCallbacks callbacks;
    private boolean notifiedCallbacks;

    public GcmRegistrationAsyncTask(GcmRegistrationCallbacks callbacks, InstanceID instanceID, String gcmSenderId) {
        this.callbacks = callbacks;
        this.instanceID = instanceID;
        this.gcmSenderId = gcmSenderId;
    }

    @Override protected String doInBackground(Void... params) {
        String registrationId = null;
        long backOff = BACK_OFF_MILLI_SECONDS + random.nextInt(1000);
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            if (DEBUG) {
                Log.d(TAG, "Attempt #" + i + " to register for GCM.");
            }
            try {
                registrationId = instanceID.getToken(gcmSenderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE);
                if (DEBUG) {
                    Log.d(TAG, "Received a GCM registration id: " + registrationId);
                }
                break;
            } catch (Exception e) {
                Log.w(TAG, "Failed to register on attempt " + i, e);
                if (i == MAX_ATTEMPTS) {
                    // break out of loop if we've exhausted our attempts
                    break;
                }
                try {
                    if (DEBUG) {
                        Log.d(TAG, "Sleeping for " + backOff + " ms before retry");
                    }
                    Thread.sleep(backOff);
                } catch (InterruptedException e1) {
                    if (DEBUG) {
                        Log.d(TAG, "Thread interrupted: abort remaining retries!");
                    }
                    Thread.currentThread().interrupt();
                    break;
                }
                // increase back-off exponentially
                backOff *= 2;
            }
        }
        return registrationId;
    }

    @Override protected void onPostExecute(String gcmRegistrationId) {
        if (callbacks != null && !notifiedCallbacks) {
            if (!TextUtils.isEmpty(gcmRegistrationId)) {
                callbacks.onRegistrationSuccessful(gcmRegistrationId);
            } else {
                callbacks.onRegistrationFailed();
            }
            notifiedCallbacks = true;
        }
    }
}
