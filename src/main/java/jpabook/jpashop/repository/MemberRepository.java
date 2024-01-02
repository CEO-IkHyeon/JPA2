package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository //자동으로 spring bean으로 등록해줌
@RequiredArgsConstructor
public class MemberRepository {

    private final EntityManager em;

    public void save(Member member) {
        em.persist(member);
    }

    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)   // JPQL 활용
                .getResultList();
    }

    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class) // 조회 타입이 Member.class
                .setParameter("name", name)
                .getResultList();
    }
}
