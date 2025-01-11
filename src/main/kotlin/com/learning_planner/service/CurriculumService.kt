package com.learning_planner.service

import com.learning_planner.domain.course.Course
import com.learning_planner.domain.course.CourseRepository
import com.learning_planner.dto.curriculum.request.CurriculumCreateRequest
import com.learning_planner.dto.curriculum.response.CourseInfo
import com.learning_planner.dto.curriculum.response.CurriculumResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate

@Service
class CurriculumService(
    private val courseRepository: CourseRepository
) {
    private val restTemplate = RestTemplate()

    /**
     * 강의 URL을 받아 해당 강의의 커리큘럼 정보를 반환
     *
     * @param request 강의 URL을 포함한 요청 객체
     * @return 커리큘럼 정보
     * @throws IllegalStateException 강의 정보를 불러올 수 없거나 강의 ID를 찾을 수 없는 경우
     */
    @Transactional
    fun makeCurriculum(request: CurriculumCreateRequest): CurriculumResponse {
        // 강의 정보 추출
        val courseInfo = fetchCourseInfo(request.courseUrl)

        // 강의 커리큘럼 추출
        val curriculumResponse = fetchCurriculum(courseInfo.courseId)

        // DB 저장
        courseRepository.save(Course(
            id = courseInfo.courseId,
            title = courseInfo.courseName,
            instructor = courseInfo.instructor,
            curriculum = curriculumResponse.data
        ))


        return curriculumResponse
    }

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

    private fun fetchCourseHtml(courseUrl: String): String {
        val courseHtml = restTemplate.getForObject(courseUrl, String::class.java)
            ?: throw IllegalStateException("해당 강의를 불러올 수 없습니다")
        return courseHtml
    }

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
