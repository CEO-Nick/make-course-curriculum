package com.learning_planner.service

import com.learning_planner.domain.course.Course
import com.learning_planner.domain.course.CourseRepository
import com.learning_planner.dto.curriculum.request.CourseCreateRequest
import com.learning_planner.dto.curriculum.response.CourseInfo
import com.learning_planner.dto.curriculum.response.CurriculumResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate

@Service
@Transactional
class CourseService(
    private val courseRepository: CourseRepository
) {
    private val restTemplate = RestTemplate()

    /**
     * 입력한 강의 URL의 정보를 저장하기
     */
    fun addCourse(request: CourseCreateRequest): Course {
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

        return CourseInfo(
            courseId = courseId,
            courseName = courseName,
            instructor = instructor,
        )
    }

    // 강의URL로 get요청
    private fun fetchCourseHtml(courseUrl: String): String {
        val courseHtml = restTemplate.getForObject(courseUrl, String::class.java)
            ?: throw IllegalStateException("해당 강의를 불러올 수 없습니다")
        return courseHtml
    }

    // 강의ID로 커리큘럼 요청
    private fun fetchCurriculum(courseId: String): CurriculumResponse {
        val curriculumUrl = buildCurriculumUrl(courseId)
        return restTemplate.getForObject(curriculumUrl, CurriculumResponse::class.java)
            ?: throw IllegalStateException("강의 커리큘럼을 불러올 수 없습니다")
    }

    private fun buildCurriculumUrl(courseId: String): String {
        return "$CURRICULUM_API_BASE_URL$courseId$CURRICULUM_API_SUFFIX"
    }

    companion object {
        private val ITEM_ID_PATTERN =
            """<meta property="dtr:item_id" content="(\d+)"\s*/>""".toRegex()
        private val TITLE_PATTERN = """<title>(.*?) \| (.*?) - 인프런</title>""".toRegex()
        private const val CURRICULUM_API_BASE_URL =
            "https://course-api.inflearn.com/client/api/v1/course/"
        private const val CURRICULUM_API_SUFFIX = "/curriculum?lang=ko"
    }
}
