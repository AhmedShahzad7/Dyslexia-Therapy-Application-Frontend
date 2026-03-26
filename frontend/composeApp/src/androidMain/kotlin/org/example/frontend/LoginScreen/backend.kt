package org.example.frontend.LoginScreen

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore


class LoginViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    fun login(email: String, password: String,onSuccess: (Boolean) -> Unit) {
        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        checkAssessmentStatus(userId, onSuccess)
                    }
                }

            }
    }
    private fun checkAssessmentStatus(userId: String, onResult: (Boolean) -> Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val isComplete = document.getBoolean("hasCompletedAssessment") ?: false
                    onResult(isComplete)
                } else {
                    onResult(false) // Default to false if no record found
                }
            }
            .addOnFailureListener {
                onResult(false)
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
    fun signInWithGoogle(idToken: String, onSuccess: (Boolean) -> Unit, onError: (Exception) -> Unit) {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    val result = task.result
                    val user = auth.currentUser
                    val isNewUser = result?.additionalUserInfo?.isNewUser == true
                    if (isNewUser) {
                        user?.delete()
                            ?.addOnCompleteListener {
                                onError(Exception("No account found. Please Register first."))
                            }
                        auth.signOut()
                    } else {
                        val userId = auth.currentUser?.uid
                        if (userId != null) checkAssessmentStatus(userId, onSuccess)
                    }
                } else {
                    onError(task.exception ?: Exception("Unknown error"))
                }
            }
    }
}