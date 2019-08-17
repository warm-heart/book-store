package com.book.exception;


import com.book.VO.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;




/**
 * @author wangqianlong
 * @create 2018-09-14 20:00
 */
@ControllerAdvice
public class GlobalExceptionHandle {

    @ExceptionHandler(BookException.class)
    @ResponseBody
    //让http响应不再是200
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse BookException(BookException e) {
        return ApiResponse.error(e.getCode(), e.getMessage());
    }




    @ExceptionHandler(UserException.class)
    @ResponseBody
    //让http响应不再是200
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse UserException(UserException e) {
        return ApiResponse.error(e.getCode(), e.getMessage());
    }


}
