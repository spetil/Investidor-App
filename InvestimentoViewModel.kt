package com.example.investidorapp.viewmodel


import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import com.example.investidorapp.MainActivity
import com.example.investidorapp.model.Investimento
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.investidorapp.R

class InvestimentosViewModel(application: Application) : AndroidViewModel(application) {
    private val database = FirebaseDatabase.getInstance().reference.child("investimentos")
    private val _investimentos = MutableStateFlow<List<Investimento>>(emptyList())
    val investimentos: StateFlow<List<Investimento>> = _investimentos

    init {
        monitorarAlteracoes()
    }

    private fun monitorarAlteracoes() {
        database.addChildEventListener(object : ChildEventListener {
            @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val nome = snapshot.child("nome").getValue(String::class.java) ?: "Desconhecido"
                val valor = snapshot.child("valor").getValue(Int::class.java) ?: 0
                enviarNotificacao("Novo Investimento", "$nome adicionado com R$ $valor")
                carregarInvestimentos()
            }

            @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val nome = snapshot.child("nome").getValue(String::class.java) ?: "Desconhecido"
                val valor = snapshot.child("valor").getValue(Int::class.java) ?: 0
                enviarNotificacao("Investimento Atualizado", "$nome agora vale R$ $valor")
                carregarInvestimentos()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                carregarInvestimentos()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Erro: ${error.message}")
            }
        })
    }

    fun adicionarInvestimento(nome: String, valor: Double) {
        val novoId = database.push().key ?: return
        val novo = Investimento(nome = nome, valor = valor.toInt())
        database.child(novoId).setValue(novo)
    }

    @SuppressLint("MissingPermission")
    private fun carregarInvestimentos() {
        database.get().addOnSuccessListener { snapshot ->
            val lista = mutableListOf<Investimento>()
            for (item in snapshot.children) {
                val nome = item.child("nome").getValue(String::class.java) ?: "Desconhecido"
                val valor = item.child("valor").getValue(Int::class.java) ?: 0
                lista.add(Investimento(nome, valor))
            }

            if (_investimentos.value.isEmpty() && lista.isNotEmpty()) {
                val primeiro = lista.first()
                enviarNotificacao("Investimentos Carregados", "Incluindo ${primeiro.nome}")
            }

            _investimentos.value = lista
        }.addOnFailureListener {
            Log.e("FirebaseError", "Erro ao carregar investimentos: ${it.message}")
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun enviarNotificacao(titulo: String, mensagem: String) {
        val channelId = "investimentos_channel"
        val notificationId = System.currentTimeMillis().toInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificações de Investimentos",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getApplication<Application>()
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(getApplication(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            getApplication(),
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(getApplication(), channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titulo)
            .setContentText(mensagem)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(getApplication()).notify(notificationId, notification)
    }
}