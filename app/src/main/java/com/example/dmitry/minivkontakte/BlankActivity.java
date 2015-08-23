package com.example.dmitry.minivkontakte;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;

/**
 * Created by Dmitry on 23.08.2015.
 */
public class BlankActivity extends ActionBarActivity {

    public static final String MESSAGE_DONE = "MessageScreen.MESSAGE_DONE";
    private static final int REQUEST_PHOTO = 0;
    private static final int REQUEST_FILE = 1;
    private static final int UPLOAD_TASK = 2;

    private boolean isCamAvailable = true;

    private BitmapDrawable newIcon = null;
    private static Handler uploadHandler;

    private EditText mWrittenText;
    private ImageView mImage;
    String username, useravatar;
    long userid;

    // загружаем аватарки фоново
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message);

        Bundle bundle = getIntent().getExtras();
        if(getIntent().getStringExtra("username") != null && (getIntent().getStringExtra("useravatar") != null && (getIntent().getIntExtra("userid", 0) != 0))) {
            username = bundle.getString("username");
            useravatar = bundle.getString("useravatar");
            userid = bundle.getInt("userid");
        }
        Toast.makeText(getApplicationContext(), "User's Id = " + userid, Toast.LENGTH_LONG).show();

        mImage = (ImageView)findViewById(R.id.imageAtt);
        mWrittenText = (EditText)findViewById(R.id.textOfMessage);

        if (useravatar == null) {
            ((ImageView) findViewById(R.id.userAvatar)).setImageDrawable(getResources().getDrawable(R.drawable.nothing));
        } else {
            new DownloadImageTask((ImageView) findViewById(R.id.userAvatar)).execute(useravatar);
        }

        ((TextView) findViewById(R.id.userName)).setText(username);
        setTitle(username);
    }

}
