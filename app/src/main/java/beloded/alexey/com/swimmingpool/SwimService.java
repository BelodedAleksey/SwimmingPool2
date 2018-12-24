package beloded.alexey.com.swimmingpool;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static beloded.alexey.com.swimmingpool.SwimApp.CHANNEL_1;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SwimService extends JobService {

    private static AlarmManagerBroadcastReceiver alarm;
    public static int[] int_sessions;
    public static String[] sessions;
    public static int index;
    private static final int NOTIFY_ID = 101;
    public static String TAG = "TAG";


    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
    }


    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job started");
        doBackgroundWork(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before completion");
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //int_sessions = getResources().getIntArray(R.array.int_sessions);
        //sessions = getResources().getStringArray(R.array.sessions);
        //alarm = new AlarmManagerBroadcastReceiver();
        //alarm.int_sessions = int_sessions;
        Context context = this.getApplicationContext();
        sessionIni();
        sessionUpdate(context);
        Log.d("TAG", "onStart");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d("TAG", "onDestroy");
        super.onDestroy();
    }

    private void doBackgroundWork(final JobParameters params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                workerThread();
                jobFinished(params, false);
            }
        }).start();
    }

    // Set this up in the UI thread.
    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            // This is where you do your work in the UI thread.
            Toast.makeText(getApplicationContext(), "WORK!!!", Toast.LENGTH_LONG).show();
            // Your worker tells you in the message what to do.
        }
    };

    public void workerThread() {
        // And this is how you call it from the worker thread:
        Message message = mHandler.obtainMessage(1, MainActivity.class);
        message.sendToTarget();
    }

    //функция уведомлений
    public static void sendChannel1Notification(Context context) {

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_1)
                .setSmallIcon(R.drawable.ic_android)
                .setColor(Color.BLUE)
                .setContentTitle("Текущий сеанс:")
                .setContentText(sessions[index])
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();


        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(NOTIFY_ID, notification);
    }

    /*Функция инициализации*/
    public void sessionIni(){
        int hour = getTime().get("hour");
        int minute = getTime().get("minute");
        index = binarySearchNode(hour * 100 + minute, -1, int_sessions);//номер в массиве текущего сеанса
    }

    /*Функция  апдейта*/
    public static void sessionUpdate(Context context){
        sendChannel1Notification(context);
        MainActivity.mViewPager.setCurrentItem(index);

        if(index >= int_sessions.length-1){
            index = -1;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, int_sessions[index + 1] / 100);
        calendar.set(Calendar.MINUTE, int_sessions[index + 1] % 100);
        calendar.set(Calendar.SECOND, 0);
        long alarmTime = calendar.getTimeInMillis();
        if(alarmTime < System.currentTimeMillis()){//если заводим на след. сутки, альт условие if (calendar.before(Calendar.getInstance()))
            //calendar.add(Calendar.DATE, 1); альт вариант
            alarmTime = alarmTime + 24*60*60*1000;//прибавляем сутки 24ч*60мин*60сек*1000мсек
        }
        if (alarm != null) {
            alarm.setOnetimeTimer(context, alarmTime);
            //alarm.index = index;
        } else {
            Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
        }
        index=index+1;
    }

    /*Функция взятия текущего времени*/
    public Map<String, Integer> getTime(){
        int hour, minute;
        Map time = new HashMap<String, Integer>();
        hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        time.put("hour", hour);
        minute = Calendar.getInstance().get(Calendar.MINUTE);
        time.put("minute", minute);
        return time;
    }

    /* бинарный поиск с определением ближайших узлов*/
    public int binarySearchNode(int value, int old, int array[]){
        int left, right, n;
        n = array.length;
        /* проверка позиции за пределами массива*/
        if(value < array[0])
            return (n-1);
        if(value >= array[n-1])
            return (n-1);
        /* процесс расширения области поиска. Вначале проверяется валидность
        начального приближения*/
        if(old>=0 && old<n-1) {
            int inc = 1;
            left = right = old;
            if (value < array[old]) {
                while (true) {
                    left -= inc;
                    if (left <= 0) {
                        left = 0;
                        break;
                    }
                    if (array[left] <= value) break;
                    left = right;
                    inc <<= 1;
                }
            }
        }
        /* начальное приближение  плохое -
         * за область поиска принимается весь массив */
        else {
            left = 0;
            right = n-1;
        }
        /* ниже алгоритм бинарного поиска требуемого интервала */
        while (left<right-1){
            int node = (left + right)>>1;
            if(value >= array[node]) left = node;
            else right = node;
        }
        return old = left;
    }
}
