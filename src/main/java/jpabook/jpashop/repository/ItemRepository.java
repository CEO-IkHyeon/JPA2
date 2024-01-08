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
            // item의 id(식별자)로 DB든 영속성 컨텍스트를 뒤지든 해서 그 item 찾고
            // parameter로 넘긴 item 값들로 모든 data를 싹 바꿔치기 함
            // 그럼 찾은 객체는 영속성 컨텍스트 안에 있는 객체이기에 값 바꿔치기되면
            // flush될 때 JPA가 dirty checking을 통해서 update query를 자동으로 DB에 날려준다
            // 1번. 변경감지로 준영속 처리하는 거랑 뭐가 달라?
                // Item mergeItem = em.merge(item)에서 item은 영속성 컨텍스트로 관리되지 않고
                // merge메소드의 리턴값이 mergeItem이 영속성 컨텍스트에 의해 관리된다
                // 병합으로 하면 모든 속성들을 바꿔치기함. 만약에 item의 속성 중 비어 있는게 있으면 그대로 null로 넣어버린다
                // 즉, 원하는 속성만 골라서 업데이트를 하지 못하고 그냥 싹 갈아끼움

            // 결론. 내가 직접 조회해서 객체 찾고 업데이트 칠 필드들만 직접 업데이트 하자
                    // merge() 쓰지말고 변경 감지로 업데이트 하자
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
