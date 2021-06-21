package com.example.securedatabasenkioskmode.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;
import android.util.Log;

import com.example.securedatabasenkioskmode.MainApp;
import com.example.securedatabasenkioskmode.Secure.Cryptography;
import com.example.securedatabasenkioskmode.securepreferences.SecurePreferences;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class DBConnection {

    public static net.sqlcipher.database.SQLiteDatabase myDataBase;
    public static final String TAG = "DBConnection ";
    protected static final DBConnection instance = new DBConnection();
    char random[] = {10, 20, 30, 1, 1, 4, 5, 7, 3, 4, 2, 1};
    SecurePreferences secPrefs;

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static DBConnection get() {
        return instance;
    }

    private DBConnection() {

    }

    public boolean openDatabase(Context context) throws IOException {

        //Ensure migrations run first
        //AppDatabase roomDatabase = AppDatabase.getInstance(context, "fps.sqlite");

        Cryptography c = new Cryptography( context );
        String encryptedPassword;

        try {
            secPrefs = MainApp.get().getPasswordBasedPrefs();

            if (secPrefs == null) {
                deleteDB( context );
                return false;
            }

            //Timber.d( TAG + "CALLIPPUS1" + "Reached here" );

            if (secPrefs.getString( "passwd", "" ).equals( "" )) {
                DataBaseHelper dbHelper = DataBaseHelper.getInstance( context );
                deleteDB( context );

                String random = Base64.encodeToString( random().getBytes(), Base64.NO_WRAP );
                //Timber.d( TAG + " ==>> " + "This is the secret password:" + random );
                encryptedPassword = c.encryptData( random );
                dump( encryptedPassword );

                secPrefs.edit().putString( "passwd", encryptedPassword )
                        .commit();

                secPrefs.edit().putString( "default_pwd", "chattishgarhpds@12334" ).commit(); //This should be moved to a more secure location
                myDataBase = dbHelper.open( secPrefs.getString( "default_pwd", null ) );

                String decryptedPassword = c.decryptData( encryptedPassword );
                //Timber.d( TAG + " ==>> " + "This is the decrypted password:" + decryptedPassword );
                Log.d( TAG , " ==>> " + "This is the decrypted password:" + decryptedPassword );

                myDataBase.changePassword( decryptedPassword );
            }

            if (myDataBase == null || !myDataBase.isOpen()) {

                encryptedPassword = secPrefs.getString( "passwd", "" );
                dump( encryptedPassword );

                DataBaseHelper dbHelper = DataBaseHelper.getInstance( context );
                try {
                    String decryptedPassword = c.decryptData( encryptedPassword );
                    //Timber.d( TAG + " ==>> " + "This is the decrypted password:" + decryptedPassword );
                    myDataBase = dbHelper.open( decryptedPassword );

                    /*if (!secPrefs.getString("old_password", "").equals("")) {
                        restoreDatabase(context);
                    }*/
                } catch (net.sqlcipher.database.SQLiteException ex) {
                    deleteDB( context );
                }
                return true;
            }
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            deleteDB( context );
            return false;
        }

        return true;
    }

    public void dump(String string) {
        //Timber.d( TAG + " ==>> " + Base64.encodeToString( string.getBytes(), Base64.NO_WRAP ) );
    }

    public void deleteDB(Context context) {
        //Timber.d( TAG + " ==>> " + "Deleted database successfully" );
        secPrefs.edit().putString( "passwd", "" ).commit();
        try {
            if (myDataBase != null)
                myDataBase.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        DataBaseHelper dbHelper = DataBaseHelper.getInstance( context );
        dbHelper.db_delete();
        myDataBase = null;
    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt( 25 );
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (generator.nextInt( 96 ) + 32);
            randomStringBuilder.append( tempChar );
        }
        return randomStringBuilder.toString();
    }

    public static ArrayList<String> getAllAliasesInTheKeystore(KeyStore keyStore) throws KeyStoreException {

        return Collections.list( keyStore.aliases() );
    }

    // Abstract method
    public void onCreate(SQLiteDatabase db) {

        // TODO Auto-generated method stub

    }

    // Abstact Method
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }

    // Close DB Connection
    public synchronized void close() {
        if (myDataBase != null) {
            myDataBase.close();
        }
    }
}
