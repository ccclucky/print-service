package com.cclucky.printserver.handle;

import com.cclucky.printserver.common.result.Result;
import com.cclucky.printserver.config.prop.MinioProp;
import com.cclucky.printserver.utils.ConvertToMultipartFileUtil;
import com.cclucky.printserver.utils.MinioUtil;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.print.*;
import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.QueuedJobCount;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PrintEventHandle {

    @Autowired
    private MinioUtil minioUtil;
    @Autowired
    private MinioProp minioProp;

    public Map<String, String> handle(String filename) {
        Map<String, String> res = new HashMap<>();
        String msg = "正在打印中";
        Integer count = 0;
        try {
            // 获得打印属性
            PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
            pras.add(new Copies(1));

            // 获取打印服务
            PrintService[] pss = PrintServiceLookup.lookupPrintServices(null, pras);
            if (pss.length == 0) {
                throw new RuntimeException("No printer services available.");
            }

//            PrintService service = Arrays.stream(pss).filter(item -> item.getName().equals("HP")).collect(Collectors.toList()).get(0);
            PrintService service = pss[pss.length - 1];
            System.out.println("Printing to " + service);

            // 获取打印机是否接受新的打印任务的属性
            PrintServiceAttributeSet attributes = service.getAttributes();
            Attribute attr = attributes.get(QueuedJobCount.class);

            if (filename.endsWith("pdf")) {
                List<Map<String, String>> maps = this.pdfToImage(filename, "jpg");
                for (Map<String, String> map : maps) {
                    URL url = new URL(map.get("filePath"));
                    HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
                    InputStream inputStream = httpUrlConnection.getInputStream();
                    Doc doc = new SimpleDoc(inputStream, DocFlavor.INPUT_STREAM.JPEG, null);

                    DocPrintJob job = service.createPrintJob();

                    // 开始打印
                    job.print(doc, pras);

                    inputStream.close();
                }

                // 当前任务队列信息
                int jobCount = Integer.parseInt(String.valueOf(attr));
                if (jobCount > 0) {
                    msg = "排队中，请稍后";
                    res.put("msg", msg);
                    res.put("count", String.valueOf(jobCount));
                }

            } else {
                URL url = new URL(filename);
                HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpUrlConnection.getInputStream();

                Doc doc = new SimpleDoc(inputStream, DocFlavor.INPUT_STREAM.GIF, null);

                DocPrintJob job = service.createPrintJob();

                // 开始打印
                job.print(doc, pras);

                // 当前任务队列信息
                int jobCount = Integer.parseInt(String.valueOf(attr));
                if (jobCount > 0) {
                    msg = "排队中，请稍后";
                    res.put("msg", msg);
                    res.put("count", String.valueOf(jobCount));
                }

                inputStream.close();
            }
        } catch (IOException | PrintException e) {
            e.printStackTrace();
        }
        return res;
    }


    /**
     * 使用文件流整个pdf转换成图片
     *
     * @param filename 文件地址 如:C:\\Users\\user\\Desktop\\test + PDF文件名
     * @param type     图片类型 png 、jpg
     */
    // TODO: 2023/11/15 参数可以修改为文件输入流
    public List<Map<String, String>> pdfToImage(String filename, String type) {
        long startTime = System.currentTimeMillis();

        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> resultMap = null;
        PDDocument pdDocument = null;

        String fileName = null;
        String imgPath = null;

        try {
            // 将文件地址和文件名拼接成路径 注意：线上环境不能使用\\拼接
//            File FilePath = new File(filename);
//            // 文件流
//            FileInputStream inputStream = new FileInputStream(FilePath);

            // 获取url对应文件输入流
            URL url = new URL(filename);
            HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpUrlConnection.getInputStream();

            int dpi = 296;
            pdDocument = PDDocument.load(inputStream);
            PDFRenderer renderer = new PDFRenderer(pdDocument);
            int pageCount = pdDocument.getNumberOfPages();
            /* dpi越大转换后越清晰，相对转换速度越慢 */
            for (int i = 0; i < pageCount; i++) {
                resultMap = new HashMap<>();
                // 生成图片的名称
                fileName = filename.substring(filename.lastIndexOf(minioProp.getBucketName()) + minioProp.getBucketName().length() + 1, filename.lastIndexOf(".")) + "_" + (i + 1) + "." + type;
                // 图片生成路径
                int lastBackslashIndex = filename.lastIndexOf(minioProp.getBucketName()) + minioProp.getBucketName().length();
                imgPath = filename.substring(0, lastBackslashIndex) + "/" + fileName;
                // 将生成图片对象转换成文件
                BufferedImage image = renderer.renderImageWithDPI(i, dpi);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ImageIO.write(image, type, out);
                byte[] byteArray = out.toByteArray();
                MultipartFile multipartFile = new ConvertToMultipartFileUtil(byteArray, fileName, filename, type, byteArray.length);
                // 根据路径保存图片
                minioUtil.uploadFile(minioProp.getBucketName(), multipartFile, fileName, type);
                resultMap.put("filePath", imgPath); // 图片路径

                list.add(resultMap);
            }
            long endTime = System.currentTimeMillis();
            System.out.println("共耗时：" + ((endTime - startTime) / 1000.0) + "秒");  //转化用时
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 这里需要关闭PDDocument，不然如果想要删除pdf文件时会提示文件正在使用，无法删除的情况
                pdDocument.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }
}
