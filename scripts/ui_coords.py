import re, sys
path = sys.argv[1] if len(sys.argv) > 1 else r"C:\Users\l\AppData\Local\Temp\ui.xml"
data = open(path, "rb").read().decode("utf-8", "ignore")
pat = re.compile(r'(?:text|content-desc)="([^"]+)"[^>]*?bounds="(\[[0-9,\]\[]+)"')
for m in pat.finditer(data):
    label = m.group(1).strip()
    if not label:
        continue
    b = re.findall(r"\d+", m.group(2))
    if len(b) == 4:
        cx = (int(b[0]) + int(b[2])) // 2
        cy = (int(b[1]) + int(b[3])) // 2
        print(f"{label[:30]:32} tap {cx} {cy}")
