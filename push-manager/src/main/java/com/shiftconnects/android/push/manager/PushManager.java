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

package com.shiftconnects.android.push.manager;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.shiftconnects.android.push.util.GcmRegistrationAsyncTask;
import com.shiftconnects.android.push.util.GcmSharedPreferenceConstants;

/**
 * Handles registering the device with {@link com.google.android.gms.gcm.GoogleCloudMessaging}
 * and saving off the registration id. Subclasses need to implement {@link #onGcmRegistrationIdAvailable(String)}
 * to then check to see if the registration id has been registered with a push server. In order to begin
 * the registration process, {@link #registerWithGCM()} must be called and should be called every time
 * the app has started (preferably after device boot as well) since the registration id can change
 * between app launches and device boots.
 */
public abstract class PushManager implements GcmRegistrationAsyncTask.GcmRegistrationCallbacks {

    private static final boolean DEBUG = false;

    private static final String TAG = PushManager.class.getSimpleName();

    protected SharedPreferences mSharedPrefs;
    private GoogleCloudMessaging mGoogleCloudMessaging;
    private String mGcmSenderId;

    /**
     * Default constructor
     * @param googleCloudMessaging - the instance of {@link com.google.android.gms.gcm.GoogleCloudMessaging} to use
     * @param gcmSenderId - the GCM sender id to be used to retrieve registration ids
     * @param sharedPrefs - a {@link android.content.SharedPreferences} implementation to store the registration id in
     */
    public PushManager(GoogleCloudMessaging googleCloudMessaging, String gcmSenderId, SharedPreferences sharedPrefs) {
        mGoogleCloudMessaging = googleCloudMessaging;
        mGcmSenderId = gcmSenderId;
        mSharedPrefs = sharedPrefs;
    }

    /**
     * Initiates an asynchronous attempt to register with GoogleCloudMessaging. If registration is successful, {@link #onGcmRegistrationIdAvailable(String)}
     * will be called and {@link #getGcmRegistrationId()} will return the GCM registration id.
     *
     * This method should be called on app startup since it is possible for a registration id
     * to change between boots/app launches. It is recommended that you create your own {@link android.app.Application}
     * class and call this method within {@link android.app.Application#onCreate()}
     */
    public void registerWithGCM() {
        new GcmRegistrationAsyncTask(this, mGoogleCloudMessaging, mGcmSenderId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public final void onRegistrationSuccessful(String gcmRegistrationId) {
        if (DEBUG) {
            Log.d(TAG, "Received GCM registration id = " + gcmRegistrationId);
        }

        // save the registration id
        mSharedPrefs
                .edit()
                .putString(GcmSharedPreferenceConstants.GCM_REG_ID, gcmRegistrationId)
                .apply();

        onGcmRegistrationIdAvailable(gcmRegistrationId);
    }

    @Override
    public final void onRegistrationFailed() {
        if (DEBUG) {
            Log.w(TAG, "Failed to receive a GCM registration id.");
        }
    }

    /**
     * Get the last retrieved GCM registration id
     * @return the registration id or null if no registration id has been retrieved
     */
    @Nullable
    protected String getGcmRegistrationId() {
        return mSharedPrefs.getString(GcmSharedPreferenceConstants.GCM_REG_ID, null);
    }

    /**
     * A new GCM registration id is available. The passed in registration id could be the same as
     * a previously available id so it is up to the subclass to make sure the id has or hasn't been
     * registered with a push server
     * @param gcmRegistrationId - the newly available GCM registration id
     */
    protected abstract void onGcmRegistrationIdAvailable(String gcmRegistrationId);

    /**
     * Registration with GCM failed with retry.
     */
    protected abstract void onGcmRegistrationFailed();
}
