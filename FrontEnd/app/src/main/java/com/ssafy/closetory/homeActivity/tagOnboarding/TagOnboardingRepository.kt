package com.ssafy.closetory.homeActivity.tagOnboarding

import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.TagOnboardingRequest

private const val TAG = "TagOnboardingRepository_싸피"

class TagOnboardingRepository {

    private val service: TagOnboardingService =
        ApplicationClass.retrofit.create(TagOnboardingService::class.java)

    suspend fun postTagOnboarding(userId: Int, tags: List<Int>) =
        service.postTagOnboarding(userId, TagOnboardingRequest(tags))
}
