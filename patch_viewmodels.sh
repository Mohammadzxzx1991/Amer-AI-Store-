#!/bin/bash
# Insert at end of init block (find "simulateNewOrderIncoming()", insert after "startAITrendAnalysisLoop()")
sed -i '/startAITrendAnalysisLoop()/a\
        \
        val savedUserId = sharedPreferences.getInt("remembered_user_id", -1)\
        if (savedUserId != -1) {\
            viewModelScope.launch {\
                val user = userDao.getUserById(savedUserId).firstOrNull()\
                if (user != null) {\
                    _currentUser.value = user\
                    _currentRole.value = user.role\
                    _onboardingCompleted.value = user.permissionGranted\
                }\
            }\
        }' app/src/main/java/com/example/ui/ViewModels.kt

# Insert rememberMe for admin
sed -i '/updateSavedProducts(adminUser.id)/a\
                _onboardingCompleted.value = adminUser.permissionGranted\
                if (rememberMe) {\
                    sharedPreferences.edit().putInt("remembered_user_id", adminUser.id).apply()\
                } else {\
                    sharedPreferences.edit().remove("remembered_user_id").apply()\
                }' app/src/main/java/com/example/ui/ViewModels.kt

# Insert rememberMe for foundUser (both cases)
sed -i '/updateSavedProducts(foundUser.id)/a\
                            _onboardingCompleted.value = foundUser.permissionGranted\
                            if (rememberMe) {\
                                sharedPreferences.edit().putInt("remembered_user_id", foundUser.id).apply()\
                            } else {\
                                sharedPreferences.edit().remove("remembered_user_id").apply()\
                            }' app/src/main/java/com/example/ui/ViewModels.kt
