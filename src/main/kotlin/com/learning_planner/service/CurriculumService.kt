package com.learning_planner.service

import com.learning_planner.dto.curriculum.request.CurriculumCreateRequest
import com.learning_planner.dto.curriculum.response.CurriculumResponse
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class CurriculumService {
    private val restTemplate = RestTemplate()

    /**
     * 강의 URL을 받아 해당 강의의 커리큘럼 정보를 반환
     *
     * @param request 강의 URL을 포함한 요청 객체
     * @return 커리큘럼 정보
     * @throws IllegalStateException 강의 정보를 불러올 수 없거나 강의 ID를 찾을 수 없는 경우
     */
    fun makeCurriculum(request: CurriculumCreateRequest): CurriculumResponse {
        val courseId = fetchCourseId(request.courseUrl)
            ?: throw IllegalStateException("강의 ID를 찾을 수 없습니다")

        return fetchCurriculum(courseId)
    }

    private fun fetchCourseId(courseUrl: String): String? {
        val courseHtml = fetchCourseHtml(courseUrl)
        return extractCourseId(courseHtml)
    }

    private fun fetchCourseHtml(courseUrl: String): String {
        return restTemplate.getForObject(courseUrl, String::class.java)
            ?: throw IllegalStateException("해당 강의를 불러올 수 없습니다")
    }

    private fun extractCourseId(courseHtml: String): String? {
        return ITEM_ID_PATTERN.find(courseHtml)?.groupValues?.get(1)
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
        private val ITEM_ID_PATTERN = """<meta property="dtr:item_id" content="(\d+)"\s*/>""".toRegex()
        private const val CURRICULUM_API_BASE_URL = "https://course-api.inflearn.com/client/api/v1/course/"
        private const val CURRICULUM_API_SUFFIX = "/curriculum?lang=ko"
    }
}
