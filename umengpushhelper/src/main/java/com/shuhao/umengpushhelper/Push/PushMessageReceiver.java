package com.shuhao.umengpushhelper.Push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by ming.o on 2017/9/13.
 */

public class PushMessageReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("receive",intent.getAction());
        if("UM_Message".contains(intent.getAction())){

            MessageCenter.getInstance().checkAction();
        }
    }
}
