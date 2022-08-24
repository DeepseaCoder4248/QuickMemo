package com.puresoftware.quickmemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.puresoftware.quickmemo.artifacts.Folder;
import com.puresoftware.quickmemo.drawer.UserFolderAdapter;
import com.puresoftware.quickmemo.room.AppDatabase;
import com.puresoftware.quickmemo.room.Memo;
import com.puresoftware.quickmemo.room.MemoDao;
import com.puresoftware.quickmemo.room.UserFolder;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.stream.Collectors;

import jp.wasabeef.richeditor.RichEditor;

public class MainActivity extends Activity {
    MainActivity activity;

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
    ImageView imgbtnFolder;
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

    // drawer main
    LinearLayout linDrawerMain;
    ImageView imgDrawerMain;
    TextView tvDrawerMain;
    TextView tvDrawerMainTitle;

    // drawer impo
    LinearLayout linDrawerImpo;
    ImageView imgDrawerImpo;
    TextView tvDrawerImpo;
    TextView tvDrawerImpoTitle;

    // drawer trash
    LinearLayout linDrawerTrash;
    ImageView imgDrawerTrash;
    TextView tvDrawerTrash;
    TextView tvDrawerTrashTitle;

    ListView lvDrawerItem;
    ImageView ivDrawerAddFolder;
    int drawerSwitch = 0;

    public static Map<String, Boolean> folderSelect = new HashMap<>();

    // recycler
    RecyclerView recyclerView;
    Adapter adapter;

    // adapter
    List<Memo> memos; // 휴지통이 포함되지 않은 메모
    List<Memo> trashMemos; // 휴지통이 포함된 메모
    List<Memo> onlyTrashMemos; // 휴지통만 있는 메모
    List<Memo> starList;
    List<Memo> folderMemos;
    List<UserFolder> folders;
    UserFolder folder;
    UserFolderAdapter userFolderAdapter;
    Memo lastMemo;
    Memo secondMemo;
    Memo memo;
    TextView tvMainCardCount;

    // select mode
    boolean selectMode = false;
    ArrayList<String> set = new ArrayList<>(); // 받을 때는 String, 뺄 때는 int

    // first view
    TextView tvTopCardTitle;
    RichEditor tvTopCardContent;
    TextView tvTopCardDate;
    TextView tvTopCardLock;

    // secondview
    TextView tvImportantCardTitle;
    TextView tvImportantCardDate;
    TextView tvImportantCardLock;
    RichEditor tvImportantCardContent;

    // Threads
    Thread memoDaoThread;
    Thread topCardThread;
    Thread deleteThread;

    String TAG = MainActivity.class.getSimpleName();

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;

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
        imgbtnFolder = findViewById(R.id.btn_main_sel_folder);

        // DrawerLayout 내 오브젝트
        tvDrawerTitle = drawerView.findViewById(R.id.tv_main_drawer_custom_ID);
        tvDrawerEmail = drawerView.findViewById(R.id.tv_main_drawer_custom_Email);
        btnDrawerSettings = drawerView.findViewById(R.id.btn_main_drawer_custom_settings);

        // DrawerLayout 내 아이템 오브젝트
        linDrawerMain = findViewById(R.id.lin_main_drawer_main_folder);
        imgDrawerMain = findViewById(R.id.iv_main_drawer_main_folder);
        tvDrawerMain = findViewById(R.id.iv_main_drawer_main_count);
        tvDrawerMainTitle = findViewById(R.id.iv_main_drawer_main_title);
        linDrawerImpo = findViewById(R.id.lin_main_drawer_impo_folder);
        imgDrawerImpo = findViewById(R.id.iv_main_drawer_impo_folder);
        tvDrawerImpo = findViewById(R.id.iv_main_drawer_impo_count);
        tvDrawerImpoTitle = findViewById(R.id.iv_main_drawer_impo_title);
        linDrawerTrash = findViewById(R.id.lin_main_drawer_trash_folder);
        imgDrawerTrash = findViewById(R.id.iv_main_drawer_trash_folder);
        tvDrawerTrash = findViewById(R.id.iv_main_drawer_trash_count);
        tvDrawerTrashTitle = findViewById(R.id.iv_main_drawer_trash_title);
        lvDrawerItem = findViewById(R.id.lv_main_activity_drawer_list_item);
        ivDrawerAddFolder = findViewById(R.id.iv_main_drawer_add_folder);


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

        // 드로우어블 메뉴의 가장 초기모드
        linDrawerMain.setBackgroundResource(R.drawable.round_retengle_main);
        tvDrawerMainTitle.setTextColor(Color.parseColor("#862FFF"));
        tvDrawerMain.setTextColor(Color.parseColor("#862FFF"));
        linDrawerImpo.setBackground(null);
        linDrawerTrash.setBackground(null);
        folderSelect = new HashMap<>();
        drawerSwitch = 1;

        // RoomDB 메모내용 불러오기
        AppDatabase db = AppDatabase.getInstance(this);
        MemoDao memoDao = db.dao();

        // 데이터 초기화
        memos = new ArrayList<>(); // unSupportedle머시기exception으로 인해 추가.
        starList = new ArrayList<>();
        trashMemos = new ArrayList<>();
        onlyTrashMemos = new ArrayList<>();
        userFolderAdapter = new UserFolderAdapter();
        userFolderAdapter.setActivity(activity);
        folders = new ArrayList<>();

        // Room DB 불러오기
        memoDaoThread = new Thread(new Runnable() {
            @Override
            public void run() {
                trashMemos = memoDao.getAll(); // 휴지통 포함해서 받아옴.
                memos = memoDao.getNotTrashAll(false); // 휴지통이 있는 거 빼고 받아옴.
                onlyTrashMemos = memoDao.getNotTrashAll(true); // 휴지통에 있는 것만 받아옴

                // 배포할 때에는 이 코드 끄기.
                for (Memo memo : memos) {
                    Log.i(TAG, "memoDatas:" + memo.toString());
                }

                folders = memoDao.getFolderAll();
                for (com.puresoftware.quickmemo.room.UserFolder folder : folders) {
                    Log.i(TAG, "folderDatas:" + folder.toString());
                }
            }
        });
        memoDaoThread.start();

        // Top 카드 메뉴
        if (linTopcard1 == null || linTopcard2 == null) {
            linTopcard1 = findViewById(R.id.lin_main_infate_top_card1);
            linTopcard2 = findViewById(R.id.lin_main_infate_top_card2);
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

            firstView = inflater.inflate(R.layout.main_top_card_item, linTopcard1, true);
            secondView = inflater.inflate(R.layout.main_top_card_item_important, linTopcard2, true);
        }

        topCardThread = new Thread(new Runnable() {
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
                tvTopCardTitle = firstView.findViewById(R.id.tv_main_last_card_title);
                tvTopCardDate = firstView.findViewById(R.id.tv_main_last_card_date);
                tvTopCardLock = firstView.findViewById(R.id.tv_main_top_card_last_lock);
                tvTopCardContent = firstView.findViewById(R.id.tv_main_last_card_content);
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
                tvImportantCardTitle = secondView.findViewById(R.id.tv_main_impo_card_title);
                tvImportantCardDate = secondView.findViewById(R.id.tv_main_impo_card_date);
                tvImportantCardLock = secondView.findViewById(R.id.tv_main_top_card_impor_lock);
                tvImportantCardContent = secondView.findViewById(R.id.tv_main_impo_card_content);
                tvImportantCardContent.setInputEnabled(false);

                // 리스트에서 필터링 할 때 사용
                // 실무 (Java8, 람다, steam, filter)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    starList = memos.stream().filter(memo -> memo.star == true).collect(Collectors.toList());
                }
                Log.i("gugu", starList.size() + "처음의 리스트는");

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
        });
        topCardThread.start();

        // 카드 메뉴
        recyclerView = findViewById(R.id.rec_main_card);
        StaggeredGridLayoutManager staggeredGridLayoutManager
                = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        adapter = new Adapter(getBaseContext());

        if (memos.size() > 0) {

            tvMainCardCount.setText(memos.size() + "개의 메모");
            vEmpty.setVisibility(View.GONE); // 비어 있음 이미지 끄기

            for (int i = 0; i < memos.size(); i++) {

                memo = new Memo();
                memo.uid = memos.get(i).uid;
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
        adapter.filterStart(memos);

//        // 테스트
//        Log.i("gugu", "memos에서 가져온 0번:" + memos.get(0).toString());
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Log.i("gugu", "dao에서 가져온 0번:" + memoDao.getNotTrashAll(false));
//            }
//        }).start();


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

                    // trash 모드면 휴지통과 폴더, 아니면 복원과 삭제
                    if (drawerSwitch == 3) {
                        imgbtnTrash.setImageResource(R.drawable.ic_restore);
                        imgbtnFolder.setImageResource(R.drawable.ic_real_delete);
                    } else {
                        imgbtnTrash.setImageResource(R.drawable.ic_select_trash);
                        imgbtnFolder.setImageResource(R.drawable.ic_select_folder);
                    }
                }
            }
        });

        // false는 일반모드, true는 선택모드
        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Memo clickmemo = adapter.datas.get(position);

                Log.i(TAG, selectMode + "모드임");
                Log.i("gugu", "선택된 click memo" + clickmemo.toString());

                // 일반모드
                if (selectMode == false) {
                    lockContentTop(clickmemo);

                    // 선택모드
                } else {
                    // 선택 로직, String으로 받지 않으면 에러 발생..
                    holder = (MainViewHolder) recyclerView.findViewHolderForAdapterPosition(position);

                    if (!set.contains(String.valueOf(position))) {
                        set.add(String.valueOf(position));
                        holder.cardBgLayout.setBackground(AppCompatResources.getDrawable(getBaseContext(), R.drawable.delete));

                    } else {
                        set.remove(String.valueOf(position));
                        holder.cardBgLayout.setBackground(AppCompatResources.getDrawable(getBaseContext(), R.drawable.home_memo_ex));
                    }
                    tvSelCount.setText(set.size() + "개 선택됨");

                    // log
                    Log.i(TAG, "size:" + set.size() + '\n');
                    Log.i(TAG, "item:" + clickmemo.toString());
                    for (Object object : set) {
                        Log.i(TAG, object + "아이템");
                    }

                    // 폴더 이동 입력
                    imgbtnFolder.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            // trash모드면, 삭제, 아니면 폴더 이동
                            if (drawerSwitch == 3) {

                                // Todo: 220817, thread가 안먹히므로, dao.delete가 문제인 것인지, thread 자체가 문제인지 확인 완료!
//                                 1) 삭제할 메모의 인덱스 탐색
                                for (int i = 0; i < set.size(); i++) {
                                    Memo deleteMemo = onlyTrashMemos.get(Integer.parseInt(set.get(i))); // 선택된 메모의 포지션 가져오기.

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            memoDao.delete(deleteMemo);
                                            Log.i(TAG, deleteMemo.toString());
                                        }
                                    }).start();
                                }
                                set.clear();

                                try {
                                    // 3) memoDaoThread start하기
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            memos.clear();
                                            memos = memoDao.getNotTrashAll(false);

                                            // 배포할 때에는 이 코드 끄기.
                                            for (Memo memo : memos) {
                                                Log.i(TAG, "memoDatas:" + memo.toString());
                                            }
                                        }
                                    }).join();

                                    // 4) topCardThread start하기
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
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
                                            tvTopCardTitle = firstView.findViewById(R.id.tv_main_last_card_title);
                                            tvTopCardDate = firstView.findViewById(R.id.tv_main_last_card_date);
                                            tvTopCardLock = firstView.findViewById(R.id.tv_main_top_card_last_lock);
                                            tvTopCardContent = firstView.findViewById(R.id.tv_main_last_card_content);
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
                                            tvImportantCardTitle = secondView.findViewById(R.id.tv_main_impo_card_title);
                                            tvImportantCardDate = secondView.findViewById(R.id.tv_main_impo_card_date);
                                            tvImportantCardLock = secondView.findViewById(R.id.tv_main_top_card_impor_lock);
                                            tvImportantCardContent = secondView.findViewById(R.id.tv_main_impo_card_content);
                                            tvImportantCardContent.setInputEnabled(false);

                                            List<Memo> starList = new ArrayList<>();
//
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
                                    });

                                    // 5) adapter 갱신
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (memos.size() > 0) {
                                                tvMainCardCount.setText(memos.size() + "개의 메모");
                                                vEmpty.setVisibility(View.GONE); // 비어 있음 이미지 끄기

                                                adapter.refreshData(memos);
                                            } else {
                                                tvMainCardCount.setText(0 + "개의 메모");
                                                vEmpty.setVisibility(View.VISIBLE); // 비어있음 이미지 켜기
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                }
                                linDrawerTrash.setBackground(null);
                                linDrawerImpo.setBackground(null);
                                linDrawerMain.setBackgroundResource(R.drawable.round_retengle_main);
                                tvDrawerMainTitle.setTextColor(Color.parseColor("#862FFF"));
                                tvDrawerMain.setTextColor(Color.parseColor("#862FFF"));
                                tvDrawerImpo.setTextColor(Color.parseColor("#DE000000"));
                                tvDrawerImpoTitle.setTextColor(Color.parseColor("#DE000000"));
                                tvDrawerTrash.setTextColor(Color.parseColor("#DE000000"));
                                tvDrawerTrashTitle.setTextColor(Color.parseColor("#DE000000"));

                                // 폴더 사이즈가 없으면 사용이 불가능한 코드로 검증추가함.
                                if (userFolderAdapter.getItemSize() == 0) {
                                } else {
                                    userFolderAdapter.setBackground(false);
                                }
                                drawerSwitch = 1;
                                onBackPressed();

                            } else {
                                // https://youngest-programming.tistory.com/50
                                // 선택된 포지션들을 폴더 액티비티로
                                Intent intent = new Intent(MainActivity.this, SelectFolderActivity.class);
                                Bundle bundle = new Bundle(); // 직접 만든 배열 클래스가 아니라면, Bundle로 보내버리자.
                                bundle.putStringArrayList("set", set);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                        }
                    });

                    // Todo: 220818, 빌어먹을! memoDao의 가져오는 위치와 memos객체에서 가져오는 위치가 서로 다르다! +1정도 차이
                    // 입력모드 2개인 폴더 이동과 삭제.
                    imgbtnTrash.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            deleteThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    // 1) 삭제할 메모의 인덱스 탐색
                                    Log.d(TAG, "SET Size: " + set.size());
                                    Log.d(TAG, "SET: " + set);
                                    for (int i = 0; i < set.size(); i++) {
                                        final int idx = Integer.parseInt(set.get(i));

                                        // main - 1 impo -2  trash - 3

                                        // trash모드가 아니면 휴지통, 아니면 복원
                                        Log.d(TAG, "Trash is null?? " + linDrawerTrash.getBackground() + " Now: " + drawerSwitch);
                                        if (drawerSwitch == 3) {

                                            try {
                                                Memo recoveryMemo = onlyTrashMemos.stream().filter(memo -> memo.uid == adapter.getItem(idx).getUid()).findFirst().get();
                                                Log.d(TAG, "Recovery Memo: " + recoveryMemo);
                                                onlyTrashMemos.remove(recoveryMemo);
                                                memoDao.updateTrash(false, recoveryMemo.getUid());

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                        } else {

                                            try {
                                                Memo deleteMemo = memos.stream().filter(memo -> memo.uid == adapter.getItem(idx).getUid()).findFirst().get();
                                                Log.d(TAG, "Delete Memo: " + deleteMemo);
                                                memos.remove(deleteMemo); //// 이녀석이 문제임.
                                                memoDao.updateTrash(true, deleteMemo.getUid());
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    set.clear();

                                    try {
                                        // 3) memoDaoThread start하기
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                memos.clear();
                                                memos = memoDao.getNotTrashAll(false);

                                                // 배포할 때에는 이 코드 끄기.
                                                for (Memo memo : memos) {
                                                    Log.i(TAG, "memoDatas:" + memo.toString());
                                                }

                                                // 4) topCardThread start하기
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
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
                                                        tvTopCardTitle = firstView.findViewById(R.id.tv_main_last_card_title);
                                                        tvTopCardDate = firstView.findViewById(R.id.tv_main_last_card_date);
                                                        tvTopCardLock = firstView.findViewById(R.id.tv_main_top_card_last_lock);
                                                        tvTopCardContent = firstView.findViewById(R.id.tv_main_last_card_content);
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
                                                        tvImportantCardTitle = secondView.findViewById(R.id.tv_main_impo_card_title);
                                                        tvImportantCardDate = secondView.findViewById(R.id.tv_main_impo_card_date);
                                                        tvImportantCardLock = secondView.findViewById(R.id.tv_main_top_card_impor_lock);
                                                        tvImportantCardContent = secondView.findViewById(R.id.tv_main_impo_card_content);
                                                        tvImportantCardContent.setInputEnabled(false);

                                                        List<Memo> starList = new ArrayList<>();
//
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
                                                });

                                                // 5) adapter 갱신
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (memos.size() > 0) {
                                                            tvMainCardCount.setText(memos.size() + "개의 메모");
                                                            vEmpty.setVisibility(View.GONE); // 비어 있음 이미지 끄기

                                                            adapter.refreshData(memos);
                                                        } else {
                                                            tvMainCardCount.setText(0 + "개의 메모");
                                                            vEmpty.setVisibility(View.VISIBLE); // 비어있음 이미지 켜기
                                                        }
                                                        onBackPressed();

                                                        linDrawerTrash.setBackground(null);
                                                        linDrawerImpo.setBackground(null);
                                                        linDrawerMain.setBackgroundResource(R.drawable.round_retengle_main);
                                                        tvDrawerMainTitle.setTextColor(Color.parseColor("#862FFF"));
                                                        tvDrawerMain.setTextColor(Color.parseColor("#862FFF"));
                                                        tvDrawerImpo.setTextColor(Color.parseColor("#DE000000"));
                                                        tvDrawerImpoTitle.setTextColor(Color.parseColor("#DE000000"));
                                                        tvDrawerTrash.setTextColor(Color.parseColor("#DE000000"));
                                                        tvDrawerTrashTitle.setTextColor(Color.parseColor("#DE000000"));
                                                        // 폴더 사이즈가 없으면 사용이 불가능한 코드로 검증추가함.
                                                        if (userFolderAdapter.getItemSize() == 0) {
                                                        } else {
                                                            userFolderAdapter.setBackground(false);
                                                        }
                                                        drawerSwitch = 1;
                                                    }
                                                });
                                            }
                                        }).start();

//                                        // 4) topCardThread start하기
//                                        runOnUiThread(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                // data
//                                                long recentStamp = 0;
//                                                long imporRecentStamp = 0;
//                                                //향상된 for문
//
//                                                if (memos.size() <= 0) {
//                                                    linTopcard1.setVisibility(View.GONE);
//                                                    linTopcard2.setVisibility(View.GONE);
//
//                                                    return;
//                                                }
//                                                lastMemo = memos.get(memos.size() - 1);
//                                                recentStamp = lastMemo.timestamp;
//
//                                                Log.i("lock", "lock" + lastMemo.lock);
//
//                                                // firstView
//                                                tvTopCardTitle = firstView.findViewById(R.id.tv_main_last_card_title);
//                                                tvTopCardDate = firstView.findViewById(R.id.tv_main_last_card_date);
//                                                tvTopCardLock = firstView.findViewById(R.id.tv_main_top_card_last_lock);
//                                                tvTopCardContent = firstView.findViewById(R.id.tv_main_last_card_content);
//                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd a H:mm", Locale.KOREA);
//
//                                                if (lastMemo.lock == true) {
//                                                    tvTopCardTitle.setText(lastMemo.title);
//                                                    tvTopCardContent.setHtml("");
//                                                    tvTopCardDate.setText(sdf.format(lastMemo.timestamp));
//                                                    tvTopCardLock.setVisibility(View.VISIBLE);
//
//                                                } else {
//                                                    tvTopCardTitle.setText(lastMemo.title);
//                                                    tvTopCardContent.setHtml(lastMemo.content);
//                                                    tvTopCardDate.setText(sdf.format(lastMemo.timestamp));
//                                                    tvTopCardLock.setVisibility(View.GONE);
//                                                }
//
//                                                tvTopCardContent.setInputEnabled(false);
//
//                                                // secondView
//                                                tvImportantCardTitle = secondView.findViewById(R.id.tv_main_impo_card_title);
//                                                tvImportantCardDate = secondView.findViewById(R.id.tv_main_impo_card_date);
//                                                tvImportantCardLock = secondView.findViewById(R.id.tv_main_top_card_impor_lock);
//                                                tvImportantCardContent = secondView.findViewById(R.id.tv_main_impo_card_content);
//                                                tvImportantCardContent.setInputEnabled(false);
//
//                                                List<Memo> starList = new ArrayList<>();
////
//                                                // 리스트에서 필터링 할 때 사용
//                                                // 실무 (Java8, 람다, steam, filter)
//                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                                                    starList = memos.stream().filter(memo -> memo.star == true).collect(Collectors.toList());
//                                                }
//
//                                                if (starList.size() > 0) { // 데이터가 1개 이상이면 메모 전시, 없으면 메모 삭제.
//
//                                                    secondMemo = starList.get(starList.size() - 1);
//
//                                                    if (secondMemo.lock == true) {
//                                                        tvImportantCardLock.setVisibility(View.VISIBLE);
//                                                        tvImportantCardTitle.setText(secondMemo.title);
//                                                        tvImportantCardDate.setText(sdf.format(secondMemo.timestamp));
//                                                        tvImportantCardLock.setVisibility(View.VISIBLE);
//
//                                                    } else {
//                                                        tvImportantCardTitle.setText(secondMemo.title);
//                                                        tvImportantCardDate.setText(sdf.format(secondMemo.timestamp));
//                                                        tvImportantCardContent.setHtml(secondMemo.content);
//                                                        tvImportantCardLock.setVisibility(View.GONE);
//                                                    }
//                                                    linTopcard2.setVisibility(View.VISIBLE); // 메모 전시
//
//                                                } else {
//                                                    linTopcard2.setVisibility(View.GONE); // 메모 비전시
//                                                }
//                                            }
//                                        });


                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Log.d(TAG, e.getMessage());
                                    }
                                }
                            });
                            deleteThread.start();

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
                adapter.getFilter().filter(""); // 실행하자말자 필터링 초기값을 받아야 함.

                // https://stackoverflow.com/a/51656872
                // 서치창 아무데나 누르면 기능 실행. setIconified가 뭘까. 검색 모드를 홀드 아니면 확장관련인것 같다.
                edtContentSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        edtContentSearch.setIconified(false);
                        adapter.getFilter().filter(""); // 실행하자말자 필터링 초기값을 받아야 함.
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
                        adapter.getFilter().filter(newText); // 초기값을 위해서라도 받아야 함.

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
                // 설정 버튼
                btnDrawerSettings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // 폴더 선택하는 액티비티 감
                        Intent intent = new Intent(MainActivity.this, AppSettingsActivity.class);
                        intent.putStringArrayListExtra("set", set);
                        startActivity(intent);
                    }
                });

                starList = new ArrayList<>();
                starList = memos.stream().filter(Memo -> Memo.star == true).collect(Collectors.toList());

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        // 메모장을 누를 때마다 새로 갱신하기(임시)
                        memos = new ArrayList<>(); // unSupportedle머시기exception으로 인해 추가.
                        trashMemos = new ArrayList<>();
                        onlyTrashMemos = new ArrayList<>();

                        trashMemos = memoDao.getAll(); // 휴지통 포함해서 받아옴.
                        memos = memoDao.getNotTrashAll(false); // 휴지통이 있는 거 빼고 받아옴.
                        onlyTrashMemos = memoDao.getNotTrashAll(true); // 휴지통만
                        folders = memoDao.getFolderAll();

                        for (UserFolder folder : folders) {
                            Log.i(TAG, "folderDatas:" + folder.toString());
                        }

                        if (starList.size() > 0) {
                            tvDrawerImpo.setText(starList.size() + "");
                        } else {
                            tvDrawerImpo.setText(0 + "");
                        }

                        if (memos.size() > 0) {
                            tvDrawerMain.setText(memos.size() + "");
                        } else {
                            tvDrawerMain.setText(0 + "");
                        }

                        if (onlyTrashMemos.size() > 0) {
                            tvDrawerTrash.setText(onlyTrashMemos.size() + "");
                        } else {
                            tvDrawerTrash.setText(0 + "");
                        }
                    }
                }).start();


                // 드로우어블 뒤의 모든게 터치되므로 추가함.
                drawerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });

                // Todo: 220816
                linDrawerMain.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        adapter.datas = new ArrayList<>();
                        for (int i = 0; i < memos.size(); i++) {

                            memo = new Memo();
                            memo.title = memos.get(i).title;
                            memo.content = memos.get(i).content;
                            memo.timestamp = memos.get(i).timestamp;
                            memo.lock = memos.get(i).lock;
                            memo.star = memos.get(i).star;

                            adapter.setArrayData(memo);
                        }

                        if (memos.size() == 0) {
                            vEmpty.setVisibility(View.VISIBLE); // 비어있음 이미지 켜기
                            tvMainCardCount.setText("0" + "개의 메모");
                        } else {
                            vEmpty.setVisibility(View.GONE); // 비어있음 이미지 켜기
                            adapter.notifyDataSetChanged();
                        }
                        // 드로우어블 메뉴의 메인
                        folderSelect.keySet().forEach(s -> folderSelect.put(s, false));
                        linDrawerMain.setBackgroundResource(R.drawable.round_retengle_main);
                        linDrawerImpo.setBackground(null);
                        linDrawerTrash.setBackground(null);
                        tvDrawerMainTitle.setTextColor(Color.parseColor("#862FFF"));
                        tvDrawerMain.setTextColor(Color.parseColor("#862FFF"));
                        tvDrawerImpo.setTextColor(Color.parseColor("#DE000000"));
                        tvDrawerImpoTitle.setTextColor(Color.parseColor("#DE000000"));
                        tvDrawerTrash.setTextColor(Color.parseColor("#DE000000"));
                        tvDrawerTrashTitle.setTextColor(Color.parseColor("#DE000000"));
                        // 폴더 사이즈가 없으면 사용이 불가능한 코드로 검증추가함.
                        if (userFolderAdapter.getItemSize() == 0) {
                        } else {
                            userFolderAdapter.setBackground(false);
                        }
                        drawerSwitch = 1;
                        menuNavi.closeDrawer(drawerView);
                        tvMainCardCount.setText(memos.size() + "개의 메모");

                        Log.i(TAG, "메인");
                    }
                });

                linDrawerImpo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // 어댑터로 업데이트
                        folderMemos = new ArrayList<>();
                        folderMemos = memos.stream().filter(memo -> memo.star == true).collect(Collectors.toList());

                        if (folderMemos.size() == 0) {
                            vEmpty.setVisibility(View.VISIBLE); // 비어있음 이미지 켜기
                            tvMainCardCount.setText("0" + "개의 메모");

                        } else {
                            vEmpty.setVisibility(View.GONE);
                            adapter.setArrayDatas((ArrayList<Memo>) folderMemos);
                            adapter.notifyDataSetChanged();
                            tvMainCardCount.setText(starList.size() + "개의 메모");
                        }
                        // 드로우어블 메뉴의 중요
                        linDrawerMain.setBackground(null);
                        linDrawerImpo.setBackgroundResource(R.drawable.round_retengle_impo);
                        linDrawerTrash.setBackground(null);
                        tvDrawerMainTitle.setTextColor(Color.parseColor("#DE000000"));
                        tvDrawerMain.setTextColor(Color.parseColor("#DE000000"));
                        tvDrawerImpo.setTextColor(Color.parseColor("#FF2F2F"));
                        tvDrawerImpoTitle.setTextColor(Color.parseColor("#FF2F2F"));
                        tvDrawerTrash.setTextColor(Color.parseColor("#DE000000"));
                        tvDrawerTrashTitle.setTextColor(Color.parseColor("#DE000000"));

                        // 폴더 사이즈가 없으면 사용이 불가능한 코드로 검증추가함.
                        if (userFolderAdapter.getItemSize() == 0) {
                        } else {
                            userFolderAdapter.setBackground(false);
                        }
                        drawerSwitch = 2;
                        folderSelect.keySet().forEach(s -> folderSelect.put(s, false));
                        menuNavi.closeDrawer(drawerView);

                        Log.i(TAG, "중요");
                    }
                });

                linDrawerTrash.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (trashMemos.isEmpty()) {
                            vEmpty.setVisibility(View.VISIBLE);
                        } else {
                            // 어댑터로 업데이트
                            folderMemos = new ArrayList<>();
                            folderMemos = trashMemos.stream().filter(memo -> memo.isTrash == true).collect(Collectors.toList());
                            adapter.setArrayDatas((ArrayList<Memo>) folderMemos);
                            adapter.notifyDataSetChanged();
                            vEmpty.setVisibility(View.GONE);
                            tvMainCardCount.setText(folderMemos.size() + "개의 메모");

                        }
                        // 드로우어블 메뉴의 중요
                        linDrawerMain.setBackground(null);
                        linDrawerImpo.setBackground(null);
                        linDrawerTrash.setBackgroundResource(R.drawable.round_retengle_trash);
                        tvDrawerMainTitle.setTextColor(Color.parseColor("#DE000000"));
                        tvDrawerMain.setTextColor(Color.parseColor("#DE000000"));
                        tvDrawerImpo.setTextColor(Color.parseColor("#DE000000"));
                        tvDrawerImpoTitle.setTextColor(Color.parseColor("#DE000000"));
                        tvDrawerTrash.setTextColor(Color.parseColor("#2F7BFF"));
                        tvDrawerTrashTitle.setTextColor(Color.parseColor("#2F7BFF"));

                        // 폴더 사이즈가 없으면 사용이 불가능한 코드로 검증추가함.
                        if (userFolderAdapter.getItemSize() == 0) {
                        } else {
                            userFolderAdapter.setBackground(false);
                        }
                        drawerSwitch = 3;
                        folderSelect.keySet().forEach(s -> folderSelect.put(s, false));

                        menuNavi.closeDrawer(drawerView);

                        Log.i("gugu", "휴지통");
                    }
                });

                userFolderAdapter = new UserFolderAdapter();
                userFolderAdapter.setActivity(activity);
                folders = new ArrayList<>();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        folders = memoDao.getFolderAll();

                        for (UserFolder folder : folders) {
                            int folderCnt = memoDao.getFolderCount(folder.title);
                            Log.d(TAG, "Folder Count: " + folderCnt);
                            Folder item = new Folder(folder, folderCnt);
                            Log.i(TAG, "Folder Object:" + item.toString());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    userFolderAdapter.addItem(item);
                                }
                            });
                        }
                    }
                }).start();
                lvDrawerItem.setAdapter(userFolderAdapter);

                ivDrawerAddFolder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        folderSetting(userFolderAdapter, memoDao, -1);
                    }
                });

                lvDrawerItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Folder folder = (Folder) userFolderAdapter.getItem(i);

                        Log.d(TAG, "Item:" + folder.getFolderCnt());
                        if (folder.getFolderCnt() == 0) {
                            vEmpty.setVisibility(View.VISIBLE);
                        } else {
                            vEmpty.setVisibility(View.GONE);

                            folderMemos = memos.stream().filter(memo -> memo.folder.equals(folder.getFolder().title)).collect(Collectors.toList());

                            Log.d(TAG, "Object " + folderMemos);

                            adapter.setArrayDatas((ArrayList<Memo>) folderMemos);
                            adapter.notifyDataSetChanged();

                        }

                        // 드로우어블 메뉴의 중요
                        linDrawerMain.setBackground(null);
                        tvDrawerMainTitle.setTextColor(Color.parseColor("#DE000000"));
                        tvDrawerMain.setTextColor(Color.parseColor("#DE000000"));
                        linDrawerImpo.setBackground(null);
                        tvDrawerImpoTitle.setTextColor(Color.parseColor("#DE000000"));
                        tvDrawerImpo.setTextColor(Color.parseColor("#DE000000"));
                        linDrawerTrash.setBackground(null);
                        tvDrawerTrashTitle.setTextColor(Color.parseColor("#DE000000"));
                        tvDrawerTrash.setTextColor(Color.parseColor("#DE000000"));

                        drawerSwitch = 4;
                        for (String s : folderSelect.keySet()) {
                            folderSelect.put(s, false);
                        }
                        folderSelect.put(folder.getFolder().title, true);

                        menuNavi.closeDrawer(drawerView);
                        tvMainCardCount.setText(folder.getFolderCnt() + "개의 메모");
                    }
                });

                lvDrawerItem.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                        folderSetting(userFolderAdapter, memoDao, i);

                        return true; // 숏 터치와 롱터치 구분하는것.
                    }
                });
                menuNavi.openDrawer(drawerView);
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
                    assert holder != null;
                    holder.cardBgLayout.setBackground(AppCompatResources.getDrawable(getBaseContext(), R.drawable.home_memo_ex));
                    holder = (MainViewHolder) recyclerView.findViewHolderForAdapterPosition(Integer.parseInt(set.get(i)));
                }

            } else {
                super.onBackPressed(); // 종료 기능을 수행
            }
        }
    }

    public void folderSetting(UserFolderAdapter adapter, MemoDao dao, int position) {

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_drawer_edit_folder, null); // 다이얼그
        EditText edtFolderTitle = dialogView.findViewById(R.id.edt_main_activity_drawer_folder_title); // 제목
        TextView tvBtnFolderEdit = dialogView.findViewById(R.id.tv_main_actvitiy_drawer_folder_add); // 수정,추가
        TextView tvBtnFolderDelete = dialogView.findViewById(R.id.tv_main_actvitiy_drawer_folder_delete); // 삭제
        LinearLayout vMarginRight = dialogView.findViewById(R.id.v_drawer_dialog_margin_right); // 삭제 버튼에 있는 빈 공백

        // 다이어로그 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        Dialog dialog = builder.create();
        dialog.show();

        folder = new UserFolder(); // 폴더 초기화

        // View에 내용 뿌리기
        if (position > -1) {

            // 아이템이 있다면 수정,삭제 모드
            tvBtnFolderDelete.setVisibility(View.VISIBLE);
            tvBtnFolderEdit.setText("수정");
            vMarginRight.setVisibility(View.VISIBLE);

            // 포지션 데이터 받고 뷰에 뿌리기
            folder = ((Folder) adapter.getItem(position)).getFolder();
            edtFolderTitle.setText(folder.getTitle());
        } else {

            // 아이템이 없다면 추가 모드
            tvBtnFolderDelete.setVisibility(View.GONE);
            tvBtnFolderEdit.setText("추가");
            vMarginRight.setVisibility(View.GONE);
        }
        Log.i(TAG, folder.toString());

        // 추가 및 수정
        tvBtnFolderEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // add
                if (folder.title == null) {

                    // add면 새 데이터 넣는다.
                    folder.setTitle(edtFolderTitle.getText().toString());
                    folder.setTimestamp(System.currentTimeMillis());
                    Folder item = new Folder(folder, 0);
                    adapter.addItem(item);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            dao.insertFolder(folder);
                            // 폴더 리스트 다시 디비서 불러오기
                            folders = dao.getFolderAll();
                            List<Folder> newFolders = new ArrayList<>();
                            for (int i = 0; i < folders.size(); i++) {
                                int folderCnt = dao.getFolderCount(folder.title);
                                Folder folder = new Folder(folders.get(i), folderCnt);
                                // 가지고온 폴더 리스트를 어댑터에다가 다시 리프래시한다.
                                newFolders.add(folder);
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    userFolderAdapter.refreshItem(newFolders);
                                }
                            });
                        }
                    }).start();
                    Log.i(TAG, "folderSettings Add");

                    // updte
                } else {

                    // update면 dao 수정.
                    folder.setTitle(edtFolderTitle.getText().toString());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            dao.updateFolder(folder.title, folder.uid);
                            // 업데이트폴더명을 바꾸면 업데이트할 메모명들도 다 바꾸고 업데이트 해야 함.
                        }
                    }).start();
                    Log.i(TAG, "folderSettings Uptadte");
                }

                // 어댑터 업데이트. 사실 잘 되는 지 모름.
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

        // delete
        tvBtnFolderDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // delete면 dao 삭제.
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Delete!!! " + folder);
                        dao.deleteFolder(folder);

                        folders = dao.getFolderAll();
                        List<Folder> newFolders = new ArrayList<>();
                        for (int i = 0; i < folders.size(); i++) {
                            int folderCnt = dao.getFolderCount(folder.title);
                            Folder folder = new Folder(folders.get(i), folderCnt);
                            // 가지고온 폴더 리스트를 어댑터에다가 다시 리프래시한다.
                            newFolders.add(folder);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                userFolderAdapter.refreshItem(newFolders);
                            }
                        });
                    }
                }).start();

                // 업데이트. 삭제는 인식을 받지 않아 어댑터 내의 데이터를 삭제.
                adapter.deleteItem(folder); // 어댑터 데이터 삭제

                dialog.dismiss();

                Log.i(TAG, "folderSettings delete touch");
            }
        });
    }

    // 상단 카드들의(중요,최근 카드) Lock 여부를 확인하여 그에 맞는 장소로 이동하기.
    public void lockContentTop(Memo memo) {

        // 인텐트 데이터를 모아놓기
        Intent intent = new Intent();
        intent.putExtra("title", memo.title);
        intent.putExtra("content", memo.content);
        intent.putExtra("timestamp", memo.timestamp);
        intent.putExtra("star", memo.star);
        intent.putExtra("lock", memo.lock);

        Log.i("gugu", "지금 보낼메모:" + memo.toString());

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