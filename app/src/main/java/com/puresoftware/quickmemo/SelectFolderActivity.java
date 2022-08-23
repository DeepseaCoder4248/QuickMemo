package com.puresoftware.quickmemo;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.puresoftware.quickmemo.artifacts.Folder;
import com.puresoftware.quickmemo.drawer.UserFolderAdapter;
import com.puresoftware.quickmemo.room.AppDatabase;
import com.puresoftware.quickmemo.room.Memo;
import com.puresoftware.quickmemo.room.MemoDao;
import com.puresoftware.quickmemo.room.UserFolder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SelectFolderActivity extends AppCompatActivity {
    SelectFolderActivity activity;

    LinearLayout linMainFolder;
    TextView tvMainCount;
    LinearLayout linImpoFolder;
    TextView tvImpoCount;
    ListView lvUserFolder;

    UserFolderAdapter adapter;
    List<UserFolder> folderList; // 전체 폴더
    UserFolder folder; // 선택한 폴더
    List<Memo> memoList; // 전체 메모
    ArrayList<String> setList = new ArrayList<>(); // 선택한 메모 카운트
    List<Memo> noneFolderList; // 폴더 없는 리스트(View 뿌리기 전용)
    List<Memo> starList; // 중요 폴더(View 뿌리기 전용)

    String TAG = SelectFolderActivity.class.getSimpleName();

    int beforeCount = -99;
    int beforeUid = -99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_folder);
        activity = this;

        linMainFolder = findViewById(R.id.lin_selectfolder_main_folder);
        tvMainCount = findViewById(R.id.tv_selectfolder_main_count);
        linImpoFolder = findViewById(R.id.lin_selectfolder_impo_folder);
        tvImpoCount = findViewById(R.id.tv_selectfolder_impo_count);
        lvUserFolder = findViewById(R.id.lv_selectfolder_user_folder);

        // RoomDB 메모내용 불러오기
        AppDatabase db = AppDatabase.getInstance(this);
        MemoDao memoDao = db.dao();

        folderList = new ArrayList<>();
        memoList = new ArrayList<>();

        // 폴더와 휴지통 없는 메모들 가져오기
        new Thread(new Runnable() {
            @Override
            public void run() {

                // 데이터 받기
                folderList = memoDao.getFolderAll();
                memoList = memoDao.getNotTrashAll(false);

                // 중요리스트,폴더없는 리스트 카운트
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    starList = new ArrayList<>();
                    starList = memoList.stream().filter(memo -> memo.star == true).collect(Collectors.toList());
                    noneFolderList = new ArrayList<>();
                    noneFolderList = memoList.stream().filter(memo -> memo.folder == null && memo.star == false).collect(Collectors.toList());
                }

                // view에 뿌리기
                tvImpoCount.setText(starList.size() + "");
                tvMainCount.setText(noneFolderList.size() + "");

                // adapter에 아이템 넣기
                adapter = new UserFolderAdapter();
                adapter.setActivity(activity);
                for (UserFolder folder : folderList) {
                    int folderCnt = memoDao.getFolderCount(folder.title);
                    Folder item = new Folder(folder, folderCnt);
                    adapter.addItem(item);
                }
                lvUserFolder.setAdapter(adapter);

                Log.i(TAG, "foldersize:" + folderList.size() + ",memosize:" + memoList.size());
            }
        }).start();

        // 인텐트된 포지션 데이터 가져오기(오류가 너무 많아 Bundle로 하기)
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        setList = bundle.getStringArrayList("set");

        for (int i = 0; i < setList.size(); i++) {
            Log.i(TAG, "list:" + setList.get(i) + "");
        }

        // 폴더명 없애기.
        linMainFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setFolder(memoList, setList, memoDao, folder, -2);

            }
        });

        linImpoFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setFolder(memoList, setList, memoDao, folder, -1);
            }
        });

        lvUserFolder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                folder = folderList.get(i);

                Log.i(TAG, "userFolderIndex: " + i);

                // memoList: 전체
                // setList: 선택한메모 인덱스들
                for (int j = 0; j < setList.size(); j++) {
                    Log.i(TAG, "선택한 메모인덱스: " + j);

                    Memo m = memoList.get(Integer.parseInt((String) setList.get(j)));
                    Log.i(TAG, "선택한메모: " + m);

                }
                setFolder(memoList, setList, memoDao, folder, i);
            }
        });
    }

    public void setFolder(List<Memo> memoList, ArrayList setList, MemoDao memoDao, UserFolder folder, int index) {

        // folder: 선택한폴더 (새로운 폴더)
        // old_folder: 기존에 메모가 저장된 폴더

        int moveSize = setList.size();
        ArrayList<Integer> beforeUids = new ArrayList<>();
        ArrayList<Integer> beforeCounts = new ArrayList<>();
        for (int i = 0; i < setList.size(); i++) { // 0,1,2,3
            Memo updateMemo = memoList.get(Integer.parseInt((String) setList.get(i)));
            UserFolder oldFolder = null;

            Log.i(TAG, "memo: " + updateMemo.toString());

            // 입력조건에 따른 폴더명이 변경됨(업데이트 라인)
            switch (index) {
                // 메인 폴더 인덱스
                case -2:
                    updateMemo.folder = null; // 조건문 추가
                    updateMemo.star = false;
                    break;

                // 중요 폴더 인덱스
                case -1:
                    updateMemo.folder = null;
                    updateMemo.star = true;
                    break;
            }
            // 유저 폴더 인덱스는 0부터 시작함
            // 예를들어 aaaa는 0, bbbb는 1


            // 기존의 폴더 카운트, uid알아오기


            if (index >= 0) {
                // 기존의 폴더 인덱스를 구하면 끝
                for (int k = 0; k < folderList.size(); k++) {
                    if (folderList.get(k).title.equals(updateMemo.folder)) {
                        oldFolder = folderList.get(k);
                        break;
                    }
                }
            }


            if (index > -1) {
                String name = folder.title; // 전체폴더에서 내가 선택한 폴더의 이름.
                updateMemo.setFolder(name); // 그 이름을 업데이트
            }
            Log.i(TAG, "--------------finally updated memo info----------------");
            Log.i(TAG, "updatedMemo: " + updateMemo.toString());
            Log.i(TAG, "------------------------------------------------------");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // ok
                    memoDao.updateData(updateMemo.title,
                            updateMemo.content,
                            updateMemo.star,
                            updateMemo.lock,
                            updateMemo.timestamp,
                            updateMemo.folder);

                }
            }).start();
        }

//
//        // 개수 증가 (새로운폴더)
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                //                    memoDao.updateFolderCount(folder.count+moveCount, folder.uid);
////                    Log.i(TAG, "afterCount: "+ (folder.count+moveCount)+"");
//
//                memoDao.updateFolderCount(folder.count + moveSize, folder.uid);
//            }
//        }).start();

        // 개수 감소 (기존 폴더들)
//
//        // newDatas안에 같은 uid가 몇개인지
//        HashMap<Integer, Integer> newDatas = new HashMap<>();
//        HashMap<Integer, Integer> sameUidCounts = new HashMap<>();
//        for(int i=0; i<beforeUids.size(); i++) {
//            int tempBeforeUid = beforeUids.get(i);
//
//            sameUidCounts.put(tempBeforeUid, sameUidCounts.get(tempBeforeUid)+1);
//
//            int tempCountSum = 0;
//            for(int j=0; j<beforeCounts.size(); j++) {
//                int tempBeforeCount = beforeCounts.get(j);
//                tempCountSum += tempBeforeCount;
//            }
//
//            newDatas.put(tempBeforeUid, tempCountSum);
//        }
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                for(int i=0; i<beforeUids.size(); i++) {
//                    int tempBeforeUid = beforeUids.get(i);
//
//                    int beforeFolderCount = newDatas.get(tempBeforeUid);
//
//                    // uid별로 몇개인지
//                    memoDao.updateFolderCount(beforeFolderCount-sameUidCounts.get(tempBeforeUid),beforeUid);
//                }
//
//            }
//        }).start();


        // adapter에 아이템 넣기
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                // 체인지,업데이트
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    updateList = new ArrayList<>();
//                    updateList = memoList.stream().filter(memo -> memo.folder.equals(updateMemo.folder)).collect(Collectors.toList());
//                    memoDao.updateFolderCount(updateList.size(), folder.uid);
//                }
//            }
//        }).start();
        Intent intent = new Intent(SelectFolderActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}