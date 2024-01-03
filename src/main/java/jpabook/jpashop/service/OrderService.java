package jpabook.jpashop.service;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    /**
     * 주문
     * @param memberId
     * @param itemId
     * @param count
     * @return
     */
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {
        //엔티티 조회
        Member findMember = memberRepository.findOne(memberId);
        Item findItem = itemRepository.findOne(itemId);

        //배송정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(findMember.getAddress());

        //주문 상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(findItem, findItem.getPrice(), count);


        //주문 생성
        Order order = Order.createOrder(findMember, delivery, orderItem);

        //주문 저장 : 원래라면 delivery도 deliveryRepo.save(), orderItem도 orderItemRepo.save() 모두 진행해야 하지만
        // Cascade.ALL 옵션으로 인해 order만 persist()하면 연관된 나머지 것들도 자동으로 persist()된다
        orderRepository.save(order); // -> 이렇게 하나만 persist() 해줘도 delivery, orderItem 모두 자동으로 persist()된다

        // 그럼 cascade 언제 씀? -> Order가 delivery, orderItems 관리하니깐 사용
        // 즉, Delivery와 OrderItem 모두 Order에서만 참조한다 -> Order만 private한 Owner
        // 참조를 여러 곳에서 하면 cascade 막 쓰면 안된다
        return order.getId();

    }

    /**
     * 주문 취소
     * @param orderId
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        //주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);
        //주문 취소
        order.cancle(); // 엔티티의 비지니스 로직들이 실행함 -> 엔티티 수정으로 JPA가 알아서 각 수정된 table에 update query 날려준다
    }

    // 검색
//    public List<Order> findOrders() {}
}
