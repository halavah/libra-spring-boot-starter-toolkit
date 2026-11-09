package cn.myslayers.libra.framework.toolkit.demo;

import cn.myslayers.libra.framework.toolkit.http.PostmanUtil;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.HttpCore;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.ManualRegistry;

public class PostmanApplication {
    public static void main(String[] args) {
        System.out.println("=== PostmanUtil [全局模式] 启动 ===");

        // 设置端口
        PostmanUtil.setPort(5176);

        // 开启核心调试日志 (服务器启动、扫描等)
        PostmanUtil.setDebugLogEnabled(true);
        // 开启启动时打印的接口列表
        PostmanUtil.setStartupLogEnabled(true);
        // 开启请求详情日志 (curl、出入参等)
        PostmanUtil.setRequestDetailLogEnabled(true);

        // 设置普通日志开关
//        DatabaseUtil.LoggerCore.setDebugLogEnabled(true);
        // 检查普通日志开关状态
//        DatabaseUtil.LoggerCore.setSqlDebugLogEnabled(true);

        // 在全局模式下，明确指定扫描Controller所在的包
        ManualRegistry.addScan("cn.myslayers.libra.module.toolkit.controller");
//        ManualRegistry.addScan("cn.myslayers.app.device");

        // 启动服务器
        HttpCore.startServer();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n正在关闭Postman测试工具...");
            HttpCore.stopServer();
        }));
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            HttpCore.stopServer();
        }
    }
}
