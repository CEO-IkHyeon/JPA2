package jpabook.jpashop.domain.item;

import jakarta.persistence.*;
import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)   // 상속 관계 매핑 어떻게 할지 부모 클래스에 정의!
@DiscriminatorColumn(name = "dtype")
public abstract class Item {

    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    
    
    // == 비지니스 로직 == //
    // 엔티티 자체에서 해결할 수 있는 것들을 엔티티 안에 비지니스 로직 넣는게 좋다(객체 지향적)
    // 재고 수정의 경우 : stockQuantity field 를 가지고 있는 Item class 안에서 비지니스 로직 처리하는게 응집도가 높을 것이다.
    // setter를 가지고 밖에서 계산 후 값을 넣는게 아니라 핵심 비지니스 메소드로 이 안에서 변경해야 함 -> 객체 지향적
    /**
     * stock 증가
     * @param quantity
     */
    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    /**
     * stock 감소
     * @param quantity
     */
    public void removeStock(int quantity) {
        int restStock = (this.stockQuantity - quantity);
        if (restStock < 0) {
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = restStock;
    }

}
