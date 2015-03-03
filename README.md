# Android Push Manager

This library provides a mechanism for quickly getting Google Cloud Messaging running within an Android app. It provides the code required to retrieve GCM registration ids and hooks to send
them to your backend, and also covers all of the gotchas found in [this technical blog post](https://blog.pushbullet.com/2014/02/12/keeping-google-cloud-messaging-for-android-working-reliably-techincal-post/)
by PushBullet to keep it working reliably, including exponential retry when retrieving a GCM registration id has failed.

## Setup
* Add the following to your ```build.gradle``` file:
```gradle
compile('com.shiftconnects.android.push:android-push-manager:1.0.0'){transitive=true}
```
If you do not wish to pull in our version of the Play Services, feel free to ignore the transitive=true flag. If you do this, you will need to provide your own versions of the play services dependencies...
```gradle
compile 'com.google.android.gms:play-services-location:6.5.87'
compile 'com.google.android.gms:play-services-base:6.5.87'
```

* Next you will need to follow the steps listed [here](http://developer.android.com/google/gcm/gs.html) to get your GCM sender ID and api key.
* Add the following permissions to your AndroidManifest.xml file: 
```xml
<permission android:name="${applicationId}.permission.C2D_MESSAGE" android:protectionLevel="signature" />
<uses-permission android:name="${applicationId}.permission.C2D_MESSAGE" />
```
You can replace ```${applicationId}``` with your application's package or leave as is and it will get replaced with the proper
package name for your build type during the gradle build. These permissions are necessary in order to receive GCM messages.

* Create a class which extends ```GcmIntentService``` and declare it in your AndroidManifest. Make sure to include an intent filter with the listed actions for your service:
```xml
<service
    android:name=".service.ExampleGcmIntentService"
    android:exported="false" >
    <intent-filter>
        <action android:name="com.shiftconnects.android.push.action.MESSAGE" />
        <action android:name="com.shiftconnects.android.push.action.SEND_ERROR" />
        <action android:name="com.shiftconnects.android.push.action.DELETED" />
    </intent-filter>
</service>
```
* Create a class which extends ```PushManager```. This is the core class of this library and handles registering with GoogleCloudMessaging and storing the GCM registration id. More details on this class in the **Usage** and **Sample** sections.  


#### The following steps are *optional* but *highly recommended*:
* Add the following permission to your AndroidManifest.xml file:
```xml
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
```
* Add the following ```BroadcastReceiver``` to your AndroidManifest.xml file:
```xml
<receiver android:name="com.shiftconnects.android.push.receiver.OnBootCompletedReceiver" >
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
        <action android:name="android.intent.action.QUICKBOOT_POWERON" />
        <action android:name="com.htc.intent.action.QUICKBOOT_POWERON" />
    </intent-filter>
</receiver>
```

The purpose of this permission and ```OnBootCompletedReceiver``` are so that your app will be notified when the device has booted, whether it be from just turning the device off then on or more importantly, after a system software update. GCM registration ids can change from device boot to device boot or when the Android version has been updated so it is very important to re-register for a GCM registration id in this case in order to keep push working.

* Add the following ```BroadcastReceiver``` to your AndroidManifest.xml file: 
```xml
<receiver
   android:name="com.shiftconnects.android.push.receiver.AppUpgradeReceiver">
    <intent-filter>
        <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
    </intent-filter>
</receiver>
```

The purpose of ```AppUpgradeReceiver``` is so that your app will be notified when your app has been upgraded. GCM registration ids can change after an app version upgrade so it is also very important to re-register for a GCM registration id after this happens in order to keep push working.

## Usage

Create an instance of your ```PushManager```. We recommend creating a single instance of this in your ```Application``` subclass as can be seen in the sample or by  using a dependency injection framework. The class requires an instance of ```GoogleCloudMessaging``` which you can easily get by calling ```GoogleCloudMessaging.getInstance(Context)```, your GCM sender id which can be obtained via the first step in the setup, and an instance of ```SharedPreferences``` which will be used to store the GCM registration id between app launches.

In your ```PushManager``` you will need to override ```onGcmRegistrationIdAvailable(String)``` which will be passed the GCM registration id for your device when one has successfully been obtained. When you receive a registration id from this method it is your job to register it with your backend push server, and also check to make sure it isn't already registered. You will also need to override ```onGcmRegistrationFailed()``` which will be called when registration with GCM has failed after retry.

Make a call to ```PushManager.registerWithGCM``` when your app is launched (preferably in your ```Application``` subclass's ```onCreate()``` method). This will trigger an asynchronous attempt to retrieve a GCM registration id. You will be notified of success or failure via the methods mentioned above.

When a new push notification is received, you will be notified in your ```GcmIntentService``` subclass's ```handleMessage(Bundle)``` method. From within this method you can pull out the data necessary from the ```Bundle``` passed in. 

## Sample

There is a sample included with this project which demonstrates how to wire everything up. 

There are two sample projects, ```sample``` which is the Android app (client), and ```sample-push-server``` which is an example push server running on Google App Engine.

The ```sample``` Android client app uses a [Retrofit](https://github.com/square/retrofit) service which connects to the App Engine sample server and [Otto](https://github.com/square/otto) to relay push notification messages back to the ```MainActivity```, displaying them on screen.

In order to run the samples you will still need to get a GCM sender id and api key as indicated in the setup. Replace ```your_gcm_sender_id_here``` with your GCM sender id in ```ExampleApplication```. Replace ```your_gcm_api_key_here``` in appengine-web.xml with your GCM api key.

You will need to have your Android device on same network as your computer in order for everything to work properly.

Either create a App Engine DevAppServer configuration within Android Studio specifying a server address of ```0.0.0.0``` and server port ```8080```, then run it or run via the command line gradle wrapper with this command from the root directory of the project: ```./gradlew sample-push-server:appEngineRun```.

This will start the dev app server which will be used to register and unregister your device within the sample app as well as to send messages to your device.

Next, install the sample app to your device either via an Android Studio run configuration or by the command line gradle wrapper via the command: ```./gradlew sample:installD```.

Upon app launch, the app will attempt to register with GoogleCloudMessaging in ```ExampleApplication.onCreate()``` and if successful, will register the device with the sample push server. 

You can access your service at ```http://localhost:8080``` and send messages from the web app to your device from there.

## Permissions Used

The following permissions are required by this library as they are necessary for GCM to work. 

These permissions are transitive and exist in the library project's AndroidManifest.xml file:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="com.google.android.c2dm.permission.RECEIVE"/>
<uses-permission android:name="android.permission.WAKE_LOCK"/>
```

These permissions must be added to your application's AndroidManifest.xml file:

```xml
<permission android:name="${applicationId}.permission.C2D_MESSAGE" android:protectionLevel="signature" />
<uses-permission android:name="${applicationId}.permission.C2D_MESSAGE" />
```

This permission is **optional** but **highly recommended** and if you use the ```OnBootCompletedReceiver``` it is **required**:

```xml
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
```
