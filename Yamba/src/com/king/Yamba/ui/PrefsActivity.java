package com.king.Yamba.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.king.Yamba.R;


public class PrefsActivity extends PreferenceActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);


    }
}