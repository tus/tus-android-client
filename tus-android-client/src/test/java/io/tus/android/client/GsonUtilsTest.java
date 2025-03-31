package io.tus.android.client;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class GsonUtilsTest {

    @Test
    public void preservesMapsOfStrings() {
        Map<String, String> myMap = new HashMap<>();
        myMap.put("foo", "bar");
        myMap.put("my_url", "http://something.com?foo=bar");

        String marshalled = GsonUtils.GsonMarshaller.getInstance().marshal(myMap);
        Map result = GsonUtils.GsonUnmarshaller.create(Map.class).unmarshal(marshalled);
        assertThat(result.size(), is(2));
        assertThat(result.get("foo"), is("bar"));
        assertThat(result.get("my_url"), is("http://something.com?foo=bar"));
    }
}