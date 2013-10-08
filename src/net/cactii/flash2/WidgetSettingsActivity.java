package net.cactii.flash2;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RemoteViews;

public class WidgetSettingsActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    public static final String KEY_WIDGET_BRIGHT = "widget_bright";
    public static final String KEY_WIDGET_STROBE = "widget_strobe";
    public static final String KEY_WIDGET_STROBE_FREQ = "widget_strobe_freq";
    private int mAppWidgetId;

    private StrobeFreqPreference mStrobeFrequency;
    private CheckBoxPreference mBrightPref;
    private CheckBoxPreference mStrobePref;
    private SharedPreferences mPreferences;

    @SuppressWarnings("deprecation")
    //No need to go to fragments right now
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.widget_settings);
        this.mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        mBrightPref = (CheckBoxPreference) findPreference(KEY_WIDGET_BRIGHT);
        int brightValue = getResources().getInteger(R.integer.valueHigh);
        if (brightValue == -1){
            getPreferenceScreen().removePreference(mBrightPref);
        }
        mStrobePref = (CheckBoxPreference) findPreference(KEY_WIDGET_STROBE);
        mStrobeFrequency = (StrobeFreqPreference) findPreference(KEY_WIDGET_STROBE_FREQ);
        mStrobeFrequency.setEnabled(mPreferences.getBoolean(KEY_WIDGET_STROBE, false));

        //keeps 'Strobe frequency' option available
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_WIDGET_STROBE)) {
            this.mStrobeFrequency.setEnabled(sharedPreferences.getBoolean(KEY_WIDGET_STROBE, false));
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
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.widget_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saveSetting : //Changes are accepted
                addWidget();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void addWidget() {
        Editor editor = mPreferences.edit();
        editor.putBoolean("widget_strobe_" + mAppWidgetId, mPreferences.getBoolean(KEY_WIDGET_STROBE, false));
        editor.putInt("widget_strobe_freq_" + mAppWidgetId, mPreferences.getInt(KEY_WIDGET_STROBE_FREQ, 5));
        editor.putBoolean("widget_bright_" + mAppWidgetId, mPreferences.getBoolean(KEY_WIDGET_BRIGHT, false));
        editor.commit();

        //Initialize widget view for first update
        Context context = getApplicationContext();
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        Intent launchIntent = new Intent();
        launchIntent.setClass(context, TorchWidgetProvider.class);
        launchIntent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        launchIntent.setData(Uri.parse("custom:" + mAppWidgetId + "/0"));
        PendingIntent pi = PendingIntent.getBroadcast(context, 0 /*
                                                                  * no
                                                                  * requestCode
                                                                  */, launchIntent, 0 /*
                                                                                       * no
                                                                                       * flags
                                                                                       */);
        views.setOnClickPendingIntent(R.id.btn, pi);
        if (mPreferences.getBoolean("widget_strobe_" + mAppWidgetId, false)) {
            views.setTextViewText(R.id.ind_text, context.getString(R.string.label_strobe));
        } else if (mPreferences.getBoolean("widget_bright_" + mAppWidgetId, false)) {
            views.setTextViewText(R.id.ind_text, context.getString(R.string.label_high));
        }

        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(mAppWidgetId, views);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);

        //close the activity
        finish();
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
