package com.lyd.mall.thirdparty;

import com.aliyun.oss.OSSClient;
import com.lyd.mall.thirdparty.component.SmsComponent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
class MallThirdPartyApplicationTests {
    @Resource
    OSSClient ossClient;

    @Autowired
    SmsComponent smsComponent;
    @Test
    void contextLoads() {
    }
    /**
     * @Description: 阿里云原生sdk上传文件流
     * @Param: []
     * @return: void
     * @Author: Liuyunda
     * @Date: 2021/1/18
     */
    @Test
    public void testUpload() throws FileNotFoundException {
        // Endpoint以杭州为例，其它Region请按实际情况填写。
        // String endpoint = "oss-cn-beijing.aliyuncs.com";
        // // 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
        //
        // // 创建OSSClient实例。
        // OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 上传文件流。
        InputStream inputStream = new FileInputStream("D:\\ME\\avatar.jpg");
        ossClient.putObject("mall-lyd", "avatar.jpg", inputStream);

        // 关闭OSSClient。
        ossClient.shutdown();

        System.out.println("上传完成");
    }

    /**
     * @Description: springcloud-alibaba oos 快速上传
     * @Param: []
     * @return: void
     * @Author: Liuyunda
     * @Date: 2021/1/18
     */
    @Test
    public void testUpload2() throws FileNotFoundException {
        // 上传文件流。
        InputStream inputStream = new FileInputStream("D:\\ME\\hero.png");
        ossClient.putObject("mall-lyd", "mm.png", inputStream);

        // 关闭OSSClient。
        ossClient.shutdown();

        System.out.println("上传完成");
    }

    @Test
    public void sendSms(){
        smsComponent.sendCode("17648273024","iloveyou");
    }
}
