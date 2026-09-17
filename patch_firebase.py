import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

replacement = """        try {
            var app = FirebaseApp.initializeApp(this)
            if (app == null) {
                val options = com.google.firebase.FirebaseOptions.Builder()
                    .setApplicationId("1:123456789012:android:abcdef123456")
                    .setProjectId("dummy-project")
                    .setApiKey("AIzaSyDummyKeyForSandboxTesting1234")
                    .build()
                FirebaseApp.initializeApp(this, options)
            }
            initializeAppFeatures()
        } catch (e: Exception) {
            try {
                val options = com.google.firebase.FirebaseOptions.Builder()
                    .setApplicationId("1:123456789012:android:abcdef123456")
                    .setProjectId("dummy-project")
                    .setApiKey("AIzaSyDummyKeyForSandboxTesting1234")
                    .build()
                FirebaseApp.initializeApp(this, options)
                initializeAppFeatures()
            } catch (e2: Exception) {
                android.util.Log.e("MainActivity", "Firebase initialization deferred or sandboxed: ${e2.message}")
            }
        }"""

# Find the try catch block inside onCreate
content = re.sub(r"        try \{\s*FirebaseApp\.initializeApp\(this\)\s*initializeAppFeatures\(\)\s*\} catch \(e: Exception\) \{\s*android\.util\.Log\.e\(\"MainActivity\", \"Firebase initialization deferred or sandboxed: \$\{e\.message\}\"\)\s*\}", replacement, content, count=1)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
