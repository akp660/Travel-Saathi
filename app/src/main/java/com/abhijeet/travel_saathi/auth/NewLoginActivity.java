package com.abhijeet.travel_saathi.auth;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.abhijeet.travel_saathi.R;
import com.abhijeet.travel_saathi.activities.Signup_successfully;
import com.abhijeet.travel_saathi.utilities.GradientTextView;
import com.abhijeet.travel_saathi.utilities.MailHelper;
import com.abhijeet.travel_saathi.utilities.OtpFlowManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Random;

public class NewLoginActivity extends AppCompatActivity {

    GradientTextView headLogintext;
    FrameLayout otpDetails;

    TextInputEditText emailField;

    TextInputLayout layout;

    MaterialButton sendOtp;
    ImageView nextButton;

    String otp;
    String enteredOTP;
    MaterialCardView googleButton;
    TextView resendOtp;
    TextView logInPhone;
    CheckBox terms;

    TextInputEditText firstDigit, secondDigit, thirdDigit, fourthDigit;
    OtpFlowManager flowManager;

    CountDownTimer otpTimer;
    private static final long COUNTDOWN_TIME = 10000;
    private long timeLeftInMillis;


    boolean flag = false; // false means login type is email

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_login);

        flowManager = new OtpFlowManager(this);

        initializeID();
        initializeViews();

    }


    public void initializeViews() {
        headLogintext.setGradientColors(0xFF001AFF, 0xFFFB78E6);

        BottomSheetDialog loginDialog = new BottomSheetDialog(this);
        loginDialog.setContentView(R.layout.login_bottomsheet);
        BottomSheetDialog otpDialog = new BottomSheetDialog(this);
        otpDialog.setContentView(R.layout.otp_bottomsheet);

        FrameLayout bottomsheet = loginDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        FrameLayout otpSheet = otpDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) bottomsheet.getLayoutParams();
        layoutParams.leftMargin = 16;
        layoutParams.rightMargin = 16;
        bottomsheet.setLayoutParams(layoutParams);

        ViewGroup.MarginLayoutParams layoutParams1 = (ViewGroup.MarginLayoutParams) otpSheet.getLayoutParams();
        layoutParams1.leftMargin = 16;
        layoutParams1.rightMargin = 16;
        otpSheet.setLayoutParams(layoutParams1);

        loginDialog.setCancelable(false);
        otpDialog.setCancelable(true);
        loginDialog.show();

        otpDetails = otpDialog.findViewById(R.id.otpDetails);
        assert otpDetails != null;

        googleButton = loginDialog.findViewById(R.id.materialCardView3);
        terms = loginDialog.findViewById(R.id.terms);

        emailField = loginDialog.findViewById(R.id.textInputEditText);
        sendOtp = loginDialog.findViewById(R.id.sendOTP);
        nextButton = otpDialog.findViewById(R.id.nextButton);

        firstDigit = otpDialog.findViewById(R.id.digitOne);
        secondDigit = otpDialog.findViewById(R.id.digitTwo);
        thirdDigit = otpDialog.findViewById(R.id.digitThree);
        fourthDigit = otpDialog.findViewById(R.id.digitFour);

        layout = loginDialog.findViewById(R.id.textInputLayout);

        resendOtp = otpDialog.findViewById(R.id.resendOtp);
        logInPhone = loginDialog.findViewById(R.id.loginPhonebutton);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);


        sendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!flag) {
                    if (!emailField.getText().toString().isEmpty()) {
                        if (terms.isChecked()){
                            hideKeyboard(view);
                            Random random = new Random();
                            int number = 1000 + random.nextInt(9000);
                            otp = String.valueOf(number);
                            Log.d("Otp sent", "onClick: " + otp);
                            loginDialog.hide();
                            otpDialog.show();
                            startTimer();
                            MailHelper.sendEmail(emailField.getText().toString(), otp);
                            Toast.makeText(NewLoginActivity.this, "Otp Sent", Toast.LENGTH_SHORT).show();
                            otpDetails.setVisibility(View.VISIBLE);
                            showKeyboard(firstDigit);
                            flowManager.initializeOtpBoxFlow(firstDigit,secondDigit,thirdDigit,fourthDigit);
                        }
                        else{
                            Toast.makeText(NewLoginActivity.this, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(NewLoginActivity.this, "Enter email", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (!emailField.getText().toString().isEmpty()) {
                        if (terms.isChecked()){
                            hideKeyboard(view);
                            Toast.makeText(NewLoginActivity.this, "Otp Sent", Toast.LENGTH_SHORT).show();
                            loginDialog.hide();
                            otpDialog.show();
                            startTimer();
                            otpDetails.setVisibility(View.VISIBLE);
                            sendSMS(emailField.getText().toString());
                        }
                        else{
                            Toast.makeText(NewLoginActivity.this, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(NewLoginActivity.this, "Enter Phone", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        otpDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                loginDialog.show();
            }
        });

        otpDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                loginDialog.show();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enteredOTP = firstDigit.getText().toString() + secondDigit.getText() + thirdDigit.getText() + fourthDigit.getText();
                Log.d("Otp entered", "onClick: " + enteredOTP);
                if (enteredOTP.equals(otp)) {
                    Intent intent = new Intent(NewLoginActivity.this, Signup_successfully.class);

                    SharedPreferences sharedPreferences = getSharedPreferences("OnceLoggedIn", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.apply();

                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(NewLoginActivity.this, "Incorrect OTP", Toast.LENGTH_SHORT).show();
                }
            }
        });

        logInPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailField.setInputType(InputType.TYPE_CLASS_NUMBER);
                emailField.setSingleLine();
                layout.setHint("Enter Phone");
                emailField.setHint("Enter Phone Number");
                flag = true;
            }
        });

        resendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (resendOtp.getText().equals("Resend OTP")){
                    if (!emailField.getText().toString().isEmpty()){
                        hideKeyboard(view);
                        Random random = new Random();
                        int number = 1000 + random.nextInt(9000);
                        otp = String.valueOf(number);
                        Toast.makeText(NewLoginActivity.this, "Otp sent again", Toast.LENGTH_SHORT).show();
                        otpDetails.setVisibility(View.VISIBLE);
                        MailHelper.sendEmail(emailField.getText().toString(), otp);
                    }
                    else{
                        Toast.makeText(NewLoginActivity.this, "Enter email", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    public void startTimer(){
        otpTimer = new CountDownTimer(COUNTDOWN_TIME,1000) {
            @Override
            public void onTick(long l) {
                timeLeftInMillis = l;
                updateOtpTimer();
            }

            @Override
            public void onFinish() {
                resendOtp.setText("Resend OTP");
            }
        }.start();
    }

    public void updateOtpTimer(){
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format("%02d:%02d", minutes, seconds);
        resendOtp.setText("Resend in " + timeLeftFormatted);
    }

    public void initializeID() {
        headLogintext = findViewById(R.id.headLoginText);
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    private void sendSMS(String phoneNo) {
        Random random = new Random();
        int number = 1000 + random.nextInt(9000);
        otp = String.valueOf(number);

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage("+91" + phoneNo, null, otp, null, null);
            Toast.makeText(this, "Message Sent", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.v("LOG MESSAGE", e.getMessage().toString());
            Toast.makeText(this, "Message Not Sent", Toast.LENGTH_SHORT).show();
        }
    }



    public void showKeyboard(TextInputEditText editText){
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b){
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (otpTimer!=null){
            otpTimer.cancel();
        }
    }
}