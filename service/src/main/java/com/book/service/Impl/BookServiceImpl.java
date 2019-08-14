package com.book.service.Impl;


import com.book.MQ.EsMqService;
import com.book.VO.BookVo;
import com.book.dao.BookCategoryDao;
import com.book.dao.BookDao;
import com.book.entity.BookCategory;
import com.book.entity.BookInfo;
import com.book.enums.BookEnum;
import com.book.exception.BookException;
import com.book.search.BookIndexTemplate;
import com.book.search.BookSearchService;
import com.book.service.BookService;
import com.book.utils.JsonUtil;
import com.book.utils.KeyUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wangqianlong
 * @create 2019-08-06 15:41
 */

@Service
@Slf4j
public class BookServiceImpl implements BookService {


    @Autowired
    private BookDao bookDao;

    @Autowired
    private BookCategoryDao bookCategoryDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private BookSearchService bookSearchService;

    @Autowired
    private EsMqService esMqService;

    @Override
    public PageInfo<BookInfo> findAllBookInfo(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<BookInfo> bookInfoList = bookDao.findAllBookInfo();
        if (CollectionUtils.isEmpty(bookInfoList)) {
            throw new BookException(BookEnum.BOOK_NOT_EXIST);
        }
        PageInfo<BookInfo> bookInfoPageInfo = new PageInfo<>(bookInfoList);

        return bookInfoPageInfo;
    }

    @Override
    public BookInfo findByBookInfoId(String bookInfoId) {
        BookInfo bookInfo = bookDao.findByBookInfoId(bookInfoId);
        if (bookInfo == null) {
            log.info("图书信息未找到,bookInfoId={}", bookInfoId);
            throw new BookException(BookEnum.BOOK_NOT_EXIST);
        }
        return bookInfo;
    }

    @Override
    public BookInfo findByBookInfoName(String bookInfoName) {
        BookInfo bookInfo = bookDao.findByBookInfoName(bookInfoName);
        if (bookInfo == null) {
            log.info("图书信息未找到,bookName={}", bookInfoName);
            throw new BookException(BookEnum.BOOK_NOT_EXIST);
        }
        return bookInfo;
    }

    @Override
    @Transactional
    public boolean saveBookInfo(BookInfo bookInfo, String bookCategoryName, MultipartFile file) {


        //获取上传文件名
        String fileName = file.getOriginalFilename();

        /*防止不同浏览器上传出现FileIOException 目标卷不正确*/
        fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);

        //1.判断文件是否为空(是否上传文件 / 文件内容是否为空)
        if (file.isEmpty()) {
            throw new BookException("上传文件不可以为空");
        }
        //2.判断文件后缀名是否符合要求
        String fileNameSuffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        String Suffix = "png/jpg";

        if (Suffix.indexOf(fileNameSuffix) < 0) {
            throw new BookException("文件类型不正确");
        }

        //3.判断文件大小是否符合要求
        //获取上传文件大小,返回字节长度1M=1024k=1048576字节 - 文件过大进入controller之前抛出异常 - 前端判断文件大小
        int size = (int) file.getSize();

        if (size > 1024 * 1024 * 2) {
            throw new BookException("上传文件过大，请上传小于2MB大小的文件");

        }
        //4.将文件重命名，避免文件名相同覆盖文件
        String fileNamePrefix = fileName.substring(0, fileName.lastIndexOf("."));
        fileName = fileNamePrefix + "-" + System.currentTimeMillis() + "." + fileName;//获取上传文件名
        //5.判断文件夹是否存在
        String path = "E:/bookImage";

        File targetFile = new File(path + "/" + fileName);
        if (!targetFile.getParentFile().exists()) {
            //不存在创建文件夹
            targetFile.getParentFile().mkdirs();
        }


        //保存至数据库
        //设置图片路径与Id
        bookInfo.setBookIcon("/Image/" + fileName);
        bookInfo.setBookId(KeyUtils.genUniqueKey());
        BookCategory bookCategory = bookCategoryDao.findByCategoryName(bookCategoryName);
        if (bookCategory == null) {
            throw new BookException(BookEnum.BOOK_CATEGORY_NOT_EXIST);
        }
        bookInfo.setCategoryType(bookCategory.getCategoryType());
        Integer integer = bookDao.saveBookInfo(bookInfo);
        if (integer != 1) {
            throw new BookException(BookEnum.BOOK_UPDATE_FAIL);
        }

        try {
            //6.将上传文件写到服务器上指定的文件
            file.transferTo(targetFile);
        } catch (IOException e) {
            log.error("上传文件错误");
            throw new BookException("上传文件错误");
        }

        //利用MQ异步插入ES
        //组装索引对象
        BookIndexTemplate bookIndexTemplate = new BookIndexTemplate();
        bookIndexTemplate.setBookId(bookInfo.getBookId());
        bookIndexTemplate.setBookName(bookInfo.getBookName());
        bookIndexTemplate.setBookDescription(bookInfo.getBookDescription());
        bookIndexTemplate.setBookStock(bookInfo.getBookStock());
        bookIndexTemplate.setBookPrice(bookInfo.getBookPrice().doubleValue());
        bookIndexTemplate.setCreateTime(new Date());
        bookIndexTemplate.setCategoryName(bookCategory.getCategoryName());


        String json = JsonUtil.toJson(bookIndexTemplate);
        try {
            esMqService.send(json);
            return true;
        } catch (Exception e) {
            log.error("增加图书上传Mq错误");
            throw new BookException("Mq错误");
        }
    }

    @Override
    @Transactional
    public boolean deleteBookInfo(String bookInfoId) {
        bookSearchService.remove(bookInfoId);
        Integer integer = bookDao.deleteBookInfo(bookInfoId);
        if (integer == 1) {
            return true;
        }
        throw new BookException(BookEnum.BOOK_DELETE_FAIL);
    }

    @Override
    @Transactional
    public boolean updateBookInfo(BookInfo bookInfo) {

        //组装索引对象
        BookIndexTemplate bookIndexTemplate = new BookIndexTemplate();
        bookIndexTemplate.setBookId(bookInfo.getBookId());
        bookIndexTemplate.setBookName(bookInfo.getBookName());
        bookIndexTemplate.setBookDescription(bookInfo.getBookDescription());
        bookIndexTemplate.setBookStock(bookInfo.getBookStock());
        bookIndexTemplate.setBookPrice(bookInfo.getBookPrice().doubleValue());

        Integer integer = bookDao.updateBookInfo(bookInfo);
        if (integer == 1) {
            boolean flag = bookSearchService.update(bookIndexTemplate);
            if (flag == false) {
                bookSearchService.remove(bookIndexTemplate.getBookId());
                bookSearchService.add(bookIndexTemplate);
            }
            return true;
        }
        throw new BookException(BookEnum.BOOK_UPDATE_FAIL);
    }

    @Override
    public PageInfo<BookVo> findByCategoryName(String categoryName, Integer pageNum, Integer pageSize) {
        BookCategory bookCategory = bookCategoryDao.findByCategoryName(categoryName);
        if (bookCategory == null) {
            throw new BookException(BookEnum.BOOK_CATEGORY_NOT_EXIST);
        }
        PageHelper.startPage(pageNum, pageSize);
        List<BookInfo> bookInfoList = bookDao.findByCategoryType(bookCategory.getCategoryType());
        if (CollectionUtils.isEmpty(bookInfoList)) {
            throw new BookException(BookEnum.BOOK_NOT_EXIST);
        }
        List<BookVo> bookVoList = bookInfoList.stream()
                .map(e -> new BookVo(e.getBookId(), e.getBookName(),
                        e.getBookDescription(), e.getBookStock(), e.getBookIcon(),
                        e.getBookPrice(), categoryName)).collect(Collectors.toList());
        PageInfo<BookVo> bookInfoPageInfo = new PageInfo<>(bookVoList);
        return bookInfoPageInfo;
    }

    @Override
    public PageInfo<BookVo> findByCategoryType(Integer categoryType, Integer pageNum, Integer pageSize) {
        BookCategory bookCategory = bookCategoryDao.findByCategoryType(categoryType);
        if (bookCategory == null) {
            throw new BookException(BookEnum.BOOK_CATEGORY_NOT_EXIST);
        }
        PageHelper.startPage(pageNum, pageSize);
        List<BookInfo> bookInfoList = bookDao.findByCategoryType(bookCategory.getCategoryType());
        if (CollectionUtils.isEmpty(bookInfoList)) {
            throw new BookException(BookEnum.BOOK_NOT_EXIST);
        }
        List<BookVo> bookVoList = bookInfoList.stream()
                .map(e -> new BookVo(e.getBookId(), e.getBookName(),
                        e.getBookDescription(), e.getBookStock(), e.getBookIcon(),
                        e.getBookPrice(), bookCategory.getCategoryName()))
                .collect(Collectors.toList());


        PageInfo<BookVo> bookInfoPageInfo = new PageInfo<BookVo>(bookVoList);

        return bookInfoPageInfo;
    }


    public List<BookVo> search(String queryParam) {
        List<String> stringList = bookSearchService.search(queryParam);
        if (CollectionUtils.isEmpty(stringList)) {
            throw new BookException(BookEnum.BOOK_SEARCH_NOT_EXIST);
        }
        List<BookInfo> bookInfoList = bookDao.selectByIds(stringList);
        if (CollectionUtils.isEmpty(bookInfoList)) {
            throw new BookException(BookEnum.BOOK_SEARCH_NOT_EXIST);
        }


        List<BookVo> bookVoList = bookInfoList.stream()
                .map(e -> new BookVo(e.getBookId(), e.getBookName(),
                        e.getBookDescription(), e.getBookStock(), e.getBookIcon(),
                        e.getBookPrice(), queryParam))
                .collect(Collectors.toList());

        return bookVoList;
    }
}
