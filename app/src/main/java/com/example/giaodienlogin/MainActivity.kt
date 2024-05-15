@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.giaodienlogin

import android.annotation.SuppressLint
import com.example.giaodienlogin.ui.theme.GiaoDienLoginTheme
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.database.FirebaseDatabase


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbHelper = DBHelper(this)
        setContent {
            GiaoDienLoginTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthenticationScreen(dbHelper)
                }
            }
        }
    }
}

@Composable
fun AuthenticationScreen(dbHelper: DBHelper) {
    var currentScreen by remember { mutableStateOf(Screen.Login) }
    var username by remember { mutableStateOf("") }  // Store the username here to pass it to MainScreen

    when (currentScreen) {
        Screen.Login -> LoginScreen(
            dbHelper = dbHelper,
            onLoginSuccess = { loggedInUsername ->
                username = loggedInUsername  // Update the stored username when login is successful
                currentScreen = Screen.Main
            },
            onNavigateToRegister = { currentScreen = Screen.Register }
        )
        Screen.Register -> RegisterScreen(
            dbHelper = dbHelper,
            onRegisterSuccess = { currentScreen = Screen.Login }
        )
        Screen.Main -> MainScreen(
            userName = username,  // Pass the stored username to MainScreen
            onLogout = {
                username = ""  // Clear the username upon logout
                currentScreen = Screen.Login
            }
        )
    }
}

@Composable
fun LoginScreen(
    dbHelper: DBHelper,
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (loginError) {
            Text("Login failed. Please try again.", color = Color.Red)
        }

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (dbHelper.validateUser(username, password)) {
                    onLoginSuccess(username)  // Pass the username back to the AuthenticationScreen
                } else {
                    loginError = true
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            Text("Login", color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onNavigateToRegister) {
            Text("Need an account? Register here", color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun RegisterScreen(
    dbHelper: DBHelper,
    onRegisterSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var registerError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (registerError) {
            Text(errorMessage, color = Color.Red)
        }

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (username.isBlank() || password.isBlank() || email.isBlank()) {
                    errorMessage = "All fields must be filled out."
                    registerError = true
                } else if (!isValidEmail(email)) {
                    errorMessage = "Please enter a valid email address."
                    registerError = true
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        if (dbHelper.checkUserExists(username) || dbHelper.checkEmailExists(email)) {
                            if (dbHelper.checkUserExists(username)) {
                                errorMessage = "Username already exists. Please try another one."
                            } else {
                                errorMessage = "Email already exists. Please try another one."
                            }
                            registerError = true
                        } else {
                            val added = dbHelper.addUser(username, password, email)
                            if (added) {
                                onRegisterSuccess()
                            } else {
                                errorMessage = "Registration failed. Please try again."
                                registerError = true
                            }
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            Text("Register", color = Color.White)
        }
    }
}


fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(userName: String, onLogout: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Welcome, $userName") },  // Displaying the user's name in the AppBar title
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // Greeting message includes the user's name
            Text("You are logged in as $userName!", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

// Define an enum to represent the current screen
enum class Screen {
    Login,
    Register,
    Main
}



