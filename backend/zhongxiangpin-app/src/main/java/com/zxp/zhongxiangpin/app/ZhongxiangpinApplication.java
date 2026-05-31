package com.zxp.zhongxiangpin.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Zhongxiangpin backend application entry point.
 *
 * <p>This class only assembles the backend modules. Business rules stay in domain and
 * application modules, while technical adapters stay behind infrastructure boundaries.</p>
 */
@SpringBootApplication(scanBasePackages = "com.zxp.zhongxiangpin")
public class ZhongxiangpinApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhongxiangpinApplication.class, args);
    }
}
