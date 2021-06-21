package com.example.securedatabasenkioskmode.securepreferences;

import android.util.Base64;
import android.util.Log;

import com.tozny.crypto.android.AesCbcWithIntegrity;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashBasedPrefKeyObfuscator implements PrefKeyObfuscator {

    private static final String TAG = "hashprefkeyObs";

    private MessageDigest digest;
    private static final String HASH_ALG = "SHA-256";

    public HashBasedPrefKeyObfuscator() {
        try {
            digest = MessageDigest.getInstance(HASH_ALG);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, HASH_ALG + "not supported:" + e.getMessage(), e);
        }
    }

    @Override
    public String obfuscate(String keyname) {
        try {
            byte[] bytes = keyname.getBytes("UTF-8");
            digest.reset();
            digest.update(bytes, 0, bytes.length);
            byte[] hashedKeyName = digest.digest();
            return Base64.encodeToString(hashedKeyName, AesCbcWithIntegrity.BASE64_FLAGS);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UTF-8 not supported:" + e.getMessage(), e);
        }
        return keyname;
    }
}
