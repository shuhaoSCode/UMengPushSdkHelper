package com.shuhao.umengpushhelper.Push;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import io.realm.Realm;
import io.realm.RealmResults;

import static android.content.Context.NOTIFICATION_SERVICE;


/**
 * Created by ming.o on 2017/9/5.
 */

public class MessageCenter {
    //    RealmResults<Message> messageHashMap = null;
    private static Context context;
    private static MessageCenter Instance = null;
    private static Object lock = new Object();
    private Realm realm;

    private MessageCenter(Context context) {
        this.context = context;

        realm = Realm.getDefaultInstance();
//        initMap();


    }

    public void beginTransaction() {
        realm.beginTransaction();
    }

    public void commitTransaction() {
        realm.commitTransaction();
    }

    public static void init(Context context) {
        Instance = new MessageCenter(context.getApplicationContext());
    }

    public static MessageCenter getInstance() {
        if (null == Instance) {
            Instance = new MessageCenter(context);
        }
        return Instance;
    }

    public void setMessageHashMap(final Message message) {
//        Log.d("addmesssage",messageHashMap.size()+"");
        Realm.getDefaultInstance().executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(message);
            }
        });
//        Log.d("addmesssage",messageHashMap.size()+"");
    }

    //    public Message getMessage(int index){
//       return messageHashMap.get(index);
//    }
    public Message getMessage(String msg_id) {
        return realm.where(Message.class).greaterThan("end_time", new Date()).equalTo("isParsed", false).equalTo("msg_id", msg_id).findFirst();
    }

    //    public void parsed(int index){
//        realm.beginTransaction();
//        messageHashMap.deleteFromRealm(index);
//
//        realm.commitTransaction();
//    }
    public void parsed(final Message message) {
        Realm.getDefaultInstance().executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(message);
            }
        });
    }

    public boolean isMessage(Message message) {
        RealmResults<Message> t = realm.where(Message.class).greaterThan("end_time", new Date()).equalTo("msg_id", message.getMsg_id()).findAll();
        return t.size() > 0;
    }

    //    public void initMap(){
//        messageHashMap = realm.where(Message.class)
//                .greaterThan("end_time",new Date())
//                .findAll();
//        Log.d("time",messageHashMap.size()+"");
//        for(Message message : messageHashMap){
//            Log.d("time",message.end_time.getTime()+"");
//            Log.d("time",new Date().getTime()+"");
//        }
//    }
    public void clearDataBase() {
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
    }

    public RealmResults<Message> getMessageHashMap() {
        return realm.where(Message.class)
                .greaterThan("end_time", new Date())
                .equalTo("isParsed", false)
                .findAll();
    }

    volatile MessageParse messageParse = null;

    public void registMessageParse(MessageParse messageParse) {
        this.messageParse = messageParse;
//        context.sendBroadcast(new Intent("UM_Message"));
    }

    public void unregistMessageParse() {
        messageParse = null;
    }

    public void MessageParse(final Message message) {
        messageParse.setLoginCallBack(null);
        String storedHashMapString = message.getExtString();
        if (!"null".equals(storedHashMapString) || null != storedHashMapString) {
            HashMap<String, String> testHashMap2 = UMHelper.stringToMap(storedHashMapString);
            if (testHashMap2.get("end_date") != null && "".equals(testHashMap2.get("end_date"))) {
                try {
                    if (new Date().getTime() > new SimpleDateFormat("yyyy-MM-ddHH:mm:ss").parse(testHashMap2.get("end_date")).getTime()) {
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (testHashMap2.get("need_login") == null) testHashMap2.put("need_login", "false");
            Map<String, String> userMap = messageParse.getUserMap();
            String ticket = "";
            if ("true".equals(testHashMap2.get("need_login"))) {
                if (userMap != null && userMap.get("ticket") != null)
                    ticket = "ticket=" + userMap.get("ticket");
                else {
                    if (MessageCenter.getInstance().isMessage(message)) {
                        messageParse.setLoginCallBack(new LoginCallBack() {
                            @Override
                            public Message backToMessage() {
                                return message;
                            }
                        });
                        messageParse.openLoginDialog();
                        // MessageCenter.getInstance().parsed(message); // remove message
                    }
                    return;
                }
            }

            if (null == testHashMap2.get("open_url") || "".equals(testHashMap2.get("open_url")))
                return;
            Pattern pattern = Pattern
                    .compile("^([hH][tT]{2}[pP]://|[hH][tT]{2}[pP][sS]://)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~\\/])+$");
            if (!pattern.matcher(testHashMap2.get("open_url")).matches())
                return;
            StringBuilder urlbuild = new StringBuilder();
            urlbuild.append(testHashMap2.get("open_url"));
            if (testHashMap2.get("open_url").endsWith("/")) {
                urlbuild.append("?");
            } else if (testHashMap2.get("open_url").contains("?") && !testHashMap2.get("open_url").endsWith("?")) {
                urlbuild.append("&");
            } else {
                urlbuild.append("/?");
            }
            urlbuild.append(ticket);
            String url = urlbuild.toString();

            MessageCenter.getInstance().parsed(message);
            if (!"internal".equals(testHashMap2.get("open_type"))) {
                Log.d("goExurl", url);
                messageParse.openbroser(url);
                return;
            }
            messageParse.openURL(url);
        }

    }

    public static final String EXTRA_KEY_ACTION = "ACTION";
    public static final String EXTRA_KEY_MSG = "MSG";
    public static final int ACTION_CLICK = 10;
    public static final int ACTION_DISMISS = 11;
    public static final int EXTRA_ACTION_NOT_EXIST = -1;

    public void cancel(){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public void checkAction() {
        Log.d("message____size", MessageCenter.getInstance().getMessageHashMap().size() + "");
        if (messageParse == null)
            return;
        Log.d("message", MessageCenter.getInstance().getMessageHashMap().size() + "");
        for (final Message message : MessageCenter.getInstance().getMessageHashMap()) {
            if (!message.isParsed()) {
                cancel();
                MessageCenter.getInstance().beginTransaction();
                message.setParsed(true);
                MessageCenter.getInstance().commitTransaction();
                MessageCenter.getInstance().setMessageHashMap(message);
                if (message.isNeedPOP()) {
                    messageParse.showDialog(message);


                } else {
                    MessageParse(message);
                }
                MessageCenter.getInstance().setMessageHashMap(message);
            } else {
                MessageCenter.getInstance().parsed(message);
            }
        }
    }
}
