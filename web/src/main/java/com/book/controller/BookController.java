package com.book.controller;

import com.book.VO.BookVo;
import com.book.entity.BookInfo;


import com.book.service.Impl.BookServiceImpl;

import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.multipart.MultipartFile;


import java.util.*;


/**
 * @author wangqianlong
 * @create 2019-08-06 16:14
 */
@Controller
@RequestMapping("/book")
public class BookController {
    @Autowired
    BookServiceImpl bookService;


    @RequestMapping("/list")
    public String list(Model model,
                       @RequestParam(required = true, defaultValue = "1") int pageNum,
                       @RequestParam(required = true, defaultValue = "5") int pageSize) {

        PageInfo<BookInfo> bookPageInfo = bookService.findAllBookInfo(pageNum, pageSize);
        model.addAttribute("pageInfo", bookPageInfo);
        return "book/list";

    }


    @RequestMapping("/bookSearch")
    public String bookSearch(Model model,
                                       @RequestParam(value = "queryParam", required = false)
                                               String queryParam) {
        List<BookVo> list = bookService.search(queryParam);
        model.addAttribute("pageInfo", list);
        return "book/SearchList";
    }


    @PostMapping("/addBook")
    public String addBook(Model model, BookVo bookVo, @RequestParam("file") MultipartFile file) {


        BookInfo bookInfo = new BookInfo();
        bookInfo.setBookName(bookVo.getBookName());
        bookInfo.setBookDescription(bookVo.getBookDescription());
        bookInfo.setBookPrice(bookVo.getBookPrice());
        bookInfo.setBookStock(bookVo.getBookStock());

        boolean flag = bookService.saveBookInfo(bookInfo, bookVo.getCategoryName(), file);
        if (flag) {
            PageInfo<BookInfo> list = bookService.findAllBookInfo(1, 5);
            model.addAttribute("pageInfo", list);
            return "book/list";
        }
        return "redirect:/";
    }


}
