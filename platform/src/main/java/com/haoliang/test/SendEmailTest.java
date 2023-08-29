package com.haoliang.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haoliang.common.util.HttpUtil;

import java.util.HashMap;

/**
 * @author Dominick Li
 * @Description
 * @CreateTime 2023/7/31 10:25
 **/
public class SendEmailTest {

    public static void main(String[] args) {
        HashMap<String,String> headers=new HashMap<>();
        JSONObject data=new JSONObject();
        headers.put("accept","application/json");
        //填写api-key
        headers.put("api-key","");

        JSONObject sender=new JSONObject();
        sender.put("name","TikTok Guild");
        //发送邮箱的账号
        sender.put("email","");

        JSONArray to=new JSONArray();
        JSONObject to1=new JSONObject();
        //接受邮箱的账号
        to1.put("email","");
        to.add(to1);
        //data.put("sender",sender);
        data.put("to",to);
        data.put("subject","[TikTok Guild] Please verify your device");
        data.put("htmlContent","<p><span style=\"font-size: 14px;\">Hey !</span></p><p><span style=\"font-size: 14px;\">Verification code: {{code}}</span></p><p><span style=\"font-size: 14px;\">Expires in {{time}} minutes</span></p><p><span style=\"font-size: 14px;\">Please use it as soon as possible!</span></p>".replace("{{code}}", "996688").replace("{{time}}", "60"));
        System.out.println(JSONObject.toJSONString(data));
        HttpUtil.postJson("https://api.brevo.com/v3/smtp/email",headers,data.toJSONString());
    }

}
