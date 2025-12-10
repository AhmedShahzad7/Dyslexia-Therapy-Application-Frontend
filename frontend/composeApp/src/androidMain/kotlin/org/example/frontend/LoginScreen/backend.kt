package org.example.frontend.LoginScreen

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

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
}