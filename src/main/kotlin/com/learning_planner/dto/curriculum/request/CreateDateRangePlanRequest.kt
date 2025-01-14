package com.learning_planner.dto.curriculum.request

data class CreateDateRangePlanRequest (
    val courseId: String,
    val startDate: String, // "2025-01-13" 형식
    val endDate: String,
    val preferredPlaybackSpeed: Float,
    val weekendStudyPlan: WeekendStudyPlan,
    val holidayInclusion: HolidayInclusionType,
)

enum class WeekendStudyPlan {
    INCLUDE_WEEKENDS,
    EXCLUDE_WEEKENDS,
    WEEKEND_ONLY_ONE_DAY,
}