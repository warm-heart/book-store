package com.book.service.Impl;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;


import com.book.VO.ServiceResult;
import com.book.service.ISmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;


/**
 * @author wangqianlong
 * @create 2019-08-14 16:14
 */
@Service
@Slf4j
public class SmsServiceImpl implements ISmsService, InitializingBean {
    private String accessKey = "LTAI2IbZ0EkRRfgj";


    private String secertKey = "ml42vxubJtlDtARhyJZWa35ysZFHCg";


    private String templateCode = "SMS_172356198";

    private IAcsClient acsClient;

    private final static String SMS_CODE_CONTENT_PREFIX = "SMS::CODE::CONTENT";

    private static final String[] NUMS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

    private static final Random random = new Random();

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public ServiceResult<String> sendSms(String telephone) {

        //gapKey 标识用户已经请求过了
        String gapKey = "SMS::CODE::INTERVAL::" + telephone;
        String result = redisTemplate.opsForValue().get(gapKey);
        if (result != null) {
            return new ServiceResult<String>(false, "请求次数太频繁");
        }

        String code = generateRandomSmsCode();

        String templateParam = String.format("{\"code\": \"%s\"}", code);

        // 组装请求对象
        SendSmsRequest request = new SendSmsRequest();

        // 使用post提交
        request.setMethod(MethodType.POST);
        request.setPhoneNumbers(telephone);
        request.setTemplateParam(templateParam);
        request.setTemplateCode(templateCode);
        request.setSignName("三味书屋");

        boolean success = false;
        try {
            SendSmsResponse response = acsClient.getAcsResponse(request);

            log.info("aliyunSms返回数据requestId{}", response.getRequestId());
            log.info("aliyunSms返回数据code{}", response.getCode());
            log.info("aliyunSms返回数据message{}", response.getMessage());


            if ("OK".equals(response.getCode())) {
                success = true;
            } else {
                // TODO log this question
                log.error("短信发送失败");
            }
        } catch (ClientException e) {
            log.error(e.getMessage());
        }
        if (success) {

            //设置短信间隔 1min
           /* redisTemplate.opsForValue().set(gapKey,     code, 60, TimeUnit.SECONDS);
           //设置验证码
            redisTemplate.opsForValue().set(SMS_CODE_CONTENT_PREFIX + telephone, code, 10, TimeUnit.MINUTES);*/

            redisTemplate.opsForValue().set(gapKey, code);
            redisTemplate.opsForValue().set(SMS_CODE_CONTENT_PREFIX + telephone, code);

            return ServiceResult.of(code);
        } else {
            return new ServiceResult<String>(false, "服务忙，请稍后重试");
        }
    }

    @Override
    public String getSmsCode(String telephone) {
        return this.redisTemplate.opsForValue().get(SMS_CODE_CONTENT_PREFIX + telephone);
    }

    @Override
    public void remove(String telephone) {
        this.redisTemplate.delete(SMS_CODE_CONTENT_PREFIX + telephone);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 设置超时时间
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");

        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKey, secertKey);

        String product = "Dysmsapi";
        String domain = "dysmsapi.aliyuncs.com";

        DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
        this.acsClient = new DefaultAcsClient(profile);

    }

    /**
     * 6位验证码生成器
     *
     * @return
     */
    private static String generateRandomSmsCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(10);
            sb.append(NUMS[index]);
        }
        return sb.toString();
    }
}