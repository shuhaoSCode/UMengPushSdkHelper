package com.shuhao.umengpushhelper.Push;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ming.o on 2017/9/5.
 */

public class Message extends RealmObject {
    boolean isNeedPOP = true;
    String Title;
    String Content ;
    String Confirm;
    String Cancel ;
    @PrimaryKey
    String msg_id;
    String extString;
    Date end_time;
    boolean isParsed = false;
    public Message(){}
    public Message(MessageBuild build){
        isNeedPOP = build.isNeedPOP;
        Title = build.Title;
        Content = build.Content;
        Confirm = build.Confirm;
        Cancel = build.Cancel;
        extString = build.extString;
        msg_id = build.msg_id;
        isParsed = build.isParsed;
        end_time = build.end_time;
    }
    public  String getMsg_id(){
        return msg_id;
    }
    public String getExtString(){
        return extString;
    }

    public boolean isNeedPOP() {
        return isNeedPOP;
    }

    public String getTitle() {
        return Title;
    }

    public String getContent() {
        return Content;
    }

    public String getConfirm() {
        return Confirm;
    }

    public String getCancel() {
        return Cancel;
    }

    public boolean isParsed() {
        return isParsed;
    }
    public void setNeedPOP(boolean needPOP){
        this.isNeedPOP = needPOP;
    }
    public void setParsed(boolean parsed) {
        isParsed = parsed;
    }
    public Date getEndTime(){
        return end_time;
    }
    public static final class MessageBuild{
        boolean isNeedPOP = true;
        boolean isParsed = false;
        String Title;
        String Content ;
        String Confirm;
        String Cancel ;
        String msg_id;
        String extString;
        Date end_time;
        public MessageBuild(String message){
            this.extString = message;
        }
        public MessageBuild setNeedPOP(boolean isNeedPOP){
            this.isNeedPOP = isNeedPOP;
            return this;
        }
        public MessageBuild setTitle(String title){
            this.Title = title;
            return this;
        }
        public MessageBuild setContent(String Content){
            this.Content = Content;
            return this;
        }
        public MessageBuild setConfirm(String Confirm){
            this.Confirm = Confirm;
            return this;
        }
        public MessageBuild setCancel(String Cancel){
            this.Cancel = Cancel;
            return this;
        }
        public MessageBuild setMsgID(String msg_id){
            this.msg_id = msg_id;
            return this;
        }
        public MessageBuild setParsed(boolean parsed){
            this.isParsed = parsed;
            return this;
        }
        public MessageBuild setEndTime(Date end_time){
            this.end_time = end_time;
            return this;
        }
        public Message build(){
            return new Message(this);
        }

    }
}
