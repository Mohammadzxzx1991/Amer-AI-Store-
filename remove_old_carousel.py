with open("app/src/main/java/com/example/ui/MainLayout.kt", "r") as f:
    lines = f.readlines()

out_lines = []
skip = False
for line in lines:
    if "// Modern Category Horizontal Carousel Row" in line:
        skip = True
    elif "// Modern Price Range Filter component" in line:
        skip = False
    
    if not skip:
        out_lines.append(line)

with open("app/src/main/java/com/example/ui/MainLayout.kt", "w") as f:
    f.writelines(out_lines)
