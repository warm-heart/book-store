package com.book.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * @author wangqianlong
 * @create 2019-07-30 9:54
 */

public class DateUtils {
    static ThreadLocal<Calendar> calendarThreadLocal = new ThreadLocal<Calendar>() {
        @Override
        protected Calendar initialValue() {
            return Calendar.getInstance();
        }
    };

    /**
     * @param date 一月之后时间
     * @return
     */
    public static Date DataAddMonth(Date date) {
        Calendar calendar = calendarThreadLocal.get();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 1);
        Date date1 = calendar.getTime();
        return date1;
    }


    /**
     * @param date 一周以后时间
     * @return
     */
    public static Date DataAddOneWeek(Date date) {
        Calendar calendar = calendarThreadLocal.get();
        calendar.setTime(date);
        //一周之前
        // calendar.add(Calendar.WEEK_OF_MONTH, -1);
        calendar.add(Calendar.WEEK_OF_MONTH, 1);
        Date date1 = calendar.getTime();
        return date1;
    }


}
