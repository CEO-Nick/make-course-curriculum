package com.learning_planner.service

import com.learning_planner.domain.course.Course
import com.learning_planner.domain.course.CourseRepository
import com.learning_planner.dto.course.request.CreateCourseRequest
import com.learning_planner.dto.course.response.CourseInfo
import com.learning_planner.dto.curriculum.response.CurriculumResponse
import groovy.util.logging.Slf4j
import org.slf4j.LoggerFactory
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
        // 강의 정보 추출
        val courseInfo = fetchCourseInfo(request.courseUrl)

        // 강의 커리큘럼 추출
        val courseCurriculum = fetchCurriculum(courseInfo.courseId)

        // 강의 정보 & 커리큘럼 저장
        return courseRepository.save(
            Course(
                id = courseInfo.courseId,
                title = courseInfo.courseName,
                instructor = courseInfo.instructor,
                curriculum = courseCurriculum.data,
            )
        )
    }

    /**
     * 강의 검색
     */
    fun searchCourse(term: String): List<Course> {
        return courseRepository.findAllByTitleContainingIgnoreCase(term)
    }

    fun findAll(): MutableList<Course> {
        return courseRepository.findAll()
    }

    /**
     * 인프런 강의 URL로 강의 정보(강의ID, 강의명, 강사명) 추출
     */
    private fun fetchCourseInfo(courseUrl: String): CourseInfo {
        val courseHtml = fetchCourseHtml(courseUrl)

        // 강의 ID 추출
        val courseId = ITEM_ID_PATTERN.find(courseHtml)?.groupValues?.get(1)
            ?: throw IllegalStateException("강의 ID를 알 수 없습니다")

        // 강의명 & 강사명 추출
        val title = TITLE_PATTERN.find(courseHtml)?.groupValues
        val courseName = title?.get(1)?.trim() ?: throw IllegalStateException("강의명을 알 수 없습니다")
        val instructor = title[2].trim()


        // 강의 개수 & 강의 전체 시간 추출
        // TODO : 에러 발생 해결하기
        val courseMetadata = COURSE_METADATA_PATTERN.find(courseHtml)?.groupValues ?: throw IllegalStateException("강의 개수와 강의 전체 시간을 추출할 수 없습니다")
        val numberOfLessons = courseMetadata[1].toInt()
        val hours = courseMetadata[2].toInt()
        val minutes = courseMetadata[3].toInt()

        log.info("강의 개수 : $numberOfLessons\t$hours 시간 $minutes 분")

        return CourseInfo(
            courseId = courseId,
            courseName = courseName,
            instructor = instructor,
            lessonCount = numberOfLessons,
            totalDuration = hours * 60 + minutes
        )
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

    private fun buildCurriculumUrl(courseId: String): String {
        return "$CURRICULUM_API_BASE_URL$courseId$CURRICULUM_API_SUFFIX"
    }

    companion object {
        // 강의 ID 추출을 위한 정규식 패턴
        private val ITEM_ID_PATTERN =
            """<meta property="dtr:item_id" content="(\d+)"\s*/>""".toRegex()

        // 강의명 추출
        private val TITLE_PATTERN = """<title>(.*?) \| (.*?) - 인프런</title>""".toRegex()

        // 강의 개수 & 강의 전체 시간 추출
        private val COURSE_METADATA_PATTERN = """(\d+)\s*∙\s*\((\d+)시간\s*(\d+)분\)""".toRegex()

        // 커리큘럼 api 앞부분
        private const val CURRICULUM_API_BASE_URL =
            "https://course-api.inflearn.com/client/api/v1/course/"

        // 커리큘럼 api 뒷부분
        private const val CURRICULUM_API_SUFFIX = "/curriculum?lang=ko"

        private val log = LoggerFactory.getLogger(CourseService::class.java)


    }
}
