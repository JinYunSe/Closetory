package com.ssafy.closetory.homeActivity

import com.ssafy.closetory.ApplicationClass

class HomeInitRepository {

    val homeInitService = ApplicationClass
        .retrofit.create(HomeInitService::class.java)

    suspend fun getTagsList() = homeInitService.getTagsList()
}
