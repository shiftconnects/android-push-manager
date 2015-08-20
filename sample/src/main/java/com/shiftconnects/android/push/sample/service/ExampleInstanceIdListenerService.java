package com.shiftconnects.android.push.sample.service;

import com.google.android.gms.iid.InstanceIDListenerService;
import com.shiftconnects.android.push.sample.ExampleApplication;

/**
 * Example {@link com.google.android.gms.iid.InstanceIDListenerService} which will attempt
 * to register with GCM after an {@link #onTokenRefresh()} callback.
 */
public class ExampleInstanceIdListenerService extends InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        ExampleApplication.PUSH_MANAGER.registerWithGCM();
    }
}
