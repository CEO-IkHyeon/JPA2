package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    @Transactional
    public void updateItem(Long itemId, String name, int price, int stockQuantity) {
        // 1. 변경 감지로 준영속 엔티티를 수정하는 방법
        // 자.. itemRepo에서 찾아온 findItem은 영속상태임
        // 그럼 아래와 같이 setter로 값 변경하고 나서 itemRepo.save(), persist() 이런거 할 필요가 없다
        // 영속 객체니깐 JPA가 이 함수 끝날 때 @Transactional 에 의해서 트랜잭션 커밋된다 
        // 커밋되면 JPA는 flush 날림
        // flush 날린다는 것은 영속성 컨텍스트 안에 있는 애들 중 변경된 건 뭔지 다 찾아서 update query를 DB에 날려줌
        Item findItem = itemRepository.findOne(itemId);
        findItem.setName(name);
        findItem.setPrice(price);
        findItem.setStockQuantity(stockQuantity);

        // 위의 코드는 ItemRepository에서 saveItem의 em.merge()와 똑같은 동작을 한다
    }
    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long id) {
        return itemRepository.findOne(id);
    }


}
