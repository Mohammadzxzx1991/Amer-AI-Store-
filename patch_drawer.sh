#!/bin/bash
sed -i '/✨ Extra Tools & Features/i\
                    Divider(color = PrimaryCyan.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))\
                    Text(\
                        text = if (lang == "ar") "الصلاحيات (الأدوار)" else "Permissions (Roles)",\
                        fontSize = 14.sp,\
                        fontWeight = FontWeight.Bold,\
                        color = PrimaryCyan,\
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)\
                    )\
                    roles.forEach { r ->\
                        val isSelected = currentRole == r\
                        androidx.compose.material3.NavigationDrawerItem(\
                            label = {\
                                Text(\
                                    text = when (r) {\
                                        "Customer" -> txt("role_customer")\
                                        "Merchant" -> txt("role_merchant")\
                                        "Delivery" -> txt("role_delivery")\
                                        "Admin" -> txt("role_admin")\
                                        else -> r\
                                    },\
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else PolarLight\
                                )\
                            },\
                            selected = isSelected,\
                            onClick = {\
                                coroutineScope.launch { drawerState.close() }\
                                viewModel.switchRole(r)\
                            },\
                            colors = androidx.compose.material3.NavigationDrawerItemDefaults.colors(\
                                unselectedContainerColor = Color.Transparent,\
                                selectedContainerColor = PrimaryCyan\
                            )\
                        )\
                    }\
                    Divider(color = PrimaryCyan.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))' app/src/main/java/com/example/ui/MainLayout.kt
