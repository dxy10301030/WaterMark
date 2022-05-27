import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PDFKeywordLocation extends PDFTextStripper {

    // 关键字
    private String keyword;
    // PDF文件路径
    private String pdfPath;
    // 坐标信息集合
    private List<Map<String, Float>> list = new ArrayList<Map<String, Float>>();
    // 当前页信息集合
    private List<Map<String,Float>> pageList = new ArrayList<Map<String,Float>>();

    // 有参构造方法
    public PDFKeywordLocation(String keyword, String pdfPath) throws IOException {
        super();
        super.setSortByPosition(true);
        this.pdfPath = pdfPath;
        this.keyword = keyword;
    }

    public List<Map<String, Float>> getKeyWordLocation() throws IOException {
        return this.getCoordinate(null);
    }

    public List<Map<String, Float>> getKeyWordLocation(Integer page) throws IOException {
        return this.getCoordinate(page);
    }

    // 获取坐标信息
    private List<Map<String, Float>> getCoordinate(Integer page) throws IOException {
        try {
            document = PDDocument.load(new File(pdfPath));
            int pages = document.getNumberOfPages();
            for (int i = (page != null ? page : 0); i <= (page != null ? page : pages); i++) {
                pageList.clear();
                super.setSortByPosition(true);
                super.setStartPage(i);
                super.setEndPage(i);
                Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
                super.writeText(document, dummy);
                for (Map<String, Float> stringFloatMap : pageList) {
                    stringFloatMap.put("page", (float) i);
                }
                list.addAll(pageList);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (document != null) {
                document.close();
            }
        }
        return list;
    }
    // 获取坐标信息
    @Override
    protected void writeString(String string, List<TextPosition> textPositions) {
        int length = keyword.length();
        int index = string.indexOf(keyword);
        if (index != -1) {
            int middle = index + length / 2;
            TextPosition textPosition = textPositions.get(middle);
            Map<String,Float> map = new HashMap<String, Float>();
            map.put("x",textPosition.getTextMatrix().getTranslateX() + (textPosition.getWidth() / 2));
            map.put("y",textPosition.getTextMatrix().getTranslateY() + (textPosition.getHeight() / 2) * 0.865f);
            pageList.add(map);
        }
    }
}

