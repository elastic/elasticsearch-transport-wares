/*
 *
 *  Copyright 2011 Rajendra Patil
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.elasticsearch.wares.filter.compression;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

import static org.elasticsearch.wares.filter.common.Constants.HTTP_ACCEPT_ENCODING_HEADER;
import static org.elasticsearch.wares.filter.common.Constants.HTTP_CONTENT_ENCODING_HEADER;

public final class CompressedHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final HttpServletRequest request;
    private final EncodedStreamsFactory encodedStreamsFactory;
    private CompressedServletInputStream compressedStream;
    private BufferedReader bufferedReader;
    private boolean getInputStreamCalled;
    private boolean getReaderCalled;

    public CompressedHttpServletRequestWrapper(HttpServletRequest request, EncodedStreamsFactory encodedStreamsFactory) {
        super(request);
        this.request = request;
        this.encodedStreamsFactory = encodedStreamsFactory;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (getReaderCalled) {
            throw new IllegalStateException("getReader() has been already called");
        }
        getInputStreamCalled = true;
        return getCompressedServletInputStream();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (getInputStreamCalled) {
            throw new IllegalStateException("getInputStream() has been already called");
        }
        getReaderCalled = true;
        if (bufferedReader == null) {
            bufferedReader = new BufferedReader(new InputStreamReader(getCompressedServletInputStream(),
                    getCharacterEncoding()));
        }
        return bufferedReader;
    }

    private CompressedServletInputStream getCompressedServletInputStream() throws IOException {
        if (compressedStream == null) {
            compressedStream = new CompressedServletInputStream(request.getInputStream(),
                    encodedStreamsFactory);
        }
        return compressedStream;
    }

    private static boolean skippedHeader(String headerName) {
        return HTTP_ACCEPT_ENCODING_HEADER.equalsIgnoreCase(headerName) ||
                HTTP_CONTENT_ENCODING_HEADER.equalsIgnoreCase(headerName);
    }

    @Override
    public String getHeader(String header) {
        return skippedHeader(header) ? null : super.getHeader(header);
    }

    @Override
    public Enumeration<String> getHeaders(String header) {
        Enumeration<String> original = super.getHeaders(header);
        if (original == null) {
            return null;
        }
        return skippedHeader(header) ? Collections.enumeration(Collections.<String>emptyList()) : original;
    }

    @Override
    public long getDateHeader(String header) {
        return skippedHeader(header) ? -1L : super.getDateHeader(header);
    }

    @Override
    public int getIntHeader(String header) {
        return skippedHeader(header) ? -1 : super.getIntHeader(header);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Enumeration<String> originalHeaderNames = super.getHeaderNames();
        if (originalHeaderNames == null) {
            return null;
        }

        Collection<String> headerNames = new ArrayList();
        while (originalHeaderNames.hasMoreElements()) {
            String headerName = originalHeaderNames.nextElement();
            if (!skippedHeader(headerName)) {
                headerNames.add(headerName);
            }
        }
        return Collections.enumeration(headerNames);
    }

}