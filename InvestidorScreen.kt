package com.example.investidorapp.viewmodel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.investidorapp.model.Investimento
import kotlinx.coroutines.launch

@Composable
fun InvestidorScreen(viewModel: InvestimentosViewModel) {
    var telaAtual by remember { mutableStateOf("lista") }

    if (telaAtual == "adicionar") {
        AddInvestimentoScreen(
            viewModel = viewModel,
            onVoltar = { telaAtual = "lista" }
        )
    } else {
        TelaLista(viewModel = viewModel, onAdicionar = { telaAtual = "adicionar" })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaLista(viewModel: InvestimentosViewModel, onAdicionar: () -> Unit) {
    val investimentos by viewModel.investimentos.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Investidor App",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = onAdicionar) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar investimento")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ListaInvestimentos(investimentos)

            snackbarMessage?.let { message ->
                LaunchedEffect(message) {
                    snackbarHostState.showSnackbar(message)
                    snackbarMessage = null
                }
            }
        }
    }
}

@Composable
fun ListaInvestimentos(investimentos: List<Investimento>) {
    if (investimentos.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Nenhum investimento encontrado.",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            items(investimentos.size) { index ->
                InvestimentoItem(investimentos[index])
            }
        }
    }
}

@Composable
fun InvestimentoItem(investimento: Investimento) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Ícone de investimento",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = investimento.nome,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Valor: R$ ${investimento.valor}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInvestimentoScreen(viewModel: InvestimentosViewModel, onVoltar: () -> Unit) {
    var nome by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novo Investimento") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
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
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.adicionarInvestimento(
                        nome = nome,
                        valor = valor.toDoubleOrNull() ?: 0.0
                    )
                    onVoltar()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Adicionar")
            }
        }
    }
}
