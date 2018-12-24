package beloded.alexey.com.swimmingpool;

import android.app.AlarmManager;
import android.app.job.JobInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.binarySearch;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    public static ViewPager mViewPager;
    private static AlarmManagerBroadcastReceiver alarm;
    //public static int[] int_sessions;
    //public static String[] sessions;
    SharedPreferences spref;
    public static final String MyPrefs= "MYPREFS";
    public static final String ACTIVITY_RUN = "ACTIVITY_RUN";
    public static final String FIRST_LAUNCH = "beloded.alexey.com.swimmingpool.action.FIRST_LAUNCH";
    public static final String ACTIVITY_STOP = "beloded.alexey.com.swimmingpool.action.ACTIVITY_STOP";
    public static final String ACTIVITY_START = "beloded.alexey.com.swimmingpool.action.ACTIVITY_START";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //sessions = getResources().getStringArray(R.array.sessions);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        //alarm = new AlarmManagerBroadcastReceiver();
        spref = getSharedPreferences(MyPrefs, MODE_PRIVATE);
        SharedPreferences.Editor editor = spref.edit();
        editor.putBoolean(ACTIVITY_RUN, true);
        editor.commit();
        //Первый запуск
        if (!spref.getBoolean("AFTER_FIRST_LAUNCH", false)){
            editor.putBoolean("AFTER_FIRST_LAUNCH", true);
            editor.commit();
            //послать кастомный экшн первого запуска
            //IntentFilter intentFilter = new IntentFilter(FIRST_LAUNCH);
            //registerReceiver(alarm, intentFilter);

            Intent intent = new Intent(this, AlarmManagerBroadcastReceiver.class);
            intent.setAction(FIRST_LAUNCH);
            sendBroadcast(intent);
            //Toast.makeText(getApplicationContext(), "FIRST_LAUNCH", Toast.LENGTH_LONG).show();
        } else {
            Intent intent = new Intent(this, AlarmManagerBroadcastReceiver.class);
            intent.setAction(ACTIVITY_START);
            sendBroadcast(intent);
        }

        //int_sessions = getResources().getIntArray(R.array.int_sessions);

        /*alarm.int_sessions = int_sessions;

        sessionIni();
        Context context = this.getApplicationContext();
        sessionUpdate(context);*/
        /*ListView spisok = (ListView)findViewById(R.id.spisok);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.times, R.layout.list_item);
        spisok.setAdapter(adapter);*/

    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = spref.edit();
        editor.putBoolean(ACTIVITY_RUN, false);
        editor.commit();
        Intent intent = new Intent(this, AlarmManagerBroadcastReceiver.class);
        intent.setAction(ACTIVITY_STOP);
        sendBroadcast(intent);
        //unregisterReceiver(alarm);
    }

    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SESSION = "session";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putString(ARG_SESSION, SwimApp.sessions[sectionNumber-1]);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section, getArguments().getString(ARG_SESSION)));
            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {

            return SwimApp.sessions.length;
        }
    }
}
