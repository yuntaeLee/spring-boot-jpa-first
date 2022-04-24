package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // Test 클래스에 존재할 시 Test가 끝난 후 DB를 Rollback
class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired EntityManager em;

    @Test
    @DisplayName("회원가입")
    public void join() throws Exception {
        // given
        Member member = new Member();
        member.setName("Lee");

        // when
        Long joinId = memberService.join(member);

        // then
        em.flush(); // insert query 확인
        assertEquals(member, memberRepository.findOne(joinId));
    }
    
    @Test
    @DisplayName("중복_회원_예외")
    public void duplicateMemberExceptionO() throws Exception {
        // given
        String name = "Lee";

        Member memberA = new Member();
        memberA.setName(name);

        Member memberB = new Member();
        memberB.setName(name);
        
        // when
        memberService.join(memberA);

        // then
//        IllegalStateException thrown = assertThrows(IllegalStateException.class,
//                () -> memberService.join(memberB));
//        assertEquals("이미 존재하는 회원입니다.", thrown.getMessage());

        Assertions.assertThatThrownBy(() -> memberService.join(memberB))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 존재하는 회원입니다.");
    }

}