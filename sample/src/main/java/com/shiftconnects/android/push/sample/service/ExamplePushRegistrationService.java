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

import retrofit.Callback;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by mattkranzler on 2/27/15.
 */
public interface ExamplePushRegistrationService {

    String REGISTRATION_BASE = "/registration/v1";

    @POST(REGISTRATION_BASE + "/registerDevice/{registrationId}")
    void registerDevice(@Path("registrationId") String registrationId, Callback<Void> callback);

    @POST(REGISTRATION_BASE + "/unregisterDevice/{registrationId}")
    void unregisterDevice(@Path("registrationId") String registrationId, Callback<Void> callback);
}
