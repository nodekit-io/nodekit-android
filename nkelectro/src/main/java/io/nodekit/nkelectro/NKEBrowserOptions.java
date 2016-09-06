/*
* nodekit.io
*
* Copyright (c) 2016 OffGrid Networks. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package io.nodekit.nkelectro;


public class NKEBrowserOptions   
{
    public NKEBrowserOptions() {
    }

    public static final String nkBrowserType = "nk.browserType";
    public static final String kTitle = "title";
    public static final String kIcon = "icon";
    public static final String kFrame = "frame";
    public static final String kShow = "show";
    public static final String kCenter = "center";
    public static final String kX = "x";
    public static final String kY = "y";
    public static final String kWidth = "width";
    public static final String kHeight = "height";
    public static final String kMinWidth = "minWidth";
    public static final String kMinHeight = "minHeight";
    public static final String kMaxWidth = "maxWidth";
    public static final String kMaxHeight = "maxHeight";
    public static final String kResizable = "resizable";
    public static final String kFullscreen = "fullscreen";
    // Whether the window should show in taskbar.
    public static final String kSkipTaskbar = "skipTaskbar";
    // Start with the kiosk mode, see Opera's page for description:
    // http://www.opera.com/support/mastering/kiosk/
    public static final String kKiosk = "kiosk";
    // Make windows stays on the top of all other windows.
    public static final String kAlwaysOnTop = "alwaysOnTop";
    // Enable the NSView to accept first mouse event.
    public static final String kAcceptFirstMouse = "acceptFirstMouse";
    // Whether window size should include window frame.
    public static final String kUseContentSize = "useContentSize";
    // The requested title bar style for the window
    public static final String kTitleBarStyle = "titleBarStyle";
    // The menu bar is hidden unless "Alt" is pressed.
    public static final String kAutoHideMenuBar = "autoHideMenuBar";
    // Enable window to be resized larger than screen.
    public static final String kEnableLargerThanScreen = "enableLargerThanScreen";
    // Forces to use dark theme on Linux.
    public static final String kDarkTheme = "darkTheme";
    // Whether the window should be transparent.
    public static final String kTransparent = "transparent";
    // Window type hint.
    public static final String kType = "type";
    // Disable auto-hiding cursor.
    public static final String kDisableAutoHideCursor = "disableAutoHideCursor";
    // Use the OS X's standard window instead of the textured window.
    public static final String kStandardWindow = "standardWindow";
    // Default browser window background color.
    public static final String kBackgroundColor = "backgroundColor";
    // The WebPreferences.
    public static final String kWebPreferences = "webPreferences";
    // The factor of which page should be zoomed.
    public static final String kZoomFactor = "zoomFactor";
    // Script that will be loaded by guest WebContents before other scripts.
    public static final String kPreloadScript = "preload";
    // Like --preload, but the passed argument is an URL.
    public static final String kPreloadURL = "preloadURL";
    // Enable the node integration.
    public static final String kNodeIntegration = "nodeIntegration";
    // Instancd ID of guest WebContents.
    public static final String kGuestInstanceID = "guestInstanceId";
    // Enable DirecNKRemotingMessage on Windows.
    public static final String kDirecNKRemotingMessage = "direcNKRemotingMessage";
    // Web runtime features.
    public static final String kExperimentalFeatures = "experimentalFeatures";
    public static final String kExperimentalCanvasFeatures = "experimentalCanvasFeatures";
    // Opener window's ID.
    public static final String kOpenerID = "openerId";
    // Enable blink features.
    public static final String kBlinkFeatures = "blinkFeatures";
}


