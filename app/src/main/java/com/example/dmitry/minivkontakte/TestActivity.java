package com.example.dmitry.minivkontakte;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKBatchRequest;
import com.vk.sdk.api.VKBatchRequest.VKBatchRequestListener;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKRequest.VKRequestListener;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.methods.VKApiCaptcha;
import com.vk.sdk.api.model.VKApiMessage;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKPhotoArray;
import com.vk.sdk.api.model.VKWallPostResult;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;
import com.vk.sdk.dialogs.VKShareDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TestActivity extends ActionBarActivity {

    public static final int TARGET_GROUP = 60479154;
    public static final int TARGET_ALBUM = 181808365;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_test, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            view.findViewById(R.id.users_get).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.FIELDS,
                            "id,first_name,last_name,sex,bdate,city,country,photo_50,photo_100," +
                                    "photo_200_orig,photo_200,photo_400_orig,photo_max,photo_max_orig,online," +
                                    "online_mobile,lists,domain,has_mobile,contacts,connections,site,education," +
                                    "universities,schools,can_post,can_see_all_posts,can_see_audio,can_write_private_message," +
                                    "status,last_seen,common_count,relation,relatives,counters"));
//                    VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.USER_IDS, "1,2"));
                    request.secure = false;
                    request.useSystemLanguage = false;
                    startApiCall(request);
                }
            });

            view.findViewById(R.id.friends_get).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VKRequest request = VKApi.friends().get(VKParameters.from(VKApiConst.FIELDS, "id,first_name,last_name,sex,bdate,city"));
                    startApiCall(request);
                }
            });

            view.findViewById(R.id.messages_get).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VKRequest request = VKApi.messages().get();
                    startApiCall(request);
                }
            });

            view.findViewById(R.id.dialogs_get).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VKRequest request = VKApi.messages().getDialogs();
                    startApiCall(request);
                }
            });

            view.findViewById(R.id.captcha_force).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VKRequest request = new VKApiCaptcha().force();
                    startApiCall(request);
                }
            });

            view.findViewById(R.id.wall_post).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    makePost(null, "Hello, friends!");
                }
            });

            view.findViewById(R.id.upload_photo).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Bitmap photo = getPhoto();
                    VKRequest request = VKApi.uploadAlbumPhotoRequest(new VKUploadImage(photo, VKImageParameters.pngImage()), TARGET_ALBUM, TARGET_GROUP);
                    request.executeWithListener(new VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            photo.recycle();
                            VKPhotoArray photoArray = (VKPhotoArray) response.parsedModel;
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://vk.com/photo-%d_%s", TARGET_GROUP, photoArray.get(0).id)));
                            startActivity(i);
                        }

                        @Override
                        public void onError(VKError error) {
                            showError(error);
                        }
                    });
                }
            });

            view.findViewById(R.id.test_validation).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final VKRequest test = new VKRequest("account.testValidation");
                    startApiCall(test);
                }
            });
            view.findViewById(R.id.test_share).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Bitmap b = getPhoto();
                    VKPhotoArray photos = new VKPhotoArray();
                    photos.add(new VKApiPhoto("photo-47200925_314622346"));
                    new VKShareDialog()
                            .setText("I created this post with VK Android SDK\nSee additional information below\n#vksdk")
                            .setUploadedPhotos(photos)
                            .setAttachmentImages(new VKUploadImage[]{
                                    new VKUploadImage(b, VKImageParameters.pngImage())
                            })
                            .setAttachmentLink("VK Android SDK information", "https://vk.com/dev/android_sdk")
                            .setShareDialogListener(new VKShareDialog.VKShareDialogListener() {
                                @Override
                                public void onVkShareComplete(int postId) {
                                    b.recycle();
                                }

                                @Override
                                public void onVkShareCancel() {
                                    b.recycle();
                                }

                                @Override
                                public void onVkShareError(VKError error) {
                                    b.recycle();
                                }
                            })
                            .show(getFragmentManager(), "VK_SHARE_DIALOG");
                }
            });

            view.findViewById(R.id.upload_photo_to_wall).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Bitmap photo = getPhoto();
                    VKRequest request = VKApi.uploadWallPhotoRequest(new VKUploadImage(photo, VKImageParameters.jpgImage(0.9f)), 0, TARGET_GROUP);
                    request.executeWithListener(new VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            photo.recycle();
                            VKApiPhoto photoModel = ((VKPhotoArray) response.parsedModel).get(0);
                            makePost(new VKAttachments(photoModel));
                        }

                        @Override
                        public void onError(VKError error) {
                            showError(error);
                        }
                    });
                }
            });

            view.findViewById(R.id.upload_several_photos_to_wall).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Bitmap photo = getPhoto();
                    VKRequest request1 = VKApi.uploadWallPhotoRequest(new VKUploadImage(photo, VKImageParameters.jpgImage(0.9f)), 0, TARGET_GROUP);
                    VKRequest request2 = VKApi.uploadWallPhotoRequest(new VKUploadImage(photo, VKImageParameters.jpgImage(0.5f)), 0, TARGET_GROUP);
                    VKRequest request3 = VKApi.uploadWallPhotoRequest(new VKUploadImage(photo, VKImageParameters.jpgImage(0.1f)), 0, TARGET_GROUP);
                    VKRequest request4 = VKApi.uploadWallPhotoRequest(new VKUploadImage(photo, VKImageParameters.pngImage()), 0, TARGET_GROUP);

                    VKBatchRequest batch = new VKBatchRequest(request1, request2, request3, request4);
                    batch.executeWithListener(new VKBatchRequestListener() {
                        @Override
                        public void onComplete(VKResponse[] responses) {
                            super.onComplete(responses);
                            photo.recycle();
                            VKAttachments attachments = new VKAttachments();
                            for (VKResponse response : responses) {
                                VKApiPhoto photoModel = ((VKPhotoArray) response.parsedModel).get(0);
                                attachments.add(photoModel);
                            }
                            makePost(attachments);
                        }

                        @Override
                        public void onError(VKError error) {
                            showError(error);
                        }
                    });
                }
            });

            view.findViewById(R.id.upload_doc).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    File file = getFile();
                    VKRequest request = VKApi.docs().uploadDocRequest(file);
                    startApiCall(request);
                }
            });
        }

        private void startApiCall(VKRequest request) {
            Intent i = new Intent(getActivity(), ApiCallActivity.class);
            i.putExtra("request", request.registerObject());
            startActivity(i);
        }

        private void showError(VKError error) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(error.toString())
                    .setPositiveButton("OK", null)
                    .show();

            if (error.httpError != null) {
                Log.w("Test", "Error in request or upload", error.httpError);
            }
        }

        private Bitmap getPhoto() {
            Bitmap b = null;

            try {
                b = BitmapFactory.decodeStream(getActivity().getAssets().open("android.jpg"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return b;
        }

        private File getFile() {
            try {
                InputStream inputStream = getActivity().getAssets().open("android.jpg");
                File file = new File(getActivity().getCacheDir(), "android.jpg");
                OutputStream output = new FileOutputStream(file);
                byte[] buffer = new byte[4 * 1024]; // or other buffer size
                int read;

                while ((read = inputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
                output.flush();
                output.close();
                return file;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private void makePost(VKAttachments attachments) {
            makePost(attachments, null);
        }

        private void makePost(VKAttachments attachments, String message) {
            VKRequest post = VKApi.wall().post(VKParameters.from(VKApiConst.OWNER_ID, "-" + TARGET_GROUP, VKApiConst.ATTACHMENTS, attachments, VKApiConst.MESSAGE, message));
            post.setModelClass(VKWallPostResult.class);
            post.executeWithListener(new VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    VKWallPostResult result = (VKWallPostResult)response.parsedModel;
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://vk.com/wall-%d_%s", TARGET_GROUP, result.post_id) ) );
                    startActivity(i);
                }

                @Override
                public void onError(VKError error) {
                    showError(error.apiError != null ? error.apiError : error);
                }
            });
        }
    }
}