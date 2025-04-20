@echo off
echo =====================================
echo Copying resource files to output dir...
echo =====================================
xcopy /s /i /y resources out
echo.
echo ===================
echo Compiling Java Code
echo ===================
javac ^
  --module-path "C:\Users\mouli\OneDrive\Documents\OOAD_Project\javafx-sdk-21.0.6\lib" ^
  --add-modules javafx.controls,javafx.fxml ^
  -cp "lib\mysql-connector-j-9.2.0.jar;lib\javax.mail.jar;lib\activation.jar" ^
  -d out ^
  src\MainApp.java src\controller\*.java src\dao\*.java src\model\*.java src\db\*.java src\utils\*.java src\observer\*.java src\service\*.java
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ==============================
    echo Compilation failed. Exiting...
    echo ==============================
    pause
    exit /b %ERRORLEVEL%
)
echo.
echo ======================
echo Running JavaFX Program
echo ======================
java ^
  --module-path "C:\Users\mouli\OneDrive\Documents\OOAD_Project\javafx-sdk-21.0.6\lib" ^
  --add-modules javafx.controls,javafx.fxml ^
  -cp "out;lib/mysql-connector-j-9.2.0.jar;lib/javax.mail.jar;lib/activation.jar" ^
  MainApp
echo.
pause