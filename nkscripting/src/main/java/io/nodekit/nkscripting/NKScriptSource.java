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

package io.nodekit.nkscripting;

import io.nodekit.nkscripting.util.NKLogging;

public class NKScriptSource   
{
    public String source;
    public String cleanup;
    public String filename;
    public String ns;
    private boolean injected = false;
    private NKScriptContext _context;

    public NKScriptSource(String source, String asFilename, String ns, String cleanup) {
        injected = false;
        this.filename = asFilename;
        if (cleanup != null)
        {
            this.cleanup = cleanup;
            this.ns = ns;
        }
        else if (ns != null)
        {
            this.cleanup = String.format("delete %s", ns);
        }
        else
        {
            this.ns = null;
            this.cleanup = null;
        }
        if (this.filename.equals(""))
        {
            this.source = source;
        }
        else
        {
            this.source = source + "\n//# sourceURL=" + this.filename + "\n";
        }
    }

    public NKScriptSource(String source, String asFilename, String ns)  {
        this(source, asFilename, ns, null);
    }

    public NKScriptSource(String source, String asFilename)  {
        this(source, asFilename, null, null);
    }

    public void inject(NKScriptContext context) throws Exception {
        this.injected = true;
        this._context = context;
        context.evaluateJavaScript(source, null);
        NKLogging.log(String.format("+E%s Injected %s ", context.id(), filename));
    }

    public void eject() {
        if (!injected)
            return ;
         
        if (_context == null)
            return ;

        try {
            _context.evaluateJavaScript(cleanup,  null);
        } catch (Exception ex) {
            NKLogging.log(ex.toString());
        }

        source = null;
        cleanup = null;
        filename = null;
        _context = null;
    }

}

