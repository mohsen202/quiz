package com.wrteam.quiz.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.NotNull;
import com.hbb20.CountryCodePicker;
import com.wrteam.quiz.Constant;
import com.wrteam.quiz.R;
import com.wrteam.quiz.activity.LoginTabActivity;
import com.wrteam.quiz.activity.MainActivity;
import com.wrteam.quiz.activity.PrivacyPolicy;

import com.wrteam.quiz.helper.ApiConfig;
import com.wrteam.quiz.helper.AppController;
import com.wrteam.quiz.helper.Session;
import com.wrteam.quiz.helper.Utils;
import com.wrteam.quiz.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.WIFI_SERVICE;
import static com.wrteam.quiz.helper.AppController.getAppContext;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginActivityFragment extends Fragment {
    String TAG = "LoginActivityFragment";
    int RC_SIGN_IN = 9001;
    CallbackManager mCallbackManager;
    String token;
    GoogleSignInClient mGoogleSignInClient;
    TextView tvPrivacy, fragmenttxt;
    ProgressDialog mProgressDialog;
    public TextInputEditText edtEmail, edtPassword;
    public TextInputLayout inputEmail, inputPass;
    String id;
    CardView loginrlyt;
    AlertDialog alertDialog;
    RelativeLayout googleryt, faceookryt, mobileryt;

    private BottomSheetDialog bottomSheetDialog;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack;

    public LoginActivityFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);


        tvPrivacy = (TextView) v.findViewById(R.id.tvPrivacy);
        edtEmail = (TextInputEditText) v.findViewById(R.id.edtEmail);
        edtPassword = (TextInputEditText) v.findViewById(R.id.edtPassword);


        inputEmail = (TextInputLayout) v.findViewById(R.id.inputEmail);
        inputPass = (TextInputLayout) v.findViewById(R.id.inputPass);
        loginrlyt = (CardView) v.findViewById(R.id.loginrlyt);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        mGoogleSignInClient.signOut();
        LoginTabActivity.mAuth = FirebaseAuth.getInstance();
        mCallbackManager = CallbackManager.Factory.create();


        loginrlyt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateForm()) {
                    return;
                }
                showProgressDialog();
                LoginTabActivity.mAuth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {
                                    FirebaseUser user = LoginTabActivity.mAuth.getCurrentUser();

                                    assert user != null;
                                    String personName = user.getDisplayName() + "";

                                    if (user.isEmailVerified()) {
                                        String[] userName = Objects.requireNonNull(user.getEmail()).split("@");
                                        UserSignUpWithSocialMedia(user.getUid(), Session.getFCode(getActivity()), userName[0] + id, personName, user.getEmail(), "", "email");

                                    } else {
                                        FirebaseAuth.getInstance().signOut();
                                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                                        alertDialog.setTitle(getString(R.string.act_verify_1));
                                        alertDialog.setIcon(R.drawable.ic_privacy);
                                        alertDialog.setMessage(getString(R.string.act_verify_2));
                                        alertDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                        alertDialog.show();
                                    }
                                } else {
                                    try {
                                        throw Objects.requireNonNull(task.getException());
                                    } catch (FirebaseAuthInvalidUserException invalidEmail) {
                                        inputEmail.setError(getString(R.string.signup_alert));

                                    } catch (FirebaseAuthInvalidCredentialsException wrongPassword) {
                                        inputPass.setError(getString(R.string.invalid_pass));
                                        // TODO: Take your action
                                    } catch (Exception e) {
                                        Log.d(TAG, "onComplete last: " + e.getMessage());
                                    }
                                }
                                hideProgressDialog();
                            }
                        });
            }
        });
        fragmenttxt = (TextView) v.findViewById(R.id.fragmenttxt);
        fragmenttxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetDialog(LoginTabActivity.mAuth);
            }
        });
        googleryt = (RelativeLayout) v.findViewById(R.id.googleryt);
        googleryt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.isNetworkAvailable(getActivity()))
                    signIn();
                else
                    setSnackBar(getString(R.string.msg_no_internet), getString(R.string.retry));
            }
        });
        faceookryt = (RelativeLayout) v.findViewById(R.id.faceookryt);
        faceookryt.setOnClickListener(view -> {
            if (Utils.isNetworkAvailable(getActivity())) {
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));

                LoginManager.getInstance().registerCallback(mCallbackManager,
                        new FacebookCallback<LoginResult>() {
                            @Override
                            public void onSuccess(LoginResult loginResult) {
                               // System.out.println("=====sucess login fb");
                                getActivity().setResult(RESULT_OK);
                                handleFacebookAccessToken(loginResult.getAccessToken());
                            }

                            @Override
                            public void onCancel() {
                               // System.out.println("=====cancel login fb");
                                hideProgressDialog();
                            }

                            @Override
                            public void onError(FacebookException error) {
                                Log.d(TAG, "facebook:onError", error);
                                error.printStackTrace();
                            }
                        });

            } else
                setSnackBar(getString(R.string.msg_no_internet), getString(R.string.retry));
        });

        mobileryt = (RelativeLayout) v.findViewById(R.id.mobileryt);
        mobileryt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MobileBottomshit(LoginTabActivity.mAuth);
           /*     Intent intent = new Intent(getActivity(), MobileActivity.class);
                startActivity(intent);*/
            }
        });
        if (!Utils.isNetworkAvailable(getActivity())) {
            setSnackBar(getString(R.string.msg_no_internet), getString(R.string.retry));
        }
        token = Session.getDeviceToken(getActivity());
        if (token == null) {
            token = "token";
        }
        Random rand = new Random();
        id = String.format("%04d", rand.nextInt(10000));

        System.out.println("valuesGEtt::="+id);
        PrivacyPolicy();
        Utils.GetSystemConfig(getActivity());
        GetUpadate(getActivity());

        return v;
    }

    public static void GetUpadate(final Activity activity) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_SYSTEM_CONFIG, "1");
        ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {

                if (result) {
                    try {
                        JSONObject obj = new JSONObject(response);

                        boolean error = obj.getBoolean("error");
                        if (!error) {
                            JSONObject jsonobj = obj.getJSONObject("data");
                            Constant.APP_LINK = jsonobj.getString(Constant.KEY_APP_LINK);
                            Constant.MORE_APP_URL = jsonobj.getString(Constant.KEY_MORE_APP);
                            Constant.VERSION_CODE = jsonobj.getString(Constant.KEY_APP_VERSION);
                            Constant.REQUIRED_VERSION = jsonobj.getString(Constant.KEY_APP_VERSION);

                            String versionName = "";
                            try {
                                PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
                                versionName = packageInfo.versionName;
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }

                            if (Constant.FORCEUPDATE.equals("1")) {
                                if (compareVersion(versionName, Constant.VERSION_CODE) < 0) {
                                    OpenBottomDialog(activity);
                                } else if (compareVersion(versionName, Constant.REQUIRED_VERSION) < 0) {
                                    OpenBottomDialog(activity);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, params);

    }

    public static void OpenBottomDialog(final Activity activity) {
        View sheetView = activity.getLayoutInflater().inflate(R.layout.lyt_terms_privacy, null);
        ViewGroup parentViewGroup = (ViewGroup) sheetView.getParent();
        if (parentViewGroup != null) {
            parentViewGroup.removeAllViews();
        }
        final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(activity);
        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.show();
        //FrameLayout bottomSheet = (FrameLayout) mBottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        ImageView imgclose = sheetView.findViewById(R.id.imgclose);
        TextView txttitle = sheetView.findViewById(R.id.tvTitle);
        Button btnNotNow = sheetView.findViewById(R.id.btnNotNow);
        Button btnUpadateNow = sheetView.findViewById(R.id.btnUpdateNow);

        mBottomSheetDialog.setCancelable(false);
        imgclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBottomSheetDialog.isShowing())
                    mBottomSheetDialog.dismiss();
            }
        });
        btnNotNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBottomSheetDialog.isShowing())
                    mBottomSheetDialog.dismiss();
            }
        });
        btnUpadateNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.APP_LINK)));
                //System.out.println("Packge Name::=" + Constant.APP_LINK + activity.getPackageName());

            }
        });
    }


    public static int compareVersion(String version1, String version2) {
        String[] arr1 = version1.split("\\.");
        String[] arr2 = version2.split("\\.");

        int i = 0;
        while (i < arr1.length || i < arr2.length) {
            if (i < arr1.length && i < arr2.length) {
                if (Integer.parseInt(arr1[i]) < Integer.parseInt(arr2[i])) {
                    return -1;
                } else if (Integer.parseInt(arr1[i]) > Integer.parseInt(arr2[i])) {
                    return 1;
                }
            } else if (i < arr1.length) {
                if (Integer.parseInt(arr1[i]) != 0) {
                    return 1;
                }
            } else {
                if (Integer.parseInt(arr2[i]) != 0) {
                    return -1;
                }
            }

            i++;
        }

        return 0;
    }


    public void LoginWithFacebook(View view) {
        if (Utils.isNetworkAvailable(getActivity())) {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
            LoginManager.getInstance().registerCallback(mCallbackManager,
                    new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            getActivity().setResult(RESULT_OK);
                            handleFacebookAccessToken(loginResult.getAccessToken());
                        }

                        @Override
                        public void onCancel() {
                            hideProgressDialog();
                        }

                        @Override
                        public void onError(FacebookException error) {
                            Log.d(TAG, "facebook:onError", error);
                            error.printStackTrace();
                        }
                    });

        } else
            setSnackBar(getString(R.string.msg_no_internet), getString(R.string.retry));
    }

    public void LoginWithGoogle(View view) {
        if (Utils.isNetworkAvailable(getActivity()))
            signIn();
        else
            setSnackBar(getString(R.string.msg_no_internet), getString(R.string.retry));
    }


    public void ShowReferDialog(final String authId, final String referCode, final String name, final String email, final String profile, final String type) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.refer_dailog, null);
        dialog.setView(dialogView);
        dialog.setCancelable(false);
        final AlertDialog alertDialog = dialog.create();

        TextView tvCancel = dialogView.findViewById(R.id.tvCancel);
        TextView tvApply = dialogView.findViewById(R.id.tvApply);
        final EditText edtRefCode = dialogView.findViewById(R.id.edtRefCode);

        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                UserSignUpWithSocialMedia(authId, edtRefCode.getText().toString(), referCode + id, name, email, profile, type);
                alertDialog.dismiss();

            }
        });
        tvApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressDialog();
                UserSignUpWithSocialMedia(authId, edtRefCode.getText().toString(), referCode + id, name, email, profile, type);
                alertDialog.dismiss();

            }
        });
        alertDialog.show();

    }

    public void UserSignUpWithSocialMedia(final String authId, final String fCode, final String referCode, final String name, final String email, final String profile, final String type) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.userSignUp, "1");
        params.put(Constant.email, email);
        params.put(Constant.AUTH_ID, authId);
        params.put(Constant.name, name);
        params.put(Constant.PROFILE, profile);
        params.put(Constant.fcmId, token);
        params.put(Constant.type, type);
        params.put(Constant.mobile, "");
        params.put(Constant.REFER_CODE, referCode);
        params.put(Constant.FRIENDS_CODE, fCode);
            /*    WifiManager wm = (WifiManager) getActivity().getSystemService(WIFI_SERVICE);
                String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());*/
        params.put(Constant.ipAddress, "0.0.0.0");
        ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {

                if (result) {
                    try {
                        JSONObject obj = new JSONObject(response);
                        System.out.println("urlResponse:="+response);
                        if (obj.getString("error").equals("false")) {
                            JSONObject jsonobj = obj.getJSONObject("data");
                            if (!jsonobj.getString(Constant.status).equals(Constant.DE_ACTIVE)) {
                                Session.saveUserDetail(getActivity(),
                                        jsonobj.getString(Constant.userId),
                                        jsonobj.getString(Constant.name),
                                        jsonobj.getString(Constant.email),
                                        jsonobj.getString(Constant.mobile),
                                        jsonobj.getString(Constant.PROFILE), jsonobj.getString(Constant.REFER_CODE), type);
                                Intent i = new Intent(getContext(), MainActivity.class);
                                i.putExtra("type", "default");
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                                getActivity().finish();

                                User user = new User(jsonobj.getString(Constant.name), jsonobj.getString(Constant.email), jsonobj.getString(Constant.userId));
                                FirebaseDatabase.getInstance().getReference("user").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task1) {
                                    }
                                });
                                hideProgressDialog();
                            } else
                                setSnackBarStatus();
                        } else {
                            LoginManager.getInstance().logOut();
                            Toast.makeText(getActivity(), obj.getString("message"), Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        }, params);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        showProgressDialog();
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        LoginTabActivity.mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        try {
                            if (task.isSuccessful()) {
                                //Sign in success, update UI with the signed-in user's information
                                boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();
                                FirebaseUser user = LoginTabActivity.mAuth.getCurrentUser();
                                assert user != null;
                                String personName = user.getDisplayName();
                                if (personName.contains(" ")) {
                                    personName = personName.substring(0, personName.indexOf(" "));
                                }
                                String referCode = "";

                                if (user.getEmail() != null) {
                                    String[] userName = user.getEmail().split("@");
                                    referCode = userName[0];
                                } else {
                                    referCode = user.getPhoneNumber();
                                }
                                if (isNew) {
                                    hideProgressDialog();
                                    ShowReferDialog(user.getUid(), referCode + id, personName, "" + user.getEmail(), user.getPhotoUrl().toString(), "fb");
                                } else
                                    UserSignUpWithSocialMedia(user.getUid(), "", referCode + id, personName, "" + user.getEmail(), user.getPhotoUrl().toString(), "fb");
                            } else {
                                // If sign in fails, display a message to the user.

                                LoginManager.getInstance().logOut();
                                hideProgressDialog();
                                try {
                                    throw Objects.requireNonNull(task.getException());
                                } catch (FirebaseAuthInvalidCredentialsException | FirebaseAuthInvalidUserException | FirebaseAuthUserCollisionException invalidEmail) {
                                    setSnackBar(invalidEmail.getMessage(), getString(R.string.ok));
                                } catch (Exception e) {
                                    e.printStackTrace();

                                    setSnackBar(e.getMessage(), getString(R.string.ok));
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        LoginTabActivity.mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();
                            FirebaseUser user = LoginTabActivity.mAuth.getCurrentUser();

                            assert user != null;
                            String personName = user.getDisplayName();

                            if (personName.contains(" ")) {
                                personName = personName.substring(0, personName.indexOf(" "));
                            }
                            String email = user.getEmail();
                            String[] userName = user.getEmail().split("@");

                            if (isNew) {
                                hideProgressDialog();
                                ShowReferDialog(user.getUid(), userName[0] + id, personName, email, user.getPhotoUrl().toString(), "gmail");
                            } else
                                UserSignUpWithSocialMedia(user.getUid(), "", userName[0] + id, user.getDisplayName(), user.getEmail(), user.getPhotoUrl().toString(), "gmail");
                        } else {
                            hideProgressDialog();

                            try {
                                throw Objects.requireNonNull(task.getException());
                            } catch (FirebaseAuthInvalidCredentialsException | FirebaseAuthInvalidUserException | FirebaseAuthUserCollisionException invalidEmail) {

                                setSnackBar(invalidEmail.getMessage(), getString(R.string.ok));
                            } catch (Exception e) {
                                e.printStackTrace();
                                setSnackBar(e.getMessage(), getString(R.string.ok));
                            }
                        }

                    }
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        showProgressDialog();
    }


    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }


    public void PrivacyPolicy() {
        tvPrivacy.setClickable(true);
        tvPrivacy.setMovementMethod(LinkMovementMethod.getInstance());

        String message = getString(R.string.term_privacy);
        String s2 = getString(R.string.terms);
        String s1 = getString(R.string.privacy_policy);
        final Spannable wordtoSpan = new SpannableString(message);

        wordtoSpan.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PrivacyPolicy.class);
                intent.putExtra("type", "privacy");
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.RED);
                ds.isUnderlineText();
            }
        }, message.indexOf(s1), message.indexOf(s1) + s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        wordtoSpan.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PrivacyPolicy.class);
                intent.putExtra("type", "terms");
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.RED);
                ds.isUnderlineText();
            }
        }, message.indexOf(s2), message.indexOf(s2) + s2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvPrivacy.setText(wordtoSpan);
    }


    private boolean validateForm() {
        boolean valid = true;

        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        //String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            inputEmail.setError(getString(R.string.email_alert_1));
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            valid = false;
            inputEmail.setError(getString(R.string.email_alert_2));
        } else {
            inputEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            inputPass.setError(getString(R.string.pass_alert));
            valid = false;
        } else {
            inputPass.setError(null);
        }


        return valid;
    }


    public void MobileBottomshit(final FirebaseAuth firebaseAuth) {

        bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetTheme);

        View sheetView = getLayoutInflater().inflate(R.layout.mobile_loginbottom, null);

        sheetView.findViewById(R.id.imgClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });

        final TextInputEditText edtName, edtEmail, edtPassword, edtRefer, editTextPhone, editTextCountryCode;
        final TextInputLayout inputName, inputEmail, inputPass;
        final CountryCodePicker countryCodePicker;

        final EditText editText = sheetView.findViewById(R.id.edtEmail);
        edtName = sheetView.findViewById(R.id.edtName);
        edtRefer = sheetView.findViewById(R.id.edtRefer);
        inputName = sheetView.findViewById(R.id.inputName);
        editTextPhone = sheetView.findViewById(R.id.editTextPhone);
        countryCodePicker = sheetView.findViewById(R.id.edtCountryCodePicker);


        countryCodePicker.setCountryForNameCode("IN");

        StartFirebaseLogin(edtName, countryCodePicker, editTextPhone, inputName);


        sheetView.findViewById(R.id.emailsubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = edtName.getText().toString().trim();
                final String code = countryCodePicker.getSelectedCountryCode();
                ;
                final String number = editTextPhone.getText().toString().trim();

                String refer = edtRefer.getText().toString();
                if (!refer.isEmpty())
                    Session.setFCode(refer, getActivity());

                //String email = mEmailField.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    inputName.setError("Required.");
                    getActivity().getCurrentFocus();
                    return;
                } else {
                    inputName.setError(null);

                }

                if (number.isEmpty()) {
                    editTextPhone.setError(getString(R.string.enternumber));
                    return;
                }

                final String phoneNumber = "+" + code + number;
               // System.out.println("valuesGet::=" + phoneNumber);
                sendVerificationCode(phoneNumber);


            }
        });
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();

    }

    public void VerifyBottomSheet(final FirebaseAuth firebaseAuth, String phonenumber, String Name) {

        bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetTheme);

        View sheetView = getLayoutInflater().inflate(R.layout.verify_bottom, null);

        sheetView.findViewById(R.id.imgClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });

        final TextInputEditText edtName, edtcode;
        final TextInputLayout inputName;


        edtcode = sheetView.findViewById(R.id.editTextCode);


        inputName = sheetView.findViewById(R.id.inputName);




        sheetView.findViewById(R.id.emailsubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = edtcode.getText().toString().trim();

                if (code.isEmpty() || code.length() < 6) {

                    edtcode.setError("Enter code...");
                    edtcode.requestFocus();
                    return;
                }
                verifyCode(code, Name, phonenumber);
            }
        });
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();

    }

    private void verifyCode(String code, String name, String phoneNumber) {
        showProgressDialog();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(Constant.verificationCode, code);
        signInWithCredential(credential, name, phoneNumber);
    }

    private void signInWithCredential(PhoneAuthCredential credential, String name, String phoneNumber) {
        LoginTabActivity.mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = LoginTabActivity.mAuth.getCurrentUser();
                            UserSignUpWithSocialMedia(user.getUid(), Session.getFCode(getActivity()), name + id, name, "", "", "mobile", phoneNumber);
                        } else {
                            String message = "Something is wrong, we will fix it soon...";
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Invalid code entered...";
                            }
                            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                            hideProgressDialog();
                        }

                    }
                });
    }


    public void UserSignUpWithSocialMedia(final String authId, final String fCode, final String referCode, final String name, final String email, final String profile, final String type, final String mobile) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.userSignUp, "1");
        params.put(Constant.email, email);
        params.put(Constant.AUTH_ID, authId);
        params.put(Constant.name, name);
        params.put(Constant.PROFILE, profile);
        params.put(Constant.fcmId, token);
        params.put(Constant.type, type);
        params.put(Constant.mobile, mobile);
        params.put(Constant.REFER_CODE, referCode);
        params.put(Constant.FRIENDS_CODE, fCode);
    /*            WifiManager wm = (WifiManager) getActivity().getSystemService(WIFI_SERVICE);
                String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());*/
        params.put(Constant.ipAddress, "0.0.0.0");
      //  System.out.println("---params social  " + params.toString());
        ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {

                if (result) {
                    try {
                       // System.out.println("Response ::=" + response);
                        JSONObject obj = new JSONObject(response);
                        if (obj.getString("error").equals("false")) {
                            JSONObject jsonobj = obj.getJSONObject("data");
                            if (!jsonobj.getString(Constant.status).equals(Constant.DE_ACTIVE)) {
                                Session.saveUserDetail(getActivity(),
                                        jsonobj.getString(Constant.userId),
                                        jsonobj.getString(Constant.name),
                                        jsonobj.getString(Constant.email),
                                        jsonobj.getString(Constant.mobile),
                                        jsonobj.getString(Constant.PROFILE), jsonobj.getString(Constant.REFER_CODE), type);
                                Intent i = new Intent(getActivity(), MainActivity.class);
                                bottomSheetDialog.dismiss();
                                i.putExtra("type", "default");
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                                getActivity().finish();
                                User user = new User(jsonobj.getString(Constant.name), jsonobj.getString(Constant.email), jsonobj.getString(Constant.userId));
                                FirebaseDatabase.getInstance().getReference("user").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task1) {
                                    }
                                });
                                hideProgressDialog();
                            } else
                                setSnackBarStatus();
                        } else {
                            LoginManager.getInstance().logOut();
                            Toast.makeText(getActivity(), obj.getString("message"), Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        }, params);

    }

    private void StartFirebaseLogin(TextInputEditText edtName, CountryCodePicker countryCodePicker, TextInputEditText editTextPhone, TextInputLayout inputName) {
        LoginTabActivity.mAuth = FirebaseAuth.getInstance();
        mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NotNull PhoneAuthCredential phoneAuthCredential) {
            }

            @Override
            public void onVerificationFailed(@NotNull FirebaseException e) {
                hideProgressDialog();
                setSnackBar(getActivity(), e.getLocalizedMessage(), getString(R.string.ok), Color.RED);
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                hideProgressDialog();
                Constant.verificationCode = s;
                final String name = edtName.getText().toString().trim();
                final String code = countryCodePicker.getSelectedCountryCode();
                final String number = editTextPhone.getText().toString().trim();

                //String email = mEmailField.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    inputName.setError("Required.");
                    getActivity().getCurrentFocus();
                    return;
                } else {
                    inputName.setError(null);

                }

                if (number.isEmpty()) {
                    editTextPhone.setError("Valid number is required");
                    return;
                }
                final String phoneNumber = code + number;

                bottomSheetDialog.dismiss();
                VerifyBottomSheet(LoginTabActivity.mAuth, phoneNumber, name);
            }
        };
    }

    private void sendVerificationCode(String number) {
        showProgressDialog();
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(LoginTabActivity.mAuth)
                .setPhoneNumber(number)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(getActivity())                 // Activity (for callback binding)
                .setCallbacks(mCallBack)          // OnVerificationStateChangedCallbacks
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }


    public void setSnackBar(final Activity activity, String message, String action, int color) {
        bottomSheetDialog.dismiss();
        final Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(action, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.setActionTextColor(color);
        View snackbarView = snackbar.getView();
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setMaxLines(5);
        snackbar.show();
    }

    public void BottomSheetDialog(final FirebaseAuth firebaseAuth) {

        bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetTheme);

        View sheetView = LayoutInflater.from(getActivity()).inflate(R.layout.forget_passwordbottom, getView().findViewById(R.id.bootomshit));

        sheetView.findViewById(R.id.imgClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });

        final EditText editText = sheetView.findViewById(R.id.edtEmail);


        sheetView.findViewById(R.id.emailsubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressDialog();
                String email = editText.getText().toString().trim();
                if (email.isEmpty()) {
                    hideProgressDialog();
                    editText.setError(getActivity().getResources().getString(R.string.email_alert_1));
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    hideProgressDialog();
                    editText.setError(getActivity().getResources().getString(R.string.email_alert_2));
                } else {
                    firebaseAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        hideProgressDialog();
                                        Toast.makeText(getActivity(), "Email sent", Toast.LENGTH_SHORT).show();
                                        bottomSheetDialog.dismiss();
                                    }
                                }
                            });
                }
            }
        });
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }


    public void setSnackBar(String message, String action) {
        final Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(action, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.isNetworkAvailable(getActivity())) {
                    snackbar.dismiss();
                } else {
                    snackbar.show();
                }
            }
        });
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setMaxLines(5);
        snackbar.setActionTextColor(Color.RED);
        snackbar.show();
    }

    public void setSnackBarStatus() {
        final Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.account_deactivate), Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(getString(R.string.ok), new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Session.clearUserSession(getActivity());
                LoginTabActivity.mAuth.signOut();
                LoginManager.getInstance().logOut();

            }
        });

        snackbar.setActionTextColor(Color.RED);
        snackbar.show();
        hideProgressDialog();
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            hideProgressDialog();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with FireBase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {
                e.printStackTrace();
            }
        } else {
            // Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
}