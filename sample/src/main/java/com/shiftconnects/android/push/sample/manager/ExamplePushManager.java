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

package com.shiftconnects.android.push.sample.manager;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.shiftconnects.android.push.manager.PushManager;
import com.shiftconnects.android.push.sample.service.ExamplePushRegistrationService;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * An example {@link com.shiftconnects.android.push.manager.PushManager} which registers with a sample
 * push server when a gcm registration id is available
 */
public class ExamplePushManager extends PushManager {

    public interface PushServerCallbacks {
        void onRegisteredWithPushServer(String registeredId);
        void onUnregisteredWithPushServer(String unregisteredId);
    }

    private static final String TAG = ExamplePushManager.class.getSimpleName();
    private static final String REGISTERED_ID = "registeredGcmId";

    private ExamplePushRegistrationService mRegistrationService;
    private SharedPreferences mSharedPrefs;

    private PushServerCallbacks mCallbacks;

    public ExamplePushManager(InstanceID instanceID, String gcmSenderId, SharedPreferences sharedPrefs, ExamplePushRegistrationService registrationService) {
        super(instanceID, gcmSenderId, sharedPrefs);
        mSharedPrefs = sharedPrefs;
        mRegistrationService = registrationService;
    }

    public void setCallbacks(PushServerCallbacks callbacks) {
        mCallbacks = callbacks;
    }

    public void removeCallbacks(PushServerCallbacks callbacks) {
        mCallbacks = null;
    }

    @Override
    protected void onGcmRegistrationIdAvailable(String gcmRegistrationId) {
        registerPush();
    }

    @Override protected void onGcmRegistrationFailed() {
        // TODO notify someone or something that we couldn't set up GCM
    }

    /**
     * Registers the currently available GCM registration id with the push server if it hasn't already been registered
     */
    public void registerPush() {
        final String gcmRegistrationId = getGcmRegistrationId();
        final String registeredId = getRegisteredId();

        // make sure we haven't already registered this id
        if (!TextUtils.isEmpty(gcmRegistrationId)
                && !TextUtils.equals(gcmRegistrationId, registeredId)) {
            mRegistrationService.registerDevice(gcmRegistrationId, new Callback<Void>() {
                @Override public void success(Void aVoid, Response response) {
                    Log.d(TAG, "Successfully registered with push server.");
                    mSharedPrefs.edit().putString(REGISTERED_ID, gcmRegistrationId).apply();
                    if (mCallbacks != null) {
                        mCallbacks.onRegisteredWithPushServer(gcmRegistrationId);
                    }
                }

                @Override public void failure(RetrofitError error) {
                    Log.w(TAG, "Failed to register with push server.", error);
                    // TODO add retry, etc.
                }
            });
        }
    }

    /**
     * Unregisters the currently registered GCM registration id from the push server if one is registered.
     */
    public void unregisterPush() {
        final String registeredId = getRegisteredId();

        // make sure we have a gcm registration id registered
        if (!TextUtils.isEmpty(registeredId)) {
            mRegistrationService.unregisterDevice(registeredId, new Callback<Void>() {
                @Override public void success(Void aVoid, Response response) {
                    Log.d(TAG, "Successfully unregistered with push server");
                    mSharedPrefs.edit().remove(REGISTERED_ID).apply();
                    if (mCallbacks != null) {
                        mCallbacks.onUnregisteredWithPushServer(registeredId);
                    }
                }

                @Override public void failure(RetrofitError error) {
                    Log.w(TAG, "Failed to unregister with push server.", error);
                    // TODO add retry, etc.
                }
            });
        }
    }

    /**
     * Get the GCM registration id registered with the push server
     * @return the GCM registration id registered if registered, null if there is no registered id
     */
    @Nullable
    public String getRegisteredId() {
        return mSharedPrefs.getString(REGISTERED_ID, null);
    }

    /**
     * Whether or not a GCM registration id is available
     * @return true if a GCM registration id is available, false if not
     */
    public boolean hasGcmRegistrationId() {
        return !TextUtils.isEmpty(getGcmRegistrationId());
    }

    /**
     * Whether or not registration with the push server has occurred
     * @return true if registered, false if not
     */
    public boolean isRegisteredWithPushServer() {
        return !TextUtils.isEmpty(getRegisteredId());
    }

}
