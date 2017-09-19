package com.shuhao.umengpushhelper.Push;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.MsgConstant;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.common.UmLog;
import com.umeng.message.entity.UMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.sina.util.dnscache.AppConfigUtil.getApplicationContext;

/**
 * Created by ming.o on 2017/9/13.
 */

public class UMHelper {
    private final static String TAG = "UMHelper";
    public static void initUM(Context context, Handler handler){
        PushAgent mPushAgent = PushAgent.getInstance(context);
        mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SDK_ENABLE);
        regist(mPushAgent);
        registUMHandle(mPushAgent,handler);
        registUMClickHandle(mPushAgent);
    }
    private static void registUMHandle(PushAgent mPushAgent, final Handler handler){
        UmengMessageHandler messageHandler = new UmengMessageHandler() {
            @Override
            public void dealWithNotificationMessage(Context context,UMessage uMessage) {
                super.dealWithNotificationMessage(context, uMessage);
                try {
                    Log.d("dealWithCustomAction", uMessage.extra.toString());
                    if("open_url".equals(uMessage.extra.get("enum_action"))) {
                        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date time = null;
                        try {
                            time = format.parse(uMessage.extra.get("end_date"));
                        }catch (Exception e){
                            time = format.parse("2222-09-15 16:51:00");
                        }
                        Message message = new Message.MessageBuild(uMessage.extra.toString())
                                .setNeedPOP(true)
                                .setMsgID(uMessage.msg_id)
                                .setEndTime(time)
                                .setTitle(uMessage.title)
                                .setConfirm("查看页面")
                                .setCancel("取消")
                                .setContent(uMessage.text)
                                .build();
                        MessageCenter.getInstance().setMessageHashMap(message);

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            getApplicationContext().sendBroadcast(new Intent("UM_Message"));
                        }
                    });

                }
            }

            /**
             * 自定义消息的回调方法
             * */

            @Override
            public void dealWithCustomMessage(final Context context, final UMessage msg) {
                try {
                    Log.d("dealWithCustomAction", msg.extra.toString());
                    Log.d("dealWithCustomAction", msg.getRaw().toString());
                }catch (Exception e){
                    e.printStackTrace();
                }
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        // 对自定义消息的处理方式，点击或者忽略
                        boolean isClickOrDismissed = true;
                        if (isClickOrDismissed) {
                            //自定义消息的点击统计
                            UTrack.getInstance(getApplicationContext()).trackMsgClick(msg);
                        } else {
                            //自定义消息的忽略统计
                            UTrack.getInstance(getApplicationContext()).trackMsgDismissed(msg);
                        }
                        Toast.makeText(context, msg.custom, Toast.LENGTH_LONG).show();
                    }
                });
            }

            /**
             * 自定义通知栏样式的回调方法
             * */
            @Override
            public Notification getNotification(Context context, UMessage msg) {
                return super.getNotification(context, msg);
            }
        };
        mPushAgent.setMessageHandler(messageHandler);
    }
    private static void registUMClickHandle(PushAgent mPushAgent){
        UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler() {
            @Override
            public void handleMessage(Context context, UMessage uMessage) {
                Message message =  MessageCenter.getInstance().getMessage(uMessage.msg_id);
                if(message !=null) {
                    MessageCenter.getInstance().beginTransaction();
                    message.setNeedPOP(false);
                    MessageCenter.getInstance().commitTransaction();
                    MessageCenter.getInstance().setMessageHashMap(message);
                }
                super.handleMessage(context, uMessage);

            }


        };
        //使用自定义的NotificationHandler，来结合友盟统计处理消息通知，参考http://bbs.umeng.com/thread-11112-1-1.html
        //CustomNotificationHandler notificationClickHandler = new CustomNotificationHandler();
        mPushAgent.setNotificationClickHandler(notificationClickHandler);

    }
    private static void regist(PushAgent mPushAgent){
        mPushAgent.register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String deviceToken) {
                Log.i(TAG, "device token: " + deviceToken);
//                sendBroadcast(new Intent(UPDATE_STATUS_ACTION));
            }

            @Override
            public void onFailure(String s, String s1) {
                UmLog.i(TAG, "register failed: " + s + " " +s1);
//                sendBroadcast(new Intent(UPDATE_STATUS_ACTION));
            }
        });
    }
}
