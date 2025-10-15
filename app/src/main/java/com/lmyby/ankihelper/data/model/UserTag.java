package com.lmyby.ankihelper.data.model;

/**
 * Created by liao on 2017/4/20.
 */

 public class UserTag {
     private String tag;

     public UserTag(String pTag) {
         tag = pTag;
     }

     public void setTag(String planName) {
         this.tag = planName;
     }

     public String getTag() {
         return tag;
     }
 }
