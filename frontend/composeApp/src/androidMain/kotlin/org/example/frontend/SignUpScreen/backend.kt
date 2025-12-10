package org.example.frontend.SignUpScreen

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.tasks.Task
class SignUpViewModel : ViewModel() {
    fun Register(email: String, password: String,onSuccess: (String?) -> Unit) {
        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener {task ->
            if (task.isSuccessful) {
                val userId = task.result?.user?.uid
                onSuccess(userId)
            }
        }
    }
    fun signInWithGoogle(idToken: String, onSuccess: (String?) -> Unit, onError: (Exception) -> Unit) {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid
                    onSuccess(userId)
                } else {
                    onError(task.exception ?: Exception("Unknown error"))
                }
            }
    }
}