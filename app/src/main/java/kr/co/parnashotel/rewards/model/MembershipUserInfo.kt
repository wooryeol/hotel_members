package kr.co.parnashotel.rewards.model

import kotlinx.serialization.Serializable

data class MembershipUserInfo (
    var membershipYn: String,
    var membershipId: String,
    var membershipNo: String,
    var memberName: String,
    var memberGender: String,
    var memberEmail: String,
    var memberMobile: String,
    var memberFirstName: String,
    var memberLastName: String,
    var employeeStatus: String,
    var recommenderStatus: String,
    var temporaryYn: String
)