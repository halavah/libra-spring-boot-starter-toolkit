package cn.myslayers.libra.framework.toolkit.demo.controller;

import cn.myslayers.libra.framework.toolkit.http.PostmanUtil;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.AuthManager;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.PostmanController;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.PostmanMapping;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.RequiresAuth;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.Result;

@PostmanController("/auth")
public class AuthController {

    // Bearer Token 认证接口
    @PostmanMapping(value = "/bearer")
    @RequiresAuth(AuthManager.BearerTokenStrategy.class)
    public Result<String> bearerAuthTest() {
        return Result.ok("Bearer Token 认证成功，欢迎访问受保护的资源！");
    }

    // Signature 签名认证接口
    @PostmanMapping(value = "/signature")
    @RequiresAuth(AuthManager.SignatureStrategy.class)
    public Result<String> signatureAuthTest() {
        return Result.ok("Signature 签名认证成功，欢迎访问受保护的资源！");
    }

    // Main入口
    public static void main(String[] args) {
        PostmanUtil.HttpCore.startServer(AuthController.class);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            PostmanUtil.HttpCore.stopServer();
        }));
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            PostmanUtil.HttpCore.stopServer();
        }
    }
}
