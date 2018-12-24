package beloded.alexey.com.swimmingpool;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.icu.text.UnicodeSetSpanner;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.JOB_SCHEDULER_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static beloded.alexey.com.swimmingpool.SwimApp.CHANNEL_1;

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver{

    final public static String ONE_TIME="onetime";
    public static String INDEX = "INDEX";
    //private static int[] int_sessions;
    //private static String[] sessions;
    public static int index;
    public static String TAG = "TAG";
    public static final String ACTIVITY_RUN = "ACTIVITY_RUN";
    public static final String MyPrefs= "MYPREFS";
    private static final int NOTIFY_ID = 101;
    public static final String FIRST_LAUNCH = "beloded.alexey.com.swimmingpool.action.FIRST_LAUNCH";
    public static final String ACTIVITY_STOP = "beloded.alexey.com.swimmingpool.action.ACTIVITY_STOP";
    public static final String ACTIVITY_START = "beloded.alexey.com.swimmingpool.action.ACTIVITY_START";
    SharedPreferences spref;

    @Override
    public void onReceive(Context context, Intent intent){
        spref = context.getApplicationContext().getSharedPreferences(MyPrefs, MODE_PRIVATE);
        index = spref.getInt(INDEX, 0);
        //принять кастомный бродкаст с экшном первого запуска
        if (intent.getAction() == null) {
            PowerManager pm=(PowerManager) context.getSystemService(Context.POWER_SERVICE);
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");
//Осуществляем блокировку
            wl.acquire();
//Здесь можно делать обработку.
            Bundle extras= intent.getExtras();
            StringBuilder msgStr=new StringBuilder();

            if(extras != null && extras.getBoolean(ONE_TIME, Boolean.TRUE)){
//проверяем параметр ONE_TIME, если это одиночный будильник,
//выводим соответствующее сообщение.
                msgStr.append("Одноразовый будильник: ");
                //index = extras.getInt(INDEX);
            }
            Format formatter=new SimpleDateFormat("hh:mm:ss a");
            msgStr.append(formatter.format(new Date()));
            Toast.makeText(context, msgStr, Toast.LENGTH_LONG).show();
        //SwimService.index = index;
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scheduleJob(context);
        }*/
            sessionUpdate(context);
//Разблокируем поток.
            wl.release();
        } else if(intent.getAction().equals(FIRST_LAUNCH)){
            //Toast.makeText(context, "ONRECEIVE", Toast.LENGTH_SHORT).show();
            //int_sessions = context.getResources().getIntArray(R.array.int_sessions);
            //sessions = context.getResources().getStringArray(R.array.sessions);
            sessionIni();
            sessionUpdate(context);
        }
        else if(intent.getAction().equals(ACTIVITY_STOP)){

        }
        else if(intent.getAction().equals(ACTIVITY_START)){
            if(index == 0){
                index = SwimApp.int_sessions.length;
            }
            MainActivity.mViewPager.setCurrentItem(index-1);
        }
    }

    //функция уведомлений
    public void sendChannel1Notification(Context context) {
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_1)
                .setSmallIcon(R.drawable.ic_android)
                .setColor(Color.BLUE)
                .setContentTitle("Текущий сеанс:")
                .setContentText(SwimApp.sessions[index])
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();


        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(NOTIFY_ID, notification);
    }

    /*Функция  апдейта*/
    public void sessionUpdate(Context context){
        sendChannel1Notification(context);
        if (spref.getBoolean(ACTIVITY_RUN, false)) {
            MainActivity.mViewPager.setCurrentItem(index);
        }

        index = index + 1;
        if(index > SwimApp.int_sessions.length-1){
            index = 0;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, SwimApp.int_sessions[index] / 100);
        calendar.set(Calendar.MINUTE, SwimApp.int_sessions[index] % 100);
        calendar.set(Calendar.SECOND, 0);
        long alarmTime = calendar.getTimeInMillis();
        if(alarmTime < System.currentTimeMillis()){//если заводим на след. сутки, альт условие if (calendar.before(Calendar.getInstance()))
            //calendar.add(Calendar.DATE, 1); альт вариант
            alarmTime = alarmTime + 24*60*60*1000;//прибавляем сутки 24ч*60мин*60сек*1000мсек
        }
        setOnetimeTimer(context, alarmTime);
        spref.edit().putInt(INDEX, index).commit();
    }

    /*Функция инициализации*/
    public void sessionIni(){
        int hour = getTime().get("hour");
        int minute = getTime().get("minute");
        index = binarySearchNode(hour * 100 + minute, -1, SwimApp.int_sessions);//номер в массиве текущего сеанса
        Log.d(TAG, String.valueOf(index));
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void scheduleJob(Context context) {
        ComponentName componentName = new ComponentName(context, SwimService.class);
        JobInfo info = new JobInfo.Builder(123, componentName)
                .setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                //  .setPeriodic(15*60 * 1000)
                .build();

        JobScheduler scheduler = (JobScheduler)context.getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Job scheduled");
        } else {
            Log.d(TAG, "Job scheduling failed");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void cancelJob(Context context) {
        JobScheduler scheduler = (JobScheduler)context.getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancel(123);
        Log.d(TAG, "Job cancelled");
    }

    public void SetAlarm(Context context)
    {
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent=new Intent(context, AlarmManagerBroadcastReceiver.class);
        intent.putExtra(ONE_TIME, Boolean.FALSE);//Задаем параметр интента
        PendingIntent pi= PendingIntent.getBroadcast(context,0, intent,0);
//Устанавливаем интервал срабатывания в 5 секунд.
        am.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),1000*5,pi);
    }

    public void CancelAlarm(Context context)
    {
        Intent intent=new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent sender= PendingIntent.getBroadcast(context,0, intent,0);
        AlarmManager alarmManager=(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);//Отменяем будильник, связанный с интентом данного класса
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setOnetimeTimer(Context context, long timer){
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent=new Intent(context, AlarmManagerBroadcastReceiver.class);
        intent.putExtra(ONE_TIME, Boolean.TRUE);//Задаем параметр интента
        //intent.putExtra(INDEX, index);
        PendingIntent pi = PendingIntent.getBroadcast(context,0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setExact(AlarmManager.RTC_WAKEUP, timer ,pi);
    }
}

