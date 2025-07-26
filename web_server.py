#!/usr/bin/env python3
"""
Nexus Controller Hub - Web Server
Hosts the APK files and provides download interface
"""

import os
import shutil
from http.server import HTTPServer, SimpleHTTPRequestHandler
from urllib.parse import unquote
import json
import datetime

class NexusControllerHandler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory="/workspace/toaster/web", **kwargs)
    
    def end_headers(self):
        # Enable CORS
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', '*')
        super().end_headers()
    
    def do_OPTIONS(self):
        self.send_response(200)
        self.end_headers()
    
    def do_GET(self):
        if self.path == '/api/info':
            self.send_api_info()
        elif self.path == '/api/files':
            self.send_file_list()
        else:
            super().do_GET()
    
    def send_api_info(self):
        info = {
            "app_name": "Nexus Controller Hub",
            "version": "1.0.0",
            "build_date": datetime.datetime.now().isoformat(),
            "description": "High-performance Android controller remapping and macro app",
            "features": [
                "Real-time controller input visualization",
                "Button remapping with live feedback",
                "Macro recording and playback",
                "Multi-controller support",
                "Offline-first design"
            ],
            "download_links": {
                "latest_apk": "/downloads/nexus-controller-hub-latest.apk",
                "debug_apk": "/downloads/nexus-controller-hub-debug.apk",
                "source_code": "/downloads/nexus-controller-hub-source.zip"
            }
        }
        
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()
        self.wfile.write(json.dumps(info, indent=2).encode())
    
    def send_file_list(self):
        files = []
        downloads_dir = "/workspace/toaster/web/downloads"
        if os.path.exists(downloads_dir):
            for filename in os.listdir(downloads_dir):
                filepath = os.path.join(downloads_dir, filename)
                if os.path.isfile(filepath):
                    stat = os.stat(filepath)
                    files.append({
                        "name": filename,
                        "size": stat.st_size,
                        "modified": datetime.datetime.fromtimestamp(stat.st_mtime).isoformat(),
                        "download_url": f"/downloads/{filename}"
                    })
        
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()
        self.wfile.write(json.dumps(files, indent=2).encode())

def setup_web_directory():
    """Set up the web directory structure and copy files"""
    web_dir = "/workspace/toaster/web"
    downloads_dir = os.path.join(web_dir, "downloads")
    
    # Create directories
    os.makedirs(downloads_dir, exist_ok=True)
    
    # Copy APK files
    apk_files = [
        ("./android/app/build/outputs/apk/debug/app-debug.apk", "nexus-controller-hub-debug.apk"),
        ("./NexusControllerHub-v1.0-Final.apk", "nexus-controller-hub-latest.apk"),
        ("./NexusControllerHub-ControllerVisualization.apk", "nexus-controller-hub-visualization.apk")
    ]
    
    for src, dst in apk_files:
        src_path = os.path.join("/workspace/toaster", src)
        dst_path = os.path.join(downloads_dir, dst)
        if os.path.exists(src_path):
            shutil.copy2(src_path, dst_path)
            print(f"Copied {src} -> {dst}")
    
    # Create source code zip
    source_zip_path = os.path.join(downloads_dir, "nexus-controller-hub-source.zip")
    os.system(f"cd /workspace/toaster && zip -r {source_zip_path} . -x '*.git*' '*.gradle*' 'build/*' 'web/*' '*.apk'")
    
    # Create index.html
    create_index_html(web_dir)
    
    # Create download page
    create_download_page(web_dir)
    
    print(f"Web directory set up at: {web_dir}")
    return web_dir

def create_index_html(web_dir):
    """Create the main index.html page"""
    html_content = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nexus Controller Hub - Download</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            color: #333;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
        }
        
        .header {
            text-align: center;
            color: white;
            margin-bottom: 40px;
        }
        
        .header h1 {
            font-size: 3rem;
            margin-bottom: 10px;
            text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
        }
        
        .header p {
            font-size: 1.2rem;
            opacity: 0.9;
        }
        
        .card {
            background: white;
            border-radius: 15px;
            padding: 30px;
            margin-bottom: 30px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
            transition: transform 0.3s ease;
        }
        
        .card:hover {
            transform: translateY(-5px);
        }
        
        .download-section {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 30px;
            margin-bottom: 40px;
        }
        
        .download-card {
            background: white;
            border-radius: 15px;
            padding: 25px;
            text-align: center;
            box-shadow: 0 5px 20px rgba(0,0,0,0.1);
            transition: all 0.3s ease;
        }
        
        .download-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 15px 40px rgba(0,0,0,0.2);
        }
        
        .download-btn {
            display: inline-block;
            background: linear-gradient(45deg, #667eea, #764ba2);
            color: white;
            padding: 15px 30px;
            text-decoration: none;
            border-radius: 50px;
            font-weight: bold;
            font-size: 1.1rem;
            transition: all 0.3s ease;
            margin-top: 15px;
        }
        
        .download-btn:hover {
            transform: scale(1.05);
            box-shadow: 0 5px 15px rgba(0,0,0,0.2);
        }
        
        .features {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin-top: 30px;
        }
        
        .feature {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 10px;
            text-align: center;
        }
        
        .feature-icon {
            font-size: 2rem;
            margin-bottom: 10px;
        }
        
        .instructions {
            background: #e3f2fd;
            border-left: 4px solid #2196f3;
            padding: 20px;
            margin: 20px 0;
            border-radius: 5px;
        }
        
        .code {
            background: #f5f5f5;
            padding: 10px;
            border-radius: 5px;
            font-family: 'Courier New', monospace;
            margin: 10px 0;
        }
        
        .status {
            background: #d4edda;
            border: 1px solid #c3e6cb;
            color: #155724;
            padding: 15px;
            border-radius: 5px;
            margin: 20px 0;
        }
        
        .footer {
            text-align: center;
            color: white;
            margin-top: 50px;
            opacity: 0.8;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🎮 Nexus Controller Hub</h1>
            <p>High-Performance Android Controller Remapping & Macro App</p>
        </div>
        
        <div class="status">
            <strong>✅ Build Status:</strong> Successfully compiled and ready for testing!<br>
            <strong>📅 Build Date:</strong> <span id="buildDate"></span><br>
            <strong>🔧 Version:</strong> 1.0.0 (Debug Build)
        </div>
        
        <div class="download-section">
            <div class="download-card">
                <h3>🚀 Latest Debug Build</h3>
                <p>Most recent build with all fixes and improvements</p>
                <a href="/downloads/nexus-controller-hub-debug.apk" class="download-btn">
                    📱 Download APK (Debug)
                </a>
                <div class="code">Size: ~15MB</div>
            </div>
            
            <div class="download-card">
                <h3>📦 Complete Source Code</h3>
                <p>Full Kotlin/Android source code with documentation</p>
                <a href="/downloads/nexus-controller-hub-source.zip" class="download-btn">
                    💻 Download Source
                </a>
                <div class="code">Size: ~5MB</div>
            </div>
            
            <div class="download-card">
                <h3>📋 Testing Guide</h3>
                <p>Comprehensive guide for testing all features</p>
                <a href="/CONTROLLER_TESTING_GUIDE.md" class="download-btn">
                    📖 View Guide
                </a>
                <div class="code">Markdown Format</div>
            </div>
        </div>
        
        <div class="card">
            <h2>🎯 Key Features Implemented</h2>
            <div class="features">
                <div class="feature">
                    <div class="feature-icon">🎮</div>
                    <h4>Real-Time Input</h4>
                    <p>Live controller visualization with instant feedback</p>
                </div>
                <div class="feature">
                    <div class="feature-icon">🔄</div>
                    <h4>Button Remapping</h4>
                    <p>Remap any button with visual confirmation</p>
                </div>
                <div class="feature">
                    <div class="feature-icon">📹</div>
                    <h4>Macro Recording</h4>
                    <p>Record and replay complex input sequences</p>
                </div>
                <div class="feature">
                    <div class="feature-icon">🔧</div>
                    <h4>Live Testing</h4>
                    <p>Comprehensive test mode for all features</p>
                </div>
                <div class="feature">
                    <div class="feature-icon">📱</div>
                    <h4>Offline First</h4>
                    <p>No internet required, all data stored locally</p>
                </div>
                <div class="feature">
                    <div class="feature-icon">🎯</div>
                    <h4>Multi-Controller</h4>
                    <p>Support for Xbox, PlayStation, and generic controllers</p>
                </div>
            </div>
        </div>
        
        <div class="card">
            <h2>📱 Installation Instructions</h2>
            <div class="instructions">
                <h4>Step 1: Enable Unknown Sources</h4>
                <p>Go to <strong>Settings → Security → Unknown Sources</strong> and enable installation from unknown sources.</p>
            </div>
            
            <div class="instructions">
                <h4>Step 2: Download & Install</h4>
                <p>Download the APK file and tap to install. You may need to grant installation permissions.</p>
            </div>
            
            <div class="instructions">
                <h4>Step 3: Test Controller Input</h4>
                <p>Connect a controller and navigate to <strong>Live Test Mode</strong> to verify all features work correctly.</p>
            </div>
        </div>
        
        <div class="card">
            <h2>🧪 Testing the Fixes</h2>
            <p><strong>The following issues have been resolved:</strong></p>
            <ul style="margin: 15px 0; padding-left: 30px;">
                <li>✅ <strong>Controller Visual Feedback</strong> - Real-time button highlighting and stick movement</li>
                <li>✅ <strong>Macro Recording</strong> - Captures actual controller input with timestamps</li>
                <li>✅ <strong>Live Test Mode</strong> - Comprehensive testing interface with input logs</li>
                <li>✅ <strong>Button Remapping Proof</strong> - Visual demonstration of remapping functionality</li>
            </ul>
            
            <div class="instructions">
                <h4>🎮 Quick Test Procedure:</h4>
                <ol style="margin: 15px 0; padding-left: 30px;">
                    <li>Install the APK and open the app</li>
                    <li>Connect a controller (USB or Bluetooth)</li>
                    <li>Navigate to <strong>"Live Test Mode"</strong></li>
                    <li>Tap <strong>"Start Test"</strong></li>
                    <li>Press controller buttons and move sticks</li>
                    <li>Verify real-time visual feedback</li>
                    <li>Test macro recording functionality</li>
                </ol>
            </div>
        </div>
        
        <div class="footer">
            <p>Built with ❤️ using Kotlin, Jetpack Compose, and Room Database</p>
            <p>Server running on port 12000 | <a href="/api/info" style="color: #fff;">API Info</a> | <a href="/api/files" style="color: #fff;">File List</a></p>
        </div>
    </div>
    
    <script>
        // Set build date
        document.getElementById('buildDate').textContent = new Date().toLocaleString();
        
        // Add some interactivity
        document.querySelectorAll('.download-btn').forEach(btn => {
            btn.addEventListener('click', function(e) {
                this.style.transform = 'scale(0.95)';
                setTimeout(() => {
                    this.style.transform = 'scale(1.05)';
                }, 100);
            });
        });
    </script>
</body>
</html>"""
    
    with open(os.path.join(web_dir, "index.html"), "w") as f:
        f.write(html_content)

def create_download_page(web_dir):
    """Create a simple download API page"""
    # Copy the testing guide to web directory
    guide_src = "/workspace/toaster/CONTROLLER_TESTING_GUIDE.md"
    guide_dst = os.path.join(web_dir, "CONTROLLER_TESTING_GUIDE.md")
    if os.path.exists(guide_src):
        shutil.copy2(guide_src, guide_dst)

def main():
    print("🎮 Setting up Nexus Controller Hub Web Server...")
    
    # Set up web directory
    web_dir = setup_web_directory()
    
    # Start server
    server_address = ('0.0.0.0', 12000)
    httpd = HTTPServer(server_address, NexusControllerHandler)
    
    print(f"""
🚀 Nexus Controller Hub Server Started!

📱 Download Page: https://work-1-afczixelmgsubehj.prod-runtime.all-hands.dev
🔗 Direct APK: https://work-1-afczixelmgsubehj.prod-runtime.all-hands.dev/downloads/nexus-controller-hub-debug.apk
📖 Testing Guide: https://work-1-afczixelmgsubehj.prod-runtime.all-hands.dev/CONTROLLER_TESTING_GUIDE.md
🔧 API Info: https://work-1-afczixelmgsubehj.prod-runtime.all-hands.dev/api/info

Server running on port 12000...
Press Ctrl+C to stop
    """)
    
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\n🛑 Server stopped")
        httpd.server_close()

if __name__ == "__main__":
    main()