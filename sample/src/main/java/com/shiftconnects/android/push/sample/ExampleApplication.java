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

package com.shiftconnects.android.push.sample;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.shiftconnects.android.push.sample.manager.ExamplePushManager;
import com.shiftconnects.android.push.sample.service.ExamplePushRegistrationService;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import retrofit.RestAdapter;

/**
 * Created by mattkranzler on 2/27/15.
 */
public class ExampleApplication extends Application {

    private static final String TAG = ExampleApplication.class.getSimpleName();

    private static final String GCM_SHARED_PREFS_NAME = "gcm-shared-prefs";

    public static Bus EVENT_BUS;
    public static ExamplePushRegistrationService PUSH_REGISTRATION_SERVICE;
    public static ExamplePushManager PUSH_MANAGER;

    private static final String GCM_SENDER_ID = "your_gcm_sender_id_here";

    @Override public void onCreate() {
        super.onCreate();

        // create an event bus to allow our gcm intent service to notify when new messages are available
        EVENT_BUS = new Bus(ThreadEnforcer.MAIN);

        // create instances - typically these would be created as part of a dependency injection framework to allow for mocking, different environments, etc.
        SharedPreferences gcmSharedPrefs = getSharedPreferences(GCM_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        PUSH_REGISTRATION_SERVICE = new RestAdapter.Builder()
                .setEndpoint(BuildConfig.PUSH_SERVER_ENDPOINT)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build()
                .create(ExamplePushRegistrationService.class);
        PUSH_MANAGER = new ExamplePushManager(GoogleCloudMessaging.getInstance(this), GCM_SENDER_ID, gcmSharedPrefs, PUSH_REGISTRATION_SERVICE);

        // this will register the device with GCM and retrieve a registration id for this device
        PUSH_MANAGER.registerWithGCM();
    }
}
