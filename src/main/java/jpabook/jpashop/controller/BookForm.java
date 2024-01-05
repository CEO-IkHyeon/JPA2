package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookForm {
    private Long id;    // 상품 수정할 때 id 값 필요해서 넣음
    private String name;
    private int price;
    private int stockQuantity;

    private String author;
    private String isbn;
}
