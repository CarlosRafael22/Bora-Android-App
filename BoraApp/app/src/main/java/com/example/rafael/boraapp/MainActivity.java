package com.example.rafael.boraapp;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rafael.boraapp.adapters.NavDrawerListAdapter;
import com.example.rafael.boraapp.login.LastSyncDateManager;
import com.example.rafael.boraapp.login.SessionManager;
import com.example.rafael.boraapp.models.Activity;
import com.example.rafael.boraapp.models.NavDrawerItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    RelativeLayout mDrawerPane;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;

    ArrayList<NavDrawerItem> mNavItems = new ArrayList<NavDrawerItem>();
    private NavDrawerListAdapter adapter;


    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    private SessionManager session;
    private LastSyncDateManager lastSyncManager;
    private DatabaseHandler databaseHandler;

    ///Vai ter as activities nesse ArrayList dps que ele pegar do servidor
    ArrayList<Activity> activities_list;

    //Vai guardar quando foi a ultima vez que fez o sync das atividades com o servidor
    //com databaseHandler.last_time_activities_synced tava sempre voltando null
    private static Date last_time_activities_synced;

    private Date last_sync_date;

    private static final String BASE_URL = "http://bora-server.herokuapp.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Session Manager
        session = SessionManager.getSessionManager(getApplicationContext());
        lastSyncManager = LastSyncDateManager.getLastSyncDateManager(getApplicationContext());
        databaseHandler = DatabaseHandler.getDatabaseHandler(getApplicationContext());



        // enabling action bar app icon and behaving it as toggle button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //MainActivity.last_time_activities_synced = set_last_synced();
        databaseHandler.setLast_time_activities_synced(set_last_synced());

        last_sync_date = lastSyncManager.getKeyLastSync();
        if(last_sync_date == null){

            String some_date = "2012-04-22 18:49:12";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date some_datetime = null;
            try {
                some_datetime = sdf.parse(some_date);
                Log.i("MAIN ACTIVITY", "Last_synced was null: " + String.valueOf(some_datetime));
                lastSyncManager.setKeyLastSync(some_datetime);

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

        //Log.v("LAST_SYNCED", String.valueOf(MainActivity.last_time_activities_synced));
        //Log.v("LAST_SYNCED", String.valueOf(databaseHandler.getLast_time_activities_synced()));
        Log.v("LAST_SYNCED", String.valueOf(lastSyncManager.getKeyLastSync()));

        getActivitiesNearby();


        viewPager = (ViewPager) findViewById(R.id.viewpager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new Fragment_Maps(), "Maps");
        adapter.addFragment(new Fragment_ActivityList(), "Activities");
        adapter.addFragment(new Fragment_Friends_Timeline(), "Timeline");
        viewPager.setAdapter(adapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


        //Initializing NavigationView
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        //Pega o View do header do navigation
        View headerLayout = navigationView.getHeaderView(0);

        //SEtando o username e email do user logado para se mostrado no navigationView
        TextView nav_header_username = (TextView) headerLayout.findViewById(R.id.nav_header_username);
        TextView nav_header_email = (TextView) headerLayout.findViewById(R.id.nav_header_email);

        //Pegando os dados do user logado do sessionManager
        HashMap<String, String> userSession = session.getUserDetails();
        nav_header_username.setText(userSession.get(SessionManager.KEY_USERNAME));
        nav_header_email.setText(userSession.get(SessionManager.KEY_EMAIL));


        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                boolean nav_item_selected = navigationSelected(menuItem);
                return nav_item_selected;
            }
        });


        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();


//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }


    public boolean navigationSelected(MenuItem menuItem) {

        //Checking if the item is in checked state or not, if not make it in checked state
        if (menuItem.isChecked()) menuItem.setChecked(false);
        else menuItem.setChecked(true);

        //Closing drawer on item click
        drawerLayout.closeDrawers();

        //Check to see which item was being clicked and perform appropriate action
        switch (menuItem.getItemId()) {


//                    //Replacing the main content with ContentFragment Which is our Inbox View;
//                    case R.id.inbox:
//                        Toast.makeText(getApplicationContext(),"Inbox Selected",Toast.LENGTH_SHORT).show();
//                        ContentFragment fragment = new ContentFragment();
//                        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//                        fragmentTransaction.replace(R.id.frame,fragment);
//                        fragmentTransaction.commit();
//                        return true;

            // For rest of the options we just show a toast on click

            case R.id.nav_camera:
                Toast.makeText(getApplicationContext(), "Camera Selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.nav_gallery:
                Toast.makeText(getApplicationContext(), "Gallery Selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.nav_slideshow:
                Toast.makeText(getApplicationContext(), "Slideshow Selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.nav_manage:
                Toast.makeText(getApplicationContext(), "Tools Selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.nav_share:
                Toast.makeText(getApplicationContext(), "Share Selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.nav_send:
                Toast.makeText(getApplicationContext(), "Log out Selected", Toast.LENGTH_SHORT).show();
                UserLogoutTask mAuthTask = new UserLogoutTask();
                mAuthTask.execute((Void) null);
                return true;
            default:
                Toast.makeText(getApplicationContext(), "Somethings Wrong", Toast.LENGTH_SHORT).show();
                return true;

        }

    }


//    // Called when invalidateOptionsMenu() is invoked
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        // If the nav drawer is open, hide action items related to the content view
//        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
//        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
//        return super.onPrepareOptionsMenu(menu);
//    }

    public Date set_last_synced(){
        Date last_sync = null;

        if(databaseHandler.getLast_time_activities_synced()==null){

            String some_date = "2012-04-22 18:49:12";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date some_datetime = null;
            try {
                some_datetime = sdf.parse(some_date);
                Log.i("MAIN ACTIVITY", "Last_synced was null: " + String.valueOf(some_datetime));
                last_sync = some_datetime;

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }else{
            last_sync = databaseHandler.getLast_time_activities_synced();
        }

        return last_sync;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLogoutTask extends AsyncTask<Void, Void, Boolean> {


        UserLogoutTask() {

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7&APPID=3c968409a0474eb891ad0aefc33f04e2");

                URL url = new URL(BASE_URL+"/users/logout");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);


                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();

                Log.v("RESPONSE", "Server JSON REsponse: " + forecastJsonStr);

                session.logoutUser();

            } catch (IOException e) {
                Log.e("FUDEU", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        //Log.e(LOG_TAG, "Error closing stream", e);
                        e.printStackTrace();
                    }
                }
            }
            // TODO: register the new account here.
            return true;
        }

    }


    public void getActivitiesNearby() {

        ActivitiesTask getActivitiesTask = new ActivitiesTask();
        getActivitiesTask.execute((Void) null);
    }


    public void insertActivitiesInDatabase(ArrayList<Activity> activities){

//        for(Activity activity: activities){
//            Log.v("MAIN ACTIVITY", activity.getTitle());
//            databaseHandler.createActivity(activity, null);
//        }

        for(int i=0;i<activities.size();i++){
            Log.i("MAIN ACTIVITY", "Inserted: " + activities.get(i).getTitle());
            databaseHandler.createActivity(activities.get(i), null);
        }

    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class ActivitiesTask extends AsyncTask<Void, Void, JSONArray> {

        private ArrayList<Activity> activities;

        //Vai ser o que vamos mandar para a Main Thread atraves do onPostExecute, vai chegar um JSONArray e dele faremos os Activities
        JSONArray jsonActivities;

        //Pra jogar a mensagem de erro no onPostExecute e bota-la no mUsernameView ou mPasswordView
        String messageJSON = null;

        ActivitiesTask() {

        }

        @Override
        protected JSONArray doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String serverResponseStr = null;

            try {

                URL url = new URL(BASE_URL+"/activities");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.connect();


                // Read the input stream into a String
                InputStream inputStream;
                StringBuffer buffer;

                int httpResponseCode = urlConnection.getResponseCode();
                if (httpResponseCode == 200) {
                    inputStream = urlConnection.getInputStream();
                    buffer = readBuffer(reader, inputStream);
                } else {
                    //Toast.makeText(getApplicationContext(), "OLha a merda pai, "+httpResponseCode,Toast.LENGTH_SHORT).show();
                    Log.v("BRONCA", "REsponse code: " + httpResponseCode);
                    InputStream errorInputStream = urlConnection.getErrorStream();
                    buffer = readBuffer(reader, errorInputStream);
                }

                serverResponseStr = buffer.toString();

                Log.v("MAIN ACTIVITY", "Server Response JSON String: " + serverResponseStr);

                //Chegou a resposta do servidor como String mas agora tem que transformar em JSON e criar os Objetos Activities

                JSONObject jsonServerResponse;

                try {
                    jsonServerResponse = new JSONObject(serverResponseStr);

                    jsonActivities = jsonServerResponse.getJSONArray("activities_returned");
                    int activities_amount = jsonActivities.length();

                    Log.v("MAIN ACTIVITY", "Activities returned: " + String.valueOf(activities_amount));

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } catch (IOException e) {
                Log.e("FUDEU", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        //Log.e(LOG_TAG, "Error closing stream", e);
                        e.printStackTrace();
                    }
                }
            }

            return jsonActivities;

        }

        @Override
        protected void onPostExecute(final JSONArray jsonActivities) {

            activities_list = new ArrayList<>();
            String id, title, category, author, date, place, updatedAt;

            if (jsonActivities == null) {
                Toast.makeText(getApplicationContext(), "Deu merda no JSON Array", Toast.LENGTH_SHORT).show();

            } else {

                //Date last_synced = databaseHandler.getLast_time_activities_synced();
//                if(MainActivity.last_time_activities_synced==null){
//
//                    String some_date = "2012-04-22 18:49:12";
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                    Date some_datetime = null;
//                    try {
//                        some_datetime = sdf.parse(some_date);
//                        Log.i("MAIN ACTIVITY", "Last_synced was null: " + String.valueOf(some_datetime));
//                        MainActivity.last_time_activities_synced = some_datetime;
//
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }
//
//                }
                //Log.i("MAIN ACTIVITY", String.valueOf(databaseHandler.getLast_time_activities_synced()));
                Log.i("MAIN ACTIVITY", String.valueOf(lastSyncManager.getKeyLastSync()));

                Log.i("MAIN ACTIVITY", String.valueOf(jsonActivities.length()));
                //SE o JSONArray chegou certinho entao a gnt vai construir as Activities e inserir no ArrayList
                for(int i=0;i<jsonActivities.length();i++){
                    try {
                        JSONObject activity = jsonActivities.getJSONObject(i);
                        id = activity.getString("_id");
                        title = activity.getString("title");
                        category = activity.getString("category");
                        author = activity.getString("author");
                        updatedAt = activity.getString("updatedAt");

                        Date activity_date = null;
                        if(activity.has("date")){
                            date = activity.getString("date");
                            activity_date = getActivityDateFromString(date);
                        }

                        if(activity.has("place")){
                            place = activity.getString("place");
                        }else{
                            place = null;
                        }

                        //Log.i("MAIN ACTIVITY", updatedAt);


                        Date act_updatedAt = getUpdatedAtFromString(updatedAt);



                        JSONArray comments = activity.getJSONArray("comments");
                        if(comments.length() == 0){
                            Log.i("MAIN ACTIVITY", "Activity comments: " + comments.length());
                        }else{
                            //AINDA TENHO QUE CONTRUIR OS COMMENTS OBJ A PARTIR DO JSONARRAY E DPS CRIAR NEW_ACTIVITY COM ESSES COMMENTS
                            //ENTAO TB TEM QUE TER OUTRO CONSTRUTOR NO ACTIVITY PRA RECEBER COMMENTS
                        }

                        Log.i("DATE 1", "act_updatedAt: " + String.valueOf(act_updatedAt));
                        //Date1 is after Date2
//                        Log.i("DATE 2", "last_synced: " + String.valueOf(MainActivity.last_time_activities_synced));
//                        if(act_updatedAt.compareTo(MainActivity.last_time_activities_synced)>0){
                        Log.i("DATE 2", "last_synced: " + String.valueOf(lastSyncManager.getKeyLastSync()));
                        if(act_updatedAt.compareTo(lastSyncManager.getKeyLastSync())>0){

                            Activity new_activity = new Activity(id, title, category, author, activity_date, place, act_updatedAt);
                            Log.i("MAIN ACTIVITY", "Added: " + new_activity.getTitle());
                            activities_list.add(new_activity);
                        }

                        //Atualizando a ultima fez que fez o sync das atividades
                        //databaseHandler.setLast_time_activities_synced(new Date());
//                        MainActivity.last_time_activities_synced = new Date();
//                        Log.i("NEW SYNC", "last_synced: " + String.valueOf(MainActivity.last_time_activities_synced));



                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                databaseHandler.setLast_time_activities_synced(new Date());
                lastSyncManager.setKeyLastSync(new Date());
                Log.i("NEW SYNC", "last_synced: " + String.valueOf(lastSyncManager.getKeyLastSync()));

                insertActivitiesInDatabase(activities_list);

            }
        }



        public Date getUpdatedAtFromString(String updatedAt){

            //updatedAT chega assim: 2016-05-21T20:08:49.770Z
            String[] parts = updatedAt.split("T");
            String act_date = parts[0]; // 2016-05-21
            String act_wrongTime = parts[1]; // 20:08:49.770Z

            //Agora pra tirar a ultima parte do act_time e so deixar o horario mesmo
            //Log.i("MAIN ACTIVITY", act_wrongTime);
            String[] time_parts = act_wrongTime.split("\\.");
            String act_time = time_parts[0]; // 20:08:49

            String act_date_time = act_date + " " + act_time;


            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date act_updatedAt = null;
            try {
                act_updatedAt = sdf.parse(act_date_time);
                Log.i("MAIN ACTIVITY", String.valueOf(act_updatedAt));

            } catch (ParseException e) {
                e.printStackTrace();
            }

            return act_updatedAt;
        }


        public Date getActivityDateFromString(String date){

            //updatedAT chega assim: 2016-06-11T00:00:00.000Z
            String[] parts = date.split("T");
            String act_date = parts[0]; // 2016-06-11
            //String act_wrongTime = parts[1]; // 00:00:00.000Z

            //Agora pra tirar a ultima parte do act_time e so deixar o horario mesmo
            //Log.i("MAIN ACTIVITY", act_wrongTime);

            //AGORA NAO TA PEGANDO O HORARIO E SO A DATA
//            String[] time_parts = act_wrongTime.split("\\.");
//            String act_time = time_parts[0]; // 20:08:49
//
//            String act_date_time = act_date + " " + act_time;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date activity_date = null;
            try {
                activity_date = sdf.parse(act_date);
                Log.i("MAIN ACTIVITY", String.valueOf(activity_date));

            } catch (ParseException e) {
                e.printStackTrace();
            }

            return activity_date;

        }


        public StringBuffer readBuffer(BufferedReader reader, InputStream inputStream) {

            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }

            return buffer;
        }


    }
}
