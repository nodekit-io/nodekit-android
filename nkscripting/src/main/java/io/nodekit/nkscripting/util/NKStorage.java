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

package io.nodekit.nkscripting.util;

import android.webkit.JavascriptInterface;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import android.util.Base64;
import java.util.ArrayList;
import android.content.res.AssetFileDescriptor;
import io.nodekit.nkscripting.NKScriptContext;
import io.nodekit.nkscripting.NKApplication;
import io.nodekit.nkscripting.NKScriptExport;
import 	android.content.pm.PackageManager;
import 	android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import java.io.File;
import java.io.FileInputStream;

public class NKStorage implements NKScriptExport {

    private static long installedTimeStamp;

    static  {
        try {
            PackageManager pm = NKApplication.getAppContext().getPackageManager();
            String s = NKApplication.getAppContext().getPackageName();
            ApplicationInfo appInfo = pm.getApplicationInfo(s, 0);
            String appFile = appInfo.sourceDir;
            installedTimeStamp = new File(appFile).lastModified();
        } catch (Exception e) {
            NKLogging.log(e);
            installedTimeStamp = System.currentTimeMillis();
        }
    }

    // NKScriptExport
    public static void attachTo( NKScriptContext context) throws Exception
    {
        HashMap<String,Object> options = new HashMap<String, Object>();

        options.put("js","lib-scripting/native_module.js");

        context.loadPlugin(new NKStorage(), "io.nodekit.scripting.storage", options);


    }

    // PUBLIC METHODS, ACCESSIBLE FROM JAVASCRIPT

    @JavascriptInterface
    public String getSourceSync(String module) {

        String extension = "";

        int i = module.lastIndexOf('.');
        int p = module.lastIndexOf('/');

        if (i == -1 || i < p) {
            module += ".js";
        }

        return NKStorage.getResourceBase64(module);
    }

    @JavascriptInterface
    public boolean existsSync(String module) {
        return NKStorage.exists(module);
    }

    @JavascriptInterface
    public Map<String, Object> statSync(String module) {
        return NKStorage.stat(module);
    }

    @JavascriptInterface
    public ArrayList<String> getDirectorySync(String module) {
        return NKStorage.getDirectory(module);
    }

    // static members
    private static ArrayList<String> searchPaths = new ArrayList<>();


    // PUBLIC STATIC METHODS, ACCESSIBLE FROM NATIVE ONLY
    public static void includeSearchPath(String path) {

        if (!searchPaths.contains(path))
        {
            searchPaths.add(path);
        }
    }

    public static String getResource(String fileName) {

        if (isNKAR_(fileName)) return getNKARResource_(fileName);

        StringBuilder sb = new StringBuilder();

        try {
            InputStream stream = getStream(fileName);
            if (stream == null)
                  return null;

            byte buffer[] = new byte[16384];  // read 16k blocks
            int len; // how much content was read?
            while( ( len = stream.read( buffer ) ) > 0 ){
                sb.append(new String(buffer, 0, len));
            }

            stream.close();
        } catch (IOException e) {
            NKLogging.log("Error reading " + fileName + ": " + e.toString());
        }

        return sb.toString();
    }

    public static byte[] getResourceData(String fileName) {

        if (isNKAR_(fileName)) return getDataNKAR_(fileName);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int i;
        try {
            InputStream stream = getStreamRaw(fileName);
            if (stream == null)
                return null;

            i = stream.read();
            while (i != -1)
            {
                byteArrayOutputStream.write(i);
                i = stream.read();
            }
            stream.close();
        } catch (IOException e) {
            NKLogging.log("Error reading " + fileName + ": " + e.toString());
        }

        return byteArrayOutputStream.toByteArray();

    }

    public static String getResourceBase64(String fileName) {

        if (isNKAR_(fileName)) return getDataNKAR_Base64_(fileName);

        StringBuilder sb = new StringBuilder();

        try {
            InputStream stream = getStream(fileName);
            if (stream == null)
                return null;

            byte buffer[] = new byte[16383];  // read 16k-1 blocks, multiple of 3 bytes
            int len; // how much content was read?
            while( ( len = stream.read( buffer ) ) > 0 ){
                sb.append(Base64.encodeToString(buffer, 0, len, Base64.NO_WRAP | Base64.NO_PADDING));
            }

            stream.close();
        } catch (IOException e) {
            NKLogging.log("Error reading " + fileName + ": " + e.toString());
        }

        return sb.toString();
    }

    public static InputStream getStream(String fileName) {

        if (isNKAR_(fileName)) return getStreamNKAR_(fileName);

        return getStreamRaw(fileName);

    }

    public static InputStream getStreamRaw(String fileName) {

        try {
            return NKApplication.getAppContext().getAssets().open(getPath_(fileName));
        } catch (IOException e) {
            NKLogging.log(e);
        }

        for (String search : searchPaths) {
            try {
                File joinedPath = new File(search, fileName);
                if (joinedPath.exists())
                    return new FileInputStream(joinedPath);
            } catch (IOException e) {
                NKLogging.log(e);
            }

        }

        NKLogging.log("Error reading " + fileName );

        return null;
    }


    public static void copyStream(final InputStream inStream, final OutputStream outStream, final boolean closeOutput)
            throws IOException {

        new Thread(new Runnable() {
            public void run() {
                final int bufferSize = 16384;
                final byte[] buffer = new byte[bufferSize];
                int len = 0;

                try {
                    while ((len = inStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, len);
                    }
                } catch (Exception e) {
                    NKLogging.log(e);
                } finally {
                    try {
                        if (outStream != null && closeOutput)
                            outStream.close();
                    } catch (IOException e) {
                        NKLogging.log(e);
                    }
                }
            }
        }).start();
    }

    public static boolean exists(String module)  {

        if (isNKAR_(module)) return existsNKAR_(module);

        try {
            InputStream stream = NKApplication.getAppContext().getAssets().open(getPath_(module));
            stream.close();
            return true;
        } catch (Exception e) {
            // ignore
        }

        for (String search : searchPaths) {
                File joinedPath = new File(search, module);
                if (joinedPath.exists())
                    return true;
        }

        NKLogging.log("!Not found" + module);

        return false;
    }

    // PRIVATE METHODS
    private static NKArchiveReader nkArchiveReader = new NKArchiveReader();

    private static boolean existsNKAR_(String module)  {
        String[] moduleArr = module.split(".nkar/", 2);
        String archive = moduleArr[0] + ".nkar";
        String fileentry  = moduleArr[1];

        return nkArchiveReader.exists(archive, fileentry);
    }

    private static String getPath_(String filename) {
        return filename.replaceAll("^/+", "");
    }

    private static boolean isNKAR_(String module) {
        return module.toLowerCase().contains(".nkar/");
    }


    private static String getNKARResource_(String module) {

        byte [] bytes = getDataNKAR_(module);

        return  new String(bytes, StandardCharsets.UTF_8);
    }

    private static String getDataNKAR_Base64_(String module) {

        byte [] bytes = getDataNKAR_(module);

        if (bytes != null) {
            return Base64.encodeToString(bytes, 0, bytes.length, Base64.NO_WRAP | Base64.NO_PADDING);
        } else
            return null;
    }

    private static Map<String, Object> stat(String module) {

        if (isNKAR_(module)) return statNKAR_(module);

        try {

            AssetFileDescriptor fd = NKApplication.getAppContext().getAssets().openFd(getPath_(module));

            HashMap<String, Object> storageItem  = new HashMap<>();
            storageItem.put("birthtime", installedTimeStamp);
            storageItem.put("size", fd.getLength());
            storageItem.put("mtime",installedTimeStamp);
            storageItem.put("path", module);
            storageItem.put("filetype", "File");
            return storageItem;

        } catch (Exception e)

        {
            // ignore
        }

        for (String search : searchPaths) {
            File file = new File(search, module);
            if (file.exists())
            {
                HashMap<String, Object> storageItem  = new HashMap<>();
                storageItem.put("birthtime", file.lastModified());
                storageItem.put("size", file.length());
                storageItem.put("mtime",file.lastModified());
                storageItem.put("path", file.getPath());
                storageItem.put("filetype", file.isDirectory() ? "Directory" : "File");
                return storageItem;
            }
        }

        return null;

    }

    private static Map<String, Object>  statNKAR_(String module)  {
        String[] moduleArr = module.split(".nkar/", 2);
        String archive = moduleArr[0] + ".nkar";
        String fileentry  = moduleArr[1];

        return nkArchiveReader.stat(archive, fileentry);
    }


    private static ArrayList<String>  getDirectory(String module)  {

        if (isNKAR_(module)) return getDirectoryNKAR_(module);

        ArrayList<String> result = null;

        final AssetManager assetManager = NKApplication.getAppContext().getAssets();
        try {

            String[] array = assetManager.list(module);
            if (array != null)
            {
                result = new ArrayList<String>(Arrays.asList(array));
            }
        } catch (IOException e) {
            NKLogging.log(e);
        }

        if ( result == null ) {
            for (String search : searchPaths) {
                File file = new File(search, module);
                if (file.exists() && file.isDirectory()) {
                    File[] array = file.listFiles();
                    if (array != null)
                    {
                        result = new ArrayList<String>();

                        for (File item : array) {
                            result.add(item.getName());
                        }

                    }
                    break;
                }
            }
        }

        return result;
    }

    private static ArrayList<String> getDirectoryNKAR_(String module) {
        String[] moduleArr = module.split(".nkar/", 2);
        String archive = moduleArr[0] + ".nkar";
        String fileentry  = moduleArr[1];

        return nkArchiveReader.getDirectory(archive, fileentry);
    }


    private static byte[] getDataNKAR_(String module) {

        String[] moduleArr = module.split(".nkar/", 2);
        String archive = moduleArr[0] + ".nkar";
        String fileentry  = moduleArr[1];

        return nkArchiveReader.dataForFile(archive, fileentry);
    }

    private static InputStream getStreamNKAR_(String module) {

        String[] moduleArr = module.split(".nkar/", 2);
        String archive = moduleArr[0] + ".nkar";
        String fileentry  = moduleArr[1];

        return nkArchiveReader.streamForFile(archive, fileentry);
    }

}