package com.example.dmitry.minivkontakte;

/**
 * Created by Dmitry on 21.08.2015.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKOpenAuthActivity;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKUsersArray;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by Dmitry on 29.01.2015. ha-ha
 */

public class MessageScreen extends Activity {

    final String TAG = "myLogs";

    public static final String MESSAGE_DONE = "MessageScreen.MESSAGE_DONE";

    public static Long useridL = null;

    public static String messageItself;
    public static String pathToImage = null;

    File directory;
    //final int TYPE_PHOTO = 1;
    final int REQUEST_CODE_PHOTO = 1;
    Uri fileUri;

    EditText writtenText;
    ImageView daImageE;

    public static long yourId;

    MainActivity ss;
    String username, useravatar;
    int userid;

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

        //yourId = Long.parseLong(VKAccessToken.USER_ID);
        Toast.makeText(getApplicationContext(), "userid = " + yourId, Toast.LENGTH_LONG).show();

        createDirectory();
        daImageE = (ImageView)findViewById(R.id.daImage);
        writtenText = (EditText)findViewById(R.id.textOfMessage);

        if (useravatar == null) {
            ((ImageView) findViewById(R.id.userAvatar)).setImageDrawable(getResources().getDrawable(R.drawable.nothing));
        } else {
            new DownloadImageTask((ImageView) findViewById(R.id.userAvatar)).execute(useravatar);
        }

        ((TextView) findViewById(R.id.userName)).setText(username);
        setTitle(username);

        //writtenText = (EditText)findViewById(R.id.textOfMessage);
        //messageItself = writtenText.getText().toString();

        useridL = Long.valueOf(userid);
        //yourId = Long.parseLong(VKOpenAuthActivity.VK_EXTRA_CLIENT_ID);
        // yourIdL = Long.valueOf(yourId);
    }

    public void onClickPhoto(View view) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "IMG_" + timeStamp + ".jpg");

        Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        //photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, generateFileUri(TYPE_PHOTO));
        photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(photoIntent, REQUEST_CODE_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode == REQUEST_CODE_PHOTO) {
            if(resultCode == RESULT_OK) {
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            /*Log.d(TAG, "Photo uri: " + intent.getData());
                    Bundle bnd1 = intent.getExtras();
                    if(bnd1 != null) {
                        Object obj = intent.getExtras().get("data");
                        if (obj instanceof Bitmap) {
                            Bitmap bitmap = (Bitmap) obj;
                            Log.d(TAG, "bitmap " + bitmap.getWidth() + " x " + bitmap.getHeight()); */
                daImageE.setImageBitmap(bitmap);
                pathToImage = fileUri.getPath();

                /*imageAttachment.setImageURI(fileUri);
                            pathToImage = fileUri.getPath();*/
            } else if(resultCode == RESULT_CANCELED) {
                Log.d(TAG, "canceled");
                pathToImage = null;
                Toast.makeText(getApplicationContext(), "Отменено", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Ошибка", Toast.LENGTH_SHORT).show();
            }
        }
    }




    /*private Uri generateFileUri(int type) {
        File file = null;
        switch (type) {
            case TYPE_PHOTO:
                file = new File(directory.getPath() + "/" + "photo_"
                        + System.currentTimeMillis() + ".jpg");
                break;
        }
        Log.d(TAG, "fileName = " + file);
        return Uri.fromFile(file);
    } */

    private void createDirectory() {
        directory = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyFolder");
        if (!directory.exists())
            directory.mkdirs();
    }

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

    // вызывается при создании меню/action bar'а и подгружает xml-разметку
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_screen, menu);
        MenuItem itemOk = menu.findItem(R.id.ok_button_menu);
        itemOk.setVisible(true);
        return super.onCreateOptionsMenu(menu);
    }

    // обработчик нажатия на пункт из action bar'а
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.ok_button_menu:
                messageItself = writtenText.getText().toString();
                String theImage = pathToImage;
                Toast.makeText(getApplicationContext(), "text = " + messageItself, Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), "image = " + theImage, Toast.LENGTH_LONG).show();

                //if(imagePath == null)  // TODO  HERE!!!!!!!!!!!!!!!!!!!!!!!!!!!
                /*if (writtenText == null && imageItself != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Ошибка")
                            .setMessage("Напишите хотя бы пару строчек текста!")
                            .setCancelable(false)
                            .setNegativeButton("ОК",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else if (writtenText != null && imageItself == null) {
                    try {
                        api.sendMessage(useridL, 0L, messageItself, null, null, null, null, null, null, null, null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (KException e) {
                        e.printStackTrace();
                    }
                } else if (writtenText != null && imageItself != null) {

                    //api.sendMessage(useridL, 0L, messageItself, null, null, null, null, null, null, null, null);
                    long photoPid = uploadPhotoToWall(imageItself, 25735573).pid;

                    Collection<String> attachments = new ArrayList<String>();
                    String att = "photo" + 25735573 + "_" + photoPid;
                    attachments.add(att);
                    try {
                        api.sendMessage(useridL, 0L, messageItself, null, null, attachments, null, null, null, null, null);
                        //api.createWallPost(account.user_id, "Test Message", attachments, null, false, false, false, null, null, null, null, null, null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (KException e) {
                        e.printStackTrace();
                    }
                    Intent answerIntent = new Intent();
                    answerIntent.putExtra(MESSAGE_DONE, username + " - сообщение отправлено");
                    setResult(RESULT_OK, answerIntent);
                    finish();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Внимание")
                            .setMessage("Невозможно отправить пустое сообщение!")
                            .setCancelable(false)
                            .setNegativeButton("ОК",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }*/
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

}
