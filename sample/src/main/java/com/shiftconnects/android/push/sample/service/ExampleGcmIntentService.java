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

package com.shiftconnects.android.push.sample.service;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.shiftconnects.android.push.sample.ExampleApplication;
import com.shiftconnects.android.push.sample.event.MessageEvent;
import com.shiftconnects.android.push.service.GcmIntentService;
import com.squareup.otto.Produce;

/**
 * Example {@link com.shiftconnects.android.push.service.GcmIntentService} which handles push
 * messages and looks for a message, then posts the message on an Otto {@link com.squareup.otto.Bus}
 * for subscribers
 */
public class ExampleGcmIntentService extends GcmIntentService {

    private static final String TAG = ExampleGcmIntentService.class.getSimpleName();

    private String lastMessage;
    private final Handler MAIN_THREAD = new Handler(Looper.getMainLooper());

    @Override public void handleSendErrorMessage(Bundle extras) {
        Log.d(TAG, "handleSendErrorMessage()");
    }

    @Override public void handleDeletedMessage(Bundle extras) {
        Log.d(TAG, "handleDeletedMessage()");
    }

    @Override public void handleMessage(Bundle extras) {
        Log.d(TAG, "handleMessage()");
        if (extras.containsKey("message")) {
            lastMessage = extras.getString("message");
            MAIN_THREAD.post(new Runnable() {
                @Override public void run() {
                    ExampleApplication.EVENT_BUS.post(produceMessage());
                }
            });
        }
    }

    @Produce
    public MessageEvent produceMessage() {
        return new MessageEvent(lastMessage);
    }
}
