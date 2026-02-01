package com.ssafy.closetory.homeActivity.home

import android.util.Log
import com.ssafy.closetory.ApplicationClass
import kotlin.math.log

private const val TAG = "HomeRepository_싸피"
class HomeRepository {
    val service = ApplicationClass.retrofit.create(HomeService::class.java)

    fun getStylingList() {
        try {
            service
        } catch (e: Exception) {
            Log.e(TAG, "현재 달 기준 앞 뒤 3달 룩 조회 : ${e.message}")
        }
    }
}
