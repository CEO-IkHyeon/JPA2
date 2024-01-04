package jpabook.jpashop.domain;

import jakarta.persistence.*;
import jpabook.jpashop.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.aspectj.weaver.ast.Or;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // = protected OrderItem() {}
public class OrderItem {

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    private int orderPrice; // 주문 가격
        
    private int count; // 주문 수량

    // 누군가 createOrderItem()을 사용하지 않고 따로 객체 생성 후 setter로 만드려는 것을 막기 위해
//    protected OrderItem() {
//    }

    //==생성 메서드==//
    // Order는 세팅안함. createOrder() 생성 메서드에서 addOrderItem() 호출시 order에 값 넣어준다
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        item.removeStock(count);

        return orderItem;
    }

    //==비지니스 로직==//
    /**
     * 재고 수량 원상복구
     */
    public void cancel() {
        // 취소할 때는 주문했던 수량만큼 item의 재고 늘려줘야 함
        getItem().addStock(count);
    }

    //==조회 로직==//
    public int getTotalPrice() {
        return getOrderPrice() * getCount();
    }
}
