package com.book.dao;

import com.book.entity.BookInfo;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author wangqianlong
 * @create 2019-08-05 15:23
 */
@Mapper
@Repository
public interface BookDao {
        /**
         * 根据id批量查询
         * @param bookIds
         * @return
         */
        public List<BookInfo> selectByIds(@Param("bookIds") List<String> bookIds);


    public List<BookInfo> findAllBookInfo();

    public BookInfo findByBookInfoId(String bookInfoId);

    public BookInfo findByBookInfoName(String bookInfoName);

    public Integer saveBookInfo(BookInfo bookInfo);

    public Integer deleteBookInfo(String bookInfoId);

    public Integer updateBookInfo(BookInfo bookInfo);

    public List<BookInfo> findByCategoryType(Integer categoryType);
}

