#!/bin/bash
adb shell monkey -p com.zhuji.note --throttle 200 --ignore-crashes --ignore-timeouts --ignore-security-exceptions -v 5000 2>&1 | tee docs/reports/monkey-output.txt
echo "Monkey test done. See docs/reports/monkey-output.txt"
