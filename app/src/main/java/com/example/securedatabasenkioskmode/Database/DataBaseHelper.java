package com.example.securedatabasenkioskmode.Database;

import android.content.Context;
import android.util.Log;

import net.sqlcipher.SQLException;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;
import net.sqlcipher.database.SQLiteException;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "chattishgarh.db";// Encrypted Database
    private static final String SUB_DATABASE_FOLDER = "/databases/";// a sub folder for database location
    public static String DATABASE_PATH;
    public static final int DATABASE_VERSION = 1;
    private SQLiteDatabase myDataBase;
    private final Context context;
    private String password = "";
    private String FULL_DB_Path;
    private static DataBaseHelper INSTANCE;
    private static final Object sLock = new Object();
    private static String TAG="DataBaseHelper";

    public static DataBaseHelper getInstance(Context context) {
        synchronized (sLock) {

            if (INSTANCE == null) {
                INSTANCE = new DataBaseHelper(context);
            }
            return INSTANCE;
        }
    }

    public DataBaseHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        DATABASE_PATH = context.getDatabasePath(DATABASE_NAME).getParent();//get the device root Directory to copy data base on it

        this.context = context;
        SQLiteDatabase.loadLibs(context.getApplicationContext());//load SqlCipher libraries

        FULL_DB_Path = context.getDatabasePath(DATABASE_NAME).getPath();//full database path
    }

    public SQLiteDatabase open(String password) {
        this.password = password;

        if (!checkDataBase()) {// if Database Not Exist
            copyDataBase();
        }

        myDataBase = getExistDataBaseFile();

        return myDataBase;
    }

    private SQLiteDatabase getExistDataBaseFile() {// this function to open an Exist database

        SQLiteDatabaseHook hook = new SQLiteDatabaseHook() {
            public void preKey(SQLiteDatabase database) {
            }

            public void postKey(SQLiteDatabase database) {
                database.rawExecSQL("PRAGMA cipher_migrate;");

            }
        };
        return SQLiteDatabase.openOrCreateDatabase(FULL_DB_Path, password,
                null, hook);

    }


    private boolean checkDataBase() {// Check database file is already exist or not
        boolean checkDB = false;
        try {
            File dbfile = new File(FULL_DB_Path);
            checkDB = dbfile.exists();
            Log.d(TAG ," ==>> "+"Database file exists?"+checkDB);

        } catch (SQLiteException e) {
        }
        return checkDB;
    }


    public void db_delete() {// delete database
        File file = new File(FULL_DB_Path);
        if (file.exists()) {
            file.delete();
            System.out.println("delete database file.");
        }

        File fileshm = new File(FULL_DB_Path + "-shm");
        if (fileshm.exists()) {
            fileshm.delete();
            System.out.println("delete database shm file.");
        }

        File filewal = new File(FULL_DB_Path + "-wal");
        if (filewal.exists()) {
            filewal.delete();
            System.out.println("delete database wal file.");
        }


    }

    private void copyDataBase() {//make a sub folder for database location and copy the database
        try {
            File fofo = new File(DATABASE_PATH);
            fofo.mkdirs();
            extractAssetToDatabaseDirectory(DATABASE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,e.getLocalizedMessage());
        }

    }

    public synchronized void closeDataBase() throws SQLException {
        if (myDataBase != null)
            myDataBase.close();
        super.close();
    }

    public void extractAssetToDatabaseDirectory(String fileName)
            throws IOException {// copy the database

        int length;
        InputStream sourceDatabase = context.getAssets().open(fileName);
        File destinationPath = new File(FULL_DB_Path);
        OutputStream destination = new FileOutputStream(destinationPath);

        byte[] buffer = new byte[4096];
        while ((length = sourceDatabase.read(buffer)) > 0) {
            destination.write(buffer, 0, length);
        }
        sourceDatabase.close();
        destination.flush();
        destination.close();
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void onCreate(SQLiteDatabase db) {
    }

    public boolean changePassword(String newPassword) {// DataBase must be
        // opened before
        // changing Password

        try {
            if (myDataBase != null && myDataBase.isOpen()) {

                myDataBase.rawExecSQL("BEGIN IMMEDIATE TRANSACTION;");
                myDataBase.rawExecSQL("PRAGMA rekey = '" + newPassword + "';");

                this.close();
                myDataBase.close();

                return true;

            } else {

                Log.e("DatabaseHelper",
                        "Change Password Error : DataBase is null or not opened  !!");
                return false;
            }
        } catch (Exception e) {

            Log.e("DatabaseHelper",
                    "Change Password Error :ExecSQL Error !!");
            return false;

        }
    }

}
