with open("app/src/main/java/com/example/ui/MainLayout.kt", "r") as f:
    lines = f.readlines()

start_idx = -1
end_idx = -1
for i, line in enumerate(lines):
    if "// Modern Category Horizontal Carousel Row" in line:
        start_idx = i
    if start_idx != -1 and "// Modern Price Range Filter component" in line:
        end_idx = i
        break

print(f"{start_idx},{end_idx}")
