package com.book.search;


import java.util.List;

/**
 * @author wangqianlong
 * @create 2019-08-08 8:57
 */

public interface BookSearchService {


     boolean add(BookIndexTemplate bookIndexTemplate);

     boolean remove(String bookId);

     boolean update(BookIndexTemplate bookIndexTemplate);

     List<String> search(String queryParam);
}