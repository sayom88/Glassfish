/* this file is generated by RelaxNGCC */
package relaxngcc.parser.state;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import relaxngcc.parser.ParserRuntime;

    import relaxngcc.MetaDataType;
    import relaxngcc.grammar.*;
    import org.xml.sax.Locator;
class NGCCcall extends NGCCHandler {
    private String alias;
    private String withParams;
    protected final ParserRuntime $runtime;
    private int $_ngcc_current_state;

    public final NGCCRuntime getRuntime() {
        return($runtime);
    }

    public NGCCcall(NGCCHandler parent, NGCCEventSource source, ParserRuntime runtime, int cookie) {
        super(source, parent, cookie);
        $runtime = runtime;
        $_ngcc_current_state = 2;
    }

    public NGCCcall(ParserRuntime runtime) {
        this(null, runtime, runtime, -1);
    }

    private void action0()throws SAXException {
        param = new NGCCCallParam(withParams,alias);}

    public void enterElement(String $uri, String $local, String $qname, Attributes $attrs)throws SAXException {
        int $ai;
        switch($_ngcc_current_state) {
        case 0:
            {
                revertToParentFromEnterElement(param, super._cookie, $uri, $local, $qname, $attrs);
            }
            break;
        case 1:
            {
                if(($ai = $runtime.getAttributeIndex("http://www.xml.gr.jp/xmlns/relaxngcc","alias"))>=0) {
                    NGCCHandler h = new NGCCalias(this, super._source, $runtime, 23);
                    spawnChildFromEnterElement(h, $uri, $local, $qname, $attrs);
                }
                else {
                    NGCCHandler h = new NGCCalias(this, super._source, $runtime, 23);
                    spawnChildFromEnterElement(h, $uri, $local, $qname, $attrs);
                }
            }
            break;
        case 2:
            {
                if(($ai = $runtime.getAttributeIndex("http://www.xml.gr.jp/xmlns/relaxngcc","with-params"))>=0) {
                    $runtime.consumeAttribute($ai);
                    $runtime.sendEnterElement(super._cookie, $uri, $local, $qname, $attrs);
                }
                else {
                    if(($ai = $runtime.getAttributeIndex("http://www.xml.gr.jp/xmlns/relaxngcc","alias"))>=0) {
                        NGCCHandler h = new NGCCalias(this, super._source, $runtime, 24);
                        spawnChildFromEnterElement(h, $uri, $local, $qname, $attrs);
                    }
                    else {
                        NGCCHandler h = new NGCCalias(this, super._source, $runtime, 24);
                        spawnChildFromEnterElement(h, $uri, $local, $qname, $attrs);
                    }
                }
            }
            break;
        default:
            {
                unexpectedEnterElement($qname);
            }
            break;
        }
    }

    public void leaveElement(String $uri, String $local, String $qname)throws SAXException {
        int $ai;
        switch($_ngcc_current_state) {
        case 0:
            {
                revertToParentFromLeaveElement(param, super._cookie, $uri, $local, $qname);
            }
            break;
        case 1:
            {
                if(($ai = $runtime.getAttributeIndex("http://www.xml.gr.jp/xmlns/relaxngcc","alias"))>=0) {
                    NGCCHandler h = new NGCCalias(this, super._source, $runtime, 23);
                    spawnChildFromLeaveElement(h, $uri, $local, $qname);
                }
                else {
                    NGCCHandler h = new NGCCalias(this, super._source, $runtime, 23);
                    spawnChildFromLeaveElement(h, $uri, $local, $qname);
                }
            }
            break;
        case 2:
            {
                if(($ai = $runtime.getAttributeIndex("http://www.xml.gr.jp/xmlns/relaxngcc","with-params"))>=0) {
                    $runtime.consumeAttribute($ai);
                    $runtime.sendLeaveElement(super._cookie, $uri, $local, $qname);
                }
                else {
                    if(($ai = $runtime.getAttributeIndex("http://www.xml.gr.jp/xmlns/relaxngcc","alias"))>=0) {
                        NGCCHandler h = new NGCCalias(this, super._source, $runtime, 24);
                        spawnChildFromLeaveElement(h, $uri, $local, $qname);
                    }
                    else {
                        NGCCHandler h = new NGCCalias(this, super._source, $runtime, 24);
                        spawnChildFromLeaveElement(h, $uri, $local, $qname);
                    }
                }
            }
            break;
        default:
            {
                unexpectedLeaveElement($qname);
            }
            break;
        }
    }

    public void enterAttribute(String $uri, String $local, String $qname)throws SAXException {
        int $ai;
        switch($_ngcc_current_state) {
        case 0:
            {
                revertToParentFromEnterAttribute(param, super._cookie, $uri, $local, $qname);
            }
            break;
        case 1:
            {
                if(($uri.equals("http://www.xml.gr.jp/xmlns/relaxngcc") && $local.equals("alias"))) {
                    NGCCHandler h = new NGCCalias(this, super._source, $runtime, 23);
                    spawnChildFromEnterAttribute(h, $uri, $local, $qname);
                }
                else {
                    NGCCHandler h = new NGCCalias(this, super._source, $runtime, 23);
                    spawnChildFromEnterAttribute(h, $uri, $local, $qname);
                }
            }
            break;
        case 2:
            {
                if(($uri.equals("http://www.xml.gr.jp/xmlns/relaxngcc") && $local.equals("with-params"))) {
                    $_ngcc_current_state = 4;
                }
                else {
                    if(($uri.equals("http://www.xml.gr.jp/xmlns/relaxngcc") && $local.equals("alias"))) {
                        NGCCHandler h = new NGCCalias(this, super._source, $runtime, 24);
                        spawnChildFromEnterAttribute(h, $uri, $local, $qname);
                    }
                    else {
                        NGCCHandler h = new NGCCalias(this, super._source, $runtime, 24);
                        spawnChildFromEnterAttribute(h, $uri, $local, $qname);
                    }
                }
            }
            break;
        default:
            {
                unexpectedEnterAttribute($qname);
            }
            break;
        }
    }

    public void leaveAttribute(String $uri, String $local, String $qname)throws SAXException {
        int $ai;
        switch($_ngcc_current_state) {
        case 0:
            {
                revertToParentFromLeaveAttribute(param, super._cookie, $uri, $local, $qname);
            }
            break;
        case 1:
            {
                NGCCHandler h = new NGCCalias(this, super._source, $runtime, 23);
                spawnChildFromLeaveAttribute(h, $uri, $local, $qname);
            }
            break;
        case 2:
            {
                NGCCHandler h = new NGCCalias(this, super._source, $runtime, 24);
                spawnChildFromLeaveAttribute(h, $uri, $local, $qname);
            }
            break;
        case 3:
            {
                if(($uri.equals("http://www.xml.gr.jp/xmlns/relaxngcc") && $local.equals("with-params"))) {
                    $_ngcc_current_state = 1;
                }
                else {
                    unexpectedLeaveAttribute($qname);
                }
            }
            break;
        default:
            {
                unexpectedLeaveAttribute($qname);
            }
            break;
        }
    }

    public void text(String $value)throws SAXException {
        int $ai;
        switch($_ngcc_current_state) {
        case 0:
            {
                revertToParentFromText(param, super._cookie, $value);
            }
            break;
        case 1:
            {
                if(($ai = $runtime.getAttributeIndex("http://www.xml.gr.jp/xmlns/relaxngcc","alias"))>=0) {
                    NGCCHandler h = new NGCCalias(this, super._source, $runtime, 23);
                    spawnChildFromText(h, $value);
                }
                else {
                    NGCCHandler h = new NGCCalias(this, super._source, $runtime, 23);
                    spawnChildFromText(h, $value);
                }
            }
            break;
        case 2:
            {
                if(($ai = $runtime.getAttributeIndex("http://www.xml.gr.jp/xmlns/relaxngcc","with-params"))>=0) {
                    $runtime.consumeAttribute($ai);
                    $runtime.sendText(super._cookie, $value);
                }
                else {
                    if(($ai = $runtime.getAttributeIndex("http://www.xml.gr.jp/xmlns/relaxngcc","alias"))>=0) {
                        NGCCHandler h = new NGCCalias(this, super._source, $runtime, 24);
                        spawnChildFromText(h, $value);
                    }
                    else {
                        NGCCHandler h = new NGCCalias(this, super._source, $runtime, 24);
                        spawnChildFromText(h, $value);
                    }
                }
            }
            break;
        case 4:
            {
                withParams = $value;
                $_ngcc_current_state = 3;
            }
            break;
        }
    }

    public void onChildCompleted(Object result, int cookie, boolean needAttCheck)throws SAXException {
        switch(cookie) {
        case 24:
            {
                this.alias = ((String)result);
                action0();
                $_ngcc_current_state = 0;
            }
            break;
        case 23:
            {
                this.alias = ((String)result);
                action0();
                $_ngcc_current_state = 0;
            }
            break;
        }
    }

    public boolean accepted() {
        return(($_ngcc_current_state == 0));
    }

    NGCCCallParam param;
}

