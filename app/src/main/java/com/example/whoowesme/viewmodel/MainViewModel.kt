package com.example.whoowesme.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.whoowesme.R
import com.example.whoowesme.data.SettingsManager
import com.example.whoowesme.model.PersonDueStatus
import com.example.whoowesme.repository.AppRepository
import com.example.whoowesme.model.Person
import com.example.whoowesme.model.ReminderTone
import com.example.whoowesme.model.MoneyTransaction
import com.example.whoowesme.model.PersonWithBalance
import com.example.whoowesme.model.enums.RecurrenceFrequency
import com.example.whoowesme.model.enums.TransactionType
import com.example.whoowesme.util.MoneyFormatter
import com.example.whoowesme.worker.ReminderWorker
import com.example.whoowesme.worker.SyncWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

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
    init {
        viewModelScope.launch {
            refreshReminders()
        }
    }

    val isDarkMode: StateFlow<Boolean> = settingsManager.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val remindersEnabled: StateFlow<Boolean> = settingsManager.remindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val onboardingCompleted: StateFlow<Boolean?> = settingsManager.onboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val appLockEnabled: StateFlow<Boolean?> = settingsManager.appLockEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setDarkMode(enabled) }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setRemindersEnabled(enabled)
            refreshReminders()
        }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch { settingsManager.setOnboardingCompleted(completed) }
    }

    fun setAppLockEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setAppLockEnabled(enabled) }
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

    val dueStatuses: Flow<List<PersonDueStatus>> = combine(
        repository.allPeople,
        repository.allTransactions
    ) { people, transactions ->
        val now = System.currentTimeMillis()
        people.mapNotNull { person ->
            val personTransactions = transactions.filter { it.personId == person.personId }
            val balance = personTransactions.sumOf {
                if (it.type == TransactionType.GIVEN) it.amount else -it.amount
            }
            val dueDate = personTransactions
                .filter { it.type == TransactionType.GIVEN }
                .mapNotNull { it.dueDate }
                .minOrNull()
            val promisedPaymentDate = personTransactions
                .filter { it.type == TransactionType.GIVEN }
                .mapNotNull { it.promisedPaymentDate }
                .maxOrNull()

            if (balance > 0 && dueDate != null) {
                val millisPerDay = TimeUnit.DAYS.toMillis(1)
                val dayDiff = (abs(now - dueDate) / millisPerDay).coerceAtLeast(0)
                PersonDueStatus(
                    person = person,
                    balance = balance,
                    dueDate = dueDate,
                    promisedPaymentDate = promisedPaymentDate,
                    isOverdue = dueDate < now,
                    isPromiseMissed = promisedPaymentDate?.let { it < now } == true,
                    daysOffset = dayDiff
                )
            } else {
                null
            }
        }.sortedWith(compareByDescending<PersonDueStatus> { it.isOverdue }.thenBy { it.dueDate })
    }

    fun getTransactionsForPerson(personId: Long): Flow<List<MoneyTransaction>> =
        repository.getTransactionsForPerson(personId)

    fun addPerson(name: String, phoneNumber: String = "", notes: String = "", initialAmount: Double = 0.0, initialType: TransactionType = TransactionType.GIVEN, initialDueDate: Long? = null, initialDate: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            val personId = repository.insertPerson(Person(name = name, phoneNumber = phoneNumber, notes = notes))
            if (initialAmount > 0) {
                repository.insertTransaction(
                    MoneyTransaction(
                        personId = personId,
                        amount = initialAmount,
                        type = initialType,
                        note = "Initial transaction",
                        date = initialDate,
                        dueDate = initialDueDate
                    )
                )
            }
            triggerSync()
            refreshReminders()
        }
    }

    fun updatePerson(person: Person) {
        viewModelScope.launch {
            repository.updatePerson(person)
            triggerSync()
            refreshReminders()
        }
    }

    fun deletePerson(person: Person) {
        viewModelScope.launch {
            repository.deletePerson(person)
            triggerSync()
            refreshReminders()
        }
    }

    fun addTransaction(
        personId: Long,
        amount: Double,
        type: TransactionType,
        note: String = "",
        date: Long = System.currentTimeMillis(),
        dueDate: Long? = null,
        promisedPaymentDate: Long? = null,
        recurrenceFrequency: RecurrenceFrequency = RecurrenceFrequency.NONE,
        recurringSeriesId: String? = null
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                MoneyTransaction(
                    personId = personId,
                    amount = amount,
                    type = type,
                    note = note,
                    date = date,
                    dueDate = dueDate,
                    promisedPaymentDate = promisedPaymentDate,
                    recurrenceFrequency = recurrenceFrequency,
                    recurringSeriesId = recurringSeriesId ?: if (recurrenceFrequency != RecurrenceFrequency.NONE) UUID.randomUUID().toString() else null
                )
            )
            triggerSync()
            refreshReminders()
        }
    }

    private fun triggerSync() {
        // Sync disabled for now
    }

    private suspend fun refreshReminders() {
        workManager.cancelAllWorkByTag(REMINDER_WORK_TAG)

        if (!settingsManager.remindersEnabled.first()) {
            return
        }

        val people = repository.allPeople.first()
        val transactions = repository.allTransactions.first()
        val now = System.currentTimeMillis()

        people.forEach { person ->
            val personTransactions = transactions.filter { it.personId == person.personId }
            val balance = personTransactions.sumOf {
                if (it.type == TransactionType.GIVEN) it.amount else -it.amount
            }
            val dueDate = personTransactions
                .filter { it.type == TransactionType.GIVEN }
                .mapNotNull { it.dueDate }
                .minOrNull()

            if (balance > 0 && dueDate != null && dueDate > now) {
                scheduleReminder(person, balance, dueDate)
            }
        }
    }

    private fun scheduleReminder(person: Person, amount: Double, dueDate: Long) {
        val delay = dueDate - System.currentTimeMillis()
        if (delay <= 0) {
            return
        }

        val data = workDataOf(
            "personId" to person.personId,
            "personName" to person.name,
            "amount" to amount,
            "type" to TransactionType.GIVEN.ordinal
        )

        val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(REMINDER_WORK_TAG)
            .build()

        workManager.enqueueUniqueWork(
            reminderWorkName(person.personId),
            ExistingWorkPolicy.REPLACE,
            reminderRequest
        )
    }

    fun sendManualReminder(
        context: Context,
        person: Person,
        balance: Double,
        tone: ReminderTone = ReminderTone.GENTLE,
        dueDate: Long? = null
    ) {
        val amountText = MoneyFormatter.format(balance, absolute = true)
        val dueDateText = dueDate?.let {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
        }
        val message = when {
            balance > 0 -> buildReceivableReminder(context, person.name, amountText, tone, dueDateText)
            balance < 0 -> context.getString(R.string.reminder_template_payable, person.name, amountText)
            else -> return
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
            val chooser = Intent.createChooser(genericIntent, context.getString(R.string.person_detail_send_chooser_title))
            // If context is not an Activity, we need NEW_TASK flag for the chooser as well
            if (context !is android.app.Activity) {
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(chooser)
            } catch (ex: Exception) {
                Toast.makeText(context, context.getString(R.string.person_detail_no_app_found), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteTransaction(transaction: MoneyTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            triggerSync()
            refreshReminders()
        }
    }

    fun updateTransaction(transaction: MoneyTransaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
            triggerSync()
            refreshReminders()
        }
    }

    fun createNextRecurringTransaction(transaction: MoneyTransaction) {
        if (transaction.recurrenceFrequency == RecurrenceFrequency.NONE) return

        viewModelScope.launch {
            val seriesId = transaction.recurringSeriesId ?: UUID.randomUUID().toString()
            val seriesTransactions = repository.allTransactions.first()
                .filter { it.recurringSeriesId == seriesId }
            val latestTransaction = (seriesTransactions + transaction).maxByOrNull { it.date } ?: transaction
            val nextDate = calculateNextRecurringDate(
                fromDate = latestTransaction.date,
                frequency = latestTransaction.recurrenceFrequency
            ) ?: return@launch

            val duplicateExists = seriesTransactions.any { it.date == nextDate }
            if (duplicateExists) return@launch

            val dueOffset = latestTransaction.dueDate?.let { it - latestTransaction.date }
            val promisedOffset = latestTransaction.promisedPaymentDate?.let { it - latestTransaction.date }

            repository.insertTransaction(
                latestTransaction.copy(
                    transactionId = 0,
                    date = nextDate,
                    dueDate = dueOffset?.let { nextDate + it },
                    promisedPaymentDate = promisedOffset?.let { nextDate + it },
                    createdAt = System.currentTimeMillis(),
                    recurringSeriesId = seriesId
                )
            )

            triggerSync()
            refreshReminders()
        }
    }

    suspend fun getPersonById(personId: Long): Person? = repository.getPersonById(personId)

    suspend fun getTransactionById(transactionId: Long): MoneyTransaction? = repository.getTransactionById(transactionId)

    private fun buildReceivableReminder(
        context: Context,
        personName: String,
        amountText: String,
        tone: ReminderTone,
        dueDateText: String?
    ): String {
        val dueLine = dueDateText?.let { context.getString(R.string.reminder_due_date_line, it) } ?: ""
        val templateRes = when (tone) {
            ReminderTone.GENTLE -> R.string.reminder_template_gentle
            ReminderTone.DIRECT -> R.string.reminder_template_direct
            ReminderTone.URGENT -> R.string.reminder_template_urgent
        }
        return context.getString(templateRes, personName, amountText, dueLine)
    }

    companion object {
        private const val REMINDER_WORK_TAG = "reminder_work"

        private fun reminderWorkName(personId: Long): String = "reminder_person_$personId"
    }

    private fun calculateNextRecurringDate(
        fromDate: Long,
        frequency: RecurrenceFrequency
    ): Long? {
        val calendar = Calendar.getInstance().apply { timeInMillis = fromDate }
        when (frequency) {
            RecurrenceFrequency.NONE -> return null
            RecurrenceFrequency.WEEKLY -> calendar.add(Calendar.DAY_OF_YEAR, 7)
            RecurrenceFrequency.MONTHLY -> calendar.add(Calendar.MONTH, 1)
        }
        return calendar.timeInMillis
    }
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
