package com.puresoftware.quickmemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.input.InputManager;
import android.inputmethodservice.Keyboard;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.puresoftware.quickmemo.room.Memo;

public class PINActivity extends AppCompatActivity {

    ConstraintLayout pinLayout;
    TextView pin1;
    TextView pin2;
    TextView pin3;
    TextView pin4;
    TextView tvMessage;
    LinearLayout resetUserInfo;

    EditText edtKeyboard;
    String pin = "";

    String data = "";

    String TAG = PINActivity.class.getSimpleName();

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinactivity);

        SharedPreferences preferences = getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        data = preferences.getString("PIN", "");

        Intent intent = getIntent();
        Memo beforeMemo = new Memo();
        beforeMemo.title = intent.getStringExtra("title");
        beforeMemo.content = intent.getStringExtra("content");
        beforeMemo.timestamp = intent.getLongExtra("timestamp", 0);
        beforeMemo.star = intent.getBooleanExtra("star", false);
        beforeMemo.lock = intent.getBooleanExtra("lock", false);

        pinLayout = findViewById(R.id.lay_pin_activity_keyboard_use);
        pin1 = findViewById(R.id.tvimg_pin_activity_1_pin);
        pin2 = findViewById(R.id.tvimg_pin_activity_2_pin);
        pin3 = findViewById(R.id.tvimg_pin_activity_3_pin);
        pin4 = findViewById(R.id.tvimg_pin_activity_4_pin);
        tvMessage = findViewById(R.id.tv_pin_activity_message);
        resetUserInfo = findViewById(R.id.laybtn_pin_activity_reset_user_info);


        if (!data.equals("no")) {
            //        https://ellordnet.tistory.com/28
            edtKeyboard = findViewById(R.id.edt_pin_activity_input_number);
            edtKeyboard.requestFocus(); // 이걸 요청하면 원하는 EditText의 키보드가 나온다.
            edtKeyboard.setCursorVisible(false); // 커서 안보이게
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE); // 키보드 매니저
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY); // 키보드 자동

            pinLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY); // 키보드 자동
                }
            });

            edtKeyboard.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    pin = String.valueOf(charSequence);
                    Log.i(TAG, "i:" + i + ",i1:" + i1 + ",i2:" + i2 + "text:" + charSequence);
                    // i는 길이, i1는 지우면 1, i2는 지우면 0

                    // 텍스트가 있는 경우
                    if (i2 == 1) {
                        switch (i) { // 레드

                            case 0:
                                pin1.setTextColor(Color.parseColor("#FF5353"));
                                break;
                            case 1:
                                pin2.setTextColor(Color.parseColor("#FF5353"));
                                break;
                            case 2:
                                pin3.setTextColor(Color.parseColor("#FF5353"));
                                break;
                            case 3:
                                pin4.setTextColor(Color.parseColor("#FF5353"));
                                break;
                        }
                    } else {
                        switch (i) { // 그레이

                            case 0:
                                pin1.setTextColor(Color.parseColor("#CDCDCD"));
                                break;
                            case 1:
                                pin2.setTextColor(Color.parseColor("#CDCDCD"));
                                break;
                            case 2:
                                pin3.setTextColor(Color.parseColor("#CDCDCD"));
                                break;
                            case 3:
                                pin4.setTextColor(Color.parseColor("#CDCDCD"));
                                break;
                        }
                    }
                    if (i == 3) {

                        if (pin.equals(preferences.getString("PIN", ""))) {
                            tvMessage.setVisibility(View.INVISIBLE);
                            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0); // 키보드 자동 내림

                            // 인텐트 데이터를 모아놓기
                            Intent intent = new Intent(PINActivity.this, EditActivity.class);
                            intent.putExtra("title", beforeMemo.title);
                            intent.putExtra("content", beforeMemo.content);
                            intent.putExtra("timestamp", beforeMemo.timestamp);
                            intent.putExtra("lock", beforeMemo.lock);
                            intent.putExtra("star", beforeMemo.star);
                            startActivity(intent);
                            finish();

                        } else {
                            tvMessage.setVisibility(View.VISIBLE);
                        }
                        // 초기값으로 받고 초기화
                        pin1.setTextColor(Color.parseColor("#CDCDCD"));
                        pin2.setTextColor(Color.parseColor("#CDCDCD"));
                        pin3.setTextColor(Color.parseColor("#CDCDCD"));
                        pin4.setTextColor(Color.parseColor("#CDCDCD"));
                        edtKeyboard.setText("");
                        edtKeyboard.getText().toString();
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

        } else {
            Intent goPIN = new Intent(PINActivity.this, PINsetActivity.class);
            startActivity(goPIN);
            finish();
        }

        resetUserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PINActivity.this, PINsetActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(PINActivity.this, MainActivity.class);
        startActivity(intent);

        super.onBackPressed();
    }
}