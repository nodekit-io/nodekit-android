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

import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import android.util.LruCache;

class NKArchiveReader {

    static int cacheSize = 3;
    static LruCache<String, ZipFile> zipCache = new LruCache<String, ZipFile>(cacheSize) {

        protected void entryRemoved(boolean evicted, String key, ZipFile oldValue, ZipFile newValue) {
            try {
                oldValue.close();
            } catch (java.io.IOException e) {
                NKLogging.log(e);
            }
        }
    };

    ZipFile getZipFile(String archive) {

        ZipFile zipFile = zipCache.get(archive);

        if (zipFile == null) {
            File file = NKStorage.getFile(archive);
            if (file == null) { return null; };
            try {
                zipFile = new ZipFile(file);
                zipCache.put(archive, zipFile);
            } catch (java.io.IOException e) {
                NKLogging.log(e);
            }

        }

        return zipFile;

    }

    byte[] dataForFile(String archive, String filename) {

        byte[] result = null;

        try  {
            ZipFile zipFile = getZipFile(archive);
            if (zipFile == null) return result;

            ZipEntry ze = zipFile.getEntry(filename);

            if (!ze.isDirectory()) {

                InputStream zip = zipFile.getInputStream(ze);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int count;
                while ((count = zip.read(buffer)) != -1) {
                    baos.write(buffer, 0, count);
                }
                result = baos.toByteArray();
                 
                }
            
        } catch(Exception e) {
            NKLogging.log(e);
        }
        return result;
    }

    boolean exists(String archive, String filename) {

        boolean result = false;

        try  {
            ZipFile zipFile = getZipFile(archive);
            if (zipFile == null) return result;

            ZipEntry ze = zipFile.getEntry(filename);

            if (ze != null && !ze.isDirectory()) {
                        result = true;
            }
            
        } catch(Exception e) {
            NKLogging.log(e);
        }
        return result;

    }

    Map<String, Object> stat(String archive, String filename) {

        Map<String, Object> result = null;

        try  {
            ZipFile zipFile = getZipFile(archive);
            if (zipFile == null) return result;

            ZipEntry ze = zipFile.getEntry(filename);

            if (ze != null && !ze.isDirectory()) {
                    result = stat(archive, filename, ze);
            }
        } catch(Exception e) {
            NKLogging.log(e);
        }
        return result;

    }

    ArrayList<String> getDirectory(String archive, String foldername) {
        ArrayList<String> result = new ArrayList<>();

        try  {
            InputStream stream =  NKStorage.getStream(archive);
            if (stream == null) return result;

            ZipInputStream zip = new ZipInputStream(stream);
            ZipEntry ze;
            if (foldername.length() > 0 && foldername.charAt(foldername.length()-1)=='/') {
                foldername = foldername.substring(0, foldername.length()-1);
            }

            int depth = pathSegments(foldername) + 2;

            while ((ze = zip.getNextEntry()) != null) {
                String item = ze.getName();

                if (item.startsWith(foldername) && (pathSegments(item) == depth && item.charAt(item.length()-1)=='/')){
                    result.add(item.substring(foldername.length() + 1, item.length()-1));
                }
            }
            zip.close();
        } catch(Exception e) {
            NKLogging.log(e);
        }
        return result;
    }

    private Map<String, Object> stat(String archive, String filename, ZipEntry ze) {
        HashMap<String, Object> storageItem  = new HashMap<>();
        storageItem.put("birthtime", ze.getTime());
        storageItem.put("size", ze.getSize());
        storageItem.put("mtime", ze.getTime());
        storageItem.put("path", archive + filename);
        storageItem.put("filetype", ze.isDirectory() ? "Directory" : "File");
        return storageItem;
    }


    private static int pathSegments(String file) {
        return file.length() - file.replace("/", "").length();
    }

}
