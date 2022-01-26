package org.zyj.hulk;

import com.alibaba.boot.dubbo.autoconfigure.DubboAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;

/**
 * @author zhangyj
 * @date 2022/1/26 18:45
 */
//@SpringBootApplication
//禁用数据库自动装配
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class})
//禁用dubbo自动装配
//@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, DubboAutoConfiguration.class})
public class HulkServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(HulkServiceApp.class, args);
    }
}
