
package com.central.market

import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

data class RegistrationResult(
    val user: FirebaseUser,
    val verificationEmailSent: Boolean
)

class FirebaseAuthManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun register(
        emailInput: String,
        password: String,
        onSuccess: (RegistrationResult) -> Unit,
        onError: (String) -> Unit
    ) {
        val email = emailInput.trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onError("يرجى إدخال بريد إلكتروني صحيح.")
            return
        }

        if (password.length < 8) {
            onError("كلمة المرور يجب أن تكون 8 أحرف على الأقل.")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                if (!task.isSuccessful) {
                    onError(
                        getErrorMessage(task.exception)
                    )
                    return@addOnCompleteListener
                }

                val user = auth.currentUser

                if (user == null) {
                    onError("تعذر إنشاء جلسة الحساب.")
                    return@addOnCompleteListener
                }

                user.sendEmailVerification()
                    .addOnCompleteListener { verificationTask ->

                        onSuccess(
                            RegistrationResult(
                                user = user,
                                verificationEmailSent =
                                    verificationTask.isSuccessful
                            )
                        )
                    }
            }
    }

    fun signIn(
        emailInput: String,
        password: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val email = emailInput.trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onError("يرجى إدخال بريد إلكتروني صحيح.")
            return
        }

        if (password.isBlank()) {
            onError("أدخل كلمة المرور.")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                if (!task.isSuccessful) {
                    onError(
                        getErrorMessage(task.exception)
                    )
                    return@addOnCompleteListener
                }

                val user = auth.currentUser

                if (user == null) {
                    auth.signOut()
                    onError("تعذر التحقق من الحساب.")
                    return@addOnCompleteListener
                }

                user.reload().addOnCompleteListener {

                    val refreshedUser = auth.currentUser

                    if (refreshedUser == null) {
                        auth.signOut()
                        onError("تعذر استعادة بيانات الحساب.")
                        return@addOnCompleteListener
                    }

                    if (!refreshedUser.isEmailVerified) {
                        auth.signOut()
                        onError(
                            "يجب تأكيد بريدك الإلكتروني أولاً."
                        )
                        return@addOnCompleteListener
                    }

                    onSuccess(refreshedUser)
                }
            }
    }

    fun resendVerificationEmail(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            onError("لا يوجد حساب مفتوح حالياً.")
            return
        }

        user.sendEmailVerification()
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(
                        getErrorMessage(task.exception)
                    )
                }
            }
    }

    fun sendPasswordReset(
        emailInput: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val email = emailInput.trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onError("يرجى إدخال بريد إلكتروني صحيح.")
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(
                        getErrorMessage(task.exception)
                    )
                }
            }
    }

    fun signOut() {
        auth.signOut()
    }

    private fun getErrorMessage(error: Exception?): String {
        val code = (error as? com.google.firebase.auth.FirebaseAuthException)
            ?.errorCode

        return when (code) {
            "ERROR_INVALID_EMAIL" ->
                "البريد الإلكتروني غير صحيح."

            "ERROR_EMAIL_ALREADY_IN_USE" ->
                "هذا البريد مسجل مسبقاً."

            "ERROR_WEAK_PASSWORD" ->
                "كلمة المرور ضعيفة."

            "ERROR_USER_NOT_FOUND" ->
                "الحساب غير موجود."

            "ERROR_WRONG_PASSWORD" ->
                "كلمة المرور غير صحيحة."

            "ERROR_INVALID_CREDENTIAL" ->
                "بيانات الدخول غير صحيحة."

            "ERROR_TOO_MANY_REQUESTS" ->
                "محاولات كثيرة. حاول لاحقاً."

            "ERROR_NETWORK_REQUEST_FAILED" ->
                "تعذر الاتصال بالإنترنت."

            else ->
                error?.localizedMessage
                    ?: "حدث خطأ غير متوقع."
        }
    }
}
