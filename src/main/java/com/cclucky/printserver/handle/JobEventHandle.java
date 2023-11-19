package com.cclucky.printserver.handle;

import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;
import javax.print.attribute.Attribute;
import javax.print.attribute.PrintJobAttributeSet;

public class JobEventHandle implements PrintJobListener {

    private PrintJobEvent pje;

    @Override
    public void printDataTransferCompleted(PrintJobEvent pje) {
        System.out.println("数据传输完成");
    }

    @Override
    public void printJobCompleted(PrintJobEvent pje) {
        System.out.println("打印完成");
        // 获取打印任务的属性
        PrintJobAttributeSet attributes = pje.getPrintJob().getAttributes();
        for (Attribute attribute : attributes.toArray()) {
            System.out.println(attribute.getName() + ": " + attribute);
        }
    }

    @Override
    public void printJobFailed(PrintJobEvent pje) {
        System.out.println("打印失败");
    }

    @Override
    public void printJobNoMoreEvents(PrintJobEvent pje) {
        System.out.println("无更多事件");
    }

    @Override
    public void printJobRequiresAttention(PrintJobEvent pje) {
        System.out.println("打印机需要关注");
    }

    @Override
    public void printJobCanceled(PrintJobEvent pje) {
        System.out.println("打印取消");
    }
}
