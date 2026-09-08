package com.jincheng.service.impl;

import com.jincheng.entity.Member;
import com.jincheng.mapper.MemberMapper;
import com.jincheng.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    private MemberMapper memberMapper;

    @Override
    public Member getByMemberNo(String memberNo) {
        return memberMapper.selectByMemberNo(memberNo);
    }

    @Override
    public List<Member> getAllMembers() {
        return memberMapper.selectAll();
    }

    @Override
    public boolean addMember(Member member) {
        return memberMapper.insert(member) > 0;
    }

    @Override
    public boolean updateMember(Member member) {
        return memberMapper.updateById(member) > 0;
    }

    @Override
    public boolean deleteMember(Long id) {
        return memberMapper.deleteById(id.intValue()) > 0;
    }

    @Override
    public Member getMemberById(Long id) {
        return memberMapper.selectById(id.intValue());
    }
}
