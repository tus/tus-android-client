package io.tus.android.client;

import android.app.Activity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class TusPreferencesURLStoreTest {

    @Test
    public void shouldSetGetAndDeleteURLs() throws Exception {
        Activity activity = Robolectric.setupActivity(Activity.class);
        TusPreferencesURLStore store = new TusPreferencesURLStore(activity.getSharedPreferences("tus-test", 0));
        System.out.println("hello");
        URL url = new URL("https://tusd.tusdemo.net/files/hello");
        String fingerprint = "foo";
        store.set(fingerprint, url);

        assertEquals(store.get(fingerprint), url);

        store.remove(fingerprint);

        assertEquals(store.get(fingerprint), null);
    }
}
