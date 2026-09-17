sed -i 's/private val storedOrganicItemDao: StoredOrganicItemDao/private val storedOrganicItemDao: StoredOrganicItemDao,\n    private val sharedPreferences: android.content.SharedPreferences/g' app/src/main/java/com/example/ui/ViewModelFactory.kt
sed -i 's/storedOrganicItemDao/storedOrganicItemDao,\n                sharedPreferences/g' app/src/main/java/com/example/ui/ViewModelFactory.kt
