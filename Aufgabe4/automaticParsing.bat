@ECHO off
IF %1!==! GOTO REQUEST
SET filePath=%1
GOTO EXECUTE

:REQUEST
SET /p filePath=Enter relative folder path:

CLS

:EXECUTE
IF NOT exist %filePath% echo "folder path does not exists!" & GOTO goEnd

IF exist results.txt del results.txt
IF exist final.dat del final.dat

FOR /f %%f IN ('dir /b "%cd%\%filePath%\"') DO (echo %%f & echo|set /p dummy=%%f >> results.txt && (FOR /F "tokens=2 delims=':'" %%i in ('findstr /r /c:"  CPU seconds to compute hull \(after input\): " %filePath%\%%f') do (echo _%%i >> results.txt)) || echo -1 >> results.txt)
::FOR /f %%f IN ('dir /b "%cd%\%filePath%\"') DO (echo|set /p dummy=%%f >> results.txt && (FOR /F "tokens=2 delims=':'" %%i in ('findstr /r /c:"  CPU seconds to compute hull \(after input\): " %filePath%\%%f') do (echo _%%i >> results.txt)) & echo. >> results.txt)

FOR /F "tokens=2-4 delims='_'" %%i in (results.txt) do echo %%i %%j %%k >> final.dat
	
:goEnd
	
	
ECHO.
ECHO Press any key to exit!
PAUSE > NUL

@echo off