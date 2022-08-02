package com.puresoftware.quickmemo;

import static com.puresoftware.quickmemo.R.drawable.ic_bold_selected;
import static com.puresoftware.quickmemo.R.drawable.ic_bold_solid;
import static com.puresoftware.quickmemo.R.drawable.ic_strikethrough_selected;
import static com.puresoftware.quickmemo.R.drawable.ic_strikethrough_solid;
import static com.puresoftware.quickmemo.R.drawable.ic_underline_selected;
import static com.puresoftware.quickmemo.R.drawable.ic_underline_solid;
import static com.puresoftware.quickmemo.R.drawable.ic_write_activity_lock_solid;
import static com.puresoftware.quickmemo.R.drawable.ic_write_activity_star_regular;
import static com.puresoftware.quickmemo.R.drawable.ic_write_activity_star_selected;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.puresoftware.quickmemo.room.AppDatabase;
import com.puresoftware.quickmemo.room.Memo;
import com.puresoftware.quickmemo.room.MemoDao;

import jp.wasabeef.richeditor.RichEditor;

public class EditActivity extends AppCompatActivity {

    //title line
    ImageView btnBack;
    EditText edtTitle;
    ImageView btnStar;
    ImageView btnLock;

    //textLine
    RichEditor richEditor;

    //menuLine
    TextView btnFontSize;
    ImageView btnColor;
    ImageView btnAlignment;
    ImageView btnBold;
    ImageView btnCancelLine;
    ImageView btnUnderLine;


    //switch
    boolean starSwitch = false;
    boolean lockSwitch = false;
    boolean boldSwitch = false;
    boolean cancelLineSwitch = false;
    boolean underLineSwitch = false;

    String afterTitle;
    String afterContent;
    boolean afterStar;
    boolean afterLock;
    long afterTimeStamp;

    String TAG = EditActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        btnBack = findViewById(R.id.btn_write_activity_back);
        edtTitle = findViewById(R.id.edt_write_activity_title);
        btnStar = findViewById(R.id.btn_write_activity_star);
        btnLock = findViewById(R.id.btn_write_activity_lock);
        richEditor = findViewById(R.id.v_write_activity_richeditor);
        btnFontSize = (TextView) findViewById(R.id.btn_write_activity_fontsize);
        btnColor = findViewById(R.id.btn_write_activity_color);
        btnAlignment = findViewById(R.id.btn_write_activity_alignment);
        btnBold = findViewById(R.id.btn__write_activity_bold);
        btnCancelLine = findViewById(R.id.btn__write_activity_canceline);
        btnUnderLine = findViewById(R.id.btn__write_activity_underline);

        // RoomDB 객체
        AppDatabase db = AppDatabase.getInstance(this);
        MemoDao memoDao = db.dao();

        //데이터 불러오기
        Intent intent = getIntent();
        Memo beforeMemo = new Memo();
        beforeMemo.title = intent.getStringExtra("title");
        beforeMemo.content = intent.getStringExtra("content");
        beforeMemo.timestamp = intent.getLongExtra("timestamp", 0);
        beforeMemo.star = intent.getBooleanExtra("star", false);
        beforeMemo.lock = intent.getBooleanExtra("lock", false);

        //데이터 셋업
        edtTitle.setText(beforeMemo.title);
        richEditor.setHtml(beforeMemo.content);
        starSwitch = beforeMemo.star;
        lockSwitch = beforeMemo.lock;

        //스위치 불러오기(중요)
        Log.i(TAG, "starSwitch status: " + starSwitch + "");
        if (starSwitch == false) {
            btnStar.setImageResource(ic_write_activity_star_regular);
        } else {
            btnStar.setImageResource(ic_write_activity_star_selected);
        }

        //스위치 불러오기(잠금)
        Log.i(TAG, "lockSwitch status: " + lockSwitch + "");
        if (lockSwitch == false) {
            btnLock.setImageResource(ic_write_activity_lock_solid);
        } else {
            btnLock.setImageResource(R.drawable.ic_write_activity_lock_selected);
        }

        // 기본으로 사용할 폰트의 크기
        richEditor.setFontSize(5);

        // 저장 후 메뉴로
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                afterTitle = edtTitle.getText().toString();
                afterContent = richEditor.getHtml();
                afterStar = starSwitch;
                afterLock = lockSwitch;
                afterTimeStamp = System.currentTimeMillis();

                if (afterTitle.trim().isEmpty() && afterContent.trim().isEmpty()) {
                    Intent intent = new Intent(EditActivity.this, MainActivity.class);
                    finish();

                    Log.i(TAG, "memo null");

                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Memo memo = new Memo();

                            if (afterContent == null) {
                                afterContent = "";
                            }

                            memo.title = afterTitle;
                            memo.content = afterContent;
                            memo.lock = afterLock;
                            memo.star = afterStar;
                            memo.timestamp = beforeMemo.timestamp;
                            memo.folder = beforeMemo.folder;
                            memoDao.updateData(memo.title, memo.content, memo.star, memo.lock, memo.timestamp, memo.folder);

                            Log.i(TAG, "BeforeData:" + beforeMemo.toString() + '\n' + "↓ ↓ ↓ ↓ ↓");
                            Log.i(TAG, "AfterData:" + memo.toString());
                            Log.i(TAG, "memo updated");

                            Intent intent = new Intent(EditActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }).start();
                }
            }
        });

        btnStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                starSwitch = !starSwitch;
                Log.i(TAG, "starSwitch status: " + starSwitch + "");

                if (starSwitch == false) {
                    btnStar.setImageResource(ic_write_activity_star_regular);
                } else {
                    btnStar.setImageResource(ic_write_activity_star_selected);
                }
            }
        });


        btnLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                lockSwitch = !lockSwitch;
                Log.i(TAG, "starSwitch status: " + lockSwitch + "");

                if (lockSwitch == false) {
                    btnLock.setImageResource(ic_write_activity_lock_solid);
                } else {
                    btnLock.setImageResource(R.drawable.ic_write_activity_lock_selected);
                }
            }
        });


        btnFontSize.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {

                //팝업윈도우 구현
                PopupWindow popupWindow = new PopupWindow(view);
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View inflateView = inflater.inflate(R.layout.write_layout_fontsize, null);
                popupWindow.setContentView(inflateView);
                popupWindow.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                //팝업윈도우 파라미터
                popupWindow.setAnimationStyle(R.style.Animation_AppCompat_Dialog); // 안드로이드 기본 애니메이션임. 이걸 넣어야 상향일때 애니메이션이 적용됨.
                popupWindow.setTouchable(true);
                popupWindow.setFocusable(true); //팝업 뷰 포커스도 주고
                popupWindow.update();
                popupWindow.setOutsideTouchable(true); //팝업 뷰 이외에도 터치되게 (터치시 팝업 닫기 위한 코드)
                popupWindow.setBackgroundDrawable(new BitmapDrawable());

//                popupWindow.showAsDropDown(btnFontSize, 0, -50);
//                popupWindow.showAtLocation(btnFontSize, Gravity.LEFT, 0,
//                        btnFontSize.getBottom() - 60);
                popupWindow.showAsDropDown(btnFontSize, 0, -310);

//                popupWindow.showAsDropDown(btnFontSize, 0, btnFontSize.getBottom() - 60);

                TextView tvFontSmall = inflateView.findViewById(R.id.tv_write_activity_menu_align_ledt);
                TextView tvFontMid = inflateView.findViewById(R.id.tv_write_activity_menu_align_center);
                TextView tvFontBig = inflateView.findViewById(R.id.tv_write_activity_menu_align_right);

                tvFontSmall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        richEditor.setFontSize(3);
                        popupWindow.dismiss();
                    }
                });

                tvFontMid.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        richEditor.setFontSize(5);
                        popupWindow.dismiss();
                    }
                });

                tvFontBig.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        richEditor.setFontSize(10);
                        popupWindow.dismiss();
                    }
                });
            }
        });

        btnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //팝업윈도우 구현
                PopupWindow popupWindow = new PopupWindow(view);
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View inflateView = inflater.inflate(R.layout.write_layout_color, null);

                popupWindow.setContentView(inflateView);
                popupWindow.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                //팝업윈도우 파라미터
                popupWindow.setAnimationStyle(R.style.Animation_AppCompat_Dialog); // 안드로이드 기본 애니메이션임. 이걸 넣어야 상향일때 애니메이션이 적용됨.
                popupWindow.setTouchable(true);
                popupWindow.setFocusable(true); //팝업 뷰 포커스도 주고
                popupWindow.update();
                popupWindow.setOutsideTouchable(true); //팝업 뷰 이외에도 터치되게 (터치시 팝업 닫기 위한 코드)
                popupWindow.setBackgroundDrawable(new BitmapDrawable());

                popupWindow.showAsDropDown(btnColor, 0, -310);

                ImageView btnMenuRed = inflateView.findViewById(R.id.tv_write_activity_menu_color_red);
                ImageView btnMenuOrange = inflateView.findViewById(R.id.tv_write_activity_menu_color_orange);
                ImageView btnMenuYellow = inflateView.findViewById(R.id.tv_write_activity_menu_color_yellow);
                ImageView btnMenuGreen = inflateView.findViewById(R.id.tv_write_activity_menu_color_green);
                ImageView btnMenuSky = inflateView.findViewById(R.id.tv_write_activity_menu_color_sky);
                ImageView btnMenuBlue = inflateView.findViewById(R.id.tv_write_activity_menu_color_blue);
                ImageView btnMenuPurple = inflateView.findViewById(R.id.tv_write_activity_menu_color_purple);
                ImageView btnMenuBlack = inflateView.findViewById(R.id.tv_write_activity_menu_color_black);

                btnMenuRed.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        richEditor.setTextColor(Color.parseColor("#FF4D4D"));
                        popupWindow.dismiss();

                    }
                });

                btnMenuOrange.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        richEditor.setTextColor(Color.parseColor("#FFA94D"));
                        popupWindow.dismiss();

                    }
                });

                btnMenuYellow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        richEditor.setTextColor(Color.parseColor("#FFED4D"));
                        popupWindow.dismiss();

                    }
                });

                btnMenuGreen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        richEditor.setTextColor(Color.parseColor("#8BFF4D"));
                        popupWindow.dismiss();

                    }
                });

                btnMenuSky.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        richEditor.setTextColor(Color.parseColor("#4DC1FF"));
                        popupWindow.dismiss();

                    }
                });

                btnMenuBlue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        richEditor.setTextColor(Color.parseColor("#4D65FF"));
                        popupWindow.dismiss();

                    }
                });

                btnMenuPurple.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        richEditor.setTextColor(Color.parseColor("#A04DFF"));
                        popupWindow.dismiss();

                    }
                });

                btnMenuBlack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        richEditor.setTextColor(Color.parseColor("#000000"));
                        popupWindow.dismiss();

                    }
                });

            }
        });

        btnAlignment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //팝업윈도우 구현
                PopupWindow popupWindow = new PopupWindow(view);
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View inflateView = inflater.inflate(R.layout.write_layout_alignment, null);
                popupWindow.setContentView(inflateView);
                popupWindow.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                //팝업윈도우 파라미터
                popupWindow.setAnimationStyle(R.style.Animation_AppCompat_Dialog); // 안드로이드 기본 애니메이션임. 이걸 넣어야 상향일때 애니메이션이 적용됨.
                popupWindow.setTouchable(true);
                popupWindow.setFocusable(true); //팝업 뷰 포커스도 주고
                popupWindow.update();
                popupWindow.setOutsideTouchable(true); //팝업 뷰 이외에도 터치되게 (터치시 팝업 닫기 위한 코드)
                popupWindow.setBackgroundDrawable(new BitmapDrawable());

                popupWindow.showAsDropDown(btnAlignment, 0, -310);

                TextView tvLeft = inflateView.findViewById(R.id.tv_write_activity_menu_align_ledt);
                TextView tvCenter = inflateView.findViewById(R.id.tv_write_activity_menu_align_center);
                TextView tvRight = inflateView.findViewById(R.id.tv_write_activity_menu_align_right);

                tvLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        richEditor.setAlignLeft();
                    }
                });

                tvCenter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        richEditor.setAlignCenter();
                    }
                });

                tvRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        richEditor.setAlignRight();
                    }
                });
            }
        });

        btnBold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (boldSwitch == false) {
                    boldSwitch = true;
                    btnBold.setImageResource(ic_bold_selected);
                    richEditor.setBold();
                } else {
                    boldSwitch = false;
                    btnBold.setImageResource(ic_bold_solid);
                    richEditor.setBold();
                }
            }
        });

        btnCancelLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cancelLineSwitch == false) {
                    cancelLineSwitch = true;
                    btnCancelLine.setImageResource(ic_strikethrough_selected);
                    richEditor.setStrikeThrough();
                } else {
                    cancelLineSwitch = false;
                    btnCancelLine.setImageResource(ic_strikethrough_solid);
                    richEditor.setStrikeThrough();

                }
            }
        });

        btnUnderLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (underLineSwitch == false) {
                    underLineSwitch = true;
                    btnUnderLine.setImageResource(ic_underline_selected);
                    richEditor.setUnderline();
                } else {
                    underLineSwitch = false;
                    btnUnderLine.setImageResource(ic_underline_solid);
                    richEditor.setUnderline();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        btnBack.callOnClick();
        super.onBackPressed();
    }
}