package com.puresoftware.quickmemo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

public class AppSettingsActivity extends PreferenceActivity {

    String TAG = AppSettingsActivity.class.getSimpleName();


    // 터치 시 이벤트
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        Log.d(TAG, "클릭된 Preference의 key는 " + key);

        switch (key) {

            case "floating mode":
                intentGo(SettingsFloatingActivity.class);

                break;

            // 업데이트 필요 기능
            case "key mode":
                break;

            // 업데이트 필요 기능
            case "login":
                intentGo(LoginActivity.class);
                break;

            // 업데이트 필요 기능
            case "upload":
                break;

            // 업데이트 필요 기능
            case "download":
                break;

            case "pin mode":
                intentGo(PINsetActivity.class);
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);


        // 값이 변경되었을 때 머시기하는거라는데 잘 모름.
        SharedPreferences prefs;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs
                .registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
                        Log.i(TAG, "클릭된 Preference의 key는 " + key);
                    }
                });
    }

    // 인텐트 중복 사용
    public void intentGo(Class classes) {
        Intent intent = new Intent(AppSettingsActivity.this, classes);
        startActivity(intent);
    }
}
