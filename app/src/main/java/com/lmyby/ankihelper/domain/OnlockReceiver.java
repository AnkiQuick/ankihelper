package com.lmyby.ankihelper.domain;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.lmyby.ankihelper.data.Settings;

public class OnlockReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent newIntent = new Intent(context, CBWatcherService.class);
        Settings settings = Settings.getInstance(context);
        if(settings.getMoniteClipboardQ()) {
            Toast.makeText(context, "debug", Toast.LENGTH_SHORT).show();
            context.startService(newIntent);
        }
    }
}
