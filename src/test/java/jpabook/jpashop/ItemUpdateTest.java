package jpabook.jpashop;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.item.Book;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class ItemUpdateTest {

    @Autowired
    EntityManager em;
    @Test
    public void updateTest() throws Exception {
        //given
        Book book = em.find(Book.class, 1L);

        //TX
        book.setName("asdfsdf"); // -> JPA가 변경된 부분 찾아서 업데이트 쿼리 자동 생성 후 DB에 반영 = 변경 감지(dirty checking)
        // entity의 값을 수정하면 JPA가 알아서 트랜잭션 커밋 시점에 바뀐 애 찾아서 update 쿼리 만들어서 DB에 반영
        // 즉, flush할 때 dirty checking 일어남




    }
}
