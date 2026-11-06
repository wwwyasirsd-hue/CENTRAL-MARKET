package com.central.market

import com.google.firebase.Timestamp

/**
 * CENTRAL MARKET
 * نموذج بيانات الملف الشخصي للمستخدم.
 *
 * لا يحتوي على صلاحيات الإدارة أو الملكية.
 * الصلاحيات الحساسة تُدار وتُتحقق منها عبر الخادم.
 */
data class UserProfile(

    val uid: String = "",

    val displayName: String = "",

    val email: String = "",

    val photoUrl: String? = null,

    val createdAt: Timestamp? = null,

    val updatedAt: Timestamp? = null
)
