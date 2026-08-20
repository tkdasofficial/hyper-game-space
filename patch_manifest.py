import re

with open('app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

# Add RECEIVE_BOOT_COMPLETED permission
if "android.permission.RECEIVE_BOOT_COMPLETED" not in content:
    content = content.replace(
        '<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />',
        '<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />\n    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />'
    )

# Change process to :overlay_service
content = content.replace('android:process=":accessibility"', 'android:process=":overlay_service"')

# Add receiver
receiver_str = """        <receiver
            android:name=".receiver.OverlayReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
            </intent-filter>
        </receiver>"""

if "OverlayReceiver" not in content:
    content = content.replace(
        '<activity android:name=".ManageAppsActivity"',
        receiver_str + '\n        <activity android:name=".ManageAppsActivity"'
    )

with open('app/src/main/AndroidManifest.xml', 'w') as f:
    f.write(content)
