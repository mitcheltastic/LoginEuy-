package com.example.logingugel

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.logingugel.ui.theme.LoginGugelTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.ktor.websocket.WebSocketDeflateExtension.Companion.install
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

val supabase = createSupabaseClient(
    supabaseUrl = "https://zxojwdxfruljjesjovdp.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inp4b2p3ZHhmcnVsamplc2pvdmRwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTQ2Njk2MDgsImV4cCI6MjAzMDI0NTYwOH0.KM0HwI9nVegx_V-yj487msOq0pAkBqh_bcKOAX05Kyk"
){
    install(Auth)
    install(Postgrest)
}




class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoginGugelTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ){
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        InsertButton()
                        GoogleSignInButton()
                    }
                }
                }
            }
        }
    }
@Composable
fun InsertButton(){
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Button(onClick = {
        coroutineScope.launch {
            try {
                supabase.from("posts").insert(mapOf("content" to "Hello from Android!"))

                Toast.makeText(context, "New row inserted", Toast.LENGTH_SHORT).show()
            }catch(e: Exception){
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
            supabase.from("posts").insert(mapOf("content" to "Hello from Android!"))

            Toast.makeText(context, "New row inserted", Toast.LENGTH_SHORT).show()
        }
    }) {
        Text("Insert a new row")
    }
}

@Composable
fun GoogleSignInButton(){
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val onClick: () -> Unit = {
        val credentialManager = CredentialManager.create(context)

        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") {str, it-> str + "%02x".format(it)}

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("348180806006-5ehec97cffvgnp1c07e69n9a6vh5blk2.apps.googleusercontent.com")
            .setNonce(hashedNonce)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        coroutineScope.launch {
            try {

                val result = credentialManager.getCredential(
                    request = request,
                    context = context,
                )
                val credential = result.credential

                val googleIdTokenCredential = GoogleIdTokenCredential
                    .createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken

                supabase.auth.signInWith(IDToken){
                    idToken = googleIdToken
                    provider = Google
                    nonce = hashe
                }

                Toast.makeText(context, "You are signed in!", Toast.LENGTH_SHORT).show()
                }catch(e: GetCredentialException){
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }catch(e: GoogleIdTokenParsingException){
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
        }

    }

    Button(onClick = onClick) {
        Text("Sign in with google ")
    }
}