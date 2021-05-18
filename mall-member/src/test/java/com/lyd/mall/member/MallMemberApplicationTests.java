package com.lyd.mall.member;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;

// @SpringBootTest
class MallMemberApplicationTests {

    @Test
    void contextLoads() {
        String s = DigestUtils.md5Hex("123456");
        Md5Crypt.md5Crypt("123456".getBytes(StandardCharsets.UTF_8));

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        bCryptPasswordEncoder.encode("123456");
        bCryptPasswordEncoder.matches("123456","");
        System.out.println(s);
    }

}
