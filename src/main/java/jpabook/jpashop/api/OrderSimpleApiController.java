package jpabook.jpashop.api;


import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * XToOne(ManyToOne, OneToOne) 관계에서 성능 최적화 하는 방법!!! ( != 컬렉션인 XToMany 가 아닌 것들 조회 )
 * Order 조회
 * Order -> Member 연관 걸림
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());   // 검색 조건이 없으니께 다 들고 온다
//
        // 강제 지연로딩 초기화하기 ( hibernate5Module.configure(Hibernate5JakartaModule.Feature.FORCE_LAZY_LOADING, true); 이거 주석처리)
        for (Order order : all) {
            // order.getMember()까지는 프록시 객체지만
            // .getName() 하는 순간 강제 초기화된다 -> member에 쿼리를 날려서 JPA가 데이터 다 끌고옴
            order.getMember().getName();    // LAZY 강제 초기화
            order.getDelivery().getAddress();   // LAZY 강제 초기화
            // 결과
            // 이러면 초기화 안된 OrderItems는 null로 나오고
            // 초기화한 Member와 Delivery는 제대로 값이 나온다
        }

        return all;
    }
    /** 문제 1 : 순환 참조 발생
     * Order -> Member로 간다
     * Member에는 orders가 있음 Member -> order로 간다
     * order에는 또 member가 있음....
     * 무한 루프...
     *
     * 객체를 json으로 만드는 json 라이브러리 입장에서는 
     * order -> member -> order -> member 계속 객체에서 json 뽑아냄
     *
     * 해결 방안 : 둘 중 한쪽은 @JsonIgnore 해야함 (양방향이 걸리는 곳은 전부 다 한쪽에는 걸어줘야 함)
     */

    /** 문제 2
     * Order의 member의 fetch가 LAZY(지연로딩)이라서 진짜 new 해서 멤버 객체 가져오는게 아님
     *      * 지연로딩이라는게 DB에서 안끌고 옴. DB에서 가져올 때는 Order의 데이터만 가져옴 -> member는 아예 손을 안댐
     *      * 그렇다고 Order의 member에 null을 넣어놓을 순 없으니까
     *        hibernate에서 Member를 상속받은 ProxyMember() 객체를 생성해서 넣어 놓는다) -> 이때 쓰는게 byteBuddy 라이브러리 사용
     *      * member 객체 값에 접근할 때 DB에 member 객체 SQL 날려서 값을 채워준다 = proxy를 초기화 한다
     * 해결방법
     *      * Hibernate5JakartaModule 이걸 @Bean으로 등록
     *      * gradle에도 등록 (버전 정보는 어차피 spring boot가 알아서 최적화된 버전 관리해줌)
     */

    /**
     * 이렇게 어찌저찌 해결하면 결국 json 잘 나온다
     * 하지만 이렇게 entity를 다 노출하면 안된다 (이젠 몽말알?)
     *      * 엔티티 수정되면 api 스펙이 바뀐다
     *      * orderItems는 알고 싶지 않은데 가져와서 노출 시키고
     *      * 또 가져올 때 DB에서 막 가져오니까 성능도 하락하고
     * => DTO로 변환해서 받자
     */

    /**
     * 처음에 findAllByString으로 Order 조회하면 SQL 1번 날라감
     * 그 결과 row는 2개 나옴(주문 데이터가 2개니까)
     * 그 다음 stream에서 row 개수인 2번 loop 돈다
     *      * 첫 번째 loop 때, Dto로 변환하면서 getName()과 getAddress()할 때 Member, Delivery 쿼리 날라감
     *      * 두 번째 loop 때, 또 날라감
     * => N+1 : 1(order) 쿼리 날라가면 N(member)개 쿼리 + N(delivery)개 쿼리 날라감
     */
    @GetMapping("/api/v2/simple-orders")
    public Result ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o)) // map은 x -> y로 바꾸는 것(여기선 Order를 SimpleOrderDto로 변환)
                .collect(Collectors.toList());  // 그걸 list로 변환
        return new Result(result);
    }

    @GetMapping("/api/v3/simple-orders")
    public Result ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(order -> new SimpleOrderDto(order))
                .collect(Collectors.toList());

        return new Result(result);
    }

    /**
     * v3처럼 entity로 조회하고 DTO로 변환해서 return 하지 않고
     * 바로 DTO로 조회하는 방법
     */
    @GetMapping("api/v4/simple-orders")
    public Result ordersV4() {
        return new Result(orderRepository.findOrderDtos());
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            // LAZY 초기화 : getMember()에서 memberId로 영속성 컨텍스트를 뒤짐 -> 없으면 DB에 쿼리
            name = order.getMember().getName();
            orderDate = getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();     // LAZY 초기화
        }
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }
}
