package jpabook.jpashop.domain;

import jakarta.persistence.*;
import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter @Setter
public class Category {

    @Id @GeneratedValue
    @Column(name = "category_id")
    private Long id;

    private String name;

    @ManyToMany
    @JoinTable(name = "category_item",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id"))  // RDB는 다대다 관계를 위해서 중간 테이블 필수
    private List<Item> items = new ArrayList<>();

    @ManyToOne(fetch = LAZY)  // 부모는 많은 자식(나)을 가질 수 있어서
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent") // 내가 여러 자식들 보유
    private List<Category> child = new ArrayList<>();

    // ==연관관계 편의 메서드== //
    public void addChildCategory(Category child) {
        this.child.add(child);
        child.setParent(this);
    }


}
