package com.shuhao.umengpushhelper.Push;

import java.util.Map;

import io.realm.Realm;

/**
 * Created by ming.o on 2017/9/13.
 */

public interface MessageParse {
    void showDialog(Message message);
    void openURL(String url);
    void openbroser(String url);
    Map<String,String> getUserMap();
    boolean openLoginDialog();
    void setLoginCallBack(LoginCallBack loginCallBack);
    Realm.Transaction.OnSuccess createTransactionSuccess();
}
