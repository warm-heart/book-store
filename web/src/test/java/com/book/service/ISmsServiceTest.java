package com.book.service;

import com.book.VO.ServiceResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author wangqianlong
 * @create 2019-08-14 9:17
 */
@Slf4j
public class ISmsServiceTest extends StartApplicationTests {

    @Autowired
    private ISmsService iSmsService;

    @Test
    public void sendSms() {
        ServiceResult result = iSmsService.sendSms("18855092143");
        System.out.println(result);


        System.out.println(iSmsService.getSmsCode("18855092143"));
    }
}
