#!/bin/bash
sed -i '/enabled = (selectedLocalInterests.size >= 2),/i\
                    Button(\
                        onClick = {\
                            viewModel.completeOnboarding(emptySet())\
                        },\
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),\
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryMint),\
                        shape = RoundedCornerShape(12.dp)\
                    ) {\
                        Text(\
                            text = if (lang == "ar") "تخطي بدون اختيار" else "Skip Without Selection",\
                            color = SlateDarkBg,\
                            fontWeight = FontWeight.Bold\
                        )\
                    }' app/src/main/java/com/example/ui/MainLayout.kt
