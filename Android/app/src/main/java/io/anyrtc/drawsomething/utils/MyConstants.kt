package io.anyrtc.drawsomething.utils

typealias str = String
typealias int = Int
typealias bool = Boolean

object MyConstants {

	// --- SP KEY ---
	const val UID = "uid"
	const val USER_NAME = "userName"
    const val WORK_NAME = "workName"
	const val PRO_MODE = "pro_mode"
	// ---  END  ---

	data class ChatData(
		val uid: str,
		val msg: str,
		val isSelf: bool = false
	)
}
