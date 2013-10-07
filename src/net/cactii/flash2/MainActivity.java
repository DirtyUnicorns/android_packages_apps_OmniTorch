
package net.cactii.flash2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TorchWidgetProvider mWidgetProvider;

    // On button
    private ToggleButton buttonOn;

    private boolean mTorchOn;

    private Context context;

    // Preferences
    private SharedPreferences mPrefs;
    
    // Labels
    private String labelOn = null;
    private String labelOff = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainnew);
        context = this.getApplicationContext();
        buttonOn = (ToggleButton) findViewById(R.id.buttonOn);

        mTorchOn = false;

        labelOn = this.getString(R.string.label_on);
        labelOff = this.getString(R.string.label_off);

        mWidgetProvider = TorchWidgetProvider.getInstance();

        // Preferences
        this.mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        buttonOn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TorchSwitch.TOGGLE_FLASHLIGHT);
                intent.putExtra("strobe", this.mPrefs.getBoolean("strobe", false));
                intent.putExtra("period", this.mPrefs.getInt("strobeperiod", 100));
                intent.putExtra("bright", this.mPrefs.getBoolean("bright", false));
                context.sendBroadcast(intent);
            }
        });
    }

    public void onPause() {
        this.updateWidget();
        context.unregisterReceiver(mStateReceiver);
        super.onPause();
    }

    public void onDestroy() {
        this.updateWidget();
        super.onDestroy();
    }

    public void onResume() {
        updateBigButtonState();
        this.updateWidget();
        context.registerReceiver(mStateReceiver, new IntentFilter(TorchSwitch.TORCH_STATE_CHANGED));
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                this.openAboutDialog();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, OptionsActivity.class);
                startActivityIfNeeded(intent, -1);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openAboutDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        View view = li.inflate(R.layout.aboutview, null);
        new AlertDialog.Builder(MainActivity    <string name="setting_widget_bright">Bright widget</string>
    <string name="setting_widget_strobe">Strobe flash</string>.this).setTitle(this.getString(R.string.about_title)).setView(view)
                .setNegativeButton(this.getString(R.string.about_close), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Log.d(MSG_TAG, "Close pressed");
                    }
                }).show();
    }

    public void updateWidget() {
        this.mWidgetProvider.updateAllStates(context);
    }

    private void updateBigButtonState() {
        buttonOn.setChecked(mTorchOn);
    }

    private BroadcastReceiver mStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(TorchSwitch.TORCH_STATE_CHANGED)) {
                mTorchOn = intent.getIntExtra("state", 0) != 0;
                updateBigButtonState();
            }
        }
    };
}
