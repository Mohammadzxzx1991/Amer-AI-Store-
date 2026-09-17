import re

with open("app/src/main/java/com/example/ui/MainLayout.kt", "r") as f:
    content = f.read()

# 1. Inject Row and Sidebar before LazyColumn
old_lazy_column_start = """                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {"""

new_lazy_column_start = """                    Row(modifier = Modifier.fillMaxSize()) {
                        val categories = listOf("All", "Organic Foods", "Spiced Coffee & Tea", "Dairy", "Health Drinks", "Snacks", "Men's Clothing", "Women's Clothing", "Shoes & Footwear", "Perfumes & Fragrances", "Electronics & Mobiles", "Furniture & Decor", "Pharmacy & Medical", "Tobacco & Vape", "Other Shops")
                        val categoriesAr = mapOf("All" to "الكل 🛍️", "Organic Foods" to "أغذية عضوية 🍯", "Spiced Coffee & Tea" to "قهوة وبن ☕", "Dairy" to "منتجات ألبان 🥛", "Health Drinks" to "طاقة وصحة ⚡", "Snacks" to "تسالي خفيفة 🍪", "Men's Clothing" to "ملابس رجالية 👔", "Women's Clothing" to "ملابس نسائية 👗", "Shoes & Footwear" to "أحذية 👟", "Perfumes & Fragrances" to "عطور وبخور ✨", "Electronics & Mobiles" to "جوالات وإلكترونيات 📱", "Furniture & Decor" to "أثاث وديكور 🛋️", "Pharmacy & Medical" to "صيدليات وطب 💊", "Tobacco & Vape" to "تدخين ومراكز 🚬", "Other Shops" to "محلات أخرى 🏪")
                        val categoriesEn = mapOf("All" to "All 🛍️", "Organic Foods" to "Organic 🍯", "Spiced Coffee & Tea" to "Coffee & Tea ☕", "Dairy" to "Dairy 🥛", "Health Drinks" to "Health ⚡", "Snacks" to "Snacks 🍪", "Men's Clothing" to "Men's Wear 👔", "Women's Clothing" to "Women's Wear 👗", "Shoes & Footwear" to "Footwear 👟", "Perfumes & Fragrances" to "Perfumes ✨", "Electronics & Mobiles" to "Electronics 📱", "Furniture & Decor" to "Decor 🛋️", "Pharmacy & Medical" to "Pharmacy 💊", "Tobacco & Vape" to "Tobacco & Vape 🚬", "Other Shops" to "Other Shops 🏪")

                        // Category Sidebar
                        Column(
                            modifier = Modifier
                                .width(85.dp)
                                .fillMaxHeight()
                                .background(CardDarkBg.copy(alpha = 0.3f))
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 16.dp, horizontal = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                val displayText = if (lang == "ar") categoriesAr[cat] ?: cat else categoriesEn[cat] ?: cat
                                val catBgColor = if (isSelected) PrimaryCyan else Color.Transparent
                                val catTextColor = if (isSelected) SlateDarkBg else PolarLight
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(catBgColor)
                                        .clickable { selectedCategory = cat }
                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.material3.Text(
                                        text = displayText,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = catTextColor,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Main Content
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxHeight().padding(start = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {"""

content = content.replace(old_lazy_column_start, new_lazy_column_start, 1)

# 2. Close the Row at the end of the LazyColumn block for tab 0
# Actually we can find where tab 1 starts, and insert the closing brace just before it.
# We'll use regex to find where tab 0's LazyColumn ends and tab 1 starts.
tab1_start = "                1 -> {\n                    // SAVED TAB VIEW"
if tab1_start in content:
    content = content.replace(tab1_start, "                    }\n                1 -> {\n                    // SAVED TAB VIEW", 1)

with open("app/src/main/java/com/example/ui/MainLayout.kt", "w") as f:
    f.write(content)
