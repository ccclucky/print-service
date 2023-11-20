package com.cclucky.printserver;

import com.cclucky.printserver.handle.JobEventHandle;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.ImageIO;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.PrinterIsAcceptingJobs;
import javax.print.attribute.standard.QueuedJobCount;
import javax.print.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrintExample {

//    public PrintExample(String filename) {
//        try {
//            // 获得打印属性
//            PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
//            pras.add(new Copies(1));
//            // 获得打印设备
//            PrintService[] pss = PrintServiceLookup.lookupPrintServices(null, pras);
//            if (pss.length == 0)
//                throw new RuntimeException("No printer services available.");
//            PrintService ps = pss[pss.length - 1];
//            System.out.println("Printing to " + ps);
//            // 获得打印工作
//            DocPrintJob job = ps.createPrintJob();
//            FileInputStream fin = null;
//            if (filename.endsWith("pdf")) {
//                List<Map<String, String>> maps = this.pdfToImage(filename, "jpg");
//                for (Map<String, String> map : maps) {
//                    fin = new FileInputStream(map.get("filePath"));
//                    Doc doc = new SimpleDoc(fin, DocFlavor.INPUT_STREAM.JPEG, null);
//                    // 开始打印
//                    job.print(doc, pras);
//                }
//            } else {
//                fin = new FileInputStream(filename);
//                Doc doc = new SimpleDoc(fin, DocFlavor.INPUT_STREAM.AUTOSENSE, null);
//                // 开始打印
//                job.print(doc, pras);
//            }
//            if (fin != null) {
//                fin.close();
//            }
//        } catch (IOException ie) {
//            ie.printStackTrace();
//        } catch (PrintException pe) {
//            pe.printStackTrace();
//        }
//    }

    public PrintExample(String filename) {
        try {
            // 获得打印属性
            PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
            pras.add(new Copies(1));

            // 在每次循环中创建一个新的打印工作
            PrintService[] pss = PrintServiceLookup.lookupPrintServices(null, null);
            if (pss.length == 0) {
                throw new RuntimeException("No printer services available.");
            }
            PrintService ps = pss[pss.length - 1];

            if (filename.endsWith("pdf")) {
                List<Map<String, String>> maps = this.pdfToImage(filename, "jpg");
                for (Map<String, String> map : maps) {
                    FileInputStream fin = new FileInputStream(map.get("filePath"));
                    Doc doc = new SimpleDoc(fin, DocFlavor.INPUT_STREAM.JPEG, null);

                    // 获取打印机是否接受新的打印任务的属性
                    PrintServiceAttributeSet attributes = ps.getAttributes();
                    Attribute attr = attributes.get(PrinterIsAcceptingJobs.class);

                    System.out.println("Printing to " + ps);
                    DocPrintJob job = ps.createPrintJob();

                    // 开始打印
                    job.print(doc, pras);

                    fin.close();
                }
            } else {
                FileInputStream fin = new FileInputStream(filename);
                Doc doc = new SimpleDoc(fin, DocFlavor.INPUT_STREAM.AUTOSENSE, null);

                // 获取打印机是否接受新的打印任务的属性
                PrintServiceAttributeSet attributes = ps.getAttributes();
                Attribute attr = attributes.get(QueuedJobCount.class);

                if (Integer.parseInt(String.valueOf(attr)) > 0) {
                    System.out.println("排队中，请稍后");
                }

                System.out.println("Printing to " + ps);
                DocPrintJob job = ps.createPrintJob();

                // 开始打印
                job.print(doc, pras);

                fin.close();
            }
        } catch (IOException | PrintException e) {
            e.printStackTrace();
        }
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
            File FilePath = new File(filename);
            // 文件流
            FileInputStream inputStream = new FileInputStream(FilePath);

            int dpi = 296;
            pdDocument = PDDocument.load(inputStream);
            PDFRenderer renderer = new PDFRenderer(pdDocument);
            int pageCount = pdDocument.getNumberOfPages();
            /* dpi越大转换后越清晰，相对转换速度越慢 */
            for (int i = 0; i < pageCount; i++) {
                resultMap = new HashMap<>();
                fileName = filename.substring(filename.lastIndexOf("\\"), filename.lastIndexOf(".")) + "_" + (i + 1) + "." + type;
                int lastBackslashIndex = filename.lastIndexOf("\\");
                imgPath = filename.substring(0, lastBackslashIndex) + "\\" + fileName;
                BufferedImage image = renderer.renderImageWithDPI(i, dpi);
                ImageIO.write(image, type, new File(imgPath));
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

    public static void main(String args[]) throws Exception {
//        if (args.length < 1) {
//            System.err.println("Usage: java PrintImage <image name>");
//            System.exit(1);
//        }
        String filename1 = "E:\\school\\数据仓库\\lab-1\\实验1 数据仓库综合实验.pdf";
        String filename2 = "E:\\school\\数据仓库\\lab-1\\Ribbetai (1).jpg";
        String filename3 = "E:\\school\\数据仓库\\lab-1";
        new PrintExample(filename2);
    }
}
