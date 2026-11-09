package cn.myslayers.libra.framework.toolkit.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = {"cn.myslayers.libra"})
public class DemoToolkitApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(DemoToolkitApplication.class, args);
    }
}
