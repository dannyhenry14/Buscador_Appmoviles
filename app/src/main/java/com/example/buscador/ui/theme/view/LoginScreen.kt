package com.example.buscador.ui.theme.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buscador.ui.theme.DarkGreen
import com.example.buscador.ui.theme.YellowDuoc

/**
 * Pantalla de inicio de sesión.
 *
 * Stateless respecto a la navegación: recibe [onIngresar] y delega la decisión
 * de a dónde ir en el NavHost (la vista no conoce rutas).
 * El estado del formulario es local porque es efímero (no sobrevive a la pantalla).
 *
 * @param onIngresar Callback con el correo y la contraseña capturados.
 */
@Composable
fun LoginScreen(
    onIngresar: (correoElectronico: String, contrasena: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var correoElectronico by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    // safeDrawingPadding evita que el formulario quede bajo la barra de estado
    // o la barra de navegación (la Activity usa enableEdgeToEdge).
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkGreen)
            .safeDrawingPadding()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "CENAMAD",
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        OutlinedTextField(
            value = correoElectronico,
            onValueChange = { nuevoCorreo -> correoElectronico = nuevoCorreo },
            label = { Text(text = "Correo electrónico", color = Color.LightGray) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth(fraction = 0.9f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { nuevaContrasena -> contrasena = nuevaContrasena },
            label = { Text(text = "Contraseña", color = Color.LightGray) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth(fraction = 0.9f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onIngresar(correoElectronico, contrasena) },
            enabled = correoElectronico.isNotBlank() && contrasena.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = YellowDuoc),
            modifier = Modifier
                .fillMaxWidth(fraction = 0.9f)
                .height(50.dp)
        ) {
            Text(
                text = "Ingresar",
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
