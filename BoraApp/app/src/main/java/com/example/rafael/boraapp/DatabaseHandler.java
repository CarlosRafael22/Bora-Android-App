package com.example.rafael.boraapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.rafael.boraapp.models.Activity;
import com.example.rafael.boraapp.models.Comment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by rafael on 27/05/16.
 */
public class DatabaseHandler extends SQLiteOpenHelper {


    public static final String DATABASE_NAME = "Bora.db";
    private static final int DATABASE_VERSION = 1;


    /*
    * As IDs do Activity e Comment nao vai ser PRIMARY KEYS pq se elas fossem elas seriam setadas automaticamente pelo banco
    * elas vao ser setadas manualmente com os valores das IDs que tao no servidor
    */
    public static final String ACTIVITY_TABLE = "activities";
    public static final String ACTIVITY_ID = "_id";
    public static final String ACTIVITY_TITLE = "title";
    public static final String ACTIVITY_CATEGORY = "category";
    public static final String ACTIVITY_AUTHOR_ID = "author_id";
    public static final String ACTIVITY_DATE = "date";
    public static final String ACTIVITY_PLACE = "place";
    public static final String ACTIVITY_PLACE_LAT_LNG = "place_LatLng";
    public static final String ACTIVITY_UPDATED_AT = "updated_at";


    public static final String COMMENTS_TABLE = "comments";
    public static final String COMMENTS_ID = "_id";
    public static final String COMMENTS_COMMENT = "comment";
    public static final String COMMENTS_AUTHOR_ID = "author_id";
    public static final String COMMENTS_UPDATED_AT = "updated_at";


    public static final String COMMENTS_ACTIVITIES_TABLE = "comments_activities";
    public static final String COMMENTS_ACTIVITIES_ID = "_id";
    public static final String COMMENTS_ACTIVITIES_ID_COMMENT = "comment_id";
    public static final String COMMENTS_ACTIVITIES_ID_ACTIVITY = "activity_id";


    //PRA GUARGDAR A ULTIMA VEZ QUE CHECOU NO BANCO SE TINHA ATIVIDADES NOVAS
    public Date last_time_activities_synced;


    //VAI VIRAR UM SINGLETON PRA TER SO UM NO PROGRAMA
    private static DatabaseHandler databaseHandler;

    public DatabaseHandler(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DatabaseHandler getDatabaseHandler(Context context){
        if(databaseHandler == null){
            databaseHandler = new DatabaseHandler(context);
        }
        return databaseHandler;
    }


    public Date getLast_time_activities_synced() {
        return last_time_activities_synced;
    }

    public void setLast_time_activities_synced(Date last_time_activities_synced) {
        this.last_time_activities_synced = last_time_activities_synced;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ACTIVITY_TABLE + "(" +
                        ACTIVITY_ID + " TEXT, " +
                        ACTIVITY_TITLE + " TEXT, " +
                        ACTIVITY_CATEGORY + " TEXT, " +
                        ACTIVITY_AUTHOR_ID + " TEXT, " +
                        ACTIVITY_DATE + " DATETIME, " +
                        ACTIVITY_PLACE + " TEXT, " +
                        ACTIVITY_PLACE_LAT_LNG + " TEXT" +
                        ACTIVITY_UPDATED_AT + " DATETIME)"
        );

        db.execSQL("CREATE TABLE " + COMMENTS_ACTIVITIES_TABLE + "(" +
                        COMMENTS_ACTIVITIES_ID + " INTEGER PRIMARY KEY, " +
                        COMMENTS_ACTIVITIES_ID_COMMENT + " TEXT, " +
                        COMMENTS_ACTIVITIES_ID_ACTIVITY + " TEXT)"
        );

        db.execSQL("CREATE TABLE " + COMMENTS_TABLE + "(" +
                        COMMENTS_ID + " TEXT, " +
                        COMMENTS_COMMENT + " TEXT, " +
                        COMMENTS_AUTHOR_ID + " TEXT, " +
                        COMMENTS_UPDATED_AT + " DATETIME)"
        );

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + ACTIVITY_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + COMMENTS_TABLE);

        // create new tables
        onCreate(db);
    }


    /*
 * Creating aN ACTIVITY
 */
    public long createActivity(Activity activity, ArrayList<Comment> comments) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ACTIVITY_ID, activity.getId());
        values.put(ACTIVITY_TITLE, activity.getTitle());
        values.put(ACTIVITY_CATEGORY, activity.getCategory());
        values.put(ACTIVITY_AUTHOR_ID, activity.getAuthor_id());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String date = (activity.getDate() == null)? "null" : sdf.format(activity.getDate());
        values.put(ACTIVITY_DATE, date);

        String place = (activity.getPlace() == null)? "null" : activity.getPlace();
        values.put(ACTIVITY_PLACE, place);

        String place_latlng = (activity.getPlace_LatLng() == null)? "null" : activity.getPlace_LatLng();
        values.put(ACTIVITY_PLACE_LAT_LNG, place_latlng);
        //values.put(ACTIVITY_UPDATED_AT, activity.getUpdated_at());

        // insert row

        long activity_id = db.insert(ACTIVITY_TABLE, null, values);
        Log.v("DATABASE HANDLER", "INserted Act id: " + String.valueOf(activity_id));

        // assigning tags to todo
        if(comments != null){
            for (Comment comment : comments) {
                createComment(comment, activity.getId());
            }
        }

        return activity_id;
    }

    public long createComment(Comment comment, String activity_id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COMMENTS_ID, comment.getId());
        values.put(COMMENTS_COMMENT, comment.getComment());
        values.put(COMMENTS_AUTHOR_ID, comment.getAuthor_id());
        createCommentActivity(comment.getId(), activity_id);


        long comment_id = db.insert(COMMENTS_TABLE, null, values);

        return comment_id;
    }

    public long createCommentActivity(String comment_id, String activity_id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COMMENTS_ACTIVITIES_ID_COMMENT, comment_id);
        values.put(COMMENTS_ACTIVITIES_ID_ACTIVITY, activity_id);

        long comment_act_id = db.insert(COMMENTS_ACTIVITIES_TABLE, null, values);

        return  comment_act_id;
    }


    //getAllContacts()
    // Getting All Contacts
    public ArrayList<Activity> getAllActivities() {
        ArrayList<Activity> activityList = new ArrayList<Activity>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + ACTIVITY_TABLE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Activity activity = new Activity();
                activity.setId(cursor.getString(cursor.getColumnIndex(ACTIVITY_ID)));
                activity.setTitle(cursor.getString(cursor.getColumnIndex(ACTIVITY_TITLE)));
                activity.setAuthor_id(cursor.getString(cursor.getColumnIndex(ACTIVITY_AUTHOR_ID)));
                activity.setCategory(cursor.getString(cursor.getColumnIndex(ACTIVITY_CATEGORY)));

                String date = cursor.getString(cursor.getColumnIndex(ACTIVITY_DATE));
                //Tenta pegar o date da activity. Se for null quer dizer que a activity nao teve data ao salvar
                //entao nao entra nesse IF e eh setada como null.
                //Se date nao for null, ela ira ser uma string que a gnt converte pra Date e faz activity.setDate()

                Date act_Date = null;
               // POOOORRRAAAAA, ISSO VEM UMA STRING "NULL" E NAO NULL OBJECT ENTAO SEMPRE TAVA ENTRANDO!!!!!
//                if(date != null){
                if(!date.equals("null")){
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                    try {
                        act_Date = sdf.parse(date);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }



                activity.setDate(act_Date);
                activity.setPlace(cursor.getString(cursor.getColumnIndex(ACTIVITY_PLACE)));
                // Adding contact to list
                Log.v("DATABASE HANDLER", activity.getTitle());
                activityList.add(activity);
            } while (cursor.moveToNext());
        }

        // return contact list
        return activityList;
    }

}
