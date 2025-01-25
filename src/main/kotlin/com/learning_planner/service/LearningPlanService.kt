package com.learning_planner.service

import com.learning_planner.domain.course.Course
import com.learning_planner.domain.course.CourseRepository
import com.learning_planner.dto.curriculum.request.CreateDailyHoursPlanRequest
import com.learning_planner.dto.curriculum.request.CreateDateRangePlanRequest
import com.learning_planner.dto.curriculum.request.HolidayInclusionType
import com.learning_planner.dto.curriculum.request.StudyFrequency
import com.learning_planner.util.findByIdOrThrow
import groovy.util.logging.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.temporal.WeekFields

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
    fun makeCurriculum(request: CreateDateRangePlanRequest): List<LectureSchedule> {
        val findCourse = courseRepository.findByIdOrThrow(request.courseId)

        val courseList = calculateAdjustedLectures(
            findCourse = findCourse,
            playBack = request.preferredPlaybackSpeed,
            startUnitId = request.startUnitId
        )

        val generateStudyDates = generateStudyDates(request)

        val distributeLectures = distributeLectures(courseList, generateStudyDates)
        log.info("최종 일별 스케줄 : $distributeLectures")

        return distributeLectures
    }

    /**
     * 하루 공부 시간 & 공부 주기를 바탕으로 계획 생성
     */
    fun makeCurriculum(request: CreateDailyHoursPlanRequest): List<WeeklyStudyPlan> {
        val findCourse = courseRepository.findByIdOrThrow(request.courseId) // DB에서 강의 정보 가져오기

        val courseList = calculateAdjustedLectures(
            findCourse = findCourse,
            playBack = request.preferredPlaybackSpeed,
            startUnitId = request.startUnitId
        )

        // 하루 공부할 양으로 나누기
        val dailyStudySeconds = request.dailyStudyMinutes * 60
        val dailyList = divideLecturesByDailyLimit(courseList, dailyStudySeconds)

        // 공부 주기에 맞춰서 주차, 일차별 공부 계획 세워주기
        val createStudyPlan = createStudyPlan(dailyList, request.studyFrequency)

        logCurriculumList(createStudyPlan)

        return createStudyPlan
    }


    fun distributeLectures(
        lectures: List<Lecture>,
        availableDates: List<LocalDate>
    ): List<LectureSchedule> {
        // 1. 총 강의 시간 계산
        val totalRuntime = lectures.sumOf { it.runtime }
        val targetDailyRuntime = (totalRuntime / availableDates.size) * 1.25 // 20% 여유 추가
        log.info("전체 런타임 : $totalRuntime")
        log.info("일별 런타임 : $targetDailyRuntime")
        // 2. 결과를 저장할 리스트
        val schedule = mutableListOf<LectureSchedule>()

        // 3. 현재 처리 중인 날짜의 강의들
        var currentLectures = mutableListOf<Lecture>()
        var currentRuntime = 0.0
        var lectureIndex = 0

        // 4. 각 날짜별로 강의 할당
        availableDates.forEachIndexed { dateIndex, date ->
            val isLastDate = dateIndex == availableDates.size - 1

            // 마지막 날짜가 아닌 경우, targetDailyRuntime에 최대한 맞추기
            while (lectureIndex < lectures.size) {
                val lecture = lectures[lectureIndex]

                // 마지막 날짜면 남은 강의 모두 추가
                if (isLastDate) {
                    currentLectures.add(lecture)
                    currentRuntime += lecture.runtime
                    lectureIndex++
                    continue
                }

                // 이 강의를 추가했을 때의 예상 런타임
                val expectedRuntime = currentRuntime + lecture.runtime

                // 목표 시간을 초과하면 다음 날짜로
                if (expectedRuntime > targetDailyRuntime && currentLectures.isNotEmpty()) {
                    break
                }

                currentLectures.add(lecture)
                currentRuntime += lecture.runtime
                lectureIndex++
            }

            // 이 날짜의 스케줄 저장
            if (currentLectures.isNotEmpty()) {
                schedule.add(
                    LectureSchedule(
                        date = date,
                        lectures = currentLectures.toList(),
                        totalRuntime = currentRuntime
                    )
                )
            }

            // 다음 날짜를 위해 초기화
            currentLectures = mutableListOf()
            currentRuntime = 0.0
        }

        return schedule
    }

    /**
     * 강의 DB 데이터에서 커리큘럼 추출 후, 배속 적용 & 시작/중간부터 강의 선택
     */
    private fun calculateAdjustedLectures(
        findCourse: Course,
        playBack: Float,
        startUnitId: Long,
    ): List<Lecture> {
        var drop = false
        val courseList =
            findCourse.curriculum!!.curriculum.flatMapIndexed { sectionIdx, section ->
                val originalSize = section.units.size

                val units = if (startUnitId == 0L || drop) {
                    section.units
                } else {
                    val remainingUnits = section.units.dropWhile { unit -> unit.id != startUnitId }
                    drop =
                        remainingUnits.isNotEmpty()  // 아직 startUnitId 찾지 못한 경우 -> false / 찾은 경우 -> true
                    remainingUnits
                }

                val dropCount =
                    originalSize - units.size   // drop한 강의 수 << unit index 설정할 때, drop한 강의 수만큼 더해줘야 함

                units
                    .mapIndexed { unitIdx, unit ->
                        val adjustedTime =
                            (unit.runtime / playBack * 10).toInt() / 10.0    // 배속 적용된 강의 시간
                        val title =
                            "[섹션 ${sectionIdx + 1}] ${unitIdx + dropCount + 1}. ${unit.title}"
                        Lecture(title, adjustedTime)
                    }
            }.filter { it.runtime > 0.0 }    // 강의 시간 0.0 제외
        return courseList
    }

    private fun logCurriculumList(dailyList: List<WeeklyStudyPlan>) {
        dailyList.map { weekPlan ->
            log.info("${weekPlan.week}주차")
            weekPlan.dayPlans.map { dayPlan ->
                log.info("${dayPlan.dayNumber}일차 : ${dayPlan.lectures}")
            }
        }
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

    // 일별 공부 계획
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


    // 목표날짜를 바탕으로 공부 가능한 날짜 리스트 만드는 함수
    private fun generateStudyDates(request: CreateDateRangePlanRequest): List<LocalDate> {
        val startDate = LocalDate.parse(request.startDate)
        val endDate = LocalDate.parse(request.endDate)

        val availableDates = startDate.datesUntil(endDate.plusDays(1))
            .toList()
            .filter { date ->
                request.holidayInclusion == HolidayInclusionType.INCLUDE_HOLIDAYS
                        || !ALL_HOLIDAYS_25.contains(date)
            }

        // 공부 주기가 "매일" 이면 바로 return
        if (request.studyFrequency == StudyFrequency.DAILY) {
            return availableDates
        }

        // 2. 주 단위로 그룹화
        val weeklyDates = availableDates.groupBy { date ->
            // ISO 주차를 기준으로 그룹화
            "${date.year}-W${date.get(WeekFields.ISO.weekOfWeekBasedYear())}"
        }
        log.info("주 단위로 그룹화 된 날짜 정보 : $weeklyDates")

        // 3. 각 주에서 필요한 만큼만 선택
        val targetDaysPerWeek = when (request.studyFrequency) {
            StudyFrequency.FIVE_TIMES_WEEK -> 5
            StudyFrequency.THREE_TIMES_WEEK -> 3
            StudyFrequency.TWO_TIMES_WEEK -> 2
            StudyFrequency.DAILY -> availableDates.size // 이미 위에서 처리됨
        }

        return weeklyDates.flatMap { (_, datesInWeek) ->
            // 각 주의 가용 날짜 중 필요한 만큼만 선택
            // 만약 해당 주의 가용 날짜가 목표보다 적다면 가용한 만큼만 선택됨
            if (datesInWeek.size <= targetDaysPerWeek) {
                datesInWeek.take(targetDaysPerWeek)
            } else {
                when (targetDaysPerWeek) {
                    2 -> {
                        when (datesInWeek.size) {
                            7, 6 -> listOf(datesInWeek[1], datesInWeek[4])
                            5, 4 -> listOf(datesInWeek[1], datesInWeek[3])
                            else -> datesInWeek.take(targetDaysPerWeek)
                        }
                    }

                    3 -> {
                        when (datesInWeek.size) {
                            7, 6 -> listOf(datesInWeek[1], datesInWeek[3], datesInWeek[5])
                            5 -> listOf(datesInWeek[0], datesInWeek[2], datesInWeek[4])
                            else -> datesInWeek.take(targetDaysPerWeek)
                        }
                    }

                    else -> datesInWeek.take(targetDaysPerWeek)
                }
            }
        }.sorted()
    }

//    fun makeCurriculumWithAI(request: CreateDateRangePlanRequest): String? {
//        val findCourse = courseRepository.findByIdOrThrow(request.courseId)
//
//        return chatClient.prompt()
//            .user(
//                LearningPrompts.createDateRangePlanPrompt(
//                    request,
//                    findCourse.curriculum.toString()
//                )
//            )
//            .call()
//            .chatResponse()
//            ?.result?.output?.content
//    }
//
//    fun makeCurriculumWithAI(request: CreateDailyHoursPlanRequest): String? {
//        val findCourse = courseRepository.findByIdOrThrow(request.courseId)
//        return chatClient.prompt()
//            .user(
//                LearningPrompts.createDailyHoursPlanPrompt(
//                    request,
//                    findCourse.curriculum.toString()
//                )
//            )
//            .call()
//            .chatResponse()
//            ?.result?.output?.content
//    }

    companion object {

        private val ALL_HOLIDAYS_25 = hashSetOf<LocalDate>(
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 27),
            LocalDate.of(2025, 1, 28),
            LocalDate.of(2025, 1, 29),
            LocalDate.of(2025, 1, 30),
            LocalDate.of(2025, 3, 1),
            LocalDate.of(2025, 3, 3),
            LocalDate.of(2025, 5, 5),
            LocalDate.of(2025, 5, 6),
            LocalDate.of(2025, 6, 6),
            LocalDate.of(2025, 8, 15),
            LocalDate.of(2025, 10, 3),
            LocalDate.of(2025, 10, 5),
            LocalDate.of(2025, 10, 6),
            LocalDate.of(2025, 10, 7),
            LocalDate.of(2025, 10, 8),
            LocalDate.of(2025, 10, 9),
            LocalDate.of(2025, 12, 25),
        )

        private val log = LoggerFactory.getLogger(LearningPlanService::class.java)
    }
}

data class Lecture(
    val title: String,
    val runtime: Double,
)

data class WeeklyStudyPlan(
    val week: Int,                      // 몇 주차
    val dayPlans: List<DayStudyPlan>,  // 해당 주의 일별 계획
)

data class DayStudyPlan(
    val dayNumber: Int,              // 요일
    val lectures: List<Lecture>,     // 그날 들을 강의들
    val minute: Int,                 // 그날 들을 강의 시간(분)
    val second: Int,                 // 그날 들을 강의 시간(초)
)

data class LectureSchedule(
    val date: LocalDate,
    val lectures: List<Lecture>,
    val totalRuntime: Double
)