package org.example.frontend.LoginScreen

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider


class LoginViewModel : ViewModel() {
    fun login(email: String, password: String,onSuccess: () -> Unit) {
        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    onSuccess()
                }

            }
    }
    fun Passwordreset(ForgotEmail: String,onSuccess: () -> Unit){

        FirebaseAuth.getInstance().sendPasswordResetEmail(ForgotEmail)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                }
            }
    }
    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(task.exception ?: Exception("Unknown error"))
                }
            }
    }
}