package com.example.securedatabasenkioskmode.securepreferences;

public interface EncryptedValueMigrator {

    void migrateValues(PrefValueEncrypter toEncrypter);
}
