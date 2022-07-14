package com.puresoftware.quickmemo;

import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.puresoftware.quickmemo.room.AppDatabase;
import com.puresoftware.quickmemo.room.Memo;
import com.puresoftware.quickmemo.room.MemoDao;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingDeque;
import java.util.stream.Collectors;

import jp.wasabeef.richeditor.RichEditor;

public class MainActivity extends Activity {


    // top
    LinearLayout linTopcard1;
    LinearLayout linTopcard2;
    View firstView;
    View secondView;

    // serch
    FrameLayout frActivitySearchBar;
    SearchView edtContentSearch;
    TextView tvSearchBarHintMessage;

    // select
    LinearLayout linSelbar;
    TextView tvSelCount;
    ImageView imgbtnTrash;
    ImageView imgbtnFunc;
    MainViewHolder holder;

    // main
    LinearLayout linActivityBar;
    ImageView btnMenu;
    FloatingActionButton fbtnWrite;
    ImageView btnSearch;
    ImageView vEmpty;
    AppBarLayout appBarLayout;

    // drawer
    DrawerLayout menuNavi;
    View drawerView;
    TextView tvDrawerTitle;
    TextView tvDrawerEmail;
    ImageView btnDrawerSettings;

    // recycler
    RecyclerView recyclerView;
    Adapter adapter;

    // adapter
    List<Memo> memos;
    Memo lastMemo;
    Memo secondMemo;
    Memo memo;
    TextView tvMainCardCount;

    // select mode
    boolean selectMode = false;
    ArrayList<String> set = new ArrayList<>(); // 받을 때는 String, 뺄 때는 int

    String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnMenu = findViewById(R.id.btn_main_menu);
        menuNavi = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        drawerView = (View) findViewById(R.id.v_main_drawer);
        fbtnWrite = findViewById(R.id.fbtn_main_write);
        btnSearch = findViewById(R.id.btn_main_search);
        tvMainCardCount = findViewById(R.id.tv_main_card_count);
        vEmpty = findViewById(R.id.v_main_empty);
        appBarLayout = findViewById(R.id.appbar_layout);

        // 일반 모드와 검색 모드를 위한 것.
        linActivityBar = findViewById(R.id.lay_main_activity_bar);
        frActivitySearchBar = findViewById(R.id.lay_main_activity_searchbar);
        edtContentSearch = findViewById(R.id.edt_main_activity_search);
        tvSearchBarHintMessage = findViewById(R.id.tv_main_activity_search_hint_object);

        // 선택 모드
        linSelbar = findViewById(R.id.lay_main_activity_sel_bar);
        tvSelCount = findViewById(R.id.tv_main_card_sel_count);
        imgbtnTrash = findViewById(R.id.btn_main_sel_trash);
        imgbtnFunc = findViewById(R.id.btn_main_sel_func);

        // DrawerLayout 내 오브젝트
        tvDrawerTitle = drawerView.findViewById(R.id.tv_main_drawer_custom_ID);
        tvDrawerEmail = drawerView.findViewById(R.id.tv_main_drawer_custom_Email);
        btnDrawerSettings = drawerView.findViewById(R.id.btn_main_drawer_custom_settings);

        // 앱을 처음 실행 할 때 가장 기본적인 데이터들(가입정보, 암호설정)
        SharedPreferences preferences = getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String appStarter = preferences.getString("starter", "yes"); // 이 앱을 깔고 처음 시작했을 때 확인 여부
        String PIN = preferences.getString("PIN", "no"); // 초기 비밀번호 설정
        Log.i(TAG, "Appstarter:" + appStarter + ",PIN:" + PIN);

        // 앱 스타터가 yes면 처음 실행한 것, 그 다음에 바로 no로 바꾸고, pin은 없다.
        if (appStarter.equals("yes")) {
            editor.putString("starter", "no");
            editor.putString("PIN", PIN);
            editor.commit();
        }

        // RoomDB 메모내용 불러오기
        AppDatabase db = AppDatabase.getInstance(this);
        MemoDao memoDao = db.dao();

        new Thread(new Runnable() {
            @Override
            public void run() {
                memos = memoDao.getAll();

                // 배포할 때에는 이 코드 끄기.
                for (Memo memo : memos) {
                    Log.i(TAG, "memoDatas:" + memo.toString());
                }
            }
        }).start();

        // Top 카드 메뉴
        if (linTopcard1 == null || linTopcard2 == null) {
            linTopcard1 = findViewById(R.id.lin_main_infate_top_card1);
            linTopcard2 = findViewById(R.id.lin_main_infate_top_card2);
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

            firstView = inflater.inflate(R.layout.main_top_card_item, linTopcard1, true);
            secondView = inflater.inflate(R.layout.main_top_card_item_important, linTopcard2, true);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                // 메모 불러오기

                // data
                long recentStamp = 0;
                long imporRecentStamp = 0;
                //향상된 for문

                if (memos.size() <= 0) {
                    linTopcard1.setVisibility(View.GONE);
                    linTopcard2.setVisibility(View.GONE);

                    return;
                }
                lastMemo = memos.get(memos.size() - 1);
                recentStamp = lastMemo.timestamp;

                Log.i("lock", "lock" + lastMemo.lock);

                // firstView
                TextView tvTopCardTitle = firstView.findViewById(R.id.tv_main_last_card_title);
                TextView tvTopCardDate = firstView.findViewById(R.id.tv_main_last_card_date);
                TextView tvTopCardLock = firstView.findViewById(R.id.tv_main_top_card_last_lock);
                RichEditor tvTopCardContent = firstView.findViewById(R.id.tv_main_last_card_content);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd a H:mm", Locale.KOREA);

                if (lastMemo.lock == true) {
                    tvTopCardTitle.setText(lastMemo.title);
                    tvTopCardContent.setHtml("");
                    tvTopCardDate.setText(sdf.format(lastMemo.timestamp));
                    tvTopCardLock.setVisibility(View.VISIBLE);

                } else {
                    tvTopCardTitle.setText(lastMemo.title);
                    tvTopCardContent.setHtml(lastMemo.content);
                    tvTopCardDate.setText(sdf.format(lastMemo.timestamp));
                    tvTopCardLock.setVisibility(View.GONE);
                }

                tvTopCardContent.setInputEnabled(false);

                // secondView
                TextView tvImportantCardTitle = secondView.findViewById(R.id.tv_main_impo_card_title);
                TextView tvImportantCardDate = secondView.findViewById(R.id.tv_main_impo_card_date);
                TextView tvImportantCardLock = secondView.findViewById(R.id.tv_main_top_card_impor_lock);
                RichEditor tvImportantCardContent = secondView.findViewById(R.id.tv_main_impo_card_content);
                tvImportantCardContent.setInputEnabled(false);


                List<Memo> starList = new ArrayList<>();
//
//                // 초보자용 코드
//                for(int i = 0; i<memos.size(); i++) {
//                    Memo memo = memos.get(i);
//
//                    if (memo.star == true) {
//                        starList.add(memo);
//                    }
//                }

                // 리스트에서 필터링 할 때 사용
                // 실무 (Java8, 람다, steam, filter)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    starList = memos.stream().filter(memo -> memo.star == true).collect(Collectors.toList());
                }

                if (starList.size() > 0) { // 데이터가 1개 이상이면 메모 전시, 없으면 메모 삭제.

                    secondMemo = starList.get(starList.size() - 1);

                    if (secondMemo.lock == true) {
                        tvImportantCardLock.setVisibility(View.VISIBLE);
                        tvImportantCardTitle.setText(secondMemo.title);
                        tvImportantCardDate.setText(sdf.format(secondMemo.timestamp));
                        tvImportantCardLock.setVisibility(View.VISIBLE);

                    } else {
                        tvImportantCardTitle.setText(secondMemo.title);
                        tvImportantCardDate.setText(sdf.format(secondMemo.timestamp));
                        tvImportantCardContent.setHtml(secondMemo.content);
                        tvImportantCardLock.setVisibility(View.GONE);
                    }
                    linTopcard2.setVisibility(View.VISIBLE); // 메모 전시

                } else {
                    linTopcard2.setVisibility(View.GONE); // 메모 비전시
                }
            }
        }).start();

        // 카드 메뉴
        recyclerView = findViewById(R.id.rec_main_card);
        StaggeredGridLayoutManager staggeredGridLayoutManager
                = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        adapter = new Adapter();

        if (memos.size() > 0) {
            tvMainCardCount.setText(memos.size() + "개의 메모");
            vEmpty.setVisibility(View.GONE); // 비어 있음 이미지 끄기

            for (int i = 0; i < memos.size(); i++) {

                memo = new Memo();
                memo.title = memos.get(i).title;
                memo.content = memos.get(i).content;
                memo.timestamp = memos.get(i).timestamp;
                memo.lock = memos.get(i).lock;
                memo.star = memos.get(i).star;
                adapter.setArrayData(memo);
            }

        } else {
            vEmpty.setVisibility(View.VISIBLE); // 비어있음 이미지 켜기
        }


        recyclerView.setAdapter(adapter);

        // 최근 카드 (last)
        linTopcard1.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                lockContentTop(lastMemo);
            }
        });

        // 중요 카드 (second)
        linTopcard2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lockContentTop(secondMemo);
            }
        });

        // 메인카드 롱 클릭 리스너. 꾹 누르면 선택 모드로 진입, 나가려면 onbackpressed
        adapter.setOnItemLongClickListener(new Adapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int position) {

                if (selectMode == false) {
                    selectMode = true;
                    set = new ArrayList<>();

                    linSelbar.setVisibility(View.VISIBLE);
                    frActivitySearchBar.setVisibility(View.GONE);
                    linActivityBar.setVisibility(View.GONE);
                    appBarLayout.setExpanded(false);
                    Toast.makeText(MainActivity.this, "선택모드", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // false는 일반모드, true는 선택모드
        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Memo memo = adapter.datas.get(position);

                if (selectMode == false) {
                    lockContentTop(memo);

                } else {

                    // 선택 로직, String으로 받지 않으면 에러 발생..
                    holder = (MainViewHolder) recyclerView.findViewHolderForAdapterPosition(position);

                    if (!set.contains(String.valueOf(position))) {
                        set.add(String.valueOf(position));
                        holder.ivMainCardDelete.setVisibility(View.VISIBLE);

                    } else {
                        set.remove(String.valueOf(position));
                        holder.ivMainCardDelete.setVisibility(View.GONE);
                    }
                    tvSelCount.setText(set.size() + "개 선택됨");

                    Log.i(TAG, "size:" + set.size() + '\n');

                    for (Object object : set) {
                        Log.i(TAG, object + "아이템");
                    }

                    // 입력모드 2개인 폴더 이동과 삭제.
                    imgbtnTrash.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // set은 선택된 메모들의 포지션
                            // memos는 전체 메모들

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    for (int i = 0; i < set.size(); i++) {
                                        Memo deleteMemo = memos.get(Integer.parseInt(set.get(i))); // 선택된 메모의 포지션 가져오기.
                                        memoDao.delete(deleteMemo);
                                    }
                                }
                            }).start();
                        }
                    });
                }
            }
        });

//// 메인카드 클릭 리스너(데이터를 백 하기 위한 것인 듯)
//// https://lesslate.github.io/android/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EB%A6%AC%EC%82%AC%EC%9D%B4%ED%81%B4%EB%9F%AC%EB%B7%B0-%ED%81%B4%EB%A6%AD/
//// https://hzie-devlog.tistory.com/7
//        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position) {
//                Memo memo = adapter.datas.get(position);
//                lockContentTop(memo);
//            }
//        });

        // 검색
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {

                // https://stackoverflow.com/questions/30655939/programmatically-collapse-or-expand-collapsingtoolbarlayout
                appBarLayout.setExpanded(false); // 단 두줄만에 해결된 애니메이션까지.

                // 검색창은 보이게, 메뉴창은 안보이게
                linActivityBar.setVisibility(View.GONE);
                linSelbar.setVisibility(View.GONE);
                frActivitySearchBar.setVisibility(View.VISIBLE);

//                https://stackoverflow.com/questions/11710042/expand-and-give-focus-to-searchview-automatically
                // 검색창을 자동으로 확장시켜주는 것.
                edtContentSearch.requestFocus(0);
                edtContentSearch.setIconified(false);

                // https://stackoverflow.com/a/51656872
                // 서치창 아무데나 누르면 기능 실행. setIconified가 뭘까. 검색 모드를 홀드 아니면 확장관련인것 같다.
                edtContentSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        edtContentSearch.setIconified(false);
                    }
                });

                // 입력을 받을 때.
                edtContentSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {

                        if (newText.trim().equals("")) {
                            tvSearchBarHintMessage.setVisibility(View.VISIBLE);
                        } else {
                            tvSearchBarHintMessage.setVisibility(View.INVISIBLE);
                        }

                        return false;
                    }
                });

                // 서치 중 내용이 없는 상태에서 x 버튼을 누르면 onBackPressed 메소드를 실행.
                edtContentSearch.setOnCloseListener(new SearchView.OnCloseListener() {
                    @Override
                    public boolean onClose() {
                        onBackPressed();
                        return false;
                    }
                });
            }
        });

        // 메뉴
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuNavi.openDrawer(drawerView);

                // 설정 버튼
                btnDrawerSettings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, AppSettingsActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });

        // 글쓰기 플로팅 버튼
        fbtnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, WriteActivity.class);
                startActivity(intent);
            }
        });
    }
    // onCreate

    // 그 외 기능
    // 백 버튼을 누를 때, 드로우메뉴가 있으면 드로우메뉴 취소, 없으면 앱 끝내기.
    // 드로우메뉴가 없을 때 검색창이 실행중이면 원래대로, 없으면 나가기.
    @Override
    public void onBackPressed() {

        if (menuNavi.isDrawerOpen(drawerView) == true) {
            menuNavi.closeDrawer(drawerView);
        } else {
            if (frActivitySearchBar.getVisibility() == View.VISIBLE) {
                frActivitySearchBar.setVisibility(View.GONE);
                linSelbar.setVisibility(View.GONE);
                linActivityBar.setVisibility(View.VISIBLE);
                appBarLayout.setExpanded(true);

            } else if (linSelbar.getVisibility() == View.VISIBLE) {
                linSelbar.setVisibility(View.GONE);
                frActivitySearchBar.setVisibility(View.GONE);
                linActivityBar.setVisibility(View.VISIBLE);
                appBarLayout.setExpanded(true);
                selectMode = false;

                // 이 반복문과 이 구조를 잊지 말것. 위치값을 받아오는 연습이 필요. , 뒤로 누르면 select된 이미지 모두 제거.
                for (int i = 0; i < set.size(); i++) {
                    holder.ivMainCardDelete.setVisibility(View.GONE);
                    holder = (MainViewHolder) recyclerView.findViewHolderForAdapterPosition(Integer.parseInt(set.get(i)));
                }

            } else {
                super.onBackPressed(); // 종료 기능을 수행
            }
        }
    }

    // 상단 카드들의(중요,최근 카드) Lock 여부를 확인하여 그에 맞는 장소로 이동하기.
    public void lockContentTop(Memo memo) {

        // 인텐트 데이터를 모아놓기
        Intent intent = new Intent();
        intent.putExtra("title", memo.title);
        intent.putExtra("content", memo.content);
        intent.putExtra("timestamp", memo.timestamp);
        intent.putExtra("lock", memo.star);
        intent.putExtra("star", memo.lock);

        // 락 모드가 true면 PIN을 입력하는 곳으로 가기.
        if (memo.lock == true) {
            intent.setClass(MainActivity.this, PINActivity.class);
            startActivity(intent);

            // 락 모드가 false면 바로 수정창으로 가기.
        } else {
            intent.setClass(MainActivity.this, EditActivity.class);
            startActivity(intent);
        }
    }
}