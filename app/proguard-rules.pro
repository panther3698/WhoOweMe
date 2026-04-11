# Keep Room classes
-keep class com.example.whoowesme.database.** { *; }
-keep class com.example.whoowesme.model.** { *; }
-keep interface com.example.whoowesme.database.dao.** { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

# Keep iText classes (for PDF generation)
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**
-dontwarn org.slf4j.**
-dontwarn org.bouncycastle.**

# Keep Lifecycle and ViewModel
-keep class androidx.lifecycle.** { *; }
-keep class * extends androidx.lifecycle.ViewModel
-keep class * extends androidx.lifecycle.AndroidViewModel

# Keep Compose Semantics (sometimes needed for R8)
-keep class androidx.compose.ui.platform.** { *; }

# Keep Biometric classes
-keep class androidx.biometric.** { *; }

# Keep WorkManager internal database
-keep class androidx.work.impl.WorkDatabase_Impl { *; }
-keep class androidx.work.impl.background.systemjob.SystemJobService { *; }
-dontwarn androidx.work.impl.**
