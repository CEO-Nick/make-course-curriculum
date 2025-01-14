package com.learning_planner.service

import com.learning_planner.domain.course.CourseRepository
import com.learning_planner.dto.curriculum.request.CreateDailyHoursPlanRequest
import com.learning_planner.dto.curriculum.request.CreateDateRangePlanRequest
import com.learning_planner.dto.curriculum.request.StudyFrequency
import com.learning_planner.util.LearningPrompts
import com.learning_planner.util.findByIdOrThrow
import groovy.util.logging.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.data.mongodb.core.aggregation.DateOperators.Minute
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek

@Service
@Transactional
@Slf4j
class LearningPlanService(
    private val courseRepository: CourseRepository,
    private val chatClient: ChatClient
) {
    /**
     * 날짜를 통해 계획 생성
     */
    fun makeCurriculum(request: CreateDateRangePlanRequest): String {
        val findCourse = courseRepository.findByIdOrThrow(request.courseId)
        log.info("course info : $findCourse")

        return "success"
    }

    /**
     * 하루 공부 시간 & 공부 주기를 바탕으로 계획 생성
     */
    fun makeCurriculum(request: CreateDailyHoursPlanRequest): List<WeeklyStudyPlan> {
        val findCourse = courseRepository.findByIdOrThrow(request.courseId) // DB에서 강의 정보 가져오기

        val playBack = request.preferredPlaybackSpeed


        val courseList = findCourse.curriculum.curriculum.flatMap { section ->  // 커리큘럼 가져와서 배속 적용하기
            section.units.map { unit ->
                val adjustedTime = (unit.runtime / playBack * 10).toInt() / 10.0    // 배속 적용된 강의 시간
                Lecture(unit.title, adjustedTime)
            }
        }.filter { it.runtime > 0.0 }    // 강의 시간 0.0 제외

        // 하루 공부할 양으로 나누기
        val dailyStudySeconds = request.dailyStudyMinutes * 60
        val dailyList = divideLecturesByDailyLimit(courseList, dailyStudySeconds)

        // 공부 주기에 맞춰서 주차, 일차별 공부 계획 세워주기
        val createStudyPlan = createStudyPlan(dailyList, request.studyFrequency)
        printCurriculumList(createStudyPlan)
        return createStudyPlan
    }

    private fun printCurriculumList(dailyList: List<WeeklyStudyPlan>) {
        dailyList.map { weekPlan ->
            log.info("${weekPlan.week}주차")
            weekPlan.dayPlans.map { dayPlan ->
                log.info("${dayPlan.dayNumber}일차 : ${dayPlan.lectures}")
            }
        }
    }

    fun makeCurriculumWithAI(request: CreateDateRangePlanRequest): String? {
        val findCourse = courseRepository.findByIdOrThrow(request.courseId)

        return chatClient.prompt()
            .user(
                LearningPrompts.createDateRangePlanPrompt(
                    request,
                    findCourse.curriculum.toString()
                )
            )
            .call()
            .chatResponse()
            ?.result?.output?.content
    }

    fun makeCurriculumWithAI(request: CreateDailyHoursPlanRequest): String? {
        val findCourse = courseRepository.findByIdOrThrow(request.courseId)
        return chatClient.prompt()
            .user(
                LearningPrompts.createDailyHoursPlanPrompt(
                    request,
                    findCourse.curriculum.toString()
                )
            )
            .call()
            .chatResponse()
            ?.result?.output?.content
    }

    // 하루 공부 시간과 공부 주기를 바탕으로 계획 생성
    private fun createStudyPlan(
        dailyLectures: List<List<Lecture>>,
        frequency: StudyFrequency
    ): List<WeeklyStudyPlan> {
        val daysPerWeek = when (frequency) {
            StudyFrequency.DAILY -> 7
            StudyFrequency.FIVE_TIMES_WEEK -> 5
            StudyFrequency.THREE_TIMES_WEEK -> 3
            StudyFrequency.TWO_TIMES_WEEK -> 2
        }

        val weeklyPlans = mutableListOf<WeeklyStudyPlan>()
        var lectureIndex = 0
        var weekNumber = 1
        var dayNumber = 1

        while (lectureIndex < dailyLectures.size) {
            val dayPlans = mutableListOf<DayStudyPlan>()

            // 이번 주의 학습 일정 생성
            repeat(daysPerWeek) {
                if (lectureIndex < dailyLectures.size) {
                    dayPlans.add(
                        DayStudyPlan(
                            dayNumber = dayNumber,
                            lectures = dailyLectures[lectureIndex],
                            minute = (dailyLectures[lectureIndex].sumOf { it.runtime } / 60).toInt(),
                            second = (dailyLectures[lectureIndex].sumOf { it.runtime } % 60).toInt(),
                        )
                    )
                    lectureIndex++
                    dayNumber++
                }
            }

            // 이번 주에 학습할 내용이 있다면 주차 계획에 추가
            if (dayPlans.isNotEmpty()) {
                weeklyPlans.add(
                    WeeklyStudyPlan(
                        week = weekNumber,
                        dayPlans = dayPlans
                    )
                )
                weekNumber++
            }
        }

        return weeklyPlans
    }

    // 하루 공부
    private fun divideLecturesByDailyLimit(
        courseList: List<Lecture>,
        dailyStudySeconds: Int
    ): List<List<Lecture>> {
        val result = mutableListOf<List<Lecture>>()         // 일별 공부리스트를 담은 리스트
        var currentDayLectures = mutableListOf<Lecture>()   // 하루 공부할 강의 담는 리스트
        var currentDaySeconds = 0.0       // 현재까지 공부한 시간

        for (lecture: Lecture in courseList) {
            // 현재 강의를 들으면 하루 공부량 넘은 경우 -> 지금까지 추가한거 결과에 넣고, 새 리스트 초기화 & 현재 공부 시간 초기화
            if (currentDaySeconds + lecture.runtime > dailyStudySeconds) {
                if (currentDayLectures.isNotEmpty()) {
                    result.add(currentDayLectures.toList()) // 지금까지 추가한 강의 리스트 결과에 추가
                    currentDayLectures = mutableListOf()
                    currentDaySeconds = 0.0
                }
            }

            currentDayLectures.add(lecture)
            currentDaySeconds += lecture.runtime
        }

        // 마지막 날 강의들 추가
        if (currentDayLectures.isNotEmpty()) {
            result.add(currentDayLectures)
        }

        return result
    }

    companion object {
        private val log = LoggerFactory.getLogger(LearningPlanService::class.java)

    }
}

data class Lecture(
    val title: String,
    val runtime: Double,
)

data class WeeklyStudyPlan(
    val week: Int,                    // 몇 주차
    val dayPlans: List<DayStudyPlan>,  // 해당 주의 일별 계획
)

data class DayStudyPlan(
    val dayNumber: Int,         // 요일
    val lectures: List<Lecture>,     // 그날 들을 강의들
    val minute: Int,       // 그날 들을 강의 시간(분)
    val second: Int,
)