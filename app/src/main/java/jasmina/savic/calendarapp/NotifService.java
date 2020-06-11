package jasmina.savic.calendarapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Calendar;
import java.util.HashMap;


public class NotifService extends Service {

    private MyBinder mBinder = null;
    private NotificationChannel channel;
    private NotificationManager mNotificationManager;

    private EventDbHelper mDbHelper = EventDbHelper.getInstance(this);

    private NotifSender sender;

    private HashMap<Long, Integer> reminders;

    public static final String LOG = "NOTIFSERVICE";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        Event[] tmp = mDbHelper.readEventsReminder();
        reminders = new HashMap<Long, Integer>();
        if (tmp != null) {
            for (Event e : tmp) {
                reminders.put(e.getId(), e.getMinuteChecked());
            }
        }
        channel = new NotificationChannel("mynotifchannel", "notifs", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("noDescription");
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannel(channel);
        sender = new NotifSender();
        sender.start();
        Log.d(LOG, "created");
    }

    private class MyBinder extends INotifBinder.Stub {

        private static final String LOG_TAG = "BINDERIDK";

        @Override
        public void addEventReminder(long id, int mins) throws RemoteException {
            if (reminders.containsKey(id)) {
                // ovo znaci da se reminder samo update-uje
                reminders.remove(id);
            }
            reminders.put(id, mins);
        }

        @Override
        public void removeEventReminder(long id) throws RemoteException {
            if (reminders.containsKey(id)) {
                reminders.remove(id);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (mBinder == null) {
            mBinder = new MyBinder();
        }

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sender.stop();
        Log.d(LOG, "stopped");
    }

    private class NotifSender implements Runnable {

        private static final long PERIOD = 60L * 1000L;

        private Handler mHandler    ;
        private boolean mRun = false;

        public NotifSender() {
            mHandler = new Handler(Looper.getMainLooper());
        }

        public void start() {
            mRun = true;
            mHandler.postDelayed(this, PERIOD);
        }

        public void stop() {
            mRun = false;
            mHandler.removeCallbacks(this);
        }

        @Override
        public void run() {
            if (!mRun) {
                return;
            }

            Calendar now = Calendar.getInstance();
            int minutes = now.get(Calendar.MINUTE);
            for (Long id : reminders.keySet()) {
                if (reminders.get(id) == minutes) {

                    Event mEvent = mDbHelper.readEvent(id);
                    if (mEvent == null) {
                        reminders.remove(id);
                        continue;
                    }
                    if(!mEvent.isEventReminder()) {
                        reminders.remove(id);
                        continue;
                    }

                    Intent mIntent = new Intent(getApplicationContext(), EventActivity.class);
                    mIntent.setFlags(0);
                    mIntent.putExtra("eventId", id);
                    mIntent.putExtra("eventName", mEvent.getEventName());
                    mIntent.putExtra("eventDate", mEvent.getEventDate());
                    //Create the TaskStackBuilder and add the intent, which inflates the back stack
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                    Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                    Intent dayIntent = new Intent(getApplicationContext(), DayActivity.class);
                    dayIntent.putExtra("date", mEvent.getEventDate());
                    stackBuilder.addNextIntent(mainIntent);
                    stackBuilder.addNextIntent(dayIntent);
                    stackBuilder.addNextIntent(mIntent);
                    //Get the PendingIntent containing the entire back stack
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "mynotifchannel")
                            .setSmallIcon(R.drawable.bell)
                            .setContentTitle("Podsetnik")
                            .setContentText("Događaj " + mEvent.getEventName() + " počinje " + mEvent.getEventDate() + " u " + mEvent.getEventTimeStart() + ".")
                            .setContentIntent(resultPendingIntent)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true);

                    //notificationID is set to current time just so we can differentiate notifications
                    // so they don't overwrite each other

                    mNotificationManager.notify((int) System.currentTimeMillis(), builder.build());

                    Log.d(LOG, mEvent.getEventName() + " " + String.valueOf(mEvent.getId()));
                }
            }

            mHandler.postDelayed(this, PERIOD);
        }
    }
}
