package com.example.securedatabasenkioskmode;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import com.example.securedatabasenkioskmode.Database.DBConnection;
import com.example.securedatabasenkioskmode.securepreferences.AesCbcWithIntegrityPrefValueEncrypter;
import com.example.securedatabasenkioskmode.securepreferences.SecurePreferenceCreator;
import com.example.securedatabasenkioskmode.securepreferences.SecurePreferences;
import com.example.securedatabasenkioskmode.securepreferences.Utils;

import java.security.GeneralSecurityException;
import java.util.Locale;

public class MainApp extends Application {

    private static final String TAG = "secureprefsample";
    protected static MainApp instance;
    public static String APP_PATH;
    public static String DB_PATH;

    private SecurePreferences securePrefs;
    private SecurePreferences securePrefsFromPassword;

    public MainApp() {
        super();
        instance = this;
    }

    public static MainApp get() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DBConnection db = null;

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        Configuration config = getBaseContext().getResources().getConfiguration();

        try {
//            Cryptography c = new Cryptography(this.getApplicationContext());
//            String encryptedPassword = c.encryptData("dTYhZHZnPzJgdzs=");
//            SecurePreferences secPrefs = MainApp.get().getPasswordBasedPrefs();
//            secPrefs.edit().putString("passwd", encryptedPassword)
//                    .commit();

            //Ensure migrations run first
//            SecurePreferences secPrefs = MainApp.get().getPasswordBasedPrefs();
//            if(!secPrefs.getString("passwd", "").equals("")){
//                AppDatabase appDatabase = AppDatabase.getInstance(this.getApplicationContext(), "fps.sqlite");
//                appDatabase.cashAcknowledgmentDao().getAllCashAcknowledgment();
//            }
            /*AppDatabase appDatabase = AppDatabase.getInstance(this.getApplicationContext(), "fps.sqlite");
            appDatabase.cashAcknowledgmentDao().getAllCashAcknowledgment();*/
            /*db = DBConnection.get();
            if(!db.openDatabase(this.getApplicationContext()))
            {
                Log.d("MAINAPP", "Unable to open database");
                return;
            }*/
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            return;
        }

        APP_PATH = this.getApplicationContext().getApplicationInfo().dataDir;
        DB_PATH = this.getApplicationContext().getDatabasePath("chattishgarhPDS.sqlite").getPath();

        String lang = settings.getString("language", "");
        if (!"".equals(lang) && !config.locale.getLanguage().equals(lang)) {
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
    }


    @Override
    public void onTerminate()
    {
        super.onTerminate();
    }


    public void initPasswordBasedSecurePrefs(String password, String deviceSerialNumber) throws GeneralSecurityException {
        securePrefsFromPassword = SecurePreferenceCreator.createPasswordBasedSecurePreferences(
                this, password, deviceSerialNumber.getBytes(), SecurePreferenceCreator.ITERATION_COUNT_STRONGER_SLOWER, "user_prefs.xml");
    }

    public SecurePreferences getPasswordBasedPrefs() {
        char random[]={10,20,30,1,1,4,5,7,3,4,2,1};
        try {
            if (securePrefsFromPassword == null)
                initPasswordBasedSecurePrefs(Util.genRandom(random), Utils.getDeviceSerialNumber(this.getApplicationContext()));
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
        return securePrefsFromPassword;
    }


    private void initSecurePrefs() {
        securePrefs = SecurePreferenceCreator.createQuickAesSecurePreferences(this, "my_secure_prefs.xml");
    }

    /**
     * Single point for the app to get the secure prefs object
     *
     * @return
     */
    public SecurePreferences getSecurePreferences() {
        return securePrefs;
    }


    public SharedPreferences getDefaultNotSecureSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    /**
     * example of changing the password used for the
     *
     * @param newPassword
     * @param salt        used with password to derive a key
     * @throws GeneralSecurityException
     */

    public void changeUserPrefPassword(String newPassword, String salt) throws GeneralSecurityException {
        if (securePrefsFromPassword != null) {
            AesCbcWithIntegrityPrefValueEncrypter aesCbcWithIntegrityPrefValueEncrypter
                    = AesCbcWithIntegrityPrefValueEncrypter.builder()
                    .withPasswordSaltAndIterationsToGenerateKey(newPassword,
                            salt.getBytes(),
                            SecurePreferenceCreator.ITERATION_COUNT_QUICK_LESS_SECURE
                    ).build();

            securePrefsFromPassword.migrateValues(aesCbcWithIntegrityPrefValueEncrypter);
        } else {
            throw new IllegalStateException("securePrefsFromPassword has not been initialised.");
        }
    }
}

