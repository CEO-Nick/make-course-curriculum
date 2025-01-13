package com.learning_planner.dto.course.response

data class CourseInfo(
    val courseId: String,
    val courseName: String,
    val instructor: String,
    val lessonCount: Int,
    val totalDuration: Int,
)
