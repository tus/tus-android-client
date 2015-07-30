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
client.setUploadCreationURL(new URL("http://master.tus.io/files"));

// Enable resumable uploads by storing the upload URL in the preferences
// and preserve them after app restarts
SharedPreferences pref = getSharedPreferences("tus", 0);
client.enableResuming(new TusPreferencesURLStore(pref));

// Open a file using which we will then create a TusUpload. If you do not have
// a File object, you can manually construct a TusUpload using an InputStream.
// See the documentation for more information.
File file = new File("./cute_kitten.png");
TusUpload upload = new TusUpload(file);

// First try to resume an upload. If that's not possible we will create a new
// upload and get a TusUploader in return. This class is responsible for opening
// a connection to the remote server and doing the uploading.
TusUploader uploader = client.resumeOrCreateUpload(upload);

// Upload the file in chunks of 1KB as long as data is available. Once the
// file has been fully uploaded the method will return -1
while(uploader.uploadChunk(1024 * 1024) > -1) {
  // Calculate the progress using the total size of the uploading file and
  // the current offset.
  long totalBytes = upload.getSize();
  long bytesUploaded = uploader.getOffset();
  double progress = (double) bytesUploaded / totalBytes * 100;
}

// Allow the HTTP connection to be closed and cleaned up
uploader.finish();

```

The [example application](/example/src/main/java/io/tus/android/example/MainActivity.java) provides a more complete example in terms of interacting with the Android platform.

## Installation

The JARs can be downloaded manually from our [Bintray project](https://bintray.com/tus/maven/tus-android-client/view#files). tus-java-client is also available in JCenter (Maven Central is coming soon).

**Gradle:**

```groovy
compile 'io.tus.android.client:tus-android-client:0.1.0'
```

**Maven:**

```xml
<dependency>
  <groupId>io.tus.android.client</groupId>
  <artifactId>tus-android-client</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Documentation

The documentation of the latest version (master branch of git repository) can be found online at [tus.github.io/tus-android-client/javadoc/](https://tus.github.io/tus-android-client/javadoc/).

## License

MIT
