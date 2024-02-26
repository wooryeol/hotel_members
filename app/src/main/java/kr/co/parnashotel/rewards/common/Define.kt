package kr.co.parnashotel.rewards.common

object Define {
    // var DOMAIN = "https://www.parnashotel.com"
    var DOMAIN = "http://192.168.0.29:3000" // 대리님 로컬
    // var DOMAIN = "http://192.168.0.194:3000" // 백지혜 매니저님 로컬
    // val DOMAIN = "http://121.138.151.6:12402" // 개발

    const val login = "/membershipLogin?isApp=true"
    const val signUp = "/signUp"
    const val rsvn = "/reservationSchedule?tab=0"
    const val search = "/reservationSchedule?tab=1"
    const val dining = "/reservationDining"
    const val reservationCheck = "/reservationCheck"
    const val loggedInReservationCheck = "/myPage/reservationCheck/roomReservation"
    const val dashBoard = "/myPage"
    const val membershipIntroduction = "/membershipIntroduction"

    const val SUCCESS = "success"
    //파일 프로바이더
    val AUTHORITY = "kr.co.parnashotel.rewards.fileprovider"
    //카메라사용 권한 요청코드
    const val CAMERA_REQUEST_CODE = 1001
    //저장소 사용 권한 요청코드
    const val STORAGE_REQUEST_CODE = 1002
    //사진 가져오기 결과
    const val GET_PHOTO_REQUEST_CODE = 1003

    //handler 사용 시 이벤트 콜백
    const val EVENT_OK = 10000
    const val EVENT_CANCEL = 10001
}