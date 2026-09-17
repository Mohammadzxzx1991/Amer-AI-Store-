with open("app/src/main/java/com/example/ui/MainLayout.kt", "r") as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "enabled = (selectedLocalInterests.size >= 2)," in line:
        # Let's see the context
        idx = i - 1
        while "Button(" not in lines[idx]:
            idx -= 1
        # Button starts at idx.
        # We need to insert the skip button BEFORE idx!
        
        # Wait, the skip button is currently lines i-12 to i-1.
        # Let's just fix it by replacing the whole block.
        break

# Actually it's easier to just use sed to replace the messed up block.
