package org.example.frontend.debugmenu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.example.frontend.NetworkConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugMenu(onDismiss: () -> Unit) {
    val context = LocalContext.current

    // State to hold the text in the input field, initialized with the current IP
    var ipInput by remember { mutableStateOf(NetworkConfig.SERVER_IP) }

    // A simple dialog (popup)
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Debug Settings") },
        text = {
            Column {
                Text("Enter Flask Server IP & Port:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = ipInput,
                    onValueChange = { ipInput = it },
                    singleLine = true,
                    label = { Text("e.g. 192.168.1.7:5001") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Save to SharedPreferences and update NetworkConfig
                    NetworkConfig.saveIp(context, ipInput)
                    onDismiss()
                }
            ) {
                Text("Save IP")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}