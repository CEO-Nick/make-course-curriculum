package com.learning_planner.dto.curriculum.response

import com.learning_planner.domain.course.CurriculumData

data class CurriculumResponse(
    val statusCode: String,
    val message: String,
    val errorCode: String?,
    val data: CurriculumData
)