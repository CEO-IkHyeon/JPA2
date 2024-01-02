package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MemberServiceTest {

    @Autowired private MemberRepository memberRepository;
    @Autowired private MemberService memberService;

    // 회원가입을 성공해야 한다
    @Test
    @Rollback(value = false)
    public void 회원가입() throws Exception{
        // given
        Member member = new Member();
        member.setName("memberA");

        // when
        Long saveId = memberService.join(member);

        // then
        assertEquals(member, memberRepository.findOne(saveId));
    }


    // 회원가입할 때, 같은 이름이 있으면 예외가 발생해야 한다.
    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외() throws Exception{
        // given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");

        // when
        memberService.join(member1);
        memberService.join(member2);

        // then
        fail("예외가 발생해야 합니다.");  // 여기까지 내려오면 안된다(fail 떨굼). 54 line에서 예외 터져야 함

    }
}
