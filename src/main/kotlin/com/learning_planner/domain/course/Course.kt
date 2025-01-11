package com.learning_planner.domain.course

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "courses")
data class Course(
    @Id
    val id: String,  // 인프런 강의 ID
    val title: String,  // 강의명
    val instructor: String,  // 강사명
    val curriculum: CurriculumData  // 커리큘럼 데이터를 그대로 사용
)

