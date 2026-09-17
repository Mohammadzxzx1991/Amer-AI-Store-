import re

with open("app/src/main/java/com/example/ui/MainLayout.kt", "r") as f:
    content = f.read()

bad_block = """                            val finalInterests = selectedLocalInterests + aiExtractedInterests
                            viewModel.completeOnboarding(finalInterests)
                        },
                    Button(
                        onClick = {
                            viewModel.completeOnboarding(emptySet())
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryMint),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (lang == "ar") "تخطي بدون اختيار" else "Skip Without Selection",
                            color = SlateDarkBg,
                            fontWeight = FontWeight.Bold
                        )
                    }
                        enabled = (selectedLocalInterests.size >= 2),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (lang == "ar") "تأكيد ودخول المتجر" else "Confirm & Enter Marketplace",
                            color = SlateDarkBg,
                            fontWeight = FontWeight.Bold
                        )
                    }"""

good_block = """                            val finalInterests = selectedLocalInterests + aiExtractedInterests
                            viewModel.completeOnboarding(finalInterests)
                        },
                        enabled = (selectedLocalInterests.size >= 2),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (lang == "ar") "تأكيد ودخول المتجر" else "Confirm & Enter Marketplace",
                            color = SlateDarkBg,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.completeOnboarding(emptySet())
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryMint),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (lang == "ar") "تخطي بدون اختيار" else "Skip Without Selection",
                            color = SlateDarkBg,
                            fontWeight = FontWeight.Bold
                        )
                    }"""

content = content.replace(bad_block, good_block)

with open("app/src/main/java/com/example/ui/MainLayout.kt", "w") as f:
    f.write(content)

