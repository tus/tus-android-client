package io.tus.android.client;


import static io.tus.android.client.TusAndroidClient.TUS_SHARED_PREFS_NAME;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.gson.JsonSyntaxException;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Used to persist information about a job across app launches, also provides a way for workers to share additional data (such as most recent failure details).
 */
/*package*/ class BetweenWorkDataStore {
    private final SharedPreferences mSharedPrefs;

    private static BetweenWorkDataStore sInstance;

    private BetweenWorkDataStore(@NonNull Context context) {
        mSharedPrefs = context.getSharedPreferences(TUS_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    public static BetweenWorkDataStore get(@NonNull Context context) {
        Objects.requireNonNull(context);
        if (sInstance == null) {
            sInstance = new BetweenWorkDataStore(context);
        }
        return sInstance;
    }

    public void storeData(@NonNull String key, @NonNull Map<String, String> data) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(data);
        mSharedPrefs.edit().putString(key, GsonUtils.GsonMarshaller.getInstance().marshal(data)).apply();
    }

    public Map<String, String> getData(@NonNull String key) {
        String stringifiedData = mSharedPrefs.getString(key, null);
        if (stringifiedData == null) {
            // bad state: the id wasn't stored
            return null;
        }
        Map<String, String> data;
        try {
            data = GsonUtils.GsonUnmarshaller.create(Map.class).unmarshal(stringifiedData);
        } catch (JsonSyntaxException e) {
            // bad state: malformed stuff stored with this id
            return null;
        }
        return data;
    }

    public void clearData(@NonNull String key) {
        Objects.requireNonNull(key);
        clearData(Collections.singleton(key));
    }

    public void clearData(@NonNull Collection<String> keys) {
        Objects.requireNonNull(keys);
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        for (String key : keys) {
            editor.remove(key);
        }
        editor.apply();
    }
}
