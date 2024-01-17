package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {
    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    /**
     * JPQL로 처리
     */
    public List<Order> findAllByString(OrderSearch orderSearch) {
        //language=JPAQL
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }
        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }

    /**
     * JPA Criteria : JPQL을 자바 코드로 작성할 수 있게 도와주는 것
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" +
                            orderSearch.getMemberName() + "%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건
        return query.getResultList();
    }

    /**
     * V3에서 사용
     * V4보다 범용성 좋다
     * @return
     */
    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).getResultList();  // order 가져올 때 한번에 member와 delivery를 join해서 가져오자 (fetch는 JPA 문법)
    }

    /**
     * repo에서 controller에 의존관계가 생기면 안되기에 Simple..DTO를 따로 repo package에 선언
     * 단점
     *      1. 딱 OrderSimpleQueryDto에 fit하게 만들었기에 이 용도 외에는 재사용 불가능
     *      2. 사실상 API 스펙이 이 쿼리에 들어와 있음 = API스펙에 맞춰서 repository 코드가 짜진 거다
     * 장점 :필요한 필드만 딱딱 select 해오기에 v3보다 조금 성능 최적화 가능
     */
//    public List<OrderSimpleQueryDto> findOrderDtos() {
//        return em.createQuery(
//                "select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
//                        " from Order o" +
//                        " join o.member m" +
//                        " join o.delivery d", OrderSimpleQueryDto.class)
//                .getResultList();
//    }
    // -> OrderSimpleQueryRepository로 옮기자

    /**
     entity나 value object(Embeddable)만 JPA가 기본적으로 반환 
     * DTO 같은건 자동 반환 안되니까 "new" operation 활용해야함
     */


    /**
     * repository는 순수하게 entity 조회하는 용도로만 사용하자
     * -> DTO를 repo에서 조회하는건 api 스펙이 사실상 findOrderDtos() 메소드에 들어와 있는 것과 똑같음
     * 그럼 어떻게 해?
     * 성능 최적화를 위한 쿼리용 repo를 뽑자-> repo 밑에 따로 package 만들자
     * 왜 이렇게 하느냐?
     *      * findAllWithMemberDelivery() -> Order 엔티티를 그냥 fetch join으로만 가져오니까 재사용 가능
     *      * findOrderDtos() -> 딱 API 스펙이 박혀있으니 재사용성 매우 낮음
     */



    public List<Order> findAllMemberDeliveryOrderItem() {
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems i", Order.class
        ).getResultList();
    }

    /**
     * Query DSL로 처리
     */
//    public List<Order> findAll(OrderSearch orderSearch) {
//        QOrder order = QOrder.order;
//    }
}

