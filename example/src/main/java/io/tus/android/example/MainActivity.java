package io.tus.android.example;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.URL;

import io.tus.android.client.TusAndroidUpload;
import io.tus.android.client.TusPreferencesURLStore;
import io.tus.java.client.TusClient;
import io.tus.java.client.TusUpload;
import io.tus.java.client.TusUploader;


public class MainActivity extends ActionBarActivity {
    private final int REQUEST_FILE_SELECT = 1;
    private TusClient client;
    private TextView status;
    private Button abortButton;
    private UploadTask uploadTask;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            SharedPreferences pref = getSharedPreferences("tus", 0);
            client = new TusClient();
            client.setUploadCreationURL(new URL("http://master.tus.io:8080/files/"));
            client.enableResuming(new TusPreferencesURLStore(pref));
        } catch(Exception e) {
            showError(e);
        }

        status = (TextView) findViewById(R.id.status);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("*/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select file to upload"), REQUEST_FILE_SELECT);

            }
        });

        abortButton = (Button) findViewById(R.id.abort_button);
        abortButton.setOnClickListener(new AbortButtonListener(this));
    }

    private void beginUpload(Uri uri) {
        try {
            TusUpload upload = new TusAndroidUpload(uri, this);
            uploadTask = new UploadTask(this, client, upload);
            uploadTask.execute(new Void[0]);
        } catch(Exception e) {
            showError(e);
        }

    }

    public void setAbortButtonEnabled(boolean enabled) {
        abortButton.setEnabled(enabled);
    }

    public void abortUpload() {
        uploadTask.cancel(false);
    }

    private void setStatus(String text) {
        status.setText(text);
    }

    private void setUploadProgress(int progress) {
        progressBar.setProgress(progress);
    }

    private class UploadTask extends AsyncTask<Void, Long, Void> {
        private MainActivity activity;
        private TusClient client;
        private TusUpload upload;
        private Exception exception;

        public UploadTask(MainActivity activity, TusClient client, TusUpload upload) {
            this.activity = activity;
            this.client = client;
            this.upload = upload;
        }

        @Override
        protected void onPreExecute() {
            activity.setStatus("Upload selected...");
            activity.setAbortButtonEnabled(true);
        }

        @Override
        protected void onPostExecute(Void result) {
            activity.setStatus("Upload finished!");
            activity.setAbortButtonEnabled(false);
        }

        @Override
        protected void onCancelled() {
            if(exception != null) {
                activity.showError(exception);
            }

            activity.setAbortButtonEnabled(false);
        }

        @Override
        protected void onProgressUpdate(Long... updates) {
            long uploadedBytes = updates[0];
            long totalBytes = updates[1];
            activity.setStatus(String.format("Uploaded %d/%d.", uploadedBytes, totalBytes));
            activity.setUploadProgress((int) ((double) uploadedBytes / totalBytes * 100));
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                TusUploader uploader = client.resumeOrCreateUpload(upload);
                long totalBytes = upload.getSize();
                long uploadedBytes = uploader.getOffset();

                while(!isCancelled() && uploader.uploadChunk(1024) > 0) {
                    uploadedBytes = uploader.getOffset();
                    publishProgress(uploadedBytes, totalBytes);
                    Thread.sleep(100);
                }

                uploader.finish();

            } catch(Exception e) {
                exception = e;
                cancel(true);
            }
            return null;
        }
    }

    private class AbortButtonListener implements View.OnClickListener {
        private MainActivity activity;

        public AbortButtonListener(MainActivity activity) {
            this.activity = activity;
        }
        @Override
        public void onClick(View view) {
            activity.abortUpload();
        }
    }

    private void showError(Exception e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Internal error");
        builder.setMessage(e.getMessage());
        AlertDialog dialog = builder.create();
        dialog.show();
        e.printStackTrace();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK) {
            return;
        }

        if(requestCode == REQUEST_FILE_SELECT) {
            Uri uri = data.getData();
            beginUpload(uri);
        }
    }
}
