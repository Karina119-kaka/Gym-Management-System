package com.jincheng.service;

import com.jincheng.entity.Member;

import java.util.List;

public interface MemberService {
    Member getByMemberNo(String memberNo);
    List<Member> getAllMembers();
    boolean addMember(Member member);
    boolean updateMember(Member member);
    boolean deleteMember(Long id);
    Member getMemberById(Long id);
}
