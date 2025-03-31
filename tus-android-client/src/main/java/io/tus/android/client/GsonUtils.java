package io.tus.android.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/*package*/ final class GsonUtils {

    private static final Gson GSON = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .create();

    public static final class GsonMarshaller {
        private static final GsonMarshaller INSTANCE = new GsonMarshaller();

        public static GsonMarshaller getInstance() {
            return INSTANCE;
        }

        public String marshal(Object value) {
            return GSON.toJson(value);
        }

        private GsonMarshaller() {}
    }

    public static class GsonUnmarshaller<T> {
        private final Class<T> mClassOfT;

        public GsonUnmarshaller(Class<T> classOfT) {
            mClassOfT = classOfT;
        }

        public static <T> GsonUnmarshaller<T> create(Class<T> classOfT) {
            return new GsonUnmarshaller<T>(classOfT);
        }

        public final T unmarshal(String input) {
            return GSON.fromJson(input, mClassOfT);
        }
    }

    private GsonUtils() {}
}
