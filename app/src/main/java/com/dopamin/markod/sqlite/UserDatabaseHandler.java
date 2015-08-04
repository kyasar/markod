package com.dopamin.markod.sqlite;

/**
 * Created by kadir on 06.07.2015.
 */
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.dopamin.markod.MainActivity;
import com.dopamin.markod.objects.User;

public class UserDatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 3;

    // Database Name
    private static final String DATABASE_NAME = "userDB";

    // Contacts table name
    private static final String TABLE_USERS = "users";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_LOGIN_TYPE = "login_type";
    private static final String KEY_SOCIAL_ID = "social_id";
    private static final String KEY_POINTS = "points";

    public UserDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_ID + " TEXT,"
                + KEY_FIRST_NAME + " TEXT," + KEY_LAST_NAME + " TEXT,"
                + KEY_EMAIL + " TEXT," + KEY_LOGIN_TYPE + " TEXT," + KEY_SOCIAL_ID + " TEXT,"
                + KEY_POINTS + " TEXT" + ");";
        db.execSQL(CREATE_USERS_TABLE);
        Log.v(MainActivity.TAG, "Users table created.");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        Log.v(MainActivity.TAG, "Users table dropped.");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new contact
    public void addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //values.put(KEY_ID, user.getId());
        values.put(KEY_FIRST_NAME, user.getFirstName());
        values.put(KEY_LAST_NAME, user.getLastName());
        values.put(KEY_EMAIL, user.getEmail());
        //values.put(KEY_LOGIN_TYPE, user.getUserLoginType().toString()); // be careful, string casting
        values.put(KEY_SOCIAL_ID, user.getSocial_id());
        values.put(KEY_POINTS, user.getPoints());

        // Inserting Row
        db.insert(TABLE_USERS, null, values);
        db.close(); // Closing database connection
    }

    // Getting single contact
    public User getUser() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USERS, null /* fetch all columns */, null /* fetch all rows */,
                null, null, null, null);
        Log.v(MainActivity.TAG, "cursor count: " + cursor.getCount());
        if (cursor == null || cursor.getCount() == 0) {
            Log.v(MainActivity.TAG, "No user available in DB");
            return null;
        }

        cursor.moveToFirst();
        Log.v(MainActivity.TAG, cursor.getString(0) + " " + cursor.getString(1) + " " + cursor.getString(2) + " " +
                cursor.getString(3) + " " + cursor.getString(4) + " " + cursor.getString(5) + " " + cursor.getString(6));

        return new User(cursor.getString(0), cursor.getString(1), cursor.getString(2),
                cursor.getString(3), cursor.getString(4),
                cursor.getString(5), Integer.parseInt(cursor.getString(6)));
    }
    /*
    // Updating single contact
    public int updateUser(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getName());
        values.put(KEY_PH_NO, contact.getPhoneNumber());

        // updating row
        return db.update(TABLE_CONTACTS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(contact.getID()) });
    }

    // Deleting single contact
    public void deleteUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
                new String[] { String.valueOf(contact.getID()) });
        db.close();
    }


    // Getting contacts Count
    public int getContactsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    } */

}