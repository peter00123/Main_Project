
import qrcode
import subprocess
import os
import sys
from http.server import SimpleHTTPRequestHandler
from socketserver import TCPServer


# package com.example.practice
# object WebURL {var message: String = " http://10.186.154.95:8080/api/test"}

def getpot():
    # Get user input for the PowerShell command
    command = '(ipconfig | findstr "IPv4") -split ":" | Select-Object -Last 1 | ForEach-Object { $_.Trim() }'

    # Run the PowerShell command
    process = subprocess.run(["powershell", "-Command", command], capture_output=True, text=True)

    # Print the output
    print("=== Output ===")
    print(process.stdout)

    # Print any errors (if any)
    if process.stderr:
        print("=== Errors ===")
        print(process.stderr)
        
    url = process.stdout.strip()
    return url
    


mode = str(input("Enter the mode: L OR W "))

if mode == "L":
    url = getpot()
    url="package com.example.practice"+"\n"+"object WebURL {var message: String = \"http://"+url+":8080/api/test\"}"
    
elif mode == "W":
    url = "https://main-project-cdol.onrender.com/api/test"







with open("WebURL.kt", "w") as file:
        
  #      file.write(a+"\n"+b+url+c)
        file.write(url)



print("updated: " + url)