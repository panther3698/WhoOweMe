package com.example.whoowesme.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.whoowesme.data.SettingsManager
import com.example.whoowesme.repository.AppRepository
import com.example.whoowesme.model.Person
import com.example.whoowesme.model.MoneyTransaction
import com.example.whoowesme.model.PersonWithBalance
import com.example.whoowesme.model.enums.TransactionType
import com.example.whoowesme.worker.ReminderWorker
import com.example.whoowesme.worker.SyncWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

data class DashboardStats(
    val totalReceivable: Double = 0.0,
    val totalPayable: Double = 0.0,
    val netBalance: Double = 0.0
)

class MainViewModel(
    private val repository: AppRepository,
    private val workManager: WorkManager,
    private val settingsManager: SettingsManager
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = settingsManager.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val remindersEnabled: StateFlow<Boolean> = settingsManager.remindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val onboardingCompleted: StateFlow<Boolean> = settingsManager.onboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setDarkMode(enabled) }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setRemindersEnabled(enabled) }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch { settingsManager.setOnboardingCompleted(completed) }
    }

    val peopleWithBalance: Flow<List<PersonWithBalance>> = combine(
        repository.allPeople,
        repository.allTransactions
    ) { people, transactions ->
        people.map { person ->
            val personTransactions = transactions.filter { it.personId == person.personId }
            val balance = personTransactions.sumOf { if (it.type == TransactionType.GIVEN) it.amount else -it.amount }
            PersonWithBalance(person, balance)
        }
    }

    val stats: Flow<DashboardStats> = peopleWithBalance.map { peopleWithBalance ->
        val receivable = peopleWithBalance.filter { it.balance > 0 }.sumOf { it.balance }
        val payable = peopleWithBalance.filter { it.balance < 0 }.sumOf { Math.abs(it.balance) }
        DashboardStats(receivable, payable, receivable - payable)
    }

    val allTransactions: Flow<List<MoneyTransaction>> = repository.allTransactions

    val allPeople: Flow<List<Person>> = repository.allPeople

    fun getTransactionsForPerson(personId: Long): Flow<List<MoneyTransaction>> =
        repository.getTransactionsForPerson(personId)

    fun addPerson(name: String, phoneNumber: String = "", notes: String = "", initialAmount: Double = 0.0, initialType: TransactionType = TransactionType.GIVEN, initialDueDate: Long? = null, initialDate: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            val personId = repository.insertPerson(Person(name = name, phoneNumber = phoneNumber, notes = notes))
            if (initialAmount > 0) {
                addTransaction(personId, initialAmount, initialType, "Initial transaction", date = initialDate, dueDate = initialDueDate)
            }
            triggerSync()
        }
    }

    fun updatePerson(person: Person) {
        viewModelScope.launch {
            repository.updatePerson(person)
            triggerSync()
        }
    }

    fun deletePerson(person: Person) {
        viewModelScope.launch {
            repository.deletePerson(person)
            triggerSync()
        }
    }

    fun addTransaction(personId: Long, amount: Double, type: TransactionType, note: String = "", date: Long = System.currentTimeMillis(), dueDate: Long? = null) {
        viewModelScope.launch {
            repository.insertTransaction(
                MoneyTransaction(personId = personId, amount = amount, type = type, note = note, date = date, dueDate = dueDate)
            )
            
            if (dueDate != null) {
                scheduleReminder(personId, amount, type, dueDate)
            }
            triggerSync()
        }
    }

    private fun triggerSync() {
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        workManager.enqueueUniqueWork("cloud_sync", ExistingWorkPolicy.REPLACE, syncRequest)
    }

    private fun scheduleReminder(personId: Long, amount: Double, type: TransactionType, dueDate: Long) {
        viewModelScope.launch {
            val p = repository.getPersonById(personId)
            if (p != null) {
                val delay = dueDate - System.currentTimeMillis()
                if (delay > 0) {
                    val data = workDataOf(
                        "personId" to personId,
                        "personName" to p.name,
                        "amount" to amount,
                        "type" to type.ordinal
                    )

                    val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(data)
                        .addTag("reminder_$personId")
                        .build()

                    workManager.enqueueUniqueWork(
                        "reminder_tx_${System.currentTimeMillis()}",
                        ExistingWorkPolicy.REPLACE,
                        reminderRequest
                    )
                }
            }
        }
    }

    fun sendManualReminder(context: Context, person: Person, balance: Double) {
        val message = if (balance > 0) {
            "Hi ${person.name}, a friendly reminder that you owe me ₹ ${String.format(Locale.getDefault(), "%,.2f", balance)}. Please clear it when possible. Thanks!"
        } else if (balance < 0) {
            "Hi ${person.name}, I wanted to let you know that I owe you ₹ ${String.format(Locale.getDefault(), "%,.2f", Math.abs(balance))}. I'll pay you back soon!"
        } else {
            return // Nothing to remind about
        }

        // Sanitize phone number: keep only digits
        val cleanPhoneNumber = person.phoneNumber.filter { it.isDigit() }
        
        // Try to open WhatsApp directly via intent with package name
        val sendIntent = Intent(Intent.ACTION_VIEW).apply {
            val url = "whatsapp://send?phone=$cleanPhoneNumber&text=${Uri.encode(message)}"
            data = Uri.parse(url)
            `package` = "com.whatsapp"
        }

        try {
            context.startActivity(sendIntent)
        } catch (e: Exception) {
            // Fallback to generic share if WhatsApp is not installed or direct intent fails
            val genericIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, message)
                type = "text/plain"
            }
            val chooser = Intent.createChooser(genericIntent, "Send Reminder via")
            // If context is not an Activity, we need NEW_TASK flag for the chooser as well
            if (context !is android.app.Activity) {
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(chooser)
            } catch (ex: Exception) {
                Toast.makeText(context, "No app found to send reminder", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteTransaction(transaction: MoneyTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            triggerSync()
        }
    }

    fun updateTransaction(transaction: MoneyTransaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
            triggerSync()
        }
    }

    suspend fun getPersonById(personId: Long): Person? = repository.getPersonById(personId)

    suspend fun getTransactionById(transactionId: Long): MoneyTransaction? = repository.getTransactionById(transactionId)
}

class MainViewModelFactory(
    private val repository: AppRepository,
    private val workManager: WorkManager,
    private val settingsManager: SettingsManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, workManager, settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
