package com.antigravity.aegis.presentation.crm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.aegis.R
import com.antigravity.aegis.data.local.entity.ClientEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientEditScreen(
    viewModel: CrmViewModel,
    clientId: Int?, // Null for create, Id for edit
    onNavigateBack: () -> Unit
) {
    val selectedClient by viewModel.selectedClient.collectAsState()
    
    // Form State
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var tipoCliente by remember { mutableStateOf("Particular") }
    var razonSocial by remember { mutableStateOf("") }
    var nifCif by remember { mutableStateOf("") }
    var personaContacto by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    
    // Address
    var calle by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var piso by remember { mutableStateOf("") }
    var poblacion by remember { mutableStateOf("") }
    var codigoPostal by remember { mutableStateOf("") }

    var notas by remember { mutableStateOf("") }

    // Initialize state if editing
    LaunchedEffect(clientId) {
        if (clientId != null && clientId != 0) {
             selectedClient?.let {
                 firstName = it.firstName
                 lastName = it.lastName
                 tipoCliente = it.tipoCliente
                 razonSocial = it.razonSocial ?: ""
                 nifCif = it.nifCif ?: ""
                 personaContacto = it.personaContacto ?: ""
                 phone = it.phone ?: ""
                 email = it.email ?: ""
                 calle = it.calle ?: ""
                 numero = it.numero ?: ""
                 piso = it.piso ?: ""
                 poblacion = it.poblacion ?: ""
                 codigoPostal = it.codigoPostal ?: ""
                 notas = it.notas ?: ""
             }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (clientId == null || clientId == 0) "Nuevo Cliente" else "Editar Cliente") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) { Text("Cancelar") }
                },
                actions = {
                    TextButton(onClick = {
                        val client = ClientEntity(
                            id = clientId ?: 0,
                            firstName = firstName,
                            lastName = lastName,
                            tipoCliente = tipoCliente,
                            razonSocial = razonSocial.ifBlank { null },
                            nifCif = nifCif.ifBlank { null },
                            personaContacto = personaContacto.ifBlank { null },
                            phone = phone.ifBlank { null },
                            email = email.ifBlank { null },
                            calle = calle.ifBlank { null },
                            numero = numero.ifBlank { null },
                            piso = piso.ifBlank { null },
                            poblacion = poblacion.ifBlank { null },
                            codigoPostal = codigoPostal.ifBlank { null },
                            notas = notas.ifBlank { null },
                            categoria = selectedClient?.categoria ?: "Potencial" // Keep existing category or default
                        )
                        viewModel.updateClient(client) // Handles both insert and update provided ID is correct
                        onNavigateBack()
                    }) {
                        Text("Guardar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .imePadding() // Avoid keyboard overlap
                .verticalScroll(rememberScrollState())
        ) {
            // Tipo Cliente
            Row {
                FilterChip(selected = tipoCliente == "Particular", onClick = { tipoCliente = "Particular" }, label = { Text("Particular") })
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(selected = tipoCliente == "Empresa", onClick = { tipoCliente = "Empresa" }, label = { Text("Empresa") })
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Name Fields
            if (tipoCliente == "Particular") {
                 OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                 OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth())
                 OutlinedTextField(value = nifCif, onValueChange = { nifCif = it }, label = { Text("NIF") }, modifier = Modifier.fillMaxWidth())
            } else {
                 OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Nombre Comercial") }, modifier = Modifier.fillMaxWidth())
                 OutlinedTextField(value = razonSocial, onValueChange = { razonSocial = it }, label = { Text("Razón Social") }, modifier = Modifier.fillMaxWidth())
                 OutlinedTextField(value = nifCif, onValueChange = { nifCif = it }, label = { Text("CIF") }, modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Contacto", style = MaterialTheme.typography.titleMedium)
            
            if (tipoCliente == "Empresa") {
                OutlinedTextField(value = personaContacto, onValueChange = { personaContacto = it }, label = { Text("Persona Contacto") }, modifier = Modifier.fillMaxWidth())
            }
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Dirección", style = MaterialTheme.typography.titleMedium)
            
            OutlinedTextField(value = calle, onValueChange = { calle = it }, label = { Text("Calle") }, modifier = Modifier.fillMaxWidth())
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = numero, onValueChange = { numero = it }, label = { Text("Número") }, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(value = piso, onValueChange = { piso = it }, label = { Text("Piso/Puerta") }, modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = codigoPostal, onValueChange = { codigoPostal = it }, label = { Text("C.P.") }, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(value = poblacion, onValueChange = { poblacion = it }, label = { Text("Población") }, modifier = Modifier.weight(2f))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Otros", style = MaterialTheme.typography.titleMedium)
            
            OutlinedTextField(
                value = notas, 
                onValueChange = { notas = it }, 
                label = { Text("Notas (Públicas)") }, 
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Spacer(modifier = Modifier.height(300.dp)) // Extra space for keyboard comfort
        }
    }
}
