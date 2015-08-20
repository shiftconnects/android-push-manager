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

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.shiftconnects.android.push.sample.event.MessageEvent;
import com.shiftconnects.android.push.sample.manager.ExamplePushManager;
import com.squareup.otto.Subscribe;

public class MainActivity extends ActionBarActivity implements ExamplePushManager.PushServerCallbacks {

    private TextView mRegisteredText;
    private TextView mUnregisteredText;
    private TextView mMessageHeader;
    private TextView mMessage;
    private Button mRegisterButton;
    private Button mUnregisterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRegisteredText = (TextView) findViewById(R.id.registered);
        mUnregisteredText = (TextView) findViewById(R.id.unregistered);
        mMessageHeader = (TextView) findViewById(R.id.message_header);
        mMessage = (TextView) findViewById(R.id.message);
        mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ExampleApplication.PUSH_MANAGER.registerPush();
            }
        });
        mUnregisterButton = (Button) findViewById(R.id.unregister_button);
        mUnregisterButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ExampleApplication.PUSH_MANAGER.unregisterPush();
            }
        });
        updateUI();
    }

    @Override protected void onResume() {
        super.onResume();
        ExampleApplication.EVENT_BUS.register(this);
        ExampleApplication.PUSH_MANAGER.setCallbacks(this);
    }

    @Override protected void onPause() {
        super.onPause();
        ExampleApplication.EVENT_BUS.unregister(this);
        ExampleApplication.PUSH_MANAGER.removeCallbacks(this);
    }

    private void updateUI() {
        if (ExampleApplication.PUSH_MANAGER.hasGcmRegistrationId()) {
            final boolean isRegistered = ExampleApplication.PUSH_MANAGER.isRegisteredWithPushServer();
            mRegisteredText.setVisibility(isRegistered ? View.VISIBLE : View.GONE);
            mUnregisteredText.setVisibility(isRegistered ? View.GONE : View.VISIBLE);
            mRegisterButton.setVisibility(isRegistered ? View.GONE : View.VISIBLE);
            mUnregisterButton.setVisibility(isRegistered ? View.VISIBLE : View.GONE);
            if (!isRegistered) {
                mMessageHeader.setVisibility(View.GONE);
                mMessage.setVisibility(View.GONE);
            }
        } else {
            mRegisteredText.setVisibility(View.GONE);
            mUnregisteredText.setVisibility(View.GONE);
            mRegisterButton.setVisibility(View.GONE);
            mUnregisterButton.setVisibility(View.GONE);
            mMessageHeader.setVisibility(View.GONE);
            mMessage.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onMessageReceived(MessageEvent event) {
        final String message = event.getMessage();
        if (!TextUtils.isEmpty(message)) {
            mMessage.setText(message);
            mMessage.setVisibility(View.VISIBLE);
            mMessageHeader.setVisibility(View.VISIBLE);
        }
    }

    @Override public void onRegisteredWithPushServer(String registeredId) {
        updateUI();
    }

    @Override public void onUnregisteredWithPushServer(String unregisteredId) {
        updateUI();
    }
}
