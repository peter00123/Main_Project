@rem =============================================================================
@rem FILE: gradlew.bat
@rem =============================================================================
@rem OBJECTIVE:
@rem   Windows batch script equivalent of gradlew for Windows developers.
@rem   Usage:  gradlew.bat assembleDebug
@rem =============================================================================
@if "%DEBUG%" == "" @echo off
@rem Set local scope
setlocal
set DIRNAME=%~dp0
set APP_BASE_NAME=%~n0
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome
set JAVA_EXE=java.exe
goto execute

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

:execute
set CLASSPATH=%DIRNAME%\gradle\wrapper\gradle-wrapper.jar
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
endlocal
