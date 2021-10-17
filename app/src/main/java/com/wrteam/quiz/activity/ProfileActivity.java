package com.wrteam.quiz.activity;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.wrteam.quiz.helper.ApiConfig;
import com.wrteam.quiz.helper.AudienceProgress;
import com.wrteam.quiz.helper.CircleImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;

import com.wrteam.quiz.Constant;
import com.wrteam.quiz.R;
import com.wrteam.quiz.helper.AppController;

import com.wrteam.quiz.helper.Session;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;


import com.wrteam.quiz.helper.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView imgProfile;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    ProgressBar progressBar;
    String name, mobile, email, filePath = null, currentPhotoPath;
    BottomSheetDialog bottomSheetDialog;
    FloatingActionButton fabProfile;
    TextView tvEmailId;
    AudienceProgress progress;
    EditText edtName, edtMobile;
    RelativeLayout mainLayout, lytEditProfile;
    ImageView tvLogout;
    int REQUEST_IMAGE_CAPTURE = 100, REQUEST_CROP_IMAGE = 120, SELECT_FILE = 110, reqWritePermission = 2;
    File output = null;
    Uri imageUri;
    AppCompatActivity activity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newprofile);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getAllWidgets();
        activity = ProfileActivity.this;

        lytEditProfile.setOnClickListener(view -> ProfileEdit());
        imgProfile.setDefaultImageResId(R.drawable.ic_account);
        imgProfile.setImageUrl(Session.getUserData(Session.PROFILE, activity), imageLoader);
        fabProfile.setOnClickListener(view -> SelectProfileImage());
        name = Session.getUserData(Session.NAME, activity);
        mobile = Session.getUserData(Session.MOBILE, activity);
        email = Session.getUserData(Session.EMAIL, activity);
        edtName.setText(name);
        edtMobile.setText(mobile);
        tvEmailId.setText(email);
        // StringBuilder s = new StringBuilder().append("Hello").append(' ', 90).append("world");
        Utils.LoadNativeAd(activity);
    }

    public void getAllWidgets() {
        mainLayout = findViewById(R.id.mainLayout);
        edtName = findViewById(R.id.edtUserName);
        edtMobile = findViewById(R.id.edtMobile);
        tvEmailId = findViewById(R.id.tvEmailId);
        tvLogout = findViewById(R.id.tvLogout);
        lytEditProfile = findViewById(R.id.lytEditProfile);


        fabProfile = findViewById(R.id.fabProfile);
        progressBar = findViewById(R.id.progressBar);
        progress = findViewById(R.id.progress);
        imgProfile = findViewById(R.id.imgProfile);
    }

    public void ProfileEdit() {
        bottomSheetDialog = new BottomSheetDialog(activity, R.style.BottomSheetTheme);
        View sheetView = getLayoutInflater().inflate(R.layout.bottomsheet_profileupdate, null);
        sheetView.findViewById(R.id.imgClose).setOnClickListener(view -> bottomSheetDialog.dismiss());
        final TextInputEditText edtName, edtEmail, editTextPhone;
        final TextInputLayout inputEmail;
        final LinearLayout lytMobile;

        edtName = sheetView.findViewById(R.id.edtName);
        editTextPhone = sheetView.findViewById(R.id.editTextPhone);
        edtEmail = sheetView.findViewById(R.id.edtEmail);
        inputEmail = sheetView.findViewById(R.id.inputEmail);
        lytMobile = sheetView.findViewById(R.id.mobilelyt);

        edtName.setText(name);
        if ((Session.getUserData(Session.TYPE, activity)).equals("mobile")) {
            lytMobile.setVisibility(View.GONE);
            inputEmail.setVisibility(View.VISIBLE);
            edtEmail.setText(email);
        } else {
            inputEmail.setVisibility(View.GONE);
            lytMobile.setVisibility(View.VISIBLE);
            editTextPhone.setText(mobile);
        }


        sheetView.findViewById(R.id.emailsubmit).setOnClickListener(view -> {
            final String name = edtName.getText().toString().trim();
            final String number = editTextPhone.getText().toString().trim();

            final String PhoneNumber = number;
            final String Email = edtEmail.getText().toString().trim();
            if ((Session.getUserData(Session.TYPE, activity)).equals("mobile")) {
                inputEmail.setVisibility(View.VISIBLE);
                if (name.isEmpty())
                    edtName.setError(getString(R.string.empty_alert_msg));
                else
                    UpdateProfileEmail(name, Email, Session.getUserData(Session.MOBILE, activity));
            } else {
                lytMobile.setVisibility(View.VISIBLE);
                if (name.isEmpty())
                    edtName.setError(getString(R.string.empty_alert_msg));
                else
                    UpdateProfile(name, PhoneNumber);
            }
        });
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();

    }


    public void dialogs() {
        final android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(activity);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.permission_dialog, null);
        dialog.setView(dialogView);
        TextView ok = dialogView.findViewById(R.id.ok);
        final android.app.AlertDialog alertDialog = dialog.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertDialog.show();
        alertDialog.setCancelable(false);
        ok.setOnClickListener(view -> alertDialog.dismiss());
    }


    public void UpdateProfileEmail(final String name, final String email, final String mobile) {
        if (Utils.isNetworkAvailable(this)) {
            Map<String, String> params = new HashMap<>();
            params.put(Constant.updateProfile, "1");
            params.put(Constant.userId, Session.getUserData(Session.USER_ID, activity));
            params.put(Constant.email, email);
            params.put(Constant.mobile, mobile);
            params.put(Constant.name, name);
            ApiConfig.RequestToVolley((result, response) -> {
                if (result) {
                    try {
                        JSONObject obj = new JSONObject(response);
                        boolean error = obj.getBoolean(Constant.ERROR);
                        String message = obj.getString(Constant.messageReport);
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        if (!error) {
                            Session.setUserData(Session.NAME, name, activity);
                            Session.setUserData(Session.EMAIL, email, activity);
                            edtName.setText(name);
                            tvEmailId.setText(email);
                            MainActivity.tvName.setText(getString(R.string.hello) + name);
                            bottomSheetDialog.dismiss();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, params);

        } else {
            setUpdate();
        }
    }

    public void uploadFileWithVolley(String filePath) {
        progressBar.setVisibility(View.VISIBLE);
        if (Utils.isNetworkAvailable(this)) {
            //default params
            Map<String, String> params = new HashMap<>();
            params.put(Constant.upload_profile_image, "1");
            params.put(Constant.userId, Session.getUserData(Session.USER_ID, activity));
            //file params
            Map<String, String> fileParams = new HashMap<>();
            fileParams.put(Constant.image, filePath);
            ApiConfig.MultipartRequestToVolley((result, response) -> {
                if (result) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response);
                        boolean error = jsonObject.getBoolean(Constant.ERROR);
                        if (!error) {
                            String imagePath = jsonObject.getString(Constant.FILE_PATH);
                            Session.setUserData(Session.PROFILE, imagePath, activity);
                            imgProfile.setImageUrl(imagePath, imageLoader);
                            DrawerActivity.imgProfile.setImageUrl(imagePath, imageLoader);
                        }
                        Toast.makeText(activity, jsonObject.getString(Constant.messageReport), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, params, fileParams);
        }
    }

    public void UpdateProfile(final String name, final String mobile) {
        if (Utils.isNetworkAvailable(this)) {
            Map<String, String> params = new HashMap<>();
            params.put(Constant.updateProfile, "1");
            params.put(Constant.userId, Session.getUserData(Session.USER_ID, activity));
            params.put(Constant.name, name);
            params.put(Constant.email, Session.getUserData(Session.EMAIL, activity));
            params.put(Constant.mobile, mobile);
            ApiConfig.RequestToVolley((result, response) -> {

                if (result) {
                    try {
                        JSONObject obj = new JSONObject(response);
                        boolean error = obj.getBoolean(Constant.ERROR);
                        String message = obj.getString(Constant.messageReport);
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        if (!error) {
                            Session.setUserData(Session.MOBILE, mobile, activity);
                            Session.setUserData(Session.NAME, name, activity);
                            edtName.setText(name);
                            edtMobile.setText(mobile);
                            MainActivity.tvName.setText(getString(R.string.hello) + name);
                            bottomSheetDialog.dismiss();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, params);

        } else {
            setUpdate();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == reqWritePermission) {
            // for each permission check if the user granted/denied them
            // you may want to group the rationale in a single dialog,
            // this is just an example
            for (int i = 0, len = permissions.length; i < len; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // user rejected the permission
                    boolean showRationale = shouldShowRequestPermissionRationale(permission);
                    if (!showRationale) {
                        dialogs();
                        // user also CHECKED "never ask again"
                        // you can either enable some fall back,
                        // disable features of your app
                        // or open another dialog explaining
                        // again the permission and directing to
                        // the app setting
                    }
                }
            }
        }
    }

    public void SelectProfileImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, reqWritePermission);
            } else if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, reqWritePermission);
            } else {
                selectDialog();
            }
        } else {
            selectDialog();
        }
    }

    public void selectDialog() {
        final CharSequence[] items = {getString(R.string.from_library), getString(R.string.from_camera), getString(R.string.cancel)};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        builder.setTitle("Add Photo!");
        builder.setItems(items, (dialog, item) -> {
            if (items[item].equals(getString(R.string.from_library))) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, SELECT_FILE);
            } else if (items[item].equals(getString(R.string.from_camera))) {
                dispatchTakePictureIntent();
            } else if (items[item].equals(getString(R.string.cancel))) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private File createImageFile() throws IOException {
        // Create an image file name

        String imageFileName = getString(R.string.app_name) + System.currentTimeMillis() + "_";
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                imageUri = data.getData();
                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setOutputCompressQuality(90)
                        .setRequestedSize(300, 300)
                        .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                        .setAspectRatio(1, 1)
                        .start(activity);

            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setOutputCompressQuality(90)
                        .setRequestedSize(300, 300)
                        .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                        .setAspectRatio(1, 1)
                        .start(activity);
            } else if (requestCode == REQUEST_CROP_IMAGE) {

                CropImage.activity(FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", output)).start(activity);
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                filePath = result.getUriFilePath(getApplicationContext(), true);
                uploadFileWithVolley(filePath);
                // new UploadFileToServer().execute();
            }
        }
    }

    public void setSnackBar() {
        Snackbar snackbar = Snackbar
                .make(findViewById(android.R.id.content), getString(R.string.msg_no_internet), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.retry), view -> SelectProfileImage());
        snackbar.setActionTextColor(Color.RED);
        snackbar.show();
    }

    public void setUpdate() {
        bottomSheetDialog.dismiss();
        Snackbar snackbar = Snackbar
                .make(findViewById(android.R.id.content), getString(R.string.msg_no_internet), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.retry), view -> ProfileEdit());
        snackbar.setActionTextColor(Color.RED);
        snackbar.show();
    }

    public void Logout(View view) {
        final AlertDialog.Builder dialog1 = new AlertDialog.Builder(activity);
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.dailog_logout, null);
        dialog1.setView(dialogView);
        dialog1.setCancelable(true);
        final AlertDialog alertDialog = dialog1.create();
        TextView tvYes = dialogView.findViewById(R.id.tvYes);
        TextView tvNo = dialogView.findViewById(R.id.tvNo);

        tvNo.setOnClickListener(v -> alertDialog.dismiss());
        tvYes.setOnClickListener(view1 -> {
            alertDialog.dismiss();
            Session.clearUserSession(activity);
            LoginManager.getInstance().logOut();
            LoginTabActivity.mAuth.signOut();
            FirebaseAuth.getInstance().signOut();
            Intent intentLogin = new Intent(activity, LoginTabActivity.class);
            intentLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intentLogin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intentLogin);
            activity.finish();
        });
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public void UserStatistics(View view) {
        Intent intent = new Intent(getApplicationContext(), UserStatistics.class);
        startActivity(intent);
    }

    public void LeaderBoard(View view) {
        Intent intent = new Intent(getApplicationContext(), LeaderboardTabActivity.class);
        startActivity(intent);
    }

    public void Bookmarks(View view) {
        Intent intent = new Intent(getApplicationContext(), BookmarkList.class);
        startActivity(intent);
    }

    public void InviteFriend(View view) {
        Intent intent = new Intent(getApplicationContext(), InviteFriendActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}