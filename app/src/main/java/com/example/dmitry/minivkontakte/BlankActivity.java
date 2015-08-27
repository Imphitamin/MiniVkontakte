package com.example.dmitry.minivkontakte;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.widget.DrawableUtils;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Dmitry on 23.08.2015.
 */
public class BlankActivity extends ActionBarActivity {

    public static final String MESSAGE_DONE = "MessageScreen.MESSAGE_DONE";
    private static final int TAKE_PHOTO = 0;
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int UPLOAD_TASK = 2;

    private boolean isCamAvailable = true;

    private Uri fileUri;
    public static String pathToImage = null;
    BitmapDrawable newIcon = null;

    private static Handler uploadHandler;
    private String mCurrentPhotoPath = null;
    //private User mUser;

    private EditText mWrittenText;
    private ImageView mImage;
    String username, useravatar;
    long userid;
    int userCharId;


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
        userCharId = (int) userid;

        mImage = (ImageView)findViewById(R.id.imageOfMessage);
        mWrittenText = (EditText)findViewById(R.id.textOfMessage);

        if (useravatar == null) {
            ((ImageView) findViewById(R.id.userAvatar)).setImageDrawable(getResources().getDrawable(R.drawable.nothing));
        } else {
            new DownloadImageTask((ImageView) findViewById(R.id.userAvatar)).execute(useravatar);
        }

        ((TextView) findViewById(R.id.userName)).setText(username);
        setTitle(username);

        PackageManager pm = BlankActivity.this.getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) &&
                !pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            isCamAvailable = false;
        }

        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // если камеры нет, то выбрать из галереи
                if (isCamAvailable) {
                    showPopupMenu(v);
                } else try {
                    getFromGallery(v);
                } catch (Exception e) {
                }
            }
        });
    }

    private void getFromGallery(View v) {
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    // спрашиваем, что открыть камеру или галерею? (чтобы выбрать фото)
    private void showPopupMenu(final View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);

        // сокращенный вариант - только для android 4.0 и выше
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            popupMenu.inflate(R.menu.menu_source);
        } else {
            popupMenu.getMenuInflater().inflate(R.menu.menu_source, popupMenu.getMenu());
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_camera:
                        try {
                            // Намерение для запуска камеры

                            //ContentValues values = new ContentValues();
                            //values.put(MediaStore.Images.Media.TITLE, "photo_" + timeStamp + ".jpg");

                            Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            //fileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                            File f = new File("test");
                            photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                            startActivityForResult(photoIntent, TAKE_PHOTO);
                            return true;
                        } catch (ActivityNotFoundException e) {
                            // Выводим сообщение об ошибке
                            String errorMessage = "Ваше устройство не поддерживает съемку";
                            Toast toast = Toast
                                    .makeText(BlankActivity.this, errorMessage, Toast.LENGTH_LONG);
                            toast.show();
                        }
                        return true;
                    case R.id.menu_gallery:
                        Intent i = new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                        startActivityForResult(i, RESULT_LOAD_IMAGE);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    // берем фото из галереи
    /* private void getFromGallery(View v) throws Exception  {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_FILE);
    } */

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode == TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                pathToImage = fileUri.getPath();
                //newIcon = PictureUtils.getScaledDrawable(BlankActivity.this, pathToImage);
                //String filename = data.getStringExtra(MediaStore.EXTRA_OUTPUT);
                //pathToImage = BlankActivity.this.getFileStreamPath(filename).getAbsolutePath();
                newIcon = PictureUtils.getScaledDrawable(BlankActivity.this, pathToImage);
                mImage.setImageDrawable(newIcon);
            } else if (requestCode == RESULT_LOAD_IMAGE) {
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();

                    mImage.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                }
            }
        }
    }

    public static String uriToPath(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sendRequest != null) {
            sendRequest.cancel();
        }
    }

    private VKRequest sendRequest;
    private JSONArray uploadResponse = null;

    private String att;

    private String uploadAndSend(BitmapDrawable bmpd) {
        att = null;
        final Bitmap file = bmpd.getBitmap();

        uploadHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPLOAD_TASK:
                        BlankActivity.this.sendMessage(
                                String.valueOf(userCharId),
                                mWrittenText.getText().toString(),
                                att);
                        break;
                    default:
                        break;
                }
            }
        };

        Thread uploadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                VKRequest uploadRequest = VKApi.uploadMessagesPhotoRequest(new VKUploadImage(file, VKImageParameters.jpgImage(0.9f)));
                uploadRequest.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        //Log.i(TAG, "It's ok! " + response);

                        try {
                            uploadResponse = response.json.getJSONArray("response");
                            //Log.i(TAG, "uploadResponse: " + uploadResponse.toString());

                            //att = uploadResponse.getString(0);

                            JSONParser postResponseParser = new JSONParser();
                            JSONArray postObject = ((JSONArray) postResponseParser.parse(uploadResponse.toString()));
                            //Log.i(TAG, "postObject: " + postObject.toString());

                            String owner = postObject.toString().substring(221);
                            String owner_id = owner.substring(0, owner.indexOf(","));
                            String media = postObject.toString().substring(93);
                            String media_id = media.substring(0, media.indexOf(","));

                            att = "photo" + owner_id + "_" + media_id;

                            //Log.i(TAG, "att: " + att);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            //Log.i(TAG, "exc: " + e);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        uploadHandler.sendEmptyMessage(UPLOAD_TASK);;
                    }

                    @Override
                    public void onError(VKError error) {
                        super.onError(error);
                        //Log.i(TAG, "Error! " + error);
                        uploadHandler.sendEmptyMessage(UPLOAD_TASK);
                    }
                });

            }
        });
        uploadThread.start();

        return att;
    }

    private void sendMessage(String id, String message, String attachment) {
        if (sendRequest != null) {
            sendRequest.cancel();
        }

        VKParameters params;
        if (attachment == null) {
            params = VKParameters.from("user_id", id, "message", message);
        } else {
            //Log.i(TAG, "att: " + attachment);
            params = VKParameters.from("user_id", id, "message", message, "attachment", attachment);
        }

        sendRequest = new VKRequest("messages.send", params);
        sendRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                Log.d("VkDemoApp", "onComplete " + response);
            }
        });
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
                if (newIcon != null) {
                    uploadAndSend(newIcon);
                } else {
                    sendMessage(String.valueOf(userCharId), mWrittenText.getText().toString(), null);
                }

                Toast toast = Toast.makeText(getApplicationContext(), "Сообщение отправлено", Toast.LENGTH_LONG);
                toast.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
