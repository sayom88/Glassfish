/***** BEGIN LICENSE BLOCK *****
 * Version: CPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Common Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2007 Charles O Nutter <headius@headius.com>
 * Copyright (C) 2007 Nick Sieger <nicksieger@gmail.com>
 * Copyright (C) 2007 Ola Bini <ola@ologix.com>
 * Copyright (C) 2007 William N Dortch <bill.dortch@gmail.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the CPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the CPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/
package org.jruby.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.jcodings.Encoding;
import org.jcodings.ascii.AsciiTables;
import org.jcodings.specific.ASCIIEncoding;


/**
 *
 * @author headius
 */
public final class ByteList implements Comparable, CharSequence, Serializable {
    private static final long serialVersionUID = -1286166947275543731L;

    public static final byte[] NULL_ARRAY = new byte[0];
    public static final ByteList EMPTY_BYTELIST = new ByteList(0);

    public byte[] bytes;
    public int begin;
    public int realSize;
    public Encoding encoding = ASCIIEncoding.INSTANCE;
    int hash;
    String stringValue;

    private static final int DEFAULT_SIZE = 4;

    /** Creates a new instance of ByteList */
    public ByteList() {
        this(DEFAULT_SIZE);
    }

    public ByteList(int size) {
        bytes = new byte[size];
        realSize = 0;
    }

    public ByteList(byte[] bytes, Encoding encoding) {
        this.bytes = bytes;
        this.realSize = bytes.length;
        this.encoding = encoding;
    }

    public ByteList(byte[] wrap) {
        this (wrap, true);
    }

    public ByteList(byte[] wrap, boolean copy) {
        assert wrap != null;
        if (copy) {
            bytes = (byte[])wrap.clone();
        } else {
            bytes = wrap;
        }
        realSize = wrap.length;
    }

    public ByteList(ByteList wrap) {
        this(wrap.bytes, wrap.begin, wrap.realSize);
    }
    
    public ByteList(ByteList wrap, boolean copy) {
        this(wrap.bytes, wrap.begin, wrap.realSize, false);
    }

    public ByteList(byte[] wrap, int index, int len) {
        this(wrap,index,len,true);
    }

    public ByteList(byte[] wrap, int index, int len, boolean copy) {
        assert wrap != null;
        if (copy || index != 0) {
            bytes = new byte[len];
            System.arraycopy(wrap, index, bytes, 0, len);
        } else {
            bytes = wrap;
        }
        realSize = len;
    }

    public ByteList(ByteList wrap, int index, int len) {
        this(wrap.bytes, wrap.begin + index, len);
    }

    private ByteList(boolean flag) {
    }

    public void delete(int start, int len) {
        realSize-=len;
        System.arraycopy(bytes,start+len,bytes,start,realSize);
    }

    public ByteList append(byte b) {
        grow(1);
        bytes[realSize++] = b;
        return this;
    }

    public ByteList append(int b) {
        append((byte)b);
        return this;
    }
    
    public ByteList append(InputStream input, int length) throws IOException {
        grow(length);
        int read = 0;
        int n;
        while (read < length) {
            n = input.read(bytes, begin + read, length - read);
            if (n == -1) {
                if (read == 0) throw new java.io.EOFException();
                break;
            }
            read += n;
        }
        
        realSize += read;
        return this;
    }

    public void append(ByteBuffer buffer, int len) {
        grow(len);
        buffer.get(bytes, realSize, len);
        realSize += len;
    }
    
    public void fill(int b, int len) {
        for ( ; --len >= 0; ) {
            append(b);
        }
    }

    public Object clone() {
        return dup();
    }

    public ByteList dup() {
        ByteList dup = dup(realSize);
        dup.hash = hash;
        dup.stringValue = stringValue;
        return dup;
    }

    public ByteList shallowDup() {
        ByteList dup = new  ByteList(bytes, false);
        dup.realSize = realSize;
        dup.begin = begin;
        dup.encoding = encoding;
        dup.hash = hash;
        dup.stringValue = stringValue;
        return dup;
    }

    /**
     * @param length is the value of how big the buffer is going to be, not the actual length to copy
     * 
     * It is used by RubyString.modify(int) to prevent COW pathological situations
     * (namely to COW with having <code>length - realSize</code> bytes ahead)
     */
    public ByteList dup(int length) {
        ByteList dup = new ByteList(false);
        dup.bytes = new byte[length];
        // use the smaller of the two sizes for the new real size, to allow truncating
        int newRealSize = Math.min(length, realSize);
        System.arraycopy(bytes, begin, dup.bytes, 0, newRealSize);
        dup.realSize = newRealSize;
        dup.begin = 0;
        dup.encoding = encoding;
        return dup;
    }

    public void ensure(int length) {
        if (length >= bytes.length) {
            byte[]tmp = new byte[length + (length >>> 1)];
            System.arraycopy(bytes, begin, tmp, 0, realSize);
            bytes = tmp;
        }
    }
    
    public ByteList makeShared(int index, int len) {
        ByteList shared = new ByteList(false);        
        shared.bytes = bytes;
        shared.realSize = len;        
        shared.begin = begin + index;
        shared.encoding = encoding;
        return shared;
    }

    public void view(int index, int len) {
        realSize = len;
        begin = begin + index;
    }

    public void unshare() {
        unshare(realSize);
    }
    
    /**
     * @param length is the value of how big the buffer is going to be, not the actual length to copy
     * 
     * It is used by RubyString.modify(int) to prevent COW pathological situations
     * (namely to COW with having <code>length - realSize</code> bytes ahead)
     */
    public void unshare(int length) {
        byte[] tmp = new byte[length];
        System.arraycopy(bytes, begin, tmp, 0, Math.min(realSize, length));
        bytes = tmp;
        begin = 0;
    }

    public void invalidate() {
        hash = 0;
        stringValue = null;
    }

    public void prepend(byte b) {
        grow(1);
        System.arraycopy(bytes, 0, bytes, 1, realSize);
        bytes[0] = b;
        realSize++;
    }

    public void append(byte[] moreBytes) {
        grow(moreBytes.length);
        System.arraycopy(moreBytes, 0, bytes, realSize, moreBytes.length);
        realSize += moreBytes.length;
    }

    public void append(ByteList moreBytes) {
        append(moreBytes.bytes, moreBytes.begin, moreBytes.realSize);
    }

    public void append(ByteList moreBytes, int index, int len) {
        append(moreBytes.bytes, moreBytes.begin + index, len);
    }

    public void append(byte[] moreBytes, int start, int len) {
        grow(len);
        System.arraycopy(moreBytes, start, bytes, realSize, len);
        realSize += len;
    }
    
    public void realloc(int length) {
        byte tmp[] = new byte[length];
        System.arraycopy(bytes, 0, tmp, 0, realSize);
        bytes = tmp;
    }

    public int length() {
        return realSize;
    }

    public int lengthEnc() {
        return encoding.strLength(bytes, begin, begin + realSize);
    }

    public void length(int newLength) {
        grow(newLength - realSize);
        realSize = newLength;
    }

    public int get(int index) {
        return bytes[begin + index];
    }

    public int getEnc(int index) {
        return encoding.strCodeAt(bytes, begin, begin + realSize, index);
    }

    public void set(int index, int b) {
        bytes[begin + index] = (byte)b;
    }

    public void replace(byte[] newBytes) {
        assert newBytes != null;
        this.bytes = newBytes;
        realSize = newBytes.length;
    }

    /**
     * Unsafe version of replace(int,int,ByteList). The contract is that these
     * unsafe versions will not make sure thet beg and len indices are correct.
     */
    public void unsafeReplace(int beg, int len, ByteList nbytes) {
        unsafeReplace(beg, len, nbytes.bytes, nbytes.begin, nbytes.realSize);
    }

    /**
     * Unsafe version of replace(int,int,byte[]). The contract is that these
     * unsafe versions will not make sure thet beg and len indices are correct.
     */
    public void unsafeReplace(int beg, int len, byte[] buf) {
        unsafeReplace(beg, len, buf, 0, buf.length);
    }

    /**
     * Unsafe version of replace(int,int,byte[],int,int). The contract is that these
     * unsafe versions will not make sure thet beg and len indices are correct.
     */
    public void unsafeReplace(int beg, int len, byte[] nbytes, int index, int count) {
        grow(count - len);
        int newSize = realSize + count - len;
        System.arraycopy(bytes,beg+len,bytes,beg+count,realSize - (len+beg));
        System.arraycopy(nbytes,index,bytes,beg,count);
        realSize = newSize;
    }

    public void replace(int beg, int len, ByteList nbytes) {
        replace(beg, len, nbytes.bytes, nbytes.begin, nbytes.realSize);
    }

    public void replace(int beg, int len, byte[] buf) {
        replace(beg, len, buf, 0, buf.length);
    }

    public void replace(int beg, int len, byte[] nbytes, int index, int count) {
        unsafeReplace(beg, len, nbytes, index, count);
    }

    public void insert(int index, int b) {
        grow(1);
        System.arraycopy(bytes, index, bytes, index + 1, realSize - index);
        bytes[index] = (byte)b;
        realSize++;
    }

    public int indexOf(int c) {
        return indexOf(c, 0);
    }

    public int indexOf(final int c, int pos) {
        // not sure if this is checked elsewhere,
        // didn't see it in RubyString. RubyString does
        // cast to char, so c will be >= 0.
        if (c > 255)
            return -1;
        final byte b = (byte)(c&0xFF);
        final int size = begin + realSize;
        final byte[] buf = bytes;
        pos += begin;
        for ( ; pos < size && buf[pos] != b ; pos++ ) ;
        return pos < size ? pos - begin : -1;
    }

    public int indexOf(ByteList find) {
        return indexOf(find, 0);
    }
    
    public int indexOf(ByteList find, int i) {
        return indexOf(bytes, begin, realSize, find.bytes, find.begin, find.realSize, i);
    }

    static int indexOf(byte[] source, int sourceOffset, int sourceCount, byte[] target, int targetOffset, int targetCount, int fromIndex) {
        if (fromIndex >= sourceCount) return (targetCount == 0 ? sourceCount : -1);
        if (fromIndex < 0) fromIndex = 0;
        if (targetCount == 0) return fromIndex;

        byte first  = target[targetOffset];
        int max = sourceOffset + (sourceCount - targetCount);

        for (int i = sourceOffset + fromIndex; i <= max; i++) {
            if (source[i] != first) while (++i <= max && source[i] != first);

            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                for (int k = targetOffset + 1; j < end && source[j] == target[k]; j++, k++);

                if (j == end) return i - sourceOffset;
            }
        }
        return -1;
    }

    public int lastIndexOf(int c) {
        return lastIndexOf(c, realSize - 1);
    }

    public int lastIndexOf(final int c, int pos) {
        // not sure if this is checked elsewhere,
        // didn't see it in RubyString. RubyString does
        // cast to char, so c will be >= 0.
        if (c > 255)
            return -1;
        final byte b = (byte)(c&0xFF);
        final int size = begin + realSize;
        pos += begin;
        final byte[] buf = bytes;
        if (pos >= size) {
            pos = size;
        } else {
            pos++;
        }
        for ( ; --pos >= begin && buf[pos] != b ; ) ;
        return pos - begin;
    }

    public int lastIndexOf(ByteList find) {
        return lastIndexOf(find, realSize);
    }

    public int lastIndexOf(ByteList find, int pos) {
        return lastIndexOf(bytes, begin, realSize, find.bytes, find.begin, find.realSize, pos);
    }    

    static int lastIndexOf(byte[] source, int sourceOffset, int sourceCount, byte[] target, int targetOffset, int targetCount, int fromIndex) {
        int rightIndex = sourceCount - targetCount;
        if (fromIndex < 0) return -1;
        if (fromIndex > rightIndex) fromIndex = rightIndex;
        if (targetCount == 0) return fromIndex;

        int strLastIndex = targetOffset + targetCount - 1;
        byte strLastChar = target[strLastIndex];
        int min = sourceOffset + targetCount - 1;
        int i = min + fromIndex;

        startSearchForLastChar:
            while (true) {
                while (i >= min && source[i] != strLastChar) i--;
                if (i < min) return -1;
                int j = i - 1;
                int start = j - (targetCount - 1);
                int k = strLastIndex - 1;

                while (j > start) {
                    if (source[j--] != target[k--]) {
                        i--;
                        continue startSearchForLastChar;
                    }
                }
                return start - sourceOffset + 1;
            }
    }

    public boolean startsWith(ByteList other, int toffset) {
        if (realSize == 0) return false;
        
        byte[]ta = bytes;
        int to = begin + toffset;
        byte[]pa = other.bytes;
        int po = other.begin;
        int pc = other.realSize;

        while (--pc >= 0) if (ta[to++] != pa[po++]) return false;
        return true;
    }

    public boolean startsWith(ByteList other) {
        return startsWith(other, 0);
    }

    public boolean endsWith(ByteList other) {
        return startsWith(other, realSize - other.realSize);
    }

    public boolean equals(Object other) {
        if (other instanceof ByteList) return equal((ByteList)other);
        return false;
    }

    public boolean equal(ByteList other) {
        if (other == this) return true; 
        if (hash != 0 && other.hash != 0 && hash != other.hash) return false;

        int first, last;
        if ((last = realSize) == other.realSize) {
            byte buf[] = bytes;
            byte otherBuf[] = other.bytes;
            // scanning from front and back simultaneously, meeting in
            // the middle. the object is to get a mismatch as quickly as
            // possible. alternatives might be: scan from the middle outward
            // (not great because it won't pick up common variations at the
            // ends until late) or sample odd bytes forward and even bytes
            // backward (I like this one, but it's more expensive for
            // strings that are equal; see sample_equals below).

            for (first = -1; 
            --last > first && buf[begin + last] == otherBuf[other.begin + last] &&
            ++first < last && buf[begin + first] == otherBuf[other.begin + first] ; ) ;
            return first >= last;
            }
        return false;
    }

    // an alternative to the new version of equals, should
    // detect inequality faster (in many cases), but is slow
    // in the case of equal values (all bytes visited), due to
    // using n+=2, n-=2 vs. ++n, --n while iterating over the array.
    public boolean sample_equals(Object other) {
        if (other == this) return true;
        if (other instanceof ByteList) {
            ByteList b = (ByteList) other;
            int first;
            int last;
            int size;
            byte[] buf;
            if ((size = realSize) == b.realSize) {
                // scanning from front and back simultaneously, sampling odd
                // bytes on the forward iteration and even bytes on the 
                // reverse iteration. the object is to get a mismatch as quickly
                // as possible. 
                for (buf = bytes, first = -1, last = (size + 1) & ~1 ;
                    (last -= 2) >= 0 && buf[begin + last] == b.bytes[b.begin + last] &&
                    (first += 2) < size && buf[begin + first] == b.bytes[b.begin + first] ; ) ;
                return last < 0 || first == size;
            }
        }
        return false;
    }

    /**
     * This comparison matches MRI comparison of Strings (rb_str_cmp).
     * I wish we had memcmp right now...
     */
    public int compareTo(Object other) {
        return cmp((ByteList)other);
    }

    public int cmp(final ByteList other) {
        if (other == this) return 0;
        final int size = realSize;
        final int len =  Math.min(size, other.realSize);
        int offset = -1;
        // a bit of VM/JIT weirdness here: though in most cases
        // performance is improved if array references are kept in
        // a local variable (saves an instruction per access, as I
        // [slightly] understand it), in some cases, when two (or more?) 
        // arrays are being accessed, the member reference is actually
        // faster.  this is one of those cases...
        for (  ; ++offset < len && bytes[begin + offset] == other.bytes[other.begin + offset]; ) ;
        if (offset < len) {
            return (bytes[begin + offset]&0xFF) > (other.bytes[other.begin + offset]&0xFF) ? 1 : -1;
        }
        return size == other.realSize ? 0 : size == len ? -1 : 1;
    }

    public int caseInsensitiveCmp(final ByteList other) {
        if (other == this) return 0;

        final int size = realSize;
        final int len =  Math.min(size, other.realSize);
        final int other_begin = other.begin;
        final byte[] other_bytes = other.bytes;

        for (int offset = -1; ++offset < len;) {
            int myCharIgnoreCase = AsciiTables.ToLowerCaseTable[bytes[begin + offset] & 0xff];
            int otherCharIgnoreCase = AsciiTables.ToLowerCaseTable[other_bytes[other_begin + offset] & 0xff];
            if (myCharIgnoreCase < otherCharIgnoreCase) {
                return -1;
            } else if (myCharIgnoreCase > otherCharIgnoreCase) {
                return 1;
            }
        }
        return size == other.realSize ? 0 : size == len ? -1 : 1;
    }

   /**
     * Returns the internal byte array. This is unsafe unless you know what you're
     * doing. But it can improve performance for byte-array operations that
     * won't change the array.
     *
     * @return the internal byte array
     */
    public byte[] unsafeBytes() {
        return bytes;  
    }

    public byte[] bytes() {
        byte[] newBytes = new byte[realSize];
        System.arraycopy(bytes, begin, newBytes, 0, realSize);
        return newBytes;
    }

    public int begin() {
        return begin;
    }

    private void grow(int increaseRequested) {
        if (increaseRequested < 0) return;

        int newSize = realSize + increaseRequested;
        if (newSize > bytes.length) {
            byte[] newBytes = new byte[newSize + (newSize >> 1)];
            if (bytes.length != 0) System.arraycopy(bytes, 0, newBytes, 0, realSize);
            bytes = newBytes;
        }
    }

    public int hashCode() {
        if (hash != 0) return hash;

        int key = 0;
        int index = begin;
        final int end = begin + realSize; 
        while (index < end) {
            // equivalent of: key = key * 65599 + byte;
            key = ((key << 16) + (key << 6) - key) + (int)(bytes[index++]); // & 0xFF ? 
        }
        key = key + (key >> 5);
        return hash = key;
    }

    /**
     * Remembers toString value, which is expensive for StringBuffer.
     * 
     * @return an ISO-8859-1 representation of the byte list
     */    
    public String toString() {
        try {
            if (stringValue == null) stringValue = new String(bytes, begin, realSize, "ISO-8859-1");
            return stringValue;
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("ISO-8859-1 encoding should never fail; report this at www.jruby.org");
        }
    }
    
    public static ByteList create(CharSequence s) {
        return new ByteList(plain(s),false);
    }

    public static byte[] plain(CharSequence s) {
        if(s instanceof String) {
            try {
                return ((String)s).getBytes("ISO8859-1");
            } catch(Exception e) {
                //FALLTHROUGH
            }
        }
        byte[] bytes = new byte[s.length()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) s.charAt(i);
        }
        return bytes;
    }

    public static byte[] plain(char[] s) {
        byte[] bytes = new byte[s.length];
        for (int i = 0; i < s.length; i++) {
            bytes[i] = (byte) s[i];
        }
        return bytes;
    }

    public static char[] plain(byte[] b, int start, int length) {
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = (char) (b[start + i] & 0xFF);
        }
        return chars;
    }

    public static char[] plain(byte[] b) {
        char[] chars = new char[b.length];
        for (int i = 0; i < b.length; i++) {
            chars[i] = (char) (b[i] & 0xFF);
        }
        return chars;
    }

    public char charAt(int ix) {
        return (char)(this.bytes[begin + ix] & 0xFF);
    }

    public CharSequence subSequence(int start, int end) {
        return new ByteList(this, start, end - start);
    }

    public static int memcmp(final byte[] first, final int firstStart, final int firstLen, final byte[] second, final int secondStart, final int secondLen) {
        if (first == second) return 0;
        final int len =  Math.min(firstLen,secondLen);
        int offset = -1;
        for (  ; ++offset < len && first[firstStart + offset] == second[secondStart + offset]; ) ;
        if (offset < len) {
            return (first[firstStart + offset]&0xFF) > (second[secondStart + offset]&0xFF) ? 1 : -1;
        }
        return firstLen == secondLen ? 0 : firstLen == len ? -1 : 1;

    }

    public static int memcmp(final byte[] first, final int firstStart, final byte[] second, final int secondStart, final int len) {
        if (first == second) return 0;
        int offset = -1;
        for (  ; ++offset < len && first[firstStart + offset] == second[secondStart + offset]; ) ;
        if (offset < len) {
            return (first[firstStart + offset]&0xFF) > (second[secondStart + offset]&0xFF) ? 1 : -1;
        }
        return 0;

    }
}
