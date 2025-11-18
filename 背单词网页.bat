@echo off
setlocal

REM 切到bat所在目录
cd /d "%~dp0"

REM 先做存在性检查，避免路径写错
if not exist ".\Word\target\Word-0.0.1-SNAPSHOT.jar" (
  echo 未找到 .\Word\target\Word-0.0.1-SNAPSHOT.jar
  echo 请先在 Word 目录下打包：mvnw clean package
  goto :END
)
if not exist ".\FrontEnd\wordbook-frontend\package.json" (
  echo 未找到 .\FrontEnd\wordbook-frontend\package.json
  echo 请确认前端目录名/位置正确
  goto :END
)

REM 启动后端
start "Backend" cmd /k "cd /d .\Word\target && java -jar Word-0.0.1-SNAPSHOT.jar"

REM 启动前端
start "Frontend" cmd /k "cd /d .\FrontEnd\wordbook-frontend && npm run dev"

REM 等5秒后打开页面
timeout /t 5 >nul
start "" http://localhost:5173

:END
endlocal
