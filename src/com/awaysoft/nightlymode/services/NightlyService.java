
package com.awaysoft.nightlymode.services;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.awaysoft.nightlymode.PreferenceActivity;
import com.awaysoft.nightlymode.R;
import com.awaysoft.nightlymode.utils.Constant;
import com.awaysoft.nightlymode.utils.Preference;
import com.awaysoft.nightlymode.widget.ControllerWidget;
import com.awaysoft.nightlymode.widget.MatteLayer;

/**
 * Background service for auto-nightly
 *
 * @author kang
 */
public class NightlyService extends Service implements Callback {
    private String mTopApp;
    private Handler mHandler;
    private AppMonitor mAppMonitor;
    private MatteLayer mMatteLayer;
    private WindowManager mGlobalWindow;
    private ControllerWidget mFloatController;

    // On preference changed monitor
    private BroadcastReceiver mPreferenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }

            String action = intent.getAction();
            if (Constant.BDC_PREFERENCE_CHENGED.equals(action)) {
                Message msg = Message.obtain(mHandler);
                msg.what = Constant.MSG_PREFERENCE_CHANGED;
                msg.obj = intent.getIntExtra(Constant.PREFERENCE_TARGET_KEY, -1);
                mHandler.sendMessage(msg);
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTopApp = "null";
        mHandler = new Handler(getMainLooper(), this);
        mGlobalWindow = (WindowManager) getSystemService(WINDOW_SERVICE);

        mMatteLayer = new MatteLayer(this);
        mMatteLayer.attachToWindows(mGlobalWindow);

        mFloatController = new ControllerWidget(this);
        mFloatController.bindHandler(mHandler);

        // Register preference monitor receiver
        registerReceiver(mPreferenceReceiver, new IntentFilter(Constant.BDC_PREFERENCE_CHENGED));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Toast.makeText(this, getString(R.string.service_started), Toast.LENGTH_SHORT).show();
        Preference.read(this);
        Preference.parseWhiteList();
        Preference.sServiceRunning = true;

        if (Preference.sNightlyMode == Constant.MODE_AUTO) {
            startMonitor();
        }

        if (Preference.sFloatWidget) {
            mFloatController.attachToWindow(mGlobalWindow);
        } else {
            mFloatController.detachFromWindow();
        }

        if (Preference.sNotification) {
            startForeground();
        } else {
            stopForeground(true);
        }

        ComponentName name = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(1).get(
                0).topActivity;
        mTopApp = name.getPackageName();
        switchMode(mTopApp);

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Unregister preference monitor receiver
        unregisterReceiver(mPreferenceReceiver);

        stopMonitor();
        switchMode("stop");

        if (mMatteLayer != null) {
            mMatteLayer.detachFromWindow();
            mMatteLayer = null;
        }

        if (mFloatController != null) {
            mFloatController.detachFromWindow();
        }

        Preference.save(this);
        stopForeground(true);
        super.onDestroy();
        Toast.makeText(this, getString(R.string.service_stoped), Toast.LENGTH_SHORT).show();
    }

    private class AppMonitor extends Thread {
        private Boolean sStoped = false;

        @Override
        public void run() {
            ComponentName name;
            while (!sStoped) {
                try {
                    name = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(1).get(
                            0).topActivity;
                    String cacheStr = name.getPackageName();

                    if (!mTopApp.equals(cacheStr)) {
                        mTopApp = cacheStr;
                        Message msg = Message.obtain(mHandler);
                        msg.what = Constant.MSG_UPDATE_APP_INFO;
                        msg.obj = cacheStr;
                        mHandler.sendMessage(msg);
                    }

                    sleep(100);
                } catch (Exception e) {
                    // do nothing
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void startMonitor() {
        if (mAppMonitor == null || !mAppMonitor.isAlive()) {
            if (mAppMonitor != null) {
                mAppMonitor.sStoped = true;
            }

            mAppMonitor = new AppMonitor();
            mAppMonitor.sStoped = false;
            mAppMonitor.start();
        }
    }

    private void stopMonitor() {
        if (mAppMonitor != null) {
            mAppMonitor.sStoped = true;
        }
    }

    @SuppressWarnings("deprecation")
    private void startForeground() {
        Notification mNotification = new Notification(R.drawable.night_notification_icon,
                                                      getText(R.string.nightly_title), System.currentTimeMillis());
        Intent notificationIntent = new Intent(this, PreferenceActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        mNotification.setLatestEventInfo(this, getText(R.string.nightly_title),
                                         getText(R.string.night_notification_tips), pendingIntent);
        startForeground(mNotification.hashCode(), mNotification);
    }

    private void switchMode(String pkgName) {
        if ("stop".equals(pkgName)) {
            mMatteLayer.matteSmoothOut();
        } else {
            switch (Preference.sNightlyMode) {
                case Constant.MODE_AUTO:
                    if (Preference.inWhiteList(pkgName)) {
                        mMatteLayer.matteSmoothIn();
                    } else {
                        mMatteLayer.matteSmoothOut();
                    }
                    break;
                case Constant.MODE_NIGHT:
                    mMatteLayer.matteSmoothIn();
                    break;
                case Constant.MODE_NORMAL:
                    mMatteLayer.matteSmoothOut();
                    break;
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            // Top application changed
            case Constant.MSG_UPDATE_APP_INFO: {
                switchMode((String) msg.obj);
                break;
            }
            // Night mode changed
            case Constant.MSG_STATUS_CHANGED: {
                if (Preference.sNightlyMode == Constant.MODE_AUTO) {
                    startMonitor();
                } else {
                    stopMonitor();
                }

                switchMode(mTopApp);
                Preference.saveKey(this, Constant.KEY_SERVICES_NIGHTLY_MODE, Preference.sNightlyMode);
                break;
            }
            // Preference changed
            case Constant.MSG_PREFERENCE_CHANGED: {
                Object obj = msg.obj;
                int tag = -1;
                if (obj instanceof Integer) {
                    tag = (Integer) obj;
                }

                onPreferenceChanged(tag);
                break;
            }

            default:
                return false;
        }

        return true;
    }

    private void onPreferenceChanged(int tag) {

        Log.d("NightService", "");

        if (!Preference.sActivityRunning && tag != -1) {
            switch (tag) {
                case Constant.TAG_ID_MODE: {
                    if (Preference.sFloatWidget) {
                        mFloatController.attachToWindow(mGlobalWindow);
                    } else {
                        mFloatController.detachFromWindow();
                    }

                    mHandler.sendEmptyMessage(Constant.MSG_STATUS_CHANGED);
                    return;
                }

                case Constant.TAG_ID_ALPHA: {
                    mMatteLayer.setMatteAlpha(Preference.sMatteAlpha);
                    return;
                }

                case Constant.TAG_ID_COLOR: {
                    //TODO change color
                    return;
                }

                case Constant.TAG_ID_NOTIFICATION: {
                    if (Preference.sNotification) {
                        startForeground();
                    } else {
                        stopForeground(true);
                    }
                    break;
                }
            }
        }
    }
}
