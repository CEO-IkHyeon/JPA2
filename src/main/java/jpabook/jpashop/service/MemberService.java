package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;


    /**
     * 회원 가입
     * */
    @Transactional
    public Long join(Member member) {
        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();
    }

    /**
     * 중복 회원 검증(이름으로) 로직
     * */
    
    private void validateDuplicateMember(Member member) {
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    /**
     * 회원 전체 조회
     * */
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    /**
     * 회원 단건 조회
     */
    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }

    @Transactional
    public void update(Long id, String name) {
        // @Transactional -> 트랜잭션이 있는 상태에서 조회하면 영속성 컨텍스트에 있는 애들 가져온다
                // jpa가 id에 해당하는 member 찾아옴 -> 영속성 컨텍스트 올리고 그걸 반환 -> member는 영속상태
        // 그 상태에서 영속 상태인 member를 수정하고 @transactional에 의해서 끝나는 시점에 트랜잭션 커밋된다
        // 그 때 JPA가 flush(dirty checking) -> 알아서 update query를 DB에 날려줌
        Member member = memberRepository.findOne(id);
        member.setName(name);
    }
}
