package com.book.service;

import com.book.VO.ServiceResult;
import com.book.entity.User;

/**
 * @author wangqianlong
 * @create 2019-08-16 17:05
 */

public interface LoginService {

    public ServiceResult<String> login(User user, String phone, String phoneCode);
}
