
package net.cactii.flash2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

    private TorchWidgetProvider mWidgetProvider;
    private ImageView mButtonOnView;
    private boolean mTorchOn;
    private Context mContext;
    private SharedPreferences mPrefs;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainnew);
        mContext = this.getApplicationContext();
        mButtonOnView = (ImageView) findViewById(R.id.buttoOnImage);

        mButtonOnView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                createIntent();
            }
        });

        mButtonOnView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        mTorchOn = false;
        mWidgetProvider = TorchWidgetProvider.getInstance();

        // Preferences
        this.mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        updateBigButtonState();
    }

    private void createIntent() {
        Intent intent = new Intent(TorchSwitch.TOGGLE_FLASHLIGHT);
        intent.putExtra("strobe", mPrefs.getBoolean(SettingsActivity.KEY_STROBE, false));
        intent.putExtra("period", mPrefs.getInt(SettingsActivity.KEY_STROBE_FREQ, 5));
        intent.putExtra("bright", mPrefs.getBoolean(SettingsActivity.KEY_BRIGHT, false));
        mContext.sendBroadcast(intent);
    }

    public void onPause() {
        this.updateWidget();
        mContext.unregisterReceiver(mStateReceiver);
        super.onPause();
    }

    public void onDestroy() {
        this.updateWidget();
        super.onDestroy();
    }

    public void onResume() {
        updateBigButtonState();
        this.updateWidget();
        mContext.registerReceiver(mStateReceiver, new IntentFilter(TorchSwitch.TORCH_STATE_CHANGED));
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
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityIfNeeded(intent, -1);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openAboutDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        View view = li.inflate(R.layout.aboutview, null);
        new AlertDialog.Builder(MainActivity.this).setTitle(this.getString(R.string.about_title)).setView(view)
                .setNegativeButton(this.getString(R.string.about_close), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Log.d(MSG_TAG, "Close pressed");
                    }
                }).show();
    }

    public void updateWidget() {
        this.mWidgetProvider.updateAllStates(mContext);
    }

    private void updateBigButtonState() {
        mButtonOnView.setImageResource(mTorchOn ? R.drawable.bulb_on : R.drawable.bulb_off);
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
