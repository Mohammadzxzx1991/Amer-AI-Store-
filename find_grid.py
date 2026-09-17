with open("app/src/main/java/com/example/ui/MainLayout.kt", "r") as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "LazyVerticalGrid" in line or "LazyVerticalStaggeredGrid" in line:
        print(f"{i}: {line.strip()}")
