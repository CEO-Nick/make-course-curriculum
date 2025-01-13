package com.learning_planner.service

import com.learning_planner.config.LearningPrompts
import com.learning_planner.domain.course.CourseRepository
import com.learning_planner.dto.curriculum.request.CreateDailyHoursPlanRequest
import com.learning_planner.dto.curriculum.request.CreateDateRangePlanRequest
import com.learning_planner.util.findByIdOrThrow
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class LearningPlanService(
    private val courseRepository: CourseRepository,
    private val chatClient: ChatClient
) {

    fun makeCurriculum(request: CreateDateRangePlanRequest): ChatResponse? {
        val findCourse = courseRepository.findByIdOrThrow(request.courseId)

        return chatClient.prompt()
            .user(LearningPrompts.createDateRangePlanPrompt(request, findCourse.curriculum.toString()))
            .call()
            .chatResponse()
    }
    fun makeCurriculum(request: CreateDailyHoursPlanRequest): ChatResponse? {
        val findCourse = courseRepository.findByIdOrThrow(request.courseId)
        return chatClient.prompt()
            .user(LearningPrompts.createDailyHoursPlanPrompt(request, findCourse.curriculum.toString()))
            .call()
            .chatResponse()
    }
}
