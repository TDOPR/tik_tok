package com.haoliang.test;

import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class AddImageWaterMark {

    public static void main(String[] args) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(new File("C:\\Users\\Administrator\\Desktop\\test.png"));
        InputStream result = addWaterMark(fileInputStream, "Zero Carbon Envoy/", "Musk", "7189-A947-4fc3-83d1-B58e", "JULY 17,2023");
        FileUtils.copyInputStreamToFile(result, new File("C:\\Users\\Administrator\\Desktop\\save.png"));
    }

    /**
     * @param srcImgFile 源图片输入流
     * @return 加水印之后的输入流
     */
    public static InputStream addWaterMark(InputStream srcImgFile, String level, String name, String number, String date) {
        try {
            //文件转化为图片
            Image srcImg = ImageIO.read(srcImgFile);
            //获取图片的宽
            int srcImgWidth = srcImg.getWidth(null);
            //获取图片的高
            int srcImgHeight = srcImg.getHeight(null);
            // 加水印
            BufferedImage bufImg = new BufferedImage(srcImgWidth, srcImgHeight, BufferedImage.TYPE_INT_RGB);
            {
                //获取水印对象
                Graphics2D g = bufImg.createGraphics();
                g.drawImage(srcImg, 0, 0, srcImgWidth, srcImgHeight, null);
                // 抗锯齿
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                //1.设置编号
                Color color = new Color(65, 144, 222);
                //根据图片的背景设置水印颜色
                g.setColor(color);
                Font font = new Font("黑体", Font.PLAIN, 32);
                //设置字体
                g.setFont(font);
                //画字
                g.drawString(number, 221, 890);

                //2.设置日期
                font = new Font("黑体", Font.PLAIN, 20);
                //设置字体
                g.setFont(font);
                g.drawString(date, 702, 707);

                //3.设置级别
                Color levelColor = new Color(153,153,153);
                g.setColor(levelColor);
                int levelFontSize=24;
                font = new Font("黑体", Font.PLAIN, levelFontSize);
                //设置字体
                g.setFont(font);
                g.drawString(level, 596, 521);
                //计算字体占用的大小
                int len= level.length()*levelFontSize/2;

                //4.设置姓名
                Color nameColor = Color.BLACK;
                g.setColor(nameColor);
                font = new Font("黑体", Font.BOLD, 36);
                //设置字体
                g.setFont(font);
                g.drawString(name, 596+len, 521);

                //渲染
                g.dispose();
            }
            // 输出图片
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Thumbnails.of(bufImg).scale(1f).outputQuality(0.25f).outputFormat("png").toOutputStream(os);
//            Thumbnails.of(bufImg).scale(1f).outputQuality(0.25f).outputFormat("png").toFile(new File("D:\\work\\2.png"));
            return new ByteArrayInputStream(os.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
