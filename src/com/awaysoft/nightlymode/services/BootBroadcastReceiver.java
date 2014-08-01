
package com.awaysoft.nightlymode.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.awaysoft.nightlymode.utils.Preference;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Preference.read(context);

            if (Preference.sAutoStart) {
                context.startService(new Intent(context, NightlyService.class));
            }
        }
    }

}
