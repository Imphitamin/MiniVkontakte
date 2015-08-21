package com.example.dmitry.minivkontakte;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKUsersArray;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dmitry on 21.08.2015.
 */

public class MainActivity extends ListActivity {

    private final static int MESSAGE_SENDED = 1;
    private VKRequest currentRequest;

    private final List<User> users = new ArrayList<User>();
    private ArrayAdapter<User> listAdapter;

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
        setContentView(R.layout.main);

        listAdapter = new ArrayAdapter<User>(this, R.layout.custom_list_activity_view, R.id.textFriend1, users) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View view = super.getView(position, convertView, parent);

                final User user = getItem(position);

                ((TextView) view.findViewById(R.id.textFriend1)).setText(user.getName());

                String userAvatar = user.getAvatar();
                if (userAvatar == null) {
                    ((ImageView) view.findViewById(R.id.avatar)).setImageDrawable(getResources().getDrawable(R.drawable.nothing));
                } else {
                    new DownloadImageTask((ImageView) view.findViewById(R.id.avatar)).execute(userAvatar);
                }

                return view;
            }
        };
        //lvSimple = (ListView)findViewById(android.R.id.list);
        // lvSimple.setAdapter(listAdapter);
        setListAdapter(listAdapter);
        startLoading();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        User user = listAdapter.getItem(position);
        String username = user.getName();
        String useravatar = user.getAvatar();
        int userid = user.getUid();

        Intent messageIntent = new Intent(this, MessageScreen.class);
        messageIntent.putExtra("username", username);
        messageIntent.putExtra("useravatar", useravatar);
        messageIntent.putExtra("userid", userid);
        this.startActivityForResult(messageIntent, MESSAGE_SENDED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == MESSAGE_SENDED) {
            if(resultCode == RESULT_OK) {
                String infoMessage = data.getStringExtra(MessageScreen.MESSAGE_DONE);
                Toast.makeText(getApplicationContext(), infoMessage, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Отменено", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLoading() {
        if (currentRequest != null) {
            currentRequest.cancel();
        }
        currentRequest = VKApi.friends().get(VKParameters.from(VKApiConst.FIELDS, "id,first_name,last_name,photo_100,bdate"));
        currentRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                Log.d("VkDemoApp", "onComplete " + response);

                VKUsersArray usersArray = (VKUsersArray) response.parsedModel;
                users.clear();
                final String[] formats = new String[]{"dd.MM.yyyy", "dd.MM"};

                for (VKApiUserFull userFull : usersArray) {
                    String photo100 = null;
                    int uid = 0;
                    if (!TextUtils.isEmpty(userFull.bdate)) {
                        for (int i = 0; i < formats.length; i++) {
                            photo100 = userFull.photo_100;
                            uid = userFull.id;
                        }

                    }
                    users.add(new User(userFull.toString(), uid, photo100));
                }
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                super.attemptFailed(request, attemptNumber, totalAttempts);
                Log.d("VkDemoApp", "attemptFailed " + request + " " + attemptNumber + " " + totalAttempts);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                Log.d("VkDemoApp", "onError: " + error);
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                super.onProgress(progressType, bytesLoaded, bytesTotal);
                Log.d("VkDemoApp", "onProgress " + progressType + " " + bytesLoaded + " " + bytesTotal);
            }
        });

    }


}
