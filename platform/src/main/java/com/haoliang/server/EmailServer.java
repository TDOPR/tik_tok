package com.haoliang.server;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haoliang.common.config.GlobalProperties;
import com.haoliang.common.model.HttpResult;
import com.haoliang.common.util.HttpUtil;
import com.haoliang.common.util.SpringUtil;
import com.haoliang.config.EmailConfig;
import com.haoliang.model.dto.EmailTemplateDTO;
import com.sun.mail.util.MailSSLSocketFactory;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Dominick Li
 * @description 邮件发送工具类
 **/
@Slf4j
@NoArgsConstructor
public class EmailServer {

    private static final EmailConfig CONFIG = SpringUtil.getBean(EmailConfig.class);

    /**
     * 发邮箱的初始者
     */
    private static AtomicInteger sendUserIndex = new AtomicInteger(0);

    public static int reset() {
        return sendUserIndex.getAndSet(0);
    }

    private static class FileInfo {

        public FileInfo(String name, InputStream inputStream) {
            this.name = name;
            this.inputStream = inputStream;
        }

        private String name;

        private InputStream inputStream;

        public String getName() {
            return name;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

    }


    public static boolean send(EmailTemplateDTO emailTemplateDTO) {
        return send(emailTemplateDTO.getTitle(), emailTemplateDTO.getContent(), emailTemplateDTO.getTo());
    }

    /**
     * 发送邮件
     *
     * @param subject 标题
     * @param text    内容
     * @param to      发送给谁
     * @return 发送是否成功
     */
    public static boolean send(String subject, String text, String to) {
        return send(subject, text, new String[]{to}, null, 0);
    }

    /**
     * 发送邮件
     *
     * @param fileList
     * @param subject
     * @param content
     * @return
     */
    public static boolean sendEmail(List<File> fileList, String subject, String content, String[] to) {
        try {
            List<FileInfo> fileInfoList = new ArrayList<>();
            if (fileList != null) {
                for (File file : fileList) {
                    fileInfoList.add(new FileInfo(file.getName(), new FileInputStream(file)));
                }
            }
            return send(subject, content, to, fileInfoList, 0);
        } catch (Exception e) {
            log.error("邮件发送失败: errorMsg={}", e.getMessage());
            return false;
        }
    }

    public static boolean sendEmail(MultipartFile[] fileList, String subject, String content, String[] to) {
        try {
            List<FileInfo> fileInfoList = new ArrayList<>();
            if (fileList != null) {
                for (MultipartFile file : fileList) {
                    fileInfoList.add(new FileInfo(file.getName(), file.getInputStream()));
                }
            }
            return send(subject, content, to, fileInfoList, 0);
        } catch (Exception e) {
            log.error("邮件发送失败: errorMsg={}", e.getMessage());
            return false;
        }
    }


    /**
     * 发送邮件的主要代码
     */
    public static boolean send(String subject, String text, String[] to, List<FileInfo> fileList, Integer deep) {
        if (deep >= CONFIG.getUsers().size()) {
            log.error("已存在的邮箱账号都不能发送邮件！");
            return false;
        }
        //如果第一次并且正常发送邮箱的下标大于0
        if (deep == 0 && sendUserIndex.get() > 0) {
            deep = sendUserIndex.get();
        }
        try {
            JavaMailSenderImpl jms = new JavaMailSenderImpl();
            MimeMessage mimeMessage = jms.createMimeMessage();
            //是否包含附件
            boolean multipart = fileList != null && fileList.size() > 0;
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, multipart, "utf-8");
            BeanUtils.copyProperties(CONFIG, jms);
            Map<String, String> emailInfo = CONFIG.getUsers().get(deep);
            log.info("sendEmailInfo:{}", emailInfo);
            String username = emailInfo.get("username");
            jms.setUsername(username);
            jms.setPassword(emailInfo.get("password"));
            //设置邮件内容的编码格式
            Properties props = new Properties();
            props.setProperty("mail.smtp.auth", "true");
            if (CONFIG.isEnableSSL()) {
                //设置ssl认证信息
                props.setProperty("mail.transport.protocol", "smtp");
                props.put("mail.smtp.ssl.enable", "true");
                //开启安全协议
                MailSSLSocketFactory sf = null;
                sf = new MailSSLSocketFactory();
                sf.setTrustAllHosts(true);
                props.put("mail.smtp.ssl.socketFactory", sf);
            }
            if (CONFIG.getHost().contains("gmail")) {
                props.put("mail.smtp.starttls.enable", "true");
            }
            jms.setJavaMailProperties(props);
            //设置发送人
            helper.setFrom(new InternetAddress(username));
            //helper.setFrom(username, CONFIG.getFormName());
            //设置收集人的账号信息       也可以把集合转换成字符串数组   String to[] = new String[List.size]; List.toArray(to);
            helper.setTo(to);
            //设置邮件主题
            helper.setSubject(subject);
            //设置邮件内容为网页格式
            helper.setText(text, true);
            //纯文本格式
            //helper.setText(text);
            //设置邮件的附件信息
            if (fileList != null) {
                for (FileInfo fileInfo : fileList) {
                    ByteArrayDataSource attachment = new ByteArrayDataSource(fileInfo.getInputStream(), "application/octet-stream");
                    helper.addAttachment(fileInfo.getName(), attachment);
                }
            }
//            //测试手动异常
//            if(deep==0){
//                System.out.println(1/0);
//            }
            jms.send(mimeMessage);
            //log.info("发送成功!");
            if (deep > 0) {
                log.info("set sendUserIndex={}", deep);
                sendUserIndex.set(deep);
            }
            return true;
        } catch (Exception e) {
            log.error("send email error:{}", e);
            if (e instanceof MailSendException || e instanceof MailAuthenticationException) {
                deep++;
                return send(subject, text, to, null, deep);
            }
            return false;
        }
    }

    /**
     * 发送第三方的Api
     *
     * @param title   标题
     * @param content 内容
     * @param email   发送到
     * @return
     */
    public static boolean sendBrevoEmail(String title, String content, String email) {
        HashMap<String, String> headers = new HashMap<>();
        JSONObject data = new JSONObject();
        headers.put("accept", "application/json");
        //填写api-key
        headers.put("api-key", GlobalProperties.getBrevoApiKey());

        JSONObject sender = new JSONObject();
        sender.put("name", CONFIG.getFormName());
        sender.put("email", GlobalProperties.getEmail());
        JSONArray to = new JSONArray();
        JSONObject to1 = new JSONObject();
        to1.put("email", email);
        to.add(to1);
        data.put("sender", sender);
        data.put("to", to);
        data.put("subject", title);
        data.put("htmlContent", content);
        //System.out.println(JSONObject.toJSONString(data));
        HttpResult<String> result = HttpUtil.postJson("https://api.brevo.com/v3/smtp/email", headers, data.toJSONString());
        if (result.isSuccess()) {
            JSONObject reulstData = JSONObject.parseObject(result.getData());
            if (reulstData.containsKey("messageId")) {
                return true;
            }
            log.error("sendBrevoEmail error:{}", result.getData());
        } else {
            log.error("sendBrevoEmail httpError:{}", result.getMsg());
        }
        return false;
    }
}



