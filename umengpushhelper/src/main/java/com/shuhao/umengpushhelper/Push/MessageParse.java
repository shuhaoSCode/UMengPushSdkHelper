package com.shuhao.umengpushhelper.Push;

import java.util.Map;

/**
 * Created by ming.o on 2017/9/13.
 */

public interface MessageParse {
    void showDialog(Message message);
    void openURL(String url);
    void openbroser(String url);
    Map<String,String> getUserMap();
    void openLoginDialog();
    void setLoginCallBack(LoginCallBack loginCallBack);
}
