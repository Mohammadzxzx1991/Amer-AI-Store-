with open("app/src/main/java/com/example/ui/MainLayout.kt", "r") as f:
    lines = f.readlines()

in_customer_screen = False
for i, line in enumerate(lines):
    if "fun CustomerScreen(" in line:
        in_customer_screen = True
    if in_customer_screen and "LazyColumn(" in line:
        print(f"LazyColumn at {i}: {line.strip()}")
        break
