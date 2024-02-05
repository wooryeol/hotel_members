package kr.co.parnashotel.rewards.model

data class MembershipUserInfoModel (
    var membershipYn: String? = null,
    var membershipId: String? = null,
    var membershipNo: String? = null,
    var memberName: String? = null,
    var memberGender: String? = null,
    var memberEmail: String? = null,
    var memberMobile: String? = null,
    var memberFirstName: String? = null,
    var memberLastName: String? = null,
    var employeeStatus: String? = null,
    var recommenderStatus: String? = null,
    var temporaryYn: String? = null
)