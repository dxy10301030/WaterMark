import org.apache.commons.lang.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;


public class ImageWaterMark {
    //设置水印长度是对角线的几倍
    private static final double waterMarkLength = 0.8;
    //额外顺时针方向旋转角度,默认为0,即在对角线上
    private static final double waterMarkAngle = 0;

    /**
     * PDf转图片加水印
     *
     * @param filepath
     * @param fontWatermarks
     * @return: void
     * @throws:
     */
    public static byte[] setWaterMarkByPath(String filepath, String... fontWatermarks) throws IOException {
        // 读取原图片信息
        if (StringUtils.isBlank(filepath) || fontWatermarks == null || fontWatermarks.length == 0) {
            return null;
        }
        File srcImgFile = new File(filepath);
        Image srcImg = ImageIO.read(srcImgFile);
        return setWaterMarksByImage(srcImg, fontWatermarks);
    }

    /**
     * 加水印
     *
     * @param b
     * @param fontWatermarks
     * @return: java.lang.String
     * @throws:
     */
    public static byte[] setWaterMarkByByte(byte[] b, String... fontWatermarks) throws IOException {
        if (b == null || b.length == 0 || fontWatermarks == null || fontWatermarks.length == 0) {
            return null;
        }
        ByteArrayInputStream in = new ByteArrayInputStream(b);
        Image srcImg = ImageIO.read(in);
        in.close();
        return setWaterMarksByImage(srcImg, fontWatermarks);
    }

    /**
     * 加水印
     *
     * @param srcImg
     * @param fontWatermarks
     * @return: java.lang.String
     * @throws:
     */
    public static byte[] setWaterMarkByImage(Image srcImg, String... fontWatermarks) throws IOException {
        if (srcImg == null || fontWatermarks == null || fontWatermarks.length == 0) {
            return null;
        }
        return setWaterMarksByImage(srcImg, fontWatermarks);
    }


    private static byte[] setWaterMarksByImage(Image srcImg, String... fontWatermarks) throws IOException {
        StringBuffer fontWatermark = new StringBuffer();
        for (String mark : fontWatermarks) {
            fontWatermark.append(mark);
        }
        // 图片宽度
        int srcImgWidth = srcImg.getWidth(null);
        // 图片高度
        int srcImgHeight = srcImg.getHeight(null);
        // 创建缓冲图片，操作原图片可能会失真
        BufferedImage bufImg = new BufferedImage(srcImgWidth, srcImgHeight, BufferedImage.TYPE_INT_RGB);
        // 创建缓冲图片图片画笔
        Graphics2D g = (Graphics2D) bufImg.getGraphics();
        // 将原图片绘制到缓冲图片
        g.drawImage(srcImg, 0, 0, srcImgWidth, srcImgHeight, null);
        // 对角线长度
        double z = Math.sqrt(srcImgWidth * srcImgWidth + srcImgHeight * srcImgHeight);
        // 计算角度
        double angle = Math.asin(srcImgHeight / z) * 180 / Math.PI;
        // 旋转图片
        g.rotate(Math.toRadians(angle + waterMarkAngle), (double) srcImgWidth / 2, (double) srcImgHeight / 2);
        // 计算字体大小


        Font font;
        // 设置字体颜色
        g.setColor(Color.lightGray);
        // 设置字体平滑
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int i = g.getFontMetrics().stringWidth(fontWatermark.toString());
        int font_size = (int) ((int) (z*12/i) *waterMarkLength);
        float realWidth;
        //设置具体水印长度
        do {
            font = new Font("黑体", Font.BOLD, font_size);
            g.setFont(font);
            realWidth = (float) g.getFontMetrics(font).stringWidth(fontWatermark.toString());
            font_size--;
        } while (realWidth >= z * waterMarkLength);
        // 获取该字体的字符高度
        float realHeight = (float) g.getFontMetrics(font).getHeight();
        // 计算水印位置
        float x = (srcImgWidth - realWidth) / 2;
        float y = (srcImgHeight + realHeight / 2) / 2;
        // 添加水印
        g.drawString(fontWatermark.toString(), x, y);
        // 销毁画笔
        g.dispose();
        // 设置文件名
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // 输出缓冲图片
        ImageIO.write(bufImg, "jpg", os);
        os.close();
        return os.toByteArray();
    }
}
