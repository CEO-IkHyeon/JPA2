package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.service.ItemService;
import jpabook.jpashop.service.MemberService;
import jpabook.jpashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final MemberService memberService;
    private final OrderService orderService;
    private final ItemService itemService;

    @GetMapping("/order")
    public String createForm(Model model) {
        // 화면에서 selectbox로 멤버와 아이템들을 보여줘서 선택할 수 있도록 하기 위해서 폼 보여줄 때 쫙 가져온다
        List<Member> findMembers = memberService.findMembers();
        List<Item> findItems = itemService.findItems();

        model.addAttribute("members", findMembers);
        model.addAttribute("items", findItems);

        return "order/orderForm";
    }

    @PostMapping("/order")
    public String order(@RequestParam("memberId") Long memberId,
                        @RequestParam("itemId") Long itemId,
                        @RequestParam("count") int count) {
        /**
         * 물론 여기서 memberId와 itemId 받았으니까 이걸로 member와 item 찾아서 넘기는 방식으로 구현할 수도 있지만
         * 이러한 과정들은 transational 안에서 처리하는게 좋다
         * 그래서 order로 id값(식별자) 넘기면 order 메소드 보면 transactional 안에서 멤버 찾고 아이템 찾고 그런다
         * 즉, 바깥에서 entity를 찾아서 넘기는 것보단 안에서 처리하는게 깔끔
         * 밖에서 entity 찾아서 넘기면 얘는 더이상 영속성 컨텍스트 안에 있는게 아니라서 order 내부에서 이 값을 바꾸려고 하면 귀찮아진다
         */
        orderService.order(memberId, itemId, count);
        return "redirect:/orders";

    }

    /**
     * @RequestParam은 form submit 방식으로 <select name="memberId" id="member" class="form-control"> 여기서 선택된 id가 value로 넘어오게 된다
     * 이를 매개변수로 매핑해줌 (name에 선언된 이름으로 넘어옴) 이 이름을 @RequestParam("[name]")에 넣으면 해당 매개변수에 매핑
     */

    @GetMapping("/orders")
    public String orderList(@ModelAttribute("orderSearch") OrderSearch orderSearch, Model model) {
        List<Order> orders = orderService.findOrders(orderSearch);
        model.addAttribute("orders", orders);

        return "order/orderList";
    }

    /**
     * @ModelAttribute("orderSearch") -> 자동으로 model.addAttribute("orderSearch", orderSearch); 이렇게 model에 담긴다
     */

    @PostMapping("/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId) {
        orderService.cancelOrder(orderId);
        return "redirect:/orders";
    }
}
