package com.learning_planner.service

import com.learning_planner.domain.course.CourseRepository
import org.springframework.stereotype.Service

@Service
class CurriculumService(
    private val courseRepository: CourseRepository
) {

}
