@ECHO OFF
SETLOCAL
SET "JAVA_EXE=%~dp0\jre\bin\java.exe"
SET "APP_JAR=%~dp0\application.jar"
CALL %JAVA_EXE% -jar %APP_JAR% %*
EXIT /B %ERRORLEVEL%
