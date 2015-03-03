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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * GCM registration ids can change from device boot to device boot or when the Android version
 * has been updated. It is recommended to add this boot receiver so that the app gets notified
 * when a device boot has completed, then upon boot a call to the PushManager.registerWithGCM() should happen
 * in order to get a new GCM registration id.
 *
 * This broadcast receiver will receive an alert when the device has been booted. The implementation
 * of onReceive() does nothing as this class only exists to get the update which will in turn start
 * the app and do GCM initialization
 */
public class OnBootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {}
}
