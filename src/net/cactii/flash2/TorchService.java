package net.cactii.flash2;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class TorchService extends Service {

    private static final String TAG = "TorchRoot";

    private Handler mHandler;
    private TimerTask mTorchTask;
    private Timer mTorchTimer;
    private WrapperTask mStrobeTask;
    private Timer mStrobeTimer;
    private NotificationManager mNotificationManager;
    private Notification mNotification;
    private Notification.Builder mNotificationBuilder;
    private int mStrobePeriod;
    private boolean mBright;
    private Runnable mStrobeRunnable;
    private Context mContext;

    public void onCreate() {
        String ns = Context.NOTIFICATION_SERVICE;
        this.mNotificationManager = (NotificationManager) getSystemService(ns);
        this.mContext = getApplicationContext();

        this.mHandler = new Handler() {
        };

        this.mTorchTask = new TimerTask() {
            public void run() {
                FlashDevice.getInstance(mContext).setFlashMode(FlashDevice.ON, mBright);
            }
        };
        this.mTorchTimer = new Timer();

        this.mStrobeRunnable = new Runnable() {
            private int mCounter = 4;

            @Override
            public void run() {
                int flashMode = FlashDevice.ON;
                if (FlashDevice.getInstance(mContext).getFlashMode() == FlashDevice.STROBE) {
                    if (this.mCounter-- < 1) {
                        FlashDevice.getInstance(mContext).setFlashMode(flashMode, mBright);
                    }
                } else {
                    FlashDevice.getInstance(mContext).setFlashMode(FlashDevice.STROBE, mBright);
                    this.mCounter = 4;
                }
            }

        };
        this.mStrobeTask = new WrapperTask(this.mStrobeRunnable);

        this.mStrobeTimer = new Timer();

    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "Starting torch");
        if (intent == null) {
            this.stopSelf();
            return START_NOT_STICKY;
        }
        this.mBright = intent.getBooleanExtra("bright", false);
        if (intent.getBooleanExtra("strobe", false)) {
            int strobePeriod = intent.getIntExtra("period", 5);
            if (strobePeriod == 0){
                strobePeriod = 1;
            }
            this.mStrobePeriod = (666 / strobePeriod) / 4;
            this.mStrobeTimer.schedule(this.mStrobeTask, 0, this.mStrobePeriod);
        } else {
            this.mTorchTimer.schedule(this.mTorchTask, 0, 100);
        }

        mNotificationBuilder = new Notification.Builder(this);
        mNotificationBuilder.setSmallIcon(R.drawable.notification_icon);
        mNotificationBuilder.setTicker(getString(R.string.not_torch_title));
        mNotificationBuilder.setContentTitle(getString(R.string.not_torch_title));
        mNotificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this,
                MainActivity.class), 0));
        mNotificationBuilder.setAutoCancel(false);
        mNotificationBuilder.setOngoing(true);

        PendingIntent turnOff = PendingIntent.getBroadcast(this, 0,
                new Intent(TorchSwitch.TOGGLE_FLASHLIGHT), 0);
        mNotificationBuilder.addAction(R.drawable.ic_appwidget_torch_off,
                getString(R.string.not_torch_toggle), turnOff);

        mNotification = mNotificationBuilder.getNotification();
        mNotificationManager.notify(getString(R.string.app_name).hashCode(), mNotification);

        startForeground(getString(R.string.app_name).hashCode(), mNotification);
        updateState(true);
        return START_STICKY;
    }

    public void onDestroy() {
        this.mNotificationManager.cancelAll();
        stopForeground(true);
        this.mTorchTimer.cancel();
        this.mStrobeTimer.cancel();
        FlashDevice.getInstance(mContext).setFlashMode(FlashDevice.OFF, mBright);
        updateState(false);
    }

    private void updateState(boolean on) {
        Intent intent = new Intent(TorchSwitch.TORCH_STATE_CHANGED);
        intent.putExtra("state", on ? 1 : 0);
        sendStickyBroadcast(intent);
    }

    public void Reshedule(int period) {
        this.mStrobeTask.cancel();
        this.mStrobeTask = new WrapperTask(this.mStrobeRunnable);

        this.mStrobePeriod = period / 4;
        this.mStrobeTimer.schedule(this.mStrobeTask, 0, this.mStrobePeriod);
    }

    public class WrapperTask extends TimerTask {
        private final Runnable target;

        public WrapperTask(Runnable target) {
            this.target = target;
        }

        public void run() {
            target.run();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
