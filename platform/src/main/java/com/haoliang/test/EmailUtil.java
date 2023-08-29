package com.haoliang.test;

import com.haoliang.config.EmailConfig;
import com.sun.mail.util.MailSSLSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


public class EmailUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailUtil.class);

    private EmailUtil() {
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


    public static boolean sendEmailByParam(EmailConfig emailConfig, List<File> fileList, String subject, String content) {
        try {
            List<FileInfo> fileInfoList = new ArrayList<>();
            if (fileList != null) {
                for (File file : fileList) {
                    fileInfoList.add(new FileInfo(file.getName(), new FileInputStream(file)));
                }
            }
            send(emailConfig, fileInfoList, subject, content);
            return true;
        } catch (Exception e) {
            LOGGER.error("邮件发送失败: errorMsg={}", e.getMessage());
            return false;
        }
    }

    public static boolean sendEmailByParam(EmailConfig emailConfig, MultipartFile[] fileList, String subject, String content) {
        try {
            List<FileInfo> fileInfoList = new ArrayList<>();
            if (fileList != null) {
                for (MultipartFile file : fileList) {
                    fileInfoList.add(new FileInfo(file.getOriginalFilename(), file.getInputStream()));
                }
            }
            send(emailConfig, fileInfoList, subject, content);
            return true;
        } catch (Exception e) {
            LOGGER.error("邮件发送失败: errorMsg={}", e.getMessage());
            return false;
        }
    }


    /**
     * 发送邮件的主要代码
     */
    private static void send(EmailConfig emailConfig, List<FileInfo> fileList, String subject, String text) throws Exception {
        JavaMailSenderImpl jms = new JavaMailSenderImpl();
        MimeMessage mimeMessage = jms.createMimeMessage();
        //是否包含附件
        boolean multipart = fileList.size() > 0;
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, multipart, "utf-8");
        //设置邮箱服务的地址
        jms.setHost(emailConfig.getHost());
        //设置邮箱服务的端口
        jms.setPort(emailConfig.getPort());
        //设置发邮件的账号
        jms.setUsername("AKIA4HGDRZF6CN5CU5YC");
        //设置发邮件的账号的客户端授权码
        jms.setPassword("BPOfhm3kpZ9u7QYbCZYDP+e2f7THt7DSMoa3wX7UDUeZ");
        //设置邮件内容的编码格式
        Properties p = new Properties();
        p.setProperty("mail.smtp.auth", "true");
        if (emailConfig.isEnableSSL()) {
            //设置ssl认证信息
            p.setProperty("mail.transport.protocol", "smtp");
            p.put("mail.smtp.ssl.enable", "true");
            //开启安全协议
            MailSSLSocketFactory sf = null;
            sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
            p.put("mail.smtp.ssl.socketFactory", sf);
        }
        jms.setJavaMailProperties(p);
        //设置发送人
        helper.setFrom(new InternetAddress("support@uncnggf.org"));
        //设置收集人的账号信息
        helper.setTo("826853123qq@gmail.com");
        //设置邮件主题
        helper.setSubject(subject);
        //设置邮件内容为网页格式
        helper.setText(text, true);
        //纯文本格式
        //helper.setText(text);
        //设置邮件的附件信息
        for (FileInfo fileInfo : fileList) {
            ByteArrayDataSource attachment = new ByteArrayDataSource(fileInfo.getInputStream(), "application/octet-stream");
            helper.addAttachment(fileInfo.getName(), attachment);
        }
        jms.send(mimeMessage);
        LOGGER.info("发送成功!");
    }
    public static void main(String[] args) throws Exception {
        EmailConfig emailConfig = new EmailConfig();
        //qq邮箱=smtp.qq.com   腾讯企业邮箱=smtp.exmail.qq.com
        emailConfig.setHost("email-smtp.ap-southeast-2.amazonaws.com");
        //qq邮箱=25  腾讯企业邮箱=465
        emailConfig.setPort(25);
        //qq邮箱=false  腾讯企业邮箱=true
        emailConfig.setEnableSSL(false);
        //发件人
        //发送人昵称
        emailConfig.setFormName("皓亮君");
        //接受邮件人的邮箱账号
        send(emailConfig, new ArrayList<>(), "邮件主题", "邮件的内容");
    }

}
