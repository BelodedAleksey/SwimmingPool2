package beloded.alexey.com.swimmingpool;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.widget.Toast;

public class SwimApp extends Application {

    public static final String CHANNEL_1 = "channel1";
    public static final String CHANNEL_2 = "channel2";
    public static int[] int_sessions;
    public static String[] sessions;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
        int_sessions = getResources().getIntArray(R.array.int_sessions);
        sessions = getResources().getStringArray(R.array.sessions);
    }

    private void createNotificationChannels(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is Channel 1");

            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_2,
                    "Channel 2",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel2.setDescription("This is Channel 2");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);
        }
    }
}
