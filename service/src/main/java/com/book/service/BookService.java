package com.book.service;




import com.book.VO.BookVo;
import com.book.entity.BookInfo;
import com.github.pagehelper.PageInfo;
import org.springframework.web.multipart.MultipartFile;


public interface BookService {

    public PageInfo<BookInfo> findAllBookInfo(Integer pageNum, Integer pageSize);

    public BookInfo findByBookInfoId(String bookInfoId);

    public BookInfo findByBookInfoName(String bookInfoName);

    public boolean saveBookInfo(BookInfo bookInfo, String bookCategoryName, MultipartFile file);

    public boolean deleteBookInfo(String bookInfoId);

    public boolean updateBookInfo(BookInfo bookInfo);


    public PageInfo<BookVo> findByCategoryName(String categoryName, Integer pageNum, Integer pageSize);


    public PageInfo<BookVo> findByCategoryType(Integer categoryType, Integer pageNum, Integer pageSize);


}
