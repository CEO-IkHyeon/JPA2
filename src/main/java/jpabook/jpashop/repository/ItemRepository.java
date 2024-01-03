package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {
    private final EntityManager em;

    public void save(Item item) {
        // 
        if (item.getId() == null) { // item은 JPA 저장하기 전까지는 id 값이 없다 = 완전히 새로 생성한 객체다
            em.persist(item);
        } else {                    // null이 아니면 이미 DB에 한번 등록된 것을 가져온거네? -> update로 진행
            em.merge(item); // update와 유사
        }
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }

}
