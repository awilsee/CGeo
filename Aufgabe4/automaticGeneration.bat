@ECHO off

IF exist data_%loopIncrement% rmdir data_%loopIncrement% /s /q
mkdir data_%loopIncrement% 

SET /a loopDim=2

SET /a loopIncrement=2
SET /a loopEnd=10000000

:goLoopDim
SET /a loopQuantitiy=100

:goLoopQuantity
echo "rbox %loopQuantitiy% D%loopDim%" & qhull-2015.2\bin\rbox.exe %loopQuantitiy% D%loopDim% > data_%loopIncrement%\file_%loopQuantitiy%_%loopDim%
IF %loopQuantitiy% GEQ %loopEnd% GOTO goLoopQuantityEnds
::SET /a loopQuantitiy=%loopQuantitiy%+(%loopQuantitiy%/%loopIncrement%)
SET /a loopQuantitiy=%loopQuantitiy%*%loopIncrement%
GOTO goLoopQuantity

:goLoopQuantityEnds
IF %loopDim% GEQ 8 GOTO goEnd
SET /a loopDim=%loopDim%+1
GOTO goLoopDim

:goEnd

ECHO.
ECHO Press any key to exit!
PAUSE > NUL