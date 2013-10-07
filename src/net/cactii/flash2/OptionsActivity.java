package net.cactii.flash2;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.RemoteViews;

public class OptionsActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    private SeekBarPreference mStrobeFrequency;

    private SharedPreferences mPreferences;

    @SuppressWarnings("deprecation")
    //No need to go to fragments right now
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.options);
        this.mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        CheckBoxPreference mBrightPref = (CheckBoxPreference) findPreference("bright");
        mBrightPref.setChecked(false);

        CheckBoxPreference mStrobePref = (CheckBoxPreference) findPreference("strobe");
        mStrobePref.setChecked(false);

        mStrobeFrequency = (SeekBarPreference) findPreference("strobe_freq");
        mStrobeFrequency.setEnabled(false);

        //keeps 'Strobe frequency' option available
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

 
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("widget_strobe")) {
            this.mStrobeFrequency.setEnabled(sharedPreferences.getBoolean("strobe", false));
        }

    }
}
