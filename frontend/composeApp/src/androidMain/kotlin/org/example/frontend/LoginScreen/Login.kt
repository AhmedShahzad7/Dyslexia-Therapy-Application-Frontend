package org.example.frontend.LoginScreen
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import org.example.frontend.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material3.AlertDialog
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
@Composable
fun LoginScreen(onSignUpScreen: () -> Unit,onHomeScreen: () -> Unit,
                viewModel: LoginViewModel=androidx.lifecycle.viewmodel.compose.viewModel()
) {
    fun navigatetosignup()
    {
        onSignUpScreen()
    }
    fun navigatetohome()
    {
        onHomeScreen()
    }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var ShowForgotDialog by remember { mutableStateOf(false) }
    var ForgotEmail by remember { mutableStateOf("") }

    //GOOGLE INTEGRATON
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)
    val WEB_CLIENT_ID = "1005589695326-btuk75uk4i67ilevqot6alkft62lfgpm.apps.googleusercontent.com"

    // 2. GOOGLE SIGN IN FUNCTION
    fun handleGoogleLogin() {
        coroutineScope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(WEB_CLIENT_ID)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val googleIdToken = googleIdTokenCredential.idToken

                    viewModel.signInWithGoogle(
                        idToken = googleIdToken,
                        onSuccess = {
                            Log.d("LoginScreen", "Google Login Success")
                            navigatetohome()
                        },
                        onError = { e ->
                            Log.e("LoginScreen", "Google Login Error: ${e.message}")
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("LoginScreen", "Google Sign-In Cancelled/Failed: ${e.message}")
            }
        }
    }



    if(ShowForgotDialog)
    {
        AlertDialog(
            onDismissRequest = { ShowForgotDialog = false },
            title ={
              Text(
                  text = "Enter Email",
                  style = TextStyle(
                      fontSize = 18.sp,
                      fontFamily = FontFamily(Font(R.font.windsol)),
                      fontWeight = FontWeight(400),
                      color = Color(0xFF000278),
                  )

              )
            },
            text={
                OutlinedTextField(
                    value=ForgotEmail,
                    onValueChange = {ForgotEmail=it},
//                    label = {Text("Email")},
                    modifier = Modifier
                        .width(253.dp).height(56.dp)
                        .clip(RoundedCornerShape(35.dp))
                        .background(color = Color(0xFFFFFFFF)),
                    shape = RoundedCornerShape(size = 35.dp),
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 16.sp
                    ),
                )
            },
            confirmButton = {
                Text(
                    text = "Send",
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            val isForgotEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(ForgotEmail).matches()
                            if(ForgotEmail.isNotBlank() && isForgotEmailValid) {
                                viewModel.Passwordreset(ForgotEmail, onSuccess = {
                                    ShowForgotDialog = false
                                    ForgotEmail = ""
                                })

                            }
                        },
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = FontFamily(Font(R.font.windsol)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF000278),
                    )
                )
            },
            dismissButton = {
                Text(
                    text = "Cancel",
                    modifier = Modifier
                        .clickable {
                            ShowForgotDialog = false
                            ForgotEmail = ""
                        }
                        .padding(8.dp),
                    color = Color.Gray,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = FontFamily(Font(R.font.windsol)),
                        fontWeight = FontWeight(400),)
                )
            }

        )

    }
    Box(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    )
    {
        Image(
                painter = painterResource(id = R.drawable.loginbackgroundimage),
                contentDescription = null,
            modifier = Modifier.fillMaxSize().graphicsLayer(rotationZ = 90f).graphicsLayer(scaleX = 2.5f, scaleY =2f ),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(316.dp)
//                .height(595.dp)
                .background(color = Color(0x216E69FF), shape = RoundedCornerShape(size = 30.dp))
//                .padding(start = 122.dp, top = 27.dp, end = 122.dp, bottom = 27.dp)
                .padding(top = 27.dp, bottom = 27.dp)
        ) {
            Text(
                text = "Login",
                modifier = Modifier.width(276.dp)
                    .height(40.dp),
                style = TextStyle(
                    fontSize = 34.sp,
                    fontFamily = FontFamily(Font(R.font.windsol)),
                    fontWeight = FontWeight.W400,
                    color = Color(0xF5000278),
                    textAlign = TextAlign.Center,

                )

            )
            Text(
                text = "Welcome Back",
                modifier = Modifier.width(276.dp)
                    .height(40.dp),
                style = TextStyle(
                    fontSize = 30.sp,
                    fontFamily = FontFamily(Font(R.font.windsol)),
                    fontWeight = FontWeight.W400,
                    color = Color(0xFF000278),
                    textAlign = TextAlign.Center,

                    )

            )
            Column (
                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
//                    .width(269.dp).height(97.dp)
                    .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
            )
            {
                Text(
                    text = "Email",
                    modifier = Modifier.width(127.dp)
                        .height(43.dp),
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontFamily = FontFamily(Font(R.font.windsol)),
                        fontWeight = FontWeight.W400,
                        color = Color(0xFF000278),

                        )

                )

                OutlinedTextField(
                    value=email,
                    onValueChange = {email=it},
//                    label = {Text("Email")},
                    modifier = Modifier
                        .width(253.dp).height(56.dp)
                        .clip(RoundedCornerShape(35.dp))
                        .background(color = Color(0xFFFFFFFF)),
                    shape = RoundedCornerShape(size = 35.dp),
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 16.sp
                    ),


                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                horizontalAlignment = Alignment.Start,
                modifier =  Modifier
//                    .width(269.dp).height(90.dp)
                    .padding(start = 10.dp, end = 10.dp),
            )
            {
                Text(
                    text = "Password",
                    modifier = Modifier.width(127.dp)
                        .height(43.dp),
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontFamily = FontFamily(Font(R.font.windsol)),
                        fontWeight = FontWeight.W400,
                        color = Color(0xFF000278),

                        )
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                )
                {
                    OutlinedTextField(
                        value=password,
                        onValueChange = {password=it},
//                    label = {Text("Email")},
                        modifier = Modifier
                            .width(253.dp).height(56.dp)
                            .clip(RoundedCornerShape(35.dp))
                            .background(color = Color(0xFFFFFFFF)),
                        shape = RoundedCornerShape(size = 35.dp),
                        textStyle = TextStyle(
                            color = Color.Black,
                            fontSize = 16.sp
                        ),
                        visualTransformation = PasswordVisualTransformation()

                    )
                    }
            }

            Text(
                text = "Forgot Password?",
                modifier = Modifier.width(165.dp).height(25.dp).clickable{ShowForgotDialog=true},
                style = TextStyle(
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.windsol)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF000278),
                    textAlign = TextAlign.Center,
                )
            )
            Text(
                text = "OR\n",
                modifier = Modifier.width(27.dp).height(35.dp),
                style = TextStyle(
                    fontSize = 24.sp,
                    fontFamily = FontFamily(Font(R.font.windsol)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF000278),
                    textAlign = TextAlign.Center,
                )
            )

            //LOGIN WITH GOOGLE
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .border(width = 5.dp, color = Color(0xFF000278), shape = RoundedCornerShape(35.dp))
                    .width(254.dp)
                    .height(44.dp)
                    .background(color = Color(0x00FFFFFF), shape = RoundedCornerShape(35.dp))
//                    .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp)
                    .padding(horizontal = 10.dp)
                    .clickable { handleGoogleLogin() }
            ) {
                Text(
                    text = "Login with Google",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.windsol)),
                        fontWeight = FontWeight.W400,
                        color = Color(0xFF000278),
                        textAlign = TextAlign.Center
                        )
                )

                Image(
                    painter = painterResource(id = R.drawable.googleicon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
//                        .width(19.46069.dp)
//                        .height(9.99332.dp)
                )
            }
            Row(
                modifier = Modifier.clickable{
                    val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                    if(email.isNotBlank() && password.isNotBlank() && isEmailValid)

                    {
                        viewModel.login(email,password, onSuccess = {
                            navigatetohome()
                        })

                    }
                }
            )
            {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(134.dp)
                    .height(66.dp)
                    .background(color = Color(0xFF000278), shape = RoundedCornerShape(size = 35.dp))
                    .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp)

            )
            {
                Text(
                    text = "Login",
                    modifier = Modifier.width(114.dp).height(27.dp),
                    fontSize = 24.sp,
                    fontFamily = FontFamily(Font(R.font.windsol)),
                    fontWeight = FontWeight.W400,
                    color = Color(0xFFFFFFFF),
                    textAlign = TextAlign.Center
                    )
            }
                }
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .width(298.dp)
                    .height(44.dp)
                    .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp)
            )
            {
                Text(
                    text = "Don't have an account?",
//                    modifier = Modifier.width(170.dp).height(24.dp),
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.windsol)),
                    fontWeight = FontWeight.W400,
                    color = Color(0xFF000278),
                    textAlign = TextAlign.Center
                    )
                Row(
                    modifier = Modifier.clickable(onClick = {navigatetosignup()})

                )
                {
                    Text(
                        text = "Register!",
                        modifier = Modifier.padding(start = 10.dp),
                        fontSize = 18.sp,
                        fontFamily = FontFamily(Font(R.font.windsol)),
                        fontWeight = FontWeight.W400,
                        color = Color(0xFF000278),
                        textAlign = TextAlign.Center

                    )
                }
            }

        }
    }
}


//@Preview
//@Composable
//fun LoginScreenPreview() {
//    LoginScreen()
//}