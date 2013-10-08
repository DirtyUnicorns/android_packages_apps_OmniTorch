package net.cactii.flash2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    private static final String TAG = "TorchSettings";

    public static final String KEY_BRIGHT = "bright";
    public static final String KEY_STROBE = "strobe";
    public static final String KEY_STROBE_FREQ = "strobe_freq";

    private StrobeFreqPreference mStrobeFrequency;
    private CheckBoxPreference mBrightPref;
    private CheckBoxPreference mStrobePref;
    private SharedPreferences mPreferences;

    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.settings);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mBrightPref = (CheckBoxPreference) findPreference(KEY_BRIGHT);
        int brightValue = getResources().getInteger(R.integer.valueHigh);
        if (brightValue == -1){
            getPreferenceScreen().removePreference(mBrightPref);
        }
        mStrobePref = (CheckBoxPreference) findPreference(KEY_STROBE);
        mStrobeFrequency = (StrobeFreqPreference) findPreference(KEY_STROBE_FREQ);
        mStrobeFrequency.setEnabled(mPreferences.getBoolean(KEY_STROBE, false));

        //keeps 'Strobe frequency' option available
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

 
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_STROBE)) {
            mStrobeFrequency.setEnabled(sharedPreferences.getBoolean(KEY_STROBE, false));
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mBrightPref) {
            if (mBrightPref.isChecked() && !mPreferences.getBoolean("bright_warn_check", false)){
                openBrightDialog();
                mPreferences.edit().putBoolean("bright_warn_check", true).commit();
            }
            return true;
        }
        return false;
    }
      
    private void openBrightDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        View view = li.inflate(R.layout.brightwarn, null);
        new AlertDialog.Builder(this).setTitle(this.getString(R.string.warning_label))
                .setView(view)
                .setNegativeButton(this.getString(R.string.brightwarn_negative), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mBrightPref.setChecked(false);
                    }
                }).setNeutralButton(this.getString(R.string.brightwarn_accept), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }
}
