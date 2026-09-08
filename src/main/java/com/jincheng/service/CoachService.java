package com.jincheng.service;

import com.jincheng.entity.Coach;

import java.util.List;

public interface CoachService {
    Coach getByCoachNo(String coachNo);
    List<Coach> getAllCoaches();
    boolean addCoach(Coach coach);
    boolean updateCoach(Coach coach);
    boolean deleteCoach(Long id);
    Coach getCoachById(Long id);
}
