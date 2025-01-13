package com.learning_planner.service

import com.learning_planner.domain.course.CourseRepository
import com.learning_planner.dto.course.request.CreateCourseRequest
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@AutoConfigureDataMongo
class CourseServiceTest @Autowired constructor(
    private val courseService: CourseService,
    private val courseRepository: CourseRepository,
) {

    @AfterEach
    fun cleanup() {
        courseRepository.deleteAll()
    }

    @Test
    @DisplayName("URL로 강의 저장 성공")
    fun addCourseTest() {
        // given
        val request =
            CreateCourseRequest(courseUrl = COURSE_URL1)

        // when
        courseService.addCourse(request)
        val allCourses = courseRepository.findAll()

        // then
        assertThat(allCourses).hasSize(1)
        assertThat(allCourses[0].title).isEqualTo("김영한의 실전 자바 - 기본편 강의")
        assertThat(allCourses[0].instructor).isEqualTo("김영한")
    }

    @Test
    @DisplayName("잘못된 URL 형식으로 강의 추가 시도")
    fun addCourseFailTest_MalformedUrl() {
        // given
        val request =
            CreateCourseRequest(courseUrl = "invalid url")
        //"https://www.inflearn.com/course/%EA%B9%80"

        // when / then
        assertThrows<IllegalArgumentException> {
            courseService.addCourse(request)
        }.apply {
            assertThat(this.message).isEqualTo("올바르지 않은 URL 형식입니다")
        }
    }

    @Test
    @DisplayName("존재하지 않는 강의 URL로 추가 시도")
    fun addCourseFailTest_NotFoundUrl() {
        // given
        val request =
            CreateCourseRequest(courseUrl = "https://www.inflearn.com/course/%EA%B9%80")

        // when / then
        assertThrows<IllegalStateException> {
            courseService.addCourse(request)
        }.apply {
            assertThat(this.message).isEqualTo("해당 강의를 찾을 수 없습니다")
        }
    }

    @Test
    @DisplayName("강의 검색 성공")
    fun searchCourseTest() {
        // given
        val searchTerm = "실전 자바"
        val request1 = CreateCourseRequest(courseUrl = COURSE_URL1)
        val request2 = CreateCourseRequest(courseUrl = COURSE_URL2)
        courseService.addCourse(request1)
        courseService.addCourse(request2)

        // when
        val result = courseService.searchCourse(searchTerm)

        // then
        assertThat(result).hasSize(2)
        assertThat(result[0].title).isEqualTo("김영한의 실전 자바 - 기본편 강의")
        assertThat(result[1].title).isEqualTo("김영한의 실전 자바 - 중급 1편 강의")
    }

    companion object {
        private const val COURSE_URL1 = "https://www.inflearn.com/course/%EA%B9%80%EC%98%81%ED%95%9C%EC%9D%98-%EC%8B%A4%EC%A0%84-%EC%9E%90%EB%B0%94-%EA%B8%B0%EB%B3%B8%ED%8E%B8"
        private const val COURSE_URL2 = "https://www.inflearn.com/course/%EA%B9%80%EC%98%81%ED%95%9C%EC%9D%98-%EC%8B%A4%EC%A0%84-%EC%9E%90%EB%B0%94-%EC%A4%91%EA%B8%89-1"
    }
}