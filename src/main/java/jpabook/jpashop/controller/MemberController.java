package jpabook.jpashop.controller;

import jakarta.validation.Valid;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    // Model : view로 넘어갈 때 view에 보여줄 데이터를 담는 곳
    @GetMapping("/members/new") //GetMapping은 폼을 보여주는 곳
    public String createForm(Model model) {
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    /**
     * @Valid가 있으면 MemberForm에서 설정해놓은 validation에 따라 검증 해준다
     * MemberForm에서 Valid 실패 시 원래는 오류 발생하면서 팅기지만, BindingResult가 있으면 이 오류를 result에 담아두고 바디부분 코드 실행함
     *
     * 왜 Member 안쓰고 MemberForm 만들어서 쓰냐? Member 엔티티와 form에서 필요한 필드가 다를 수 있고, 요구되는 validation이 다를 수 있다
     * 즉, 엔티티의 내용을 모두 파라미터로 받아서 처리하는 경우가 거의 없다. 폼에 따라 전달받는 파라미터가 다르고 검증 범위도 다름
     */
    @PostMapping("/members/new")    //PostMapping은 폼에서 입력한 데이터를 받아오는 것
    public String create(@Valid MemberForm form, BindingResult result) {

        if (result.hasErrors()) {
            return "members/createMemberForm";  // -> 이 화면에서 어떤 에러가 발생했는지 뿌릴 수 있다
        }

        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());

        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        memberService.join(member);

        return "redirect:/";
    }

    /**
     * memberService에서 findMembers로 JPQL을 통해 멤버 리스트를 뽑아와서
     * Model에 담아서
     * 화면으로 넘긴다
     */
    @GetMapping("/members")
    public String list(Model model) {
        List<Member> members = memberService.findMembers();
        // -> 이렇게 엔티티 직접 뿌리는 것보단 DTO에 담아서 화면에 꼭 필요한 데이터들만 담아서 넘기는게 좋다. 템플릿 엔진에서 서버 사이드에서 돌 때는 괜춘...
        // API 만들 때는 이유를 불문하고 절대 엔티티를 웹으로 반환하면 안된다
        // 왜? 엔티티에 private String password 필드 추가되었다고 가정하면 2가지 문제가 발생함
            // 1. 패스워드가 그대로 노출
            // 2. API 스펙이 변해버림 : 엔티티에 로직을 추가했더니 API 스펙이 변함
        model.addAttribute("members", members); // key : "members", value : members(list)

        return "members/memberList";
    }

    /**
     * 폼 객체를 만들어서 써야하는가 VS 엔티티를 직접 사용해야 하는가
     * 요구사항이 단순할 때는 엔티티 직접 써도 되긴 함...
     * 실무에서는 엔티티와 요구사항이 일치할 수 없음
     * 엔티티를 폼으로 써버리면 엔티티에 화면 처리 관련 기능들이 추가된다 -> 화면 종속적인 기능이 생기면 엔티티가 더러워짐 -> 유지보수 어렵
     * JPA는 엔티티를 최대한 순수하게 유지하자
     * 최대한 dependency 없이.. 핵심 비지니스 로직만
     * 결론 : 폼 객체나 DTO 활용하자
     */
}
