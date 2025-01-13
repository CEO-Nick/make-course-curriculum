package com.learning_planner.util

import com.learning_planner.dto.curriculum.request.CreateDailyHoursPlanRequest
import com.learning_planner.dto.curriculum.request.CreateDateRangePlanRequest
import com.learning_planner.dto.curriculum.request.StudyFrequency
import com.learning_planner.dto.curriculum.request.WeekendStudyPlan
import java.time.LocalDate

class LearningPrompts {
    companion object {
        val systemPrompt = """
            당신은 온라인 강의 학습 계획을 생성하는 AI 어시스턴트입니다.
            
            다음 사항들을 고려하여 개인화된 학습 계획을 수립해야 합니다:
            1. 시간 계산
                - 강의 시간은 초(second) 단위로 제공됨
                - 사용자가 설정한 재생 속도(배속)로 나누어 실제 시청 시간 계산
                - 예시: 
                  * 300초 강의를 1.5배속으로 시청 시 실제 시청 시간 = 300초 ÷ 1.5 = 200초
                  * 149초 강의를 1.5배속으로 시청 시 실제 시청 시간 = 149초 ÷ 1.5 ≈ 99초
                
            2. 일일 학습 시간 준수
                - 사용자가 지정한 일일 학습 시간(분)을 초과하지 않도록 계획
                - 한 강의는 분할하지 않고 한 번에 학습하도록 계획
                - 실제 시청 시간 + 실습/복습 시간을 고려하여 계획
            
            3. 응답 형식 (아래 형식을 정확히 따를 것)
                [학습 계획 요약]
                - 총 학습 기간: N일
                - 예상 완료일: YYYY-MM-DD
                - 일일 학습 시간: N분 (실제 시청 시간 기준)
                - 주의사항: (학습 시 주의할 점)
                
                [상세 학습 계획]
                - 날짜: YYYY-MM-DD
                    - 강의명 (재생 속도 N배속, M분)
                    (M은 소수점 첫째자리까지 표시, 실제 시청 시간)
                
                [학습 전략 제안]
                - 구체적인 학습 전략 3가지 이상
            
              절대 규칙:
            1. 모든 강의를 빠짐없이 포함해야 함
            2. 중간에 "..." 또는 "이어서"와 같은 문구 사용 금지
            3. 모든 날짜는 YYYY-MM-DD 형식 사용
            4. 하루 학습 시간은 정확히 지정된 시간만큼만 배정
            5. 응답을 절대 중단하지 말고 모든 강의를 포함할 것
            6. 실제 시청 시간은 정확히 계산하여 소수점 첫째자리까지 표시
        """.trimIndent()

        fun createDateRangePlanPrompt(
            request: CreateDateRangePlanRequest,
            curriculum: String
        ): String = """
            다음 조건에 맞는 학습 계획을 생성해주세요:
            
            [학습 기간]
            시작일: ${request.startDate}
            종료일: ${request.endDate}
            
            [학습 설정]
            선호 재생 속도: ${request.preferredPlaybackSpeed}배속
            주말 학습: ${translateWeekendPlan(request.weekendStudyPlan)}
            
            [커리큘럼 정보]
            $curriculum
            
            위 조건을 바탕으로 현실적이고 달성 가능한 학습 계획을 수립해주세요.
            매일의 학습량이 적절히 분배되도록 해주시고, 실습이 필요한 강의는 충분한 시간을 할당해주세요.
        """.trimIndent()

        fun createDailyHoursPlanPrompt(
            request: CreateDailyHoursPlanRequest,
            curriculum: String
        ): String = """
            다음 조건에 맞는 학습 계획을 생성해주세요:
            
            [학습 가능 시간]
            일일 학습 시간: ${request.dailyStudyMinutes}분
            학습 주기: ${translateStudyFrequency(request.studyFrequency)}
            시작일: ${LocalDate.now()}
            
            [학습 설정]
            선호 재생 속도: ${request.preferredPlaybackSpeed}배속
            
            [커리큘럼 정보]
            $curriculum
            
            위 조건을 바탕으로 하루 학습 시간에 맞춘 현실적인 학습 계획을 수립해주세요.
            학습 주기와 가능 시간을 고려하여 적절한 양의 컨텐츠를 배분해주시고,
            예상 수강 완료 기간도 함께 계산해주세요.
        """.trimIndent()

        private fun translateWeekendPlan(plan: WeekendStudyPlan): String = when (plan) {
            WeekendStudyPlan.INCLUDE_WEEKENDS -> "주말 포함"
            WeekendStudyPlan.EXCLUDE_WEEKENDS -> "주말 제외"
            WeekendStudyPlan.WEEKEND_ONLY_ONE_DAY -> "주말 하루만"
        }

        private fun translateStudyFrequency(frequency: StudyFrequency): String = when (frequency) {
            StudyFrequency.DAILY -> "매일"
            StudyFrequency.THREE_TIMES_WEEK -> "주 3회"
            StudyFrequency.TWO_TIMES_WEEK -> "주 2회"
        }
    }
}