with open("app/src/main/java/com/example/ui/MainLayout.kt", "r") as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "3 -> {" in line and "TrendingFeedScreen" in lines[i+1]:
        # found the next tab!
        # Insert "                    }\n" before it, matching the "Row" open level.
        # But wait, where is the end of tab 0? It should be the '}' just before this.
        lines.insert(i-1, "                    }\n")
        break

with open("app/src/main/java/com/example/ui/MainLayout.kt", "w") as f:
    f.writelines(lines)
