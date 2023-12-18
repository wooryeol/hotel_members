package kr.co.parnas.common

object Define {

    var DOMAIN = "https://www.parnashotel.com"
    // var DOMAIN = "http://192.168.0.194:3000"
    /*val ROOT = "https://www.kshp.or.kr/mobile/symposium/2023autumn"*/


    const val SUCCESS = "success"
    //파일 프로바이더
    val AUTHORITY = "kr.co.mindall.seapp.fileprovider"
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