package com.book.dao;

import com.book.entity.BookCategory;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author wangqianlong
 * @create 2019-08-06 15:21
 */
@Mapper
@Repository
public interface BookCategoryDao {

    public List<BookCategory> findAllBookCategory();

    public BookCategory findByCategoryName(String categoryName);

    public BookCategory findByCategoryType(Integer categoryType);

    public Integer saveBookCategory(BookCategory bookCategory);

    public Integer deleteBookCategoryByCategoryType(Integer categoryType);

    public Integer deleteBookCategoryByCategoryName(String categoryName);


}
