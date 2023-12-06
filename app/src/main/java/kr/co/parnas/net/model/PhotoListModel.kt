package kr.co.parnas.net.model

import java.io.Serializable

data class PhotoListModel(var url: ArrayList<String>?, val resultCode: String,
                          val resultMsg: String): Serializable
