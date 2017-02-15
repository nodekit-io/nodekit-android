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

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

class NKArchiveReader {

 /* static class NKLruCache<K, V> extends LinkedHashMap<K, V> {

       private final int mMaxEntries;

        public LruCache(int maxEntries) {
            super(maxEntries + 1, 1.0f, true );
            mMaxEntries = maxEntries;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > mMaxEntries;
        }
    }

    static NKArchiveReader create() {

         NKLruCache<String, Object> cacheCDirs = new NKLruCache<>(10);
         NKLruCache<String, Object> cacheArchiveData = new NKLruCache<>(10);

        return new NKArchiveReader(cacheCDirs, cacheArchiveData);

    }


    private NKLruCache<String, ZipInputStream> _cacheCDirs;
    private NKLruCache<String, ZipInputStream> _cacheArchiveData;


    private NKArchiveReader( NKLruCache<String, Object> cacheCDirs, NKLruCache<String, Object> cacheArchiveData){
        this._cacheCDirs = cacheCDirs;
        this._cacheArchiveData = cacheArchiveData;
    } */

    byte[] dataForFile(String archive, String filename) {

        byte[] result = null;

        try  {
            InputStream stream = NKStorage.getStream(archive);
            if (stream == null) return result;

            ZipInputStream zip = new ZipInputStream(stream);
            ZipEntry ze;
            while ((ze = zip.getNextEntry()) != null) {

                if (!ze.isDirectory()) {
                    if (ze.getName().equals(filename)) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int count;
                        while ((count = zip.read(buffer)) != -1) {
                            baos.write(buffer, 0, count);
                        }
                        result = baos.toByteArray();
                        break;

                    }
                }
            }
            zip.close();
        } catch(Exception e) {
            NKLogging.log(e);
        }
        return result;
    }

    InputStream streamForFile(final String archive, final String filename) {
        final PipedInputStream pipedInputStream=new PipedInputStream();
        final PipedOutputStream pipedOutputStream=new PipedOutputStream();

		/*Connect pipe*/
        try {
        pipedInputStream.connect(pipedOutputStream);
        } catch (Exception e) {
            NKLogging.log(e);
        }

            new Thread(new Runnable() {
                public void run () {
                    try {
                        InputStream stream = NKStorage.getStream(archive);

                        if (stream == null) return ;

                        ZipInputStream zip = new ZipInputStream(stream);

                        ZipEntry ze;

                        while ((ze = zip.getNextEntry()) != null) {

                            if (!ze.isDirectory()) {
                                if (ze.getName().equals(filename)) {
                                    byte[] buffer = new byte[1024];
                                    int count;
                                    while ((count = zip.read(buffer)) != -1) {
                                        pipedOutputStream.write(buffer, 0, count);
                                    }
                                    break;

                                }
                            }
                        }
                        zip.close();
                        pipedOutputStream.close();
                     } catch (Exception e) {
                        NKLogging.log(e);
                    }
                }
                }).start();

        return pipedInputStream;
    }

    boolean exists(String archive, String filename) {

        boolean result = false;

        try  {
            InputStream stream =  NKStorage.getStream(archive);
            if (stream == null) return result;

            ZipInputStream zip = new ZipInputStream(stream);
            ZipEntry ze;
            while ((ze = zip.getNextEntry()) != null) {

                if(!ze.isDirectory()) {

                    if (ze.getName().equals(filename)){
                        result = true;
                        break;
                    }
                }
            }
            zip.close();
        } catch(Exception e) {
            NKLogging.log(e);
        }
        return result;

    }

    Map<String, Object> stat(String archive, String filename) {

        Map<String, Object> result = null;

        try  {
            InputStream stream =  NKStorage.getStream(archive);
            if (stream == null) return result;

            ZipInputStream zip = new ZipInputStream(stream);
            ZipEntry ze;
            while ((ze = zip.getNextEntry()) != null) {

                    if (ze.getName().equals(filename)){
                        result = stat(archive, filename, ze);
                        break;
                    }

            }
            zip.close();
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

