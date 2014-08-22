/*
 * Copyright (C) 2014 Ruikye's open source project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.awaysoft.nightlymode.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.awaysoft.nightlymode.utils.Preference;

/**
 * For listening startup
 *
 * @author ruikye
 * @since 2014
 */
public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Preference.INSTANCE.read(context);

            if (Preference.sAutoStart && Preference.sServiceRunning) {
                context.startService(new Intent(context, NightlyService.class));
            }
        }
    }

}
