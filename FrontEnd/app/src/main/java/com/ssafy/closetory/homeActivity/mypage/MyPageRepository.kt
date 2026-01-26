// LogoutService

package com.ssafy.closetory.homeActivity.mypage

import com.ssafy.closetory.ApplicationClass

class MyPageRepository {

    private val service =
        ApplicationClass.retrofit.create(MyPageService::class.java)

    suspend fun logout() = service.logout()
}
