package com.prj.app;

import android.content.Context;
import android.content.SharedPreferences;

import com.yakivmospan.scytale.Crypto;
import com.yakivmospan.scytale.Options;
import com.yakivmospan.scytale.Store;

import java.util.UUID;

import javax.crypto.SecretKey;

public class CryptoManager {

    private static final String PREFERENCE_NAME = "SQLITE_PASSWORD";
    private static final String STORE_ALIAS = "SQLITE_KEY";

    /**
     * Generate a new key for SQLite encryption if it doesn't exit. Key is unique per app install and
     * all data is lost when key is deleted
     *
     * @param context the context that will store the encrypted key
     */
    public static void generateDatabasePassword(Context context) {
        Store store = new Store(context);
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (!store.hasKey("SQLITE") || sharedPref.getString(PREFERENCE_NAME, null) == null) {
            SecretKey key = store.generateSymmetricKey(STORE_ALIAS, null);
            Crypto crypto = new Crypto(Options.TRANSFORMATION_SYMMETRIC);

            //Generate a one time key
            String text = UUID.randomUUID().toString();
            String encryptedData = crypto.encrypt(text, key);
            editor.putString(PREFERENCE_NAME, encryptedData);
            editor.apply();
        }
    }

    /**
     * Get the encrypted SQLite key and decrypt it using the keystore.
     *
     * @param context the context that stored the encrypted key
     * @return a string containing the password for the database
     */
    public static String getDatabasePassword(Context context) {
        Store store = new Store(context);
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        SecretKey key = store.getSymmetricKey(STORE_ALIAS, null);

        Crypto crypto = new Crypto(Options.TRANSFORMATION_SYMMETRIC);
        String encryptedKey = sharedPref.getString(PREFERENCE_NAME, null);
        if (encryptedKey == null) {
            return null;
        }

        return crypto.decrypt(encryptedKey, key);
    }
}
