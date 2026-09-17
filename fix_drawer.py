import sys

with open("app/src/main/java/com/example/ui/MainLayout.kt", "r") as f:
    lines = f.readlines()

new_lines = []
skip = False
for i, line in enumerate(lines):
    if "androidx.compose.material3.NavigationDrawerItem(" in line and "Divider(color = PrimaryCyan.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))" in lines[i+1]:
        # This is the line we want to delete
        pass
    elif "Divider(color = PrimaryCyan.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))" in line and "label = { Text(if (lang == \"ar\") \"✨ أدوات وميزات إضافية\"" in lines[i+1]:
        new_lines.append(line)
        new_lines.append("                    androidx.compose.material3.NavigationDrawerItem(\n")
    else:
        new_lines.append(line)

with open("app/src/main/java/com/example/ui/MainLayout.kt", "w") as f:
    f.writelines(new_lines)
