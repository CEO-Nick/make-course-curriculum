package com.learning_planner.dto.curriculum.request

data class CreateDailyHoursPlanRequest (
    val courseId: String,
    val dailyStudyMinutes: Int,
    val studyFrequency: StudyFrequency,
    val preferredPlaybackSpeed: Float,
    var startUnitId: Long,
    )