/* Copyright 2015 Braden Farmer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.farmerbb.secondscreen.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.farmerbb.secondscreen.service.BootService;
import com.farmerbb.secondscreen.service.DisplayConnectionService;
import com.farmerbb.secondscreen.service.NotificationService;
import com.farmerbb.secondscreen.util.U;

// This receiver is responsible for recreating the DisplayConectionService and/or
// NotificationService, where applicable, after a device reboot.
// It is also responsible for launching the TurnOffService if safe mode is enabled and a profile
// was active before the device last shut down/restarted.
public final class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Load preferences
        SharedPreferences prefCurrent = U.getPrefCurrent(context);
        SharedPreferences prefMain = U.getPrefMain(context);

        // Restore DisplayConnectionService
        if(prefMain.getBoolean("hdmi", true) && prefMain.getBoolean("first-run", false)) {
            Intent serviceIntent = new Intent(context, DisplayConnectionService.class);
            context.startService(serviceIntent);
        }

        if(!prefCurrent.getBoolean("not_active", true)) {
            if(prefMain.getBoolean("safe_mode", false) && !"activity-manager".equals(prefCurrent.getString("ui_refresh", "do-nothing"))) {
                SharedPreferences.Editor editor = prefCurrent.edit();
                editor.putString("ui_refresh", "do-nothing");
                editor.apply();

                U.turnOffProfile(context);
            } else if("quick_actions".equals(prefCurrent.getString("filename", "0"))) {
                SharedPreferences prefSaved = U.getPrefQuickActions(context);
                if("0".equals(prefSaved.getString("original_filename", "0"))) {
                    SharedPreferences.Editor editor = prefCurrent.edit();
                    editor.putString("ui_refresh", "do-nothing");
                    editor.apply();

                    U.turnOffProfile(context);
                } else {
                    // Restore NotificationService
                    Intent serviceIntent = new Intent(context, NotificationService.class);
                    context.startService(serviceIntent);

                    // Start BootService to run superuser commands
                    Intent serviceIntent2 = new Intent(context, BootService.class);
                    context.startService(serviceIntent2);
                }
            } else {
                // Restore NotificationService
                Intent serviceIntent = new Intent(context, NotificationService.class);
                context.startService(serviceIntent);

                // Start BootService to run superuser commands
                Intent serviceIntent2 = new Intent(context, BootService.class);
                context.startService(serviceIntent2);
            }
        }
    }
}