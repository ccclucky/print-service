package com.cclucky.peintserver;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class PrintExample {

    public PrintExample(String filename) {
        try {
            // 获得打印属性
            PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
            pras.add(new Copies(1));
            // 获得打印设备
            DocFlavor flavor = DocFlavor.INPUT_STREAM.PDF;
//            PrintService pss[] = PrintServiceLookup.lookupPrintServices(DocFlavor.INPUT_STREAM.GIF, pras);
            PrintService[] pss = PrintServiceLookup.lookupPrintServices(null, pras);
            DocFlavor[] supportedDocFlavors = pss[pss.length - 1].getSupportedDocFlavors();
            if (pss.length == 0)
                throw new RuntimeException("No printer services available.");
            PrintService ps = pss[pss.length - 1];
            System.out.println("Printing to " + ps);
            // 获得打印工作
            DocPrintJob job = ps.createPrintJob();
            FileInputStream fin = new FileInputStream(filename);
            Doc doc = new SimpleDoc(fin, DocFlavor.INPUT_STREAM.AUTOSENSE, null);
            // 开始打印
            job.print(doc, pras);
            fin.close();
        } catch (IOException ie) {
            ie.printStackTrace();
        } catch (PrintException pe) {
            pe.printStackTrace();
        }
    }

    public static void pdfToImageExample(String filename) {
        // TODO: 2023/11/14 pdf 转 图片
    }

    public static void main(String args[]) throws Exception {
//        if (args.length < 1) {
//            System.err.println("Usage: java PrintImage <image name>");
//            System.exit(1);
//        }
        String filename1 = "E:\\school\\数据仓库\\lab-1\\实验1 数据仓库综合实验.pdf";
        String filename2 = "E:\\school\\数据仓库\\lab-1\\Ribbetai (1).jpg";
        String filename3 = "E:\\school\\数据仓库\\lab-1\\实验1 数据仓库综合实验.pdf";
        new PrintExample(filename2);
    }
}
