package com.ssafy.closetory.util

// kotlin에서 data class는

// java에서 getter, setter, hashCode, equals 메서드를 만든 class와 동일합니다.
// => 즉, 지금은 한국어와 영어에 대한 위의 메서드가 있는 클래스를 만든 상태가 됩니다.

//                  UI에 표기할 한국어             서버로 보낼 영어
data class OptionItem(val labelKorean: String, val codeEnglish: String)
