import org.apache.commons.lang.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImageWaterMark {
    // 设置水印长度是对角线的几倍
    private static final double waterMarkLength = 0.8;
    // 额外顺时针方向旋转角度,默认为0,即在对角线上
    private static final double waterMarkAngle = 0;
    // 水印透明度
    private static final float alpha = 0.3f;
    // 多行水印行数
    private static final int multiLineNumber = 5;

    /**
     * 根据文件路径添加水印
     *
     * @param filepath      文件路径
     * @param fontWatermark 水印内容
     * @param isMultiLine   是否多行水印 false则只有一行在对角线
     * @return
     * @throws Exception
     * @变更记录 2022年5月23日 上午10:53:13 丁县迎创建
     */
    public static byte[] setWaterMarkByPath(String filepath, String fontWatermark, Boolean isMultiLine)
            throws Exception {
        // 读取原图片信息
        if (StringUtils.isBlank(filepath) || fontWatermark == null) {
            return null;
        }
        File srcImgFile = new File(filepath);
        Image srcImg = ImageIO.read(srcImgFile);
        return setWaterMarksByImage(srcImg, "jpg", fontWatermark, isMultiLine);
    }

    /**
     * 根据图片字节数组添加水印
     *
     * @param b             图片字节数组
     * @param fontWatermark 水印内容
     * @param isMultiLine   是否多行水印 false则只有一行在对角线
     * @return
     * @throws Exception
     * @变更记录 2022年5月23日 上午10:53:36 丁县迎创建
     */
    public static byte[] setWaterMarkByByte(byte[] b, String fontWatermark, Boolean isMultiLine) throws Exception {
        if (b == null || b.length == 0 || fontWatermark == null) {
            return null;
        }
        ByteArrayInputStream in = new ByteArrayInputStream(b);
        Image srcImg = ImageIO.read(in);

        in.close();
        return setWaterMarksByImage(srcImg, "jpg", fontWatermark, isMultiLine);
    }

    /**
     * 创建空白图片添加水印 ,主要是为了其他文件
     *
     * @param width          创建图片的高度，一般为其他文件的高度
     * @param height         创建图片的宽度，一般为其他文件的宽度
     * @param fontWatermark 水印内容
     * @param isMultiLine    是否多行水印 false则只有一行在对角线
     * @return
     * @throws Exception
     * @变更记录 2022年5月14日 上午9:44:51 丁县迎创建
     */
    public static byte[] setWaterMarkBySize(Integer width, Integer height, String fontWatermark, Boolean isMultiLine)
            throws Exception {
        if (width < 0 || height < 0 || fontWatermark == null) {
            return null;
        }
        BufferedImage bufImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        return setWaterMarksByImage(bufImg, "png", fontWatermark, isMultiLine);
    }

    /**
     * 获取水印图片Graphics2D对象
     *
     * @param graphics
     * @param srcImgWidth
     * @param srcImgHeight
     * @param fontWatermark
     * @return
     * @变更记录 2022年5月23日 上午10:50:17 丁县迎创建
     */
    private static Graphics2D getWaterMarkGraphics2D(Graphics2D graphics, int srcImgWidth, int srcImgHeight,
                                                     String fontWatermark, Boolean isMultiLine) {
        double length = waterMarkLength;
        if (isMultiLine) {
            length = 0.4;
        }
        // 对角线长度
        double z = Math.sqrt(srcImgWidth * srcImgWidth + srcImgHeight * srcImgHeight);
        // 计算角度
        double angle = Math.asin(srcImgHeight / z) * 180 / Math.PI;
        // 旋转图片
        graphics.rotate(Math.toRadians(angle + waterMarkAngle), (double) srcImgWidth / 2, (double) srcImgHeight / 2);
        // 计算字体大小
        Font font;
        // 设置字体颜色
        graphics.setColor(Color.lightGray);
        // 设置字体平滑
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int i = graphics.getFontMetrics().stringWidth(fontWatermark);
        int font_size = (int) ((int) (z * 12 / i) * length);
        float realWidth;
        // 设置具体水印长度
        do {
            font = new Font("黑体", Font.BOLD, font_size);
            graphics.setFont(font);
            realWidth = (float) graphics.getFontMetrics(font).stringWidth(fontWatermark);
            font_size--;
        } while (realWidth >= z * length);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        return graphics;
    }

    /**
     * 图片添加水印
     *
     * @param
     * @param srcImg
     * @param formatName
     * @param fontWatermark
     * @return
     * @throws IOException
     * @throws Exception
     * @变更记录 2022年5月14日 上午9:45:27 丁县迎创建
     */
    private static byte[] setWaterMarksByImage(Image srcImg, String formatName, String fontWatermark,
                                               Boolean isMultiLine) throws Exception {
        int srcImgWidth = srcImg.getWidth(null);
        // 图片高度
        int srcImgHeight = srcImg.getHeight(null);
        float z = (float) Math.sqrt(srcImgWidth * srcImgWidth + srcImgHeight * srcImgHeight);
        // 创建缓冲图片，操作原图片可能会失真
        BufferedImage bufImg = new BufferedImage(srcImgWidth, srcImgHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = (Graphics2D) bufImg.getGraphics();
        graphics.drawImage(srcImg, 0, 0, srcImgWidth, srcImgHeight, null);
        Graphics2D g = getWaterMarkGraphics2D(graphics, srcImgWidth, srcImgHeight, fontWatermark, isMultiLine);
        float realHeight = (float) g.getFontMetrics(g.getFont()).getHeight();
        float realWidth = (float) g.getFontMetrics(g.getFont()).stringWidth(fontWatermark);
        if (isMultiLine) {
            for (int i = 0; i < multiLineNumber; i++) {
                fontWatermark = fontWatermark + "   " + fontWatermark;
                float x = -z / multiLineNumber;
                float y = i * z / multiLineNumber;
                g.drawString(fontWatermark, x, y);
            }
        } else {
            // 计算水印位置
            float x = (srcImgWidth - realWidth) / 2;
            float y = (srcImgHeight + realHeight / 2) / 2;
            // 添加水印
            g.drawString(fontWatermark, x, y);
        }
        // 销毁画笔
        g.dispose();
        // 设置文件名
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // 输出缓冲图片
        try {
            // 输出缓冲图片
            ImageIO.write(bufImg, formatName, os);
        } catch (Exception e) {
            throw new Exception("图片加水印失败");
        } finally {
            os.close();
        }
        return os.toByteArray();
    }
}
