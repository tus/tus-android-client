# tus-android-client

> **tus** is a protocol based on HTTP for *resumable file uploads*. Resumable
> means that an upload can be interrupted at any moment and can be resumed without
> re-uploading the previous data again. An interruption may happen willingly, if
> the user wants to pause, or by accident in case of an network issue or server
> outage.

**tus-android-client** is a library meant to be used in addition to [tus-java-client](https://github.com/tus/tus-java-client) for uploading files using the *tus* protocol to any remote server supporting it. This package provides additional classes which makes interacting with the Java library easier on Android.

## Usage

```java
// This example consumes the tus-java-client and tus-android-client libraries
import io.tus.android.client.TusPreferencesURLStore;
import io.tus.java.client.TusClient;
import io.tus.java.client.TusUpload;
import io.tus.java.client.TusUploader;

// Create a new TusClient instance
TusClient client = new TusClient();

// Configure tus HTTP endpoint. This URL will be used for creating new uploads
// using the Creation extension
client.setUploadCreationURL(new URL("http://tusd.tusdemo.net/files"));

// Enable resumable uploads by storing the upload URL in the preferences
// and preserve them after app restarts
SharedPreferences pref = getSharedPreferences("tus", 0);
client.enableResuming(new TusPreferencesURLStore(pref));

// Open a file using which we will then create a TusUpload. If you do not have
// a File object, you can manually construct a TusUpload using an InputStream.
// See the documentation for more information.
File file = new File("./cute_kitten.png");
final TusUpload upload = new TusUpload(file);

System.out.println("Starting upload...");

// We wrap our uploading code in the TusExecutor class which will automatically catch
// exceptions and issue retries with small delays between them and take fully
// advantage of tus' resumability to offer more reliability.
// This step is optional but highly recommended.
TusExecutor executor = new TusExecutor() {
    @Override
    protected void makeAttempt() throws ProtocolException, IOException {
        // First try to resume an upload. If that's not possible we will create a new
        // upload and get a TusUploader in return. This class is responsible for opening
        // a connection to the remote server and doing the uploading.
        TusUploader uploader = client.resumeOrCreateUpload(upload);

        // Upload the file in chunks of 1KB sizes.
        uploader.setChunkSize(1024);

        // Upload the file as long as data is available. Once the
        // file has been fully uploaded the method will return -1
        do {
            // Calculate the progress using the total size of the uploading file and
            // the current offset.
            long totalBytes = upload.getSize();
            long bytesUploaded = uploader.getOffset();
            double progress = (double) bytesUploaded / totalBytes * 100;

            System.out.printf("Upload at %06.2f%%.\n", progress);
        } while(uploader.uploadChunk() > -1);

        // Allow the HTTP connection to be closed and cleaned up
        uploader.finish();

        System.out.println("Upload finished.");
        System.out.format("Upload available at: %s", uploader.getUploadURL().toString());
    }
};
executor.makeAttempts();

```

The [example application](/example/src/main/java/io/tus/android/example/MainActivity.java) provides a more complete example in terms of interacting with the Android platform.

## Installation

The JARs can be downloaded manually from our [Maven Central](https://search.maven.org/artifact/io.tus.android.client/tus-android-client).

**Gradle:**

```groovy
compile 'io.tus.android.client:tus-android-client:0.1.10'
```

**Maven:**

```xml
<dependency>
  <groupId>io.tus.android.client</groupId>
  <artifactId>tus-android-client</artifactId>
  <version>0.1.10</version>
</dependency>
```

## Documentation

The documentation of the latest version (master branch of git repository) can be found online at [javadoc.io](https://javadoc.io/doc/io.tus.android.client/tus-android-client).

## FAQ

### I get TLS/SSL errors on older Android versions!

On devices running Android 4.4 earlier, errors can appear during the SSL handshake. This is caused by wrong behavior inside the platform and can be fixed by providing a custom `SSLSocketFactory` as described in https://stackoverflow.com/a/30302235 and https://github.com/tus/tus-java-client/blob/master/README.md#can-i-use-my-own-custom-sslsocketfactory. More details are also available at https://github.com/tus/tus-java-client/issues/14#issuecomment-402786097.

## License

MIT
