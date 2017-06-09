@ECHO off
IF %1!==! GOTO REQUEST
SET filePath=%1
GOTO EXECUTE

:REQUEST
SET /p filePath=Enter relative folder path:

CLS

:EXECUTE
IF exist %filePath%_results rmdir %filePath%_results /s /q
mkdir %filePath%_results

IF NOT exist %filePath% echo "folder path does not exists!" & GOTO goEnd

FOR /f %%f IN ('dir /b "%cd%\%filePath%\"') do ^
echo %%f & type "%filePath%\%%f" | qhull-2015.2\bin\qconvex TO "%filePath%_results\%%f"

:goEnd

ECHO.
ECHO Press any key to exit!
PAUSE > NUL