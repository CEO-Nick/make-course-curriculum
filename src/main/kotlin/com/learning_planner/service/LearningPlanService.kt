package com.learning_planner.service

import com.learning_planner.domain.course.CourseRepository
import com.learning_planner.dto.curriculum.request.CreateDailyHoursPlanRequest
import com.learning_planner.dto.curriculum.request.CreateDateRangePlanRequest
import com.learning_planner.util.LearningPrompts
import com.learning_planner.util.findByIdOrThrow
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class LearningPlanService(
    private val courseRepository: CourseRepository,
    private val chatClient: ChatClient
) {

    // <p class="mantine-Text-root mantine-1hqlk6r">
    fun makeCurriculum(request: CreateDateRangePlanRequest) {
        val findCourse = courseRepository.findByIdOrThrow(request.courseId)

    }

    fun makeCurriculum(request: CreateDailyHoursPlanRequest) {

    }


    fun makeCurriculumWithAI(request: CreateDateRangePlanRequest): String? {
        val findCourse = courseRepository.findByIdOrThrow(request.courseId)

        return chatClient.prompt()
            .user(LearningPrompts.createDateRangePlanPrompt(request, findCourse.curriculum.toString()))
            .call()
            .chatResponse()
            ?.result?.output?.content
    }

    fun makeCurriculumWithAI(request: CreateDailyHoursPlanRequest): String? {
        val findCourse = courseRepository.findByIdOrThrow(request.courseId)
        return chatClient.prompt()
            .user(LearningPrompts.createDailyHoursPlanPrompt(request, findCourse.curriculum.toString()))
            .call()
            .chatResponse()
            ?.result?.output?.content
    }

}
