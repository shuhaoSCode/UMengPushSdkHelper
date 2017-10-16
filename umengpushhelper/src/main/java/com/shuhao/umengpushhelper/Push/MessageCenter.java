package com.shuhao.umengpushhelper.Push;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import io.realm.Realm;
import io.realm.RealmResults;

import static android.content.Context.NOTIFICATION_SERVICE;


/**
 * Created by ming.o on 2017/9/5.
 */

public class MessageCenter {
    //    RealmResults<Message> messageHashMap = null;
    private static Context context ;
    private static MessageCenter Instance = null;
    private static Object lock = new Object();
    private Realm realm ;
    private MessageCenter(Context context){
        this.context = context;

        realm = Realm.getDefaultInstance();
//        initMap();


    }
    public void beginTransaction(){
        realm.beginTransaction();
    }
    public void commitTransaction(){
        realm.commitTransaction();
    }
    public static void init(Context context){
        Instance = new MessageCenter(context.getApplicationContext());
    }
    public static MessageCenter getInstance(){
        if (null == Instance) {
            Instance = new MessageCenter(context);
        }
        return Instance;
    }
    public void setMessageHashMapInBackground(final Message message){
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(message);
            }
        });
    }
    public void setMessageHashMap(final Message message){
//        Log.d("addmesssage",messageHashMap.size()+"");
        Realm.getDefaultInstance().executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(message);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Log.d("message__^^", MessageCenter.getInstance().getMessageHashMap().size()+"");
                if(messageParse!=null)
                    messageParse.createTransactionSuccess().onSuccess();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                // transaction is automatically rolled-back, do any cleanup here
            }
        });
//        Log.d("addmesssage",messageHashMap.size()+"");
    }
    //    public Message getMessage(int index){
//       return messageHashMap.get(index);
//    }
    public Message getMessage(String msg_id){
        return realm.where(Message.class).greaterThan("end_time",new Date()).equalTo("isParsed",false).equalTo("msg_id",msg_id).findFirst();
    }
    public void parsed(final Message message){
        Realm.getDefaultInstance().executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(message);
            }
        });
    }
    public boolean isMessage(Message message){
        RealmResults<Message> t = realm.where(Message.class).greaterThan("end_time",new Date()).equalTo("msg_id",message.getMsg_id()).findAll();
        return t.size()>0;
    }
    public void clearDataBase(){
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
    }
    public RealmResults<Message> getMessageHashMap(){

        return realm.where(Message.class)
                .greaterThan("end_time",new Date())
                .equalTo("isParsed",false)
                .findAll();
    }
    //    HashMap<String,MessageParse> messageParseHashMap ;
    volatile MessageParse messageParse = null;

    public void registMessageParse(MessageParse messageParse){
        Log.d("message____size","^^");
        this.messageParse = null;
        System.gc();
        this.messageParse = new WeakReference<MessageParse>(messageParse).get();
//        context.sendBroadcast(new Intent("UM_Message"));
    }

    public void unregistMessageParse(){
        messageParse = null;
    }
    private boolean checkMessage(Message message){
        String storedHashMapString = message.getExtString();
        if (!"null".equals(storedHashMapString) || null != storedHashMapString ) {
            HashMap<String, String> testHashMap2 = UMHelper.stringToMap(storedHashMapString);
            if (testHashMap2.get("end_date") != null && "".equals(testHashMap2.get("end_date"))) {
                try {
                    if (new Date().getTime() > new SimpleDateFormat("yyyy-MM-ddHH:mm:ss").parse(testHashMap2.get("end_date")).getTime()) {
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (null == testHashMap2.get("open_url") || "".equals(testHashMap2.get("open_url")))
                return false;
            Pattern pattern = Pattern
                    .compile("^([hH][tT]{2}[pP]://|[hH][tT]{2}[pP][sS]://)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~\\/])+$");
            if(!pattern.matcher(testHashMap2.get("open_url")).matches())
                return false;
            return true;
        }
        return false;
    }
    public void MessageParse(final Message message){
        messageParse.setLoginCallBack(null);
        String storedHashMapString = message.getExtString();
        if (!"null".equals(storedHashMapString) || null != storedHashMapString ) {
            HashMap<String, String> testHashMap2 = UMHelper.stringToMap(storedHashMapString);
            String ticket = "";
            if (testHashMap2.get("need_login") != null && "true".equals(testHashMap2.get("need_login"))) {
                Map<String, String> userMap = messageParse.getUserMap();
                ticket = crontrol_Login(userMap, message);
                if("".equals(ticket)){
                    return;
                }
            }
            String url = createUrl(testHashMap2,ticket);

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
    private String crontrol_Login(Map<String,String> userMap,final Message message){
        String ticket = "";
        if (userMap != null && userMap.get("ticket") != null && !"2".equals(userMap.get("mode")))
            ticket = "ticket=" + userMap.get("ticket");
        else {
            //if(MessageCenter.getInstance().isMessage(message))
            {
                messageParse.setLoginCallBack(new LoginCallBack() {
                    @Override
                    public Message backToMessage() {
                        return message;
                    }
                });
                if(!messageParse.openLoginDialog()){
                    cancelPOP(message);
                    waitList.add(message);
                }
            }

        }
        return ticket;
    }
    private String createUrl(HashMap<String, String> testHashMap2,String ticket){
        StringBuilder urlbuild = new StringBuilder();
        urlbuild.append(testHashMap2.get("open_url"));
        if( testHashMap2.get("open_url").endsWith("/")){
            urlbuild.append("?");
        } else if(testHashMap2.get("open_url").contains("?") && !testHashMap2.get("open_url").endsWith("?")){
            urlbuild.append("&");
        }else{
            urlbuild.append("/?");
        }
        urlbuild.append(ticket);
        return urlbuild.toString();
    }
    public void cancel(){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }
    public void checkAction(){
        if(messageParse == null ) {
            return;
        }

        wainParse();
        for(final Message message : MessageCenter.getInstance().getMessageHashMap()) {
            if(!message.isParsed()) {
                cancel();
                init_message_status(message);
                if(checkMessage(message)) {
                    if (message.isNeedPOP()) {
                        messageParse.showDialog(message);
                    } else {
                        MessageParse(message);
                    }
                }
                MessageCenter.getInstance().setMessageHashMap(message);
            }else{
                MessageCenter.getInstance().parsed(message);
            }
        }
    }
    private Vector<Message> waitList = new Vector<>();
    private void wainParse(){
        if(waitList.size()>0){
            MessageParse(waitList.remove(0));
        }
    }
    public void init_message_status(Message message){
        MessageCenter.getInstance().beginTransaction();
        message.setParsed(true);
        MessageCenter.getInstance().commitTransaction();
        MessageCenter.getInstance().setMessageHashMap(message);
    }
    public void cancelPOP( Message message){
        if(message !=null) {
            MessageCenter.getInstance().beginTransaction();
            message.setNeedPOP(false);
            message.setParsed(false);
            MessageCenter.getInstance().commitTransaction();
            MessageCenter.getInstance().setMessageHashMap(message);
        }
    }
}
