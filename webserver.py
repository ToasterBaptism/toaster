#!/usr/bin/env python3
"""
Simple web server to host the Nexus Controller Hub APK and documentation
"""

import os
import http.server
import socketserver
from urllib.parse import unquote
import mimetypes
import shutil
from datetime import datetime

class CustomHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory="/workspace/toaster", **kwargs)
    
    def do_GET(self):
        # Add CORS headers
        self.send_response(200)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', '*')
        
        # Handle specific routes
        if self.path == '/':
            self.send_header('Content-type', 'text/html')
            self.end_headers()
            self.serve_index()
            return
        elif self.path == '/download/latest':
            # Redirect to the latest APK v2
            self.send_response(302)
            self.send_header('Location', '/android/nexus-controller-hub-v2.apk')
            self.end_headers()
            return
        elif self.path.endswith('.apk'):
            # Set proper headers for APK files
            self.send_header('Content-type', 'application/vnd.android.package-archive')
            self.send_header('Content-Disposition', f'attachment; filename="{os.path.basename(self.path)}"')
            self.end_headers()
            self.serve_file()
            return
        else:
            # Default handling
            super().do_GET()
    
    def serve_index(self):
        """Serve the main index page"""
        html_content = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>🎮 Nexus Controller Hub - Download Center</title>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #333;
            min-height: 100vh;
        }
        .container {
            background: white;
            border-radius: 15px;
            padding: 30px;
            box-shadow: 0 20px 40px rgba(0,0,0,0.1);
        }
        .header {
            text-align: center;
            margin-bottom: 40px;
        }
        .header h1 {
            color: #2c3e50;
            margin-bottom: 10px;
            font-size: 2.5em;
        }
        .header p {
            color: #7f8c8d;
            font-size: 1.2em;
        }
        .download-section {
            background: #f8f9fa;
            border-radius: 10px;
            padding: 25px;
            margin: 20px 0;
            border-left: 5px solid #3498db;
        }
        .download-button {
            display: inline-block;
            background: linear-gradient(45deg, #3498db, #2980b9);
            color: white;
            padding: 15px 30px;
            text-decoration: none;
            border-radius: 8px;
            font-weight: bold;
            font-size: 1.1em;
            margin: 10px 10px 10px 0;
            transition: transform 0.2s, box-shadow 0.2s;
        }
        .download-button:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(52, 152, 219, 0.4);
        }
        .download-button.secondary {
            background: linear-gradient(45deg, #95a5a6, #7f8c8d);
        }
        .feature-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 20px;
            margin: 30px 0;
        }
        .feature-card {
            background: #fff;
            border: 1px solid #e1e8ed;
            border-radius: 10px;
            padding: 20px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.05);
        }
        .feature-card h3 {
            color: #2c3e50;
            margin-bottom: 15px;
        }
        .status-badge {
            display: inline-block;
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 0.9em;
            font-weight: bold;
            margin-left: 10px;
        }
        .status-fixed {
            background: #d4edda;
            color: #155724;
        }
        .status-new {
            background: #cce5ff;
            color: #004085;
        }
        .file-info {
            background: #f1f3f4;
            padding: 15px;
            border-radius: 8px;
            margin: 15px 0;
            font-family: monospace;
            font-size: 0.9em;
        }
        .installation-steps {
            background: #fff3cd;
            border: 1px solid #ffeaa7;
            border-radius: 8px;
            padding: 20px;
            margin: 20px 0;
        }
        .installation-steps h3 {
            color: #856404;
            margin-top: 0;
        }
        .installation-steps ol {
            margin: 15px 0;
        }
        .installation-steps li {
            margin: 8px 0;
            line-height: 1.5;
        }
        .changelog {
            background: #e8f5e8;
            border-left: 4px solid #28a745;
            padding: 20px;
            margin: 20px 0;
        }
        .changelog h3 {
            color: #155724;
            margin-top: 0;
        }
        .changelog ul {
            margin: 10px 0;
        }
        .changelog li {
            margin: 5px 0;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🎮 Nexus Controller Hub</h1>
            <p>High-Performance Android Controller Remapping & Macro Tool</p>
            <p><strong>Version 1.0</strong> - Built """ + datetime.now().strftime("%Y-%m-%d %H:%M:%S") + """</p>
        </div>

        <div class="download-section">
            <h2>📱 Download Latest Version</h2>
            <p>Get the latest build with all the fixes and improvements:</p>
            
            <a href="/android/nexus-controller-hub-v2.apk" class="download-button">
                📥 Download Latest APK v2 (Real Controller System) - NEW!
            </a>
            
            <a href="/android/app/build/outputs/apk/debug/app-debug.apk" class="download-button secondary">
                📦 Download Previous APK v1
            </a>
            
            <div class="file-info">
                <strong>Latest Build Info:</strong><br>
                • File: app-debug.apk<br>
                • Size: ~15-20 MB<br>
                • Target: Android 7.0+ (API 24+)<br>
                • Architecture: Universal (ARM64, ARM, x86)<br>
                • Build Type: Debug (allows installation from unknown sources)
            </div>
        </div>

        <div class="installation-steps">
            <h3>📋 Installation Instructions</h3>
            <ol>
                <li><strong>Enable Unknown Sources:</strong> Go to Settings → Security → Enable "Install from Unknown Sources" or "Allow from this source" for your browser</li>
                <li><strong>Download APK:</strong> Click the download button above</li>
                <li><strong>Install:</strong> Open the downloaded APK file and tap "Install"</li>
                <li><strong>Grant Permissions:</strong> The app will request necessary permissions for controller access</li>
                <li><strong>Test Controller:</strong> Connect a controller and use the "Live Test Mode" to verify functionality</li>
            </ol>
            <p><strong>Note:</strong> This is a debug build, so Android may show security warnings. This is normal for development APKs.</p>
        </div>

        <div class="changelog">
            <h3>🔧 Latest Fixes & Features</h3>
            <ul>
                <li><span class="status-badge status-fixed">FIXED</span> Controller visual feedback now responds to real input</li>
                <li><span class="status-badge status-fixed">FIXED</span> Macro recording captures actual controller events</li>
                <li><span class="status-badge status-new">NEW</span> Live Test Mode with real-time input monitoring</li>
                <li><span class="status-badge status-new">NEW</span> Button remapping demonstration system</li>
                <li><span class="status-badge status-new">NEW</span> Input logging (raw vs processed events)</li>
                <li><span class="status-badge status-fixed">FIXED</span> Controller detection and connection status</li>
                <li><span class="status-badge status-new">NEW</span> Comprehensive testing interface</li>
            </ul>
        </div>

        <div class="feature-grid">
            <div class="feature-card">
                <h3>🎯 Real-Time Testing</h3>
                <p>Live Test Mode shows controller input in real-time with visual feedback, button highlighting, and analog stick/trigger monitoring.</p>
            </div>
            
            <div class="feature-card">
                <h3>🔄 Button Remapping</h3>
                <p>Remap any controller button to any other button or Android key event. Visual interface shows original vs remapped actions.</p>
            </div>
            
            <div class="feature-card">
                <h3>📹 Macro Recording</h3>
                <p>Record complex button sequences with precise timing. Playback macros with a single button press.</p>
            </div>
            
            <div class="feature-card">
                <h3>🎮 Multi-Controller Support</h3>
                <p>Supports Xbox, PlayStation, and generic controllers via USB or Bluetooth connection.</p>
            </div>
            
            <div class="feature-card">
                <h3>⚙️ Advanced Calibration</h3>
                <p>Fine-tune dead zones, sensitivity curves, and trigger actuation points for optimal performance.</p>
            </div>
            
            <div class="feature-card">
                <h3>💾 Offline-First</h3>
                <p>All data stored locally on your device. No internet connection or user accounts required.</p>
            </div>
        </div>

        <div class="download-section">
            <h2>📚 Documentation & Source</h2>
            <p>Access additional resources:</p>
            
            <a href="/CONTROLLER_TESTING_GUIDE.md" class="download-button secondary">
                📖 Testing Guide
            </a>
            
            <a href="/README.md" class="download-button secondary">
                📋 README
            </a>
            
            <a href="/android/" class="download-button secondary">
                💻 Browse Source Code
            </a>
        </div>

        <div class="feature-card" style="margin-top: 30px; text-align: center;">
            <h3>🚀 Quick Start</h3>
            <p>1. Download and install the APK<br>
            2. Connect your controller<br>
            3. Open the app and tap "Live Test Mode"<br>
            4. Press controller buttons to see real-time feedback<br>
            5. Use "Configure Controller" to set up remapping</p>
        </div>
    </div>
</body>
</html>
        """
        self.wfile.write(html_content.encode('utf-8'))
    
    def serve_file(self):
        """Serve a file with proper headers"""
        try:
            file_path = unquote(self.path[1:])  # Remove leading slash
            full_path = os.path.join("/workspace/toaster", file_path)
            
            if os.path.exists(full_path) and os.path.isfile(full_path):
                with open(full_path, 'rb') as f:
                    shutil.copyfileobj(f, self.wfile)
            else:
                self.send_error(404, "File not found")
        except Exception as e:
            self.send_error(500, f"Server error: {str(e)}")

def main():
    PORT = 12001
    
    print(f"""
🎮 Nexus Controller Hub - Download Server
=========================================

Server starting on port {PORT}...

📱 Download Links:
• Main Page: https://work-2-afczixelmgsubehj.prod-runtime.all-hands.dev/
• Latest APK: https://work-2-afczixelmgsubehj.prod-runtime.all-hands.dev/android/app/build/outputs/apk/debug/app-debug.apk
• Quick Download: https://work-2-afczixelmgsubehj.prod-runtime.all-hands.dev/download/latest

📚 Documentation:
• Testing Guide: https://work-2-afczixelmgsubehj.prod-runtime.all-hands.dev/CONTROLLER_TESTING_GUIDE.md
• README: https://work-2-afczixelmgsubehj.prod-runtime.all-hands.dev/README.md

🔧 Development:
• Source Code: https://work-2-afczixelmgsubehj.prod-runtime.all-hands.dev/android/

Server ready! 🚀
    """)
    
    with socketserver.TCPServer(("0.0.0.0", PORT), CustomHTTPRequestHandler) as httpd:
        httpd.allow_reuse_address = True
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\n🛑 Server stopped by user")
            httpd.shutdown()

if __name__ == "__main__":
    main()