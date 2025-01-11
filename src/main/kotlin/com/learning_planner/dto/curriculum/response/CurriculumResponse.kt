package com.learning_planner.dto.curriculum.response

data class CurriculumResponse(
    val statusCode: String,
    val message: String,
    val errorCode: String?,
    val data: CurriculumData
)