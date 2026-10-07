@echo off
setlocal EnableExtensions EnableDelayedExpansion

rem Build TimeDial APK with the Gradle Wrapper. Android Studio is not required.
rem Needs JDK 17+ and an Android SDK (command-line tools or a local SDK copy).
rem Usage: build-apk.bat [debug|release]
rem        set NOPAUSE=1 to skip the final pause (CI / double-run from a tool).

cd /d "%~dp0"

set "VARIANT=debug"
if /I "%~1"=="release" set "VARIANT=release"
if /I "%~1"=="debug" set "VARIANT=debug"

echo.
echo === TimeDial APK build (%VARIANT%) - Android Studio not required ===
echo.

call :find_java
if errorlevel 1 goto :fail

call :find_sdk
if errorlevel 1 goto :fail

call :ensure_local_properties
if errorlevel 1 goto :fail

echo JAVA_HOME=%JAVA_HOME%
echo Android SDK=%ANDROID_SDK_DIR%
echo.

if not exist "gradlew.bat" (
  echo [error] gradlew.bat not found. Run this script from the repo root.
  goto :fail
)

if /I "%VARIANT%"=="release" (
  set "GRADLE_TASK=assembleRelease"
  set "APK_SRC=app\build\outputs\apk\release\app-release.apk"
  set "APK_DST=dist\TimeDial-release.apk"
) else (
  set "GRADLE_TASK=assembleDebug"
  set "APK_SRC=app\build\outputs\apk\debug\app-debug.apk"
  set "APK_DST=dist\TimeDial-debug.apk"
)

echo Running: gradlew.bat %GRADLE_TASK%
echo.
call gradlew.bat --no-daemon %GRADLE_TASK%
if errorlevel 1 (
  echo.
  echo [error] Gradle failed to build the APK.
  goto :fail
)

if not exist "%APK_SRC%" (
  echo [error] Build finished but APK is missing: %APK_SRC%
  goto :fail
)

if not exist "dist" mkdir "dist"
copy /Y "%APK_SRC%" "%APK_DST%" >nul
if errorlevel 1 (
  echo [error] Could not copy APK to dist\
  goto :fail
)

echo.
echo Done.
echo APK: %CD%\%APK_DST%
echo Gradle output: %CD%\%APK_SRC%
echo.
echo USB install: adb install -r "%CD%\%APK_DST%"
echo.

explorer /select,"%CD%\%APK_DST%"
goto :end_ok

:fail
echo.
echo Need JDK 17+ and an Android SDK.
echo JDK: https://adoptium.net/temurin/releases/?version=17
echo SDK: https://developer.android.com/studio#command-line-tools-only
echo Then set JAVA_HOME and ANDROID_HOME (or ANDROID_SDK_ROOT).
if /I not "%NOPAUSE%"=="1" pause
exit /b 1

:end_ok
if /I not "%NOPAUSE%"=="1" pause
exit /b 0

:find_java
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" goto :java_ok

for %%P in (
  "%ProgramFiles%\Android\Android Studio\jbr"
  "%ProgramFiles%\Android\Android Studio1\jbr"
  "%LOCALAPPDATA%\Programs\Android Studio\jbr"
) do (
  if exist "%%~P\bin\java.exe" (
    set "JAVA_HOME=%%~P"
    goto :java_ok
  )
)

for /d %%J in ("%ProgramFiles%\Eclipse Adoptium\jdk-17*") do (
  if exist "%%~J\bin\java.exe" (
    set "JAVA_HOME=%%~J"
    goto :java_ok
  )
)
for /d %%J in ("%ProgramFiles%\Java\jdk-17*") do (
  if exist "%%~J\bin\java.exe" (
    set "JAVA_HOME=%%~J"
    goto :java_ok
  )
)
for /d %%J in ("%ProgramFiles%\Microsoft\jdk-17*") do (
  if exist "%%~J\bin\java.exe" (
    set "JAVA_HOME=%%~J"
    goto :java_ok
  )
)

where java >nul 2>&1
if not errorlevel 1 (
  echo [warn] JAVA_HOME is not set; using java from PATH.
  goto :eof
)

echo [error] JDK 17+ not found. Install Temurin 17 and set JAVA_HOME.
exit /b 1

:java_ok
echo Found JDK: %JAVA_HOME%
exit /b 0

:find_sdk
if defined ANDROID_HOME if exist "%ANDROID_HOME%\platform-tools" (
  set "ANDROID_SDK_DIR=%ANDROID_HOME%"
  goto :sdk_ok
)
if defined ANDROID_SDK_ROOT if exist "%ANDROID_SDK_ROOT%\platform-tools" (
  set "ANDROID_SDK_DIR=%ANDROID_SDK_ROOT%"
  goto :sdk_ok
)

if exist "local.properties" (
  for /f "usebackq tokens=1,* delims==" %%A in ("local.properties") do (
    if /I "%%A"=="sdk.dir" set "RAW=%%B"
  )
  if defined RAW (
    set "RAW=!RAW:\\=\!"
    set "RAW=!RAW:\:=:!"
    set "RAW=!RAW:/=\!"
    if exist "!RAW!\platform-tools" (
      set "ANDROID_SDK_DIR=!RAW!"
      goto :sdk_ok
    )
  )
)

for %%S in (
  "%LOCALAPPDATA%\Android\Sdk"
  "%USERPROFILE%\AppData\Local\Android\Sdk"
  "C:\Android\Sdk"
  "D:\Android\Sdk"
) do (
  if exist "%%~S\platform-tools" (
    set "ANDROID_SDK_DIR=%%~S"
    goto :sdk_ok
  )
)

echo [error] Android SDK not found.
echo Install Command line tools and set ANDROID_HOME, for example:
echo   setx ANDROID_HOME "%LOCALAPPDATA%\Android\Sdk"
exit /b 1

:sdk_ok
set "ANDROID_HOME=%ANDROID_SDK_DIR%"
set "ANDROID_SDK_ROOT=%ANDROID_SDK_DIR%"
echo Found Android SDK: %ANDROID_SDK_DIR%
exit /b 0

:ensure_local_properties
if exist "local.properties" goto :eof

set "SDK_PROP=%ANDROID_SDK_DIR:\=/%"
> "local.properties" echo sdk.dir=%SDK_PROP%
echo Created local.properties with sdk.dir
exit /b 0
