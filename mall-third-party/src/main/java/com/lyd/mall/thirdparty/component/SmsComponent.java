package com.lyd.mall.thirdparty.component;

import com.lyd.mall.thirdparty.utils.HttpUtils;
import lombok.Data;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author Liuyunda
 * @Date 2021/5/17 21:14
 * @Email man021436@163.com
 * @Description: TODO
 */
@ConfigurationProperties(prefix = "spring.application.alicloud.sms")
@Data
@Component
public class SmsComponent {


    private String host;
    private String path;
    private String smsSignId;
    private String templateId;
    private String appCode;

    public void sendCode(String phone,String code){
        // String host = "https://gyytz.market.alicloudapi.com";
        // String path = "/sms/smsSend";
        String method = "POST";
        // String appcode = "16dd6c32315c411ab10d8061b42dfe52";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appCode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", phone);
        querys.put("param", "**code**:"+code+",**minute**:5");
        // querys.put("smsSignId", "2e65b1bb3d054466b82f0c9d125465e2");
        querys.put("smsSignId", smsSignId);
        // querys.put("templateId", "908e94ccf08b4476ba6c876d13f084ad");
        querys.put("templateId", templateId);
        Map<String, String> bodys = new HashMap<String, String>();
        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            // System.out.println(response.toString());
            //获取response的body
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
