package com.learning_planner.dto.curriculum.response

import com.learning_planner.service.WeeklyStudyPlan

data class DailyHoursStudyPlanResponse(
    val totalWeeks: Int,                    // 총 소요 주
    val totalDays: Int,                     // 총 소요 일
    val weeklyPlans: List<WeeklyStudyPlan>  // 주차별 학습 계획
)