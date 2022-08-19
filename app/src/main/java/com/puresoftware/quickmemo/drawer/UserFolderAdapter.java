package com.puresoftware.quickmemo.drawer;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.puresoftware.quickmemo.MainActivity;
import com.puresoftware.quickmemo.R;
import com.puresoftware.quickmemo.artifacts.Folder;
import com.puresoftware.quickmemo.room.AppDatabase;
import com.puresoftware.quickmemo.room.Memo;
import com.puresoftware.quickmemo.room.MemoDao;
import com.puresoftware.quickmemo.room.UserFolder;

import java.util.ArrayList;
import java.util.List;

public class UserFolderAdapter extends BaseAdapter {

    String TAG = UserFolderAdapter.class.getSimpleName();

    List<Folder> folderList = new ArrayList<>();
    Context context;
    Activity activity;
    LinearLayout linDrawerUserFolder;

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return folderList.size();
    }

    @Override
    public Object getItem(int i) {
        return folderList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (context == null) {
            context = viewGroup.getContext();
        }

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.main_drawer_select_set_item, viewGroup, false);
        }

        linDrawerUserFolder = view.findViewById(R.id.lin_main_drawer_user_folder);
        TextView tvDrawerUserTitle = view.findViewById(R.id.tv_main_drawer_user_title);
        TextView tvDrawerUserCount = view.findViewById(R.id.tv_main_drawer_user_count);

        Folder folderItem = folderList.get(i);
        UserFolder folder = folderItem.getFolder();

        Log.d(TAG, "Map: " + MainActivity.folderSelect);
        // notifyDataSetChanged 와 static 변수 메모리 관련 서로 꼬임으로 인해
        // 키에 없을 경우 색을 transparent 로 지정

        if (MainActivity.folderSelect.containsKey(folder.title)) {
            boolean isSelected = MainActivity.folderSelect.get(folder.title);
            if (isSelected == true) {
                setBackground(true);
            } else {
                setBackground(false);
            }
        } else {
            setBackground(false);
        }

        Log.d(TAG, "Folder: " + folderItem);

        AppDatabase db = AppDatabase.getInstance(context);
        MemoDao memoDao = db.dao();

        AsyncTask.execute(() -> {
            int cnt = memoDao.getFolderCount(folder.title);

            activity.runOnUiThread(() -> {
                tvDrawerUserTitle.setText(folder.getTitle());
                tvDrawerUserCount.setText(String.valueOf(cnt));
            });
        });

//        linDrawerUserFolder.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.i(TAG, "드로우어블 개인 폴더 터치됨");
//            }
//        });

//        linDrawerUserFolder.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                Log.i(TAG, "드로우어블 개인 폴더 롱 터치됨");
//
//
//                return true;
//            }
//        });

        return view;
    }

    public void addItem(Folder folder) {
        folderList.add(folder);
        notifyDataSetChanged();
    }

    public void refreshItem(List<Folder> folders) {
        ArrayList<Folder> newData = (ArrayList<Folder>) folders;
        folderList.clear();
        folderList.addAll(newData);
        notifyDataSetChanged();
    }

    // 리스트 비우기
    public void cleanItems() {
        folderList.clear();
    }

    public void deleteItem(UserFolder folder) {
        folderList.remove(folder);
        Log.d(TAG, "Size: " + folderList.size());
        notifyDataSetChanged();
    }

    public void setBackground(boolean type) {

        if (type == false) {
            linDrawerUserFolder.setBackground(null);
        } else {
            linDrawerUserFolder.setBackgroundResource(R.drawable.round_retengle_folder);
        }
    }
}
