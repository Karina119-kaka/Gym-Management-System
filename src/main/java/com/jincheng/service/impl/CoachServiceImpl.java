package com.jincheng.service.impl;

import com.jincheng.entity.Coach;
import com.jincheng.mapper.CoachMapper;
import com.jincheng.mapper.CourseMapper;
import com.jincheng.service.CoachService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CoachServiceImpl implements CoachService {

    @Autowired
    private CoachMapper coachMapper;
    @Autowired
    private CourseMapper courseMapper;

    @Override
    public Coach getByCoachNo(String coachNo) {
        return coachMapper.selectByCoachNo(coachNo);
    }

    @Override
    public List<Coach> getAllCoaches() {
        return coachMapper.selectAll();
    }

    @Override
    public boolean addCoach(Coach coach) {
        return coachMapper.insert(coach) > 0;
    }

    @Override
    public boolean updateCoach(Coach coach) {
        return coachMapper.updateById(coach) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCoach(Long id) {
        Integer coachId = id.intValue();
        courseMapper.deleteByCoachId(coachId);
        return coachMapper.deleteById(coachId) > 0;
    }

    @Override
    public Coach getCoachById(Long id) {
        return coachMapper.selectById(id.intValue());
    }
}
