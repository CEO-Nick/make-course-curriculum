package com.learning_planner.service

import com.learning_planner.domain.course.Course
import com.learning_planner.domain.course.CourseRepository
import com.learning_planner.dto.course.request.CreateCourseRequest
import com.learning_planner.dto.course.response.CourseInfo
import com.learning_planner.dto.course.response.CourseInfoApiResponse
import com.learning_planner.dto.curriculum.response.CurriculumResponse
import groovy.util.logging.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.net.MalformedURLException
import java.net.URL

@Service
@Transactional
@Slf4j
class CourseService(
    private val courseRepository: CourseRepository
) {
    private val restTemplate = RestTemplate()

    /**
     * 입력한 강의 URL의 정보를 저장하기
     */
    fun addCourse(request: CreateCourseRequest): Course {
        // 강의 ID 추출
        val courseId = extractCourseId(request.courseUrl)

        // 강의 정보 추출(강의명, 강사들명, 강의 개수, 전체 런타임)
        val courseInfo = fetchCourseInfo(courseId)

        // 강의 커리큘럼 추출
        val courseCurriculum = fetchCurriculum(courseInfo.courseId)

        // 강의 정보 & 커리큘럼 저장
        return courseRepository.save(
            Course(
                id = courseInfo.courseId,
                title = courseInfo.courseName,
                instructors = courseInfo.instructors,
                lectureUnitCount = courseInfo.lectureUnitCount,
                runtime = courseInfo.runtime,
                curriculum = courseCurriculum.data,
            )
        )
    }

    /**
     * 강의 검색
     */
    fun searchCourse(term: String): List<Course> {
        val searchQuery = term.split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ")  // 공백으로 구분된 검색어

        val searchCourses =
            courseRepository.searchCourses(searchQuery, Sort.by(Sort.Order.desc("score")))
        log.info("search result : $searchCourses")
        return searchCourses
    }

    fun getLatestCourses(): MutableList<Course> {
        return courseRepository.findAll().reversed().toMutableList()
    }

    private fun fetchCourseInfo(courseId: String): CourseInfo {
        val courseInfoUrl = buildCourseInfoUrl(courseId)

        try {
            val courseInfoResponse =
                restTemplate.getForObject(courseInfoUrl, CourseInfoApiResponse::class.java) ?.data
                    ?: throw IllegalStateException("강의 정보 요청 반환값이 null 입니다")

            return CourseInfo(
                courseId = courseId,
                courseName = courseInfoResponse.title,
                instructors = courseInfoResponse.instructors.map { it.name },
                lectureUnitCount = courseInfoResponse.unitSummary.lectureUnitCount,
                runtime = courseInfoResponse.unitSummary.runtime,
            )
        } catch (e: RestClientException) {
            throw IllegalStateException("강의 정보를 불러올 수 없습니다")
        }

    }

    /**
     * 인프런 강의 URL로 강의 ID 추출
     */
    private fun extractCourseId(courseUrl: String): String {
        val courseHtml = fetchCourseHtml(courseUrl)

        // 강의 ID 추출
        return ITEM_ID_PATTERN.find(courseHtml)?.groupValues?.get(1)
            ?: throw IllegalStateException("강의 ID를 알 수 없습니다")
    }

    // 강의URL로 get요청
    private fun fetchCourseHtml(courseUrl: String): String {
        try {
            // URL 형식 검증
            URL(courseUrl)
        } catch (e: MalformedURLException) {
            throw IllegalArgumentException("올바르지 않은 URL 형식입니다")
        }

        // HTTP 요청 및 응답 처리
        return try {
            restTemplate.getForObject(courseUrl, String::class.java)
                ?: throw IllegalStateException("강의 요청 반환값이 null 입니다")
        } catch (e: RestClientException) {
            throw IllegalStateException("해당 강의를 찾을 수 없습니다")
        }
    }


    // 강의ID로 커리큘럼 요청
    private fun fetchCurriculum(courseId: String): CurriculumResponse {
        val curriculumUrl = buildCurriculumUrl(courseId)

        return try {
            restTemplate.getForObject(curriculumUrl, CurriculumResponse::class.java)
                ?: throw IllegalStateException("강의 커리큘럼 요청 반환값이 null 입니다")
        } catch (e: RestClientException) {
            throw IllegalStateException("강의 커리큘럼을 불러올 수 없습니다")
        }
    }

    /**
     * 커리큘럼 요청 URL 생성
     */
    private fun buildCurriculumUrl(courseId: String): String {
        return "$INFLEARN_API_BASE_URL$courseId$CURRICULUM_API_SUFFIX"
    }

    /**
     * 강의 정보 요청 URL 생성
     */
    private fun buildCourseInfoUrl(courseId: String): String {
        return "$INFLEARN_API_BASE_URL$courseId$COURSE_INFO_API_SUFFIX"
    }

    companion object {
        // 강의 ID 추출을 위한 정규식 패턴
        private val ITEM_ID_PATTERN =
            """<meta property="dtr:item_id" content="(\d+)"\s*/>""".toRegex()

        // inflearn api 앞부분
        private const val INFLEARN_API_BASE_URL =
            "https://course-api.inflearn.com/client/api/v1/course/"

        // 커리큘럼 api 뒷부분
        private const val CURRICULUM_API_SUFFIX = "/curriculum?lang=ko"

        // 강의 정보 api 뒷부분
        private const val COURSE_INFO_API_SUFFIX = "/online/info?lang=ko"

        private val log = LoggerFactory.getLogger(CourseService::class.java)
    }
}
