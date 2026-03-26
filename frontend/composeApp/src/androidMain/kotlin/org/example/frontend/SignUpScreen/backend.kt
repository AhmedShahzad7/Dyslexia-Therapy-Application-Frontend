package org.example.frontend.SignUpScreen

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception
class SignUpViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    fun Register(email: String, password: String,onSuccess: (String?) -> Unit) {
        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener {task ->
            if (task.isSuccessful) {
                val userId = task.result?.user?.uid
                if (userId != null) {
                    createUserProfile(userId, onSuccess)
                }
            }
        }
    }
    fun signInWithGoogle(idToken: String, onSuccess: (String?) -> Unit, onError: (Exception) -> Unit) {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    val isNewUser = result?.additionalUserInfo?.isNewUser == true
                    if (isNewUser) {
                        val userId = result?.user?.uid
                        if (userId != null) {
                            createUserProfile(userId, onSuccess)
                        }
                    } else {
                        auth.signOut()
                        onError(Exception("Account already exists. Please go to Login."))
                    }
                } else {
                    onError(task.exception ?: Exception("Unknown error"))
                }
            }
    }
    private fun createUserProfile(userId: String, onSuccess: (String?) -> Unit ){
        val userMap = hashMapOf(
            "uid" to userId,
            "hasCompletedAssessment" to false // Your flag
        )

        db.collection("users").document(userId)
            .set(userMap)
            .addOnSuccessListener {
                onSuccess(userId)
            }

    }
}