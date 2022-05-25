import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PdfToImgUtil {

	/**
	 * 将PDF文件转为图片
	 * 
	 * @param filepath
	 * @return
	 * @throws Exception
	 * @变更记录 2022年5月13日 下午2:36:07 丁县迎创建
	 */
	public static List<byte[]> PDFToImages(String filepath) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		FileInputStream input = new FileInputStream(filepath);
		// 加载PDF文档
		PDDocument pdDocument = PDDocument.load(input);
		List<byte[]> images = new ArrayList<byte[]> ();
		try {
			// 获取页数
			int total = pdDocument.getNumberOfPages();
			// PDF阅读器对象
			PDFRenderer renderer = new PDFRenderer(pdDocument);
			for (int i = 0; i < total; i++) {
				// PDF转换缓冲Image
				BufferedImage image = renderer.renderImageWithDPI(i, 96);
				// 写入字节流
				ImageIO.write(image, "jpg", os);
				ImageIO.write(image,"jpg",new File("C:\\Users\\Administrator\\Desktop\\utilTest\\"+i+".jpg"));
				images.add(os.toByteArray());
				os.reset();
			}
		} catch (Exception e) {
			throw new Exception("PDF转图片失败");
		} finally {
			os.close();
			input.close();
			pdDocument.close();
		}
		return images;
	}

	/**
	 * PDF文件添加水印
	 * 
	 * @param filepath    文件路径
	 * @param waterMark   水印内容
	 * @param isMultiLine 是否多行水印 false则只有一行在对角线
	 * @return
	 * @throws IOException
	 * @throws Exception
	 * @变更记录 2022年5月14日 上午9:35:48 丁县迎创建
	 */
	public static byte[] setPDFWaterMark(String filepath, String waterMark, Boolean isMultiLine) throws Exception {
		FileInputStream input = null;
		PDDocument pdDocument = null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		;
		try {
			input = new FileInputStream(filepath);
			// 加载PDF文档
			pdDocument = PDDocument.load(input);
			// 获取页数
			int total = pdDocument.getNumberOfPages();

			for (int i = 0; i < total; i++) {
				PDPage page = pdDocument.getPage(i);
				PDPageContentStream stream = new PDPageContentStream(pdDocument, page,
						PDPageContentStream.AppendMode.APPEND, true, true);
				float pageHeight = page.getMediaBox().getHeight();
				float pageWidth = page.getMediaBox().getWidth();
				byte[] bytes = ImageWaterMark.setWaterMarkBySize((int) pageWidth, (int) pageHeight, waterMark,
						isMultiLine);
				if (bytes == null) {
					continue;
				}
				PDImageXObject pdImage = PDImageXObject.createFromByteArray(pdDocument, bytes, "waterMark");
				stream.drawImage(pdImage, 0, 0, pageWidth, pageHeight);
				stream.close();
				pdDocument.save(os);
			}
		} catch (Exception e) {
			throw new Exception("PDF转图片失败");
		} finally {
			input.close();
			pdDocument.close();
			os.close();
		}
		return os.toByteArray();
	}

	/**
	 * 将PDF转为图片并添加水印
	 * 
	 * @param filepath    文件路径
	 * @param waterMark   水印内容
	 * @param isMultiLine 是否多行水印 false则只有一行在对角线
	 * @return
	 * @throws Exception
	 * @变更记录 2022年5月13日 下午2:36:28 丁县迎创建
	 */
	public static List<byte[]> PDFToWaterMarkImages(String filepath, String waterMark, Boolean isMultiLine)
			throws Exception {
		List<byte[]> imgBytes = PDFToImages(filepath);
		List<byte[]> WaterMarkImages = new ArrayList<byte[]>();
		for (byte[] b : imgBytes) {
			byte[] bytes = ImageWaterMark.setWaterMarkByByte(b, waterMark, isMultiLine);
			WaterMarkImages.add(bytes);
		}
		return WaterMarkImages;
	}
}
