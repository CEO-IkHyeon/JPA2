package jpabook.jpashop.controller;

import jakarta.validation.Valid;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/items/new")
    public String createForm(Model model) {
        model.addAttribute("bookForm", new BookForm());

        return "items/createItemForm";
    }



    @PostMapping("/items/new")
    public String create(@Valid BookForm form, BindingResult result) {
        if (result.hasErrors()) {
            return "items/createItemForm";
        }
        
        // Form 객체를 Entity로 변환 후 저장
        Book book = new Book();
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());

        itemService.saveItem(book);

        return "redirect:/items";
    }

    @GetMapping("/items")
    public String list(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);

        return "items/itemList";
    }

    @GetMapping("items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {
        Book item = (Book) itemService.findOne(itemId); // 이 예제에서는 Book만 등록한다는 가정하에 이렇게 캐스팅
        
        // 수정할 때 현재 선택된 책의 값들을 보여줘야함 -> 근데 Book entity를 폼으로 넘겨주지 않고 BookForm entity를 폼으로 넘긴다
        BookForm form = new BookForm();
        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setAuthor(item.getAuthor());
        form.setStockQuantity(item.getStockQuantity());
        form.setIsbn(item.getIsbn());

        model.addAttribute("form", form);

        return "items/updateItemForm";
    }

    @PostMapping("items/{itemId}/edit")
    public String updateItem(@PathVariable Long itemId, @ModelAttribute("form") BookForm form) {
        // form으로 받은 BookForm -> Book 으로 바꾸자
//        Book book = new Book();

        // 이렇게 DB 한번 갔다온 애 = 준영속 상태의 객체 -> JPA가 식별할 수 있는 ID를 가지고 있음
        // 즉, 여기서 book은 준영속 엔티티다!!!
        // 이 book 객체는 이미 DB에 한번 저장되어서 식별자가 존재함
        // 근데 임의로 Book book = new Book()으로 만들었 객체인데??
        // 임의로 만들어도 기존에 식별할 수 있는 식별자를 가지고 있기에(setId) 준영속 엔티티로 볼 수 있음)
        
        // 그래서 준영속인데 어쩌라고?
        // 준영속은 더이상 JPA가 관리 안함
        // JPA가 관리 안하는게 어쩌라고?
        // 더이상 JPA가 변경 감지를 해주지 않아 = 엔티티 수정한다고 자동으로 JPA가 update 쿼리 만들어서 날려주고 그러지 않아
        // 그럼 이런 준영속 엔티티는 어떻게 수정할까?
            // 1. 변경 감지(dirty checking) 기능 사용
            // 2. merge 사용
//        book.setId(form.getId());
//        book.setName(form.getName());
//        book.setIsbn(form.getIsbn());
//        book.setPrice(form.getPrice());
//        book.setStockQuantity(form.getStockQuantity());
//        book.setAuthor(form.getAuthor());
//
//        itemService.saveItem(book);

        itemService.updateItem(itemId, form.getName(), form.getPrice(), form.getStockQuantity());
        // parameter 많으면 DTO로 해결하면 된다

        return "redirect:/items";
    }
}
