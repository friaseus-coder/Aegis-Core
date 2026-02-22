package com.antigravity.aegis.presentation.crm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.antigravity.aegis.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.aegis.domain.model.Client
import com.antigravity.aegis.domain.model.ClientType
import com.antigravity.aegis.domain.model.Address
import com.antigravity.aegis.domain.model.ClientCategory

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
    var tipoCliente by remember { mutableStateOf(ClientType.PARTICULAR) }

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
                 calle = it.address?.calle ?: ""
                 numero = it.address?.numero ?: ""
                 piso = it.address?.piso ?: ""
                 poblacion = it.address?.poblacion ?: ""
                 codigoPostal = it.address?.codigoPostal ?: ""
                 notas = it.notas ?: ""
             }

        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (clientId == null || clientId == 0) stringResource(R.string.client_edit_title_new) else stringResource(R.string.client_edit_title_edit)) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) { Text(stringResource(R.string.general_cancel)) }
                },
                actions = {
                    TextButton(onClick = {
                        val client = Client(
                            id = clientId ?: 0,
                            firstName = firstName,
                            lastName = lastName,
                            tipoCliente = tipoCliente,
                            razonSocial = razonSocial.ifBlank { null },
                            nifCif = nifCif.ifBlank { null },
                            personaContacto = personaContacto.ifBlank { null },
                            phone = phone.ifBlank { null },
                            email = email.ifBlank { null },
                            address = Address(
                                calle = calle.ifBlank { null },
                                numero = numero.ifBlank { null },
                                piso = piso.ifBlank { null },
                                poblacion = poblacion.ifBlank { null },
                                codigoPostal = codigoPostal.ifBlank { null }
                            ),
                            notas = notas.ifBlank { null },
                            categoria = selectedClient?.categoria ?: ClientCategory.POTENTIAL // Keep existing category or default
                        )
                        viewModel.updateClient(client) // Handles both insert and update provided ID is correct

                        onNavigateBack()
                    }) {
                        Text(stringResource(R.string.general_save))
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
                FilterChip(selected = tipoCliente == ClientType.PARTICULAR, onClick = { tipoCliente = ClientType.PARTICULAR }, label = { Text(stringResource(R.string.client_type_particular)) })
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(selected = tipoCliente == ClientType.EMPRESA, onClick = { tipoCliente = ClientType.EMPRESA }, label = { Text(stringResource(R.string.client_type_empresa)) })
            }

            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Name Fields
            if (tipoCliente == ClientType.PARTICULAR) {

                 OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text(stringResource(R.string.client_label_name)) }, modifier = Modifier.fillMaxWidth())
                 OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text(stringResource(R.string.client_label_lastname)) }, modifier = Modifier.fillMaxWidth())
                 OutlinedTextField(value = nifCif, onValueChange = { nifCif = it }, label = { Text(stringResource(R.string.client_label_nif)) }, modifier = Modifier.fillMaxWidth())
            } else {
                 OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text(stringResource(R.string.client_label_tradename)) }, modifier = Modifier.fillMaxWidth())
                 OutlinedTextField(value = razonSocial, onValueChange = { razonSocial = it }, label = { Text(stringResource(R.string.client_label_legalname)) }, modifier = Modifier.fillMaxWidth())
                 OutlinedTextField(value = nifCif, onValueChange = { nifCif = it }, label = { Text(stringResource(R.string.client_label_cif)) }, modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.client_label_contact_section), style = MaterialTheme.typography.titleMedium)
            
            if (tipoCliente == ClientType.EMPRESA) {

                OutlinedTextField(value = personaContacto, onValueChange = { personaContacto = it }, label = { Text(stringResource(R.string.client_label_contact_person)) }, modifier = Modifier.fillMaxWidth())
            }
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text(stringResource(R.string.client_label_phone)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(stringResource(R.string.client_label_email)) }, modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.client_label_address_section), style = MaterialTheme.typography.titleMedium)
            
            OutlinedTextField(value = calle, onValueChange = { calle = it }, label = { Text(stringResource(R.string.client_label_street)) }, modifier = Modifier.fillMaxWidth())
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = numero, onValueChange = { numero = it }, label = { Text(stringResource(R.string.client_label_number)) }, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(value = piso, onValueChange = { piso = it }, label = { Text(stringResource(R.string.client_label_floor)) }, modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = codigoPostal, onValueChange = { codigoPostal = it }, label = { Text(stringResource(R.string.client_label_zip)) }, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(value = poblacion, onValueChange = { poblacion = it }, label = { Text(stringResource(R.string.client_label_city)) }, modifier = Modifier.weight(2f))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(stringResource(R.string.client_label_others_section), style = MaterialTheme.typography.titleMedium)
            
            OutlinedTextField(
                value = notas, 
                onValueChange = { notas = it }, 
                label = { Text(stringResource(R.string.client_label_notes_public)) }, 
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Spacer(modifier = Modifier.height(300.dp)) // Extra space for keyboard comfort
        }
    }
}
