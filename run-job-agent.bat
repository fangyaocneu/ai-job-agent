@echo off

cd /d C:\Users\chang.fan\Documents\ai-agent

if not exist logs mkdir logs

for /f %%i in ('powershell -NoProfile -Command "Get-Date -Format yyyy-MM-dd"') do set TODAY=%%i

echo ===== Job Agent Started %date% %time% ===== >> logs\job-agent-%TODAY%.log

"C:\Tools\apache-maven-3.9.16\bin\mvn.cmd" exec:java "-Dexec.mainClass=com.fangyao.agent.DailyJobRunner" >> logs\job-agent-%TODAY%.log 2>&1

echo ===== Job Agent Finished %date% %time% ===== >> logs\job-agent-%TODAY%.log
echo. >> logs\job-agent-%TODAY%.log