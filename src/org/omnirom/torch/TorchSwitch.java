/*
 *  Copyright (C) 2013 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.omnirom.torch;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class TorchSwitch extends BroadcastReceiver {

    public static final String TOGGLE_FLASHLIGHT = "org.omnirom.torch.TOGGLE_FLASHLIGHT";
    public static final String FLASHLIGHT_OFF = "org.omnirom.torch.FLASHLIGHT_OFF";
    public static final String FLASHLIGHT_ON = "org.omnirom.torch.FLASHLIGHT_ON";
    public static final String TORCH_STATE_CHANGED = "org.omnirom.torch.TORCH_STATE_CHANGED";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        // bright setting can come from intent or from prefs depending on
        // on what send the broadcast
        //
        // Unload intent extras if they exist:
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean bright = intent.getBooleanExtra("bright", prefs.getBoolean("bright", false));
        boolean strobe = intent.getBooleanExtra("strobe", prefs.getBoolean("strobe", false));
        int period = intent.getIntExtra("period", 200);

        Intent i = new Intent(context, TorchService.class);
        if (action.equals(TOGGLE_FLASHLIGHT)) {
            if (this.torchServiceRunning(context)) {
                context.stopService(i);
            } else {
                i.putExtra("bright", bright);
                i.putExtra("strobe", strobe);
                i.putExtra("period", period);
                context.startService(i);
            }
        } else if (action.equals(FLASHLIGHT_ON)) {
            i.putExtra("bright", bright);
            i.putExtra("strobe", strobe);
            i.putExtra("period", period);
            context.startService(i);
        } else if (action.equals(FLASHLIGHT_OFF)) {
            context.stopService(i);
        }
    }

    private boolean torchServiceRunning(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> svcList = am.getRunningServices(100);

        for (RunningServiceInfo serviceInfo : svcList) {
            ComponentName serviceName = serviceInfo.service;
            if (serviceName.getClassName().endsWith(".TorchService")
                    || serviceName.getClassName().endsWith(".RootTorchService"))
                return true;
        }
        return false;
    }
}
