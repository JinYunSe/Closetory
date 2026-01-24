// LogoutService

package com.ssafy.closetory.authActivity.logout

import com.ssafy.closetory.ApplicationClass

class LogoutRepository {

    private val service =
        ApplicationClass.retrofit.create(LogoutService::class.java)

    suspend fun logout() = service.logout()
}
