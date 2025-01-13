package com.learning_planner.dto.course.response

data class CourseInfo(
    val courseId: String,
    val courseName: String,
    val instructors: List<String>,
    val lectureUnitCount: Int,
    val runtime: Int,
)
