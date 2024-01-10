package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// data 자체를 xml이나 json으로 바로 보내자
@RestController // = @Controller @ResponseBody
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberService memberService;

    /**
     * 이렇게 개발하면 나는 회원 정보만 원하는데 orders 정보도 같이 넘어온다
     * 엔티티의 정보들이 다 외부로 노출된다
        * -> Member entity에서 orders 필드에 @JsonIgnore 붙이면 이 정보는 빠진다
     * 근데 주문 정보 api를 만들 때는 orders가 필요한데 이럴 땐 어떻게 할거야? 
     * 결국 entity에 presentation 계층 로직이 추가되기 시작
     */
    @GetMapping("/api/v1/members")
    public List<Member> memberV1() {
        return memberService.findMembers();
    }

    @GetMapping("/api/v2/members")
    public Result memberV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDTO> collect = findMembers.stream()
                .map(m -> new MemberDTO(m.getName()))
                .toList();
        
        return new Result(collect.size(), collect);
        // result의 data 필드의 값은 collect 리스트가 나갈 것
        // -> 왜 이렇게 ? list 로 바로 넘기면 json 배열 타입으로 나가서 유연성이 확떨어짐 (json 배열 타입은 데이터를 더 이상 추가할 수가 없음)
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int count;  // 이렇게 추가하는 것도 가능!
        private T data;
    }

    /**
     * api 스펙과 DTO가 1대1로 매핑 -> DTO만 보고 api 스펙 파악 가능
     */
    @Data
    @AllArgsConstructor
    static class MemberDTO {    // member의 이름 정보만 반환한다는 가정
        private String name;
    }



    /**
     * @RequestBody : json으로 온 body를 이 member에 쫙 넣어준다
     */
    /**
     * 화면~controller -> presentation 계층
     * 근데 v1은 presentation 계층을 위한 검증 로직이 entity에 다 포함되어 있음
     * 근데 이게 왜 문제?
     *  -> 어떤 api에서는 name 필드에 @NotEmpty가 필요하지만 다른 API에서는 필요없을 수도 있음
     *  -> 화면 validation을 위한 로직인 엔티티에 들어간 것도 별로 좋지 않음
     *  -> 제일 큰 문제 : 엔티티 필드명을 name -> userName으로 변경해버리면 이 api를 호출하는 사람 입장에서는 json으로 "name" : " ... "
     *                  이렇게 호출해서 쓰던게 갑자기 깨져버림
     *                  즉, 엔티티의 수정이 api의 스펙에 영향을 주는게 문제다
     *  => api 스펙을 위한 별도의 DTO가 필요하다
     *      즉, api 요청 스펙에 맞춰서 별도의 DTO를 파라미터로 받는게 좋다
     */

    @PostMapping("/api/v1/members") // 회원등록 api
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);

        return new CreateMemberResponse(id);
    }

    /**
     * v1보다 좋은점
        * 1. member entity에서 name -> username으로 바꿔도 api 스펙은 바뀌지 않는다
        * 2. v1은 api 스펙을 까보지 않는 이상 member 객체로 어떤 파라미터들이 넘어오는지 모른다(name만 넘어오는지, name과 address까지 넘어오는지 몰라)
        * DTO로 받으면 "아, api 스팩 자체가 name만 받는구나!" 바로 알 수 있음
        * 3. 상황에 맞게 validation 가능!
     */

    // 항상 api로 외부와 통신할 때 DTO로 받고 DTO로 주자
    @PostMapping("/api/v2/members")         // @RequestBody를 통해 요청으로 들어온게 알아서 request로 바인딩
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);

        return new CreateMemberResponse(id);
    }

    @PutMapping("api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id,
                                               @RequestBody @Valid UpdateMemberRequest request) {

        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);

        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @Data
    static class CreateMemberRequest {
        @NotEmpty
        private String name;
    }

    // 응답 값으로 이걸로 내려주겠다
    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor //엔티티에는 롬복 @Getter만 쓰는데 DTO는 데이터 왔다갔다 하는 용도라 (비지니스 로직도 없고..) 롬복 막 쓴다
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

}
