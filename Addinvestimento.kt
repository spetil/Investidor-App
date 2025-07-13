package com.example.investidorapp.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import com.example.investidorapp.viewmodel.InvestimentosViewModel
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions // ✅ Corrige o erro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInvestimentoScreen(
    viewModel: InvestimentosViewModel,
    onVoltar: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novo Investimento") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome do investimento") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = valor,
                onValueChange = { valor = it },
                label = { Text("Valor") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    viewModel.adicionarInvestimento(nome, valor.toDoubleOrNull() ?: 0.0)
                    onVoltar()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Adicionar")
            }
        }
    }
}