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

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.wares.filter.common.IgnoreAcceptContext;
import org.elasticsearch.wares.filter.common.WebUtilitiesResponseWrapper;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.wares.filter.common.Constants.DEFAULT_COMPRESSION_SIZE_THRESHOLD;
import static org.elasticsearch.wares.filter.common.Constants.HTTP_ACCEPT_ENCODING_HEADER;
import static org.elasticsearch.wares.filter.common.Constants.HTTP_CACHE_CONTROL_HEADER;
import static org.elasticsearch.wares.filter.common.Constants.HTTP_CONTENT_ENCODING_HEADER;
import static org.elasticsearch.wares.filter.common.Constants.HTTP_CONTENT_LENGTH_HEADER;
import static org.elasticsearch.wares.filter.common.Constants.HTTP_CONTENT_TYPE_HEADER;
import static org.elasticsearch.wares.filter.common.Constants.HTTP_ETAG_HEADER;
import static org.elasticsearch.wares.filter.common.Constants.HTTP_VARY_HEADER;

public class CompressedHttpServletResponseWrapper extends WebUtilitiesResponseWrapper {

    private final HttpServletResponse httpResponse;

    private final String compressedContentEncoding;
    private final EncodedStreamsFactory encodedStreamsFactory;
    private CompressedServletOutputStream compressingStream;

    private PrintWriter printWriter;
    private boolean getOutputStreamCalled;
    private boolean getWriterCalled;

    private boolean compressing;

    private long savedContentLength;
    private boolean savedContentLengthSet;
    private String savedContentEncoding;
    private String savedETag;

    private IgnoreAcceptContext ignoreAcceptContext;

    private boolean mimeIgnored;
    private boolean noTransformSet;
    private int threshold = DEFAULT_COMPRESSION_SIZE_THRESHOLD;

    private static final List<String> UNALLOWED_HEADERS = new ArrayList();

    static {
        UNALLOWED_HEADERS.add(HTTP_CONTENT_LENGTH_HEADER.toLowerCase());
        UNALLOWED_HEADERS.add(HTTP_CACHE_CONTROL_HEADER.toLowerCase());
        UNALLOWED_HEADERS.add(HTTP_CONTENT_ENCODING_HEADER.toLowerCase());
        UNALLOWED_HEADERS.add(HTTP_ETAG_HEADER.toLowerCase());
    }

    /**
     * Logger
     */
    private static final ESLogger logger = Loggers.getLogger(CompressedHttpServletResponseWrapper.class);


    public CompressedHttpServletResponseWrapper(HttpServletResponse httpResponse,
                                                EncodedStreamsFactory encodedStreamsFactory,
                                                String contentEncoding, int threshold, IgnoreAcceptContext ignoreAcceptContext) {
        super(httpResponse);
        this.httpResponse = httpResponse;
        this.compressedContentEncoding = contentEncoding;
        compressing = false;
        this.encodedStreamsFactory = encodedStreamsFactory;
        mimeIgnored = false;
        this.threshold = threshold;
        this.ignoreAcceptContext = ignoreAcceptContext;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (getWriterCalled) {
            throw new IllegalStateException("getWriter() has already been called");
        }
        getOutputStreamCalled = true;
        return getCompressedServletOutputStream();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (getOutputStreamCalled) {
            throw new IllegalStateException("getCompressingOutputStream() has already been called");
        }
        getWriterCalled = true;
        if (printWriter == null) {
            printWriter = new PrintWriter(new OutputStreamWriter(getCompressedServletOutputStream(),
                getCharacterEncoding()),
                true);
        }
        return printWriter;
    }

    /**
     * @see #setHeader(String, String)
     */
    @Override
    public void addHeader(String name, String value) {
        if (HTTP_CACHE_CONTROL_HEADER.equalsIgnoreCase(name)) {
            httpResponse.addHeader(HTTP_CACHE_CONTROL_HEADER, value);
            if (value.contains("no-transform")) {
                logger.trace("No compression: due to no-transform");
                noTransformSet = true;
                cancelCompression();
            }
        } else if (HTTP_CONTENT_ENCODING_HEADER.equalsIgnoreCase(name)) {
            savedContentEncoding = value;
            if (alreadyCompressedEncoding(value)) {
                cancelCompression();
            }
        } else if (HTTP_CONTENT_LENGTH_HEADER.equalsIgnoreCase(name)) {
            setContentLength(Long.parseLong(value));
        } else if (HTTP_CONTENT_TYPE_HEADER.equalsIgnoreCase(name)) {
            setContentType(value);
        } else if (HTTP_ETAG_HEADER.equalsIgnoreCase(name)) {
            savedETag = value;
            setETagHeader();
        } else if (isAllowedHeader(name)) {
            httpResponse.addHeader(name, value);
        }
    }

    /**
     * @see #setIntHeader(String, int)
     */
    @Override
    public void addIntHeader(String name, int value) {
        if (HTTP_CONTENT_LENGTH_HEADER.equalsIgnoreCase(name)) {
            setContentLength(value);
        } else if (HTTP_ETAG_HEADER.equalsIgnoreCase(name)) {
            savedETag = String.valueOf(value);
            setETagHeader();
        } else if (isAllowedHeader(name)) {
            httpResponse.addIntHeader(name, value);
        }
    }

    @Override
    public void addDateHeader(String name, long value) {
        if (isAllowedHeader(name)) {
            httpResponse.addDateHeader(name, value);
        }
    }

    /**
     * @see #addHeader(String, String)
     */
    @Override
    public void setHeader(String name, String value) {
        if (HTTP_CACHE_CONTROL_HEADER.equalsIgnoreCase(name)) {
            httpResponse.setHeader(HTTP_CACHE_CONTROL_HEADER, value);
            if (value.contains("no-transform")) {
                logger.trace("No compression: due to no-transform directive");
                noTransformSet = true;
                cancelCompression();
            }
        } else if (HTTP_CONTENT_ENCODING_HEADER.equalsIgnoreCase(name)) {
            savedContentEncoding = value;
            if (alreadyCompressedEncoding(value)) {
                cancelCompression();
            }
        } else if (HTTP_CONTENT_LENGTH_HEADER.equalsIgnoreCase(name)) {
            // Not setContentLength(); we want to potentially accommodate a long value here
            setContentLength(Long.parseLong(value));
        } else if (HTTP_CONTENT_TYPE_HEADER.equalsIgnoreCase(name)) {
            setContentType(value);
        } else if (HTTP_ETAG_HEADER.equalsIgnoreCase(name)) {
            savedETag = value;
            setETagHeader();
        } else if (isAllowedHeader(name)) {
            httpResponse.setHeader(name, value);
        }
    }

    private void cancelCompression() {
        if (compressingStream != null) {
            try {
                logger.trace("Cancelling compression.");
                compressingStream.cancelCompression();
            } catch (IOException ioe) {
                logger.error("Error while cancelling compression.", ioe);
            }
        }
    }

    private void setETagHeader() {
        if (savedETag != null) {
            if (compressing) {
                httpResponse.setHeader(HTTP_ETAG_HEADER, savedETag + '-' + compressedContentEncoding);
            } else {
                httpResponse.setHeader(HTTP_ETAG_HEADER, savedETag);
            }
        }
    }

    /**
     * @see #addIntHeader(String, int)
     */
    @Override
    public void setIntHeader(String name, int value) {
        if (HTTP_CONTENT_LENGTH_HEADER.equalsIgnoreCase(name)) {
            setContentLength(value);
        } else if (HTTP_ETAG_HEADER.equalsIgnoreCase(name)) {
            savedETag = String.valueOf(value);
            setETagHeader();
        } else if (isAllowedHeader(name)) {
            httpResponse.setIntHeader(name, value);
        }
    }

    @Override
    public void setDateHeader(String name, long value) {
        if (isAllowedHeader(name)) {
            httpResponse.setDateHeader(name, value);
        }
    }

    @Override
    public void flushBuffer() throws IOException {
        flushWriter(); // make sure nothing is buffered in the writer, if applicable
        if (compressingStream != null) {
            compressingStream.flush();
        }
    }

    @Override
    public void reset() {
        flushWriter(); // make sure nothing is buffered in the writer, if applicable
        if (compressingStream != null) {
            compressingStream.reset();
        }
        httpResponse.reset();
        if (compressing) {
            setCompressionResponseHeaders();
        } else {
            setNonCompressionResponseHeaders();
        }
    }

    @Override
    public void resetBuffer() {
        flushWriter(); // make sure nothing is buffered in the writer, if applicable
        if (compressingStream != null) {
            compressingStream.reset();
        }
        httpResponse.resetBuffer();
    }

    @Override
    public void setContentLength(int contentLength) {
        setContentLength((long) contentLength);
    }

    private void setContentLength(long contentLength) {
        if (compressing) {
            // do nothing -- caller-supplied content length is not meaningful
            //LOGGER.logDebug("Ignoring application-specified content length since response is compressed");
        } else {
            savedContentLength = contentLength;
            savedContentLengthSet = true;
            //LOGGER.logDebug("Saving application-specified content length for later: " + contentLength);
            if (compressingStream != null && compressingStream.isCancelled()) {
                httpResponse.setHeader(HTTP_CONTENT_LENGTH_HEADER, String.valueOf(contentLength));
            }
        }
    }

    @Override
    public void setContentType(String contentType) {
        httpResponse.setContentType(contentType);
        mimeIgnored = ignoreAcceptContext != null && !ignoreAcceptContext.isMIMEAccepted(contentType);
        if (mimeIgnored && compressingStream != null) {
            cancelCompression();
        }
    }

    public boolean isCompressed() {
        return compressing;
    }

    public void close() throws IOException {
        if (compressingStream != null && !compressingStream.isClosed()) {
            compressingStream.close();
        }
    }

    private void setCompressionResponseHeaders() {
        httpResponse.addHeader(HTTP_VARY_HEADER, HTTP_ACCEPT_ENCODING_HEADER);
        String fullContentEncodingHeader = savedContentEncoding == null ?
            compressedContentEncoding :
            savedContentEncoding + ',' + compressedContentEncoding;
        httpResponse.setHeader(HTTP_CONTENT_ENCODING_HEADER, fullContentEncodingHeader);
        setETagHeader();
    }

    private void setNonCompressionResponseHeaders() {
        if (savedContentLengthSet) {
            httpResponse.setHeader(HTTP_CONTENT_LENGTH_HEADER, String.valueOf(savedContentLength));
        }
        if (savedContentEncoding != null) {
            httpResponse.setHeader(HTTP_CONTENT_ENCODING_HEADER, savedContentEncoding);
        }
    }

    void noCompression() {
        assert !compressing;
        setNonCompressionResponseHeaders();
    }

    void useCompression() {
        logger.trace("Switching to compression");
        compressing = true;
        setCompressionResponseHeaders();
    }

    private boolean isAllowedHeader(String header) {
        return header == null || !UNALLOWED_HEADERS.contains(header.toLowerCase());
    }

    private void flushWriter() {
        if (printWriter != null) {
            printWriter.flush();
        }
    }

    private static boolean alreadyCompressedEncoding(String encoding) {

        return (encoding != null && EncodedStreamsFactory.SUPPORTED_ENCODINGS.containsKey(encoding));

    }

    private CompressedServletOutputStream getCompressedServletOutputStream() throws IOException {
        if (compressingStream == null) {
            compressingStream =
                new CompressedServletOutputStream(httpResponse.getOutputStream(),
                    encodedStreamsFactory,
                    this, threshold);
        }

        if (!compressingStream.isClosed()) {
            if (mustNotCompress()) {
                compressingStream.cancelCompression();
            }
        }

        return compressingStream;
    }

    private boolean mustNotCompress() {
        if (mimeIgnored) {
            logger.trace("No Compression: Mime is ignored");
            return true;
        }
        if (savedContentLengthSet && savedContentLength < (long) threshold) {
            logger.trace("No Compression: Already set content length {} less than threshold {}", new Object[]{savedContentLength, threshold});
            return true;
        }
        if (noTransformSet) {
            logger.trace("No Compression: no-transform is set");
            return true;
        }
        return alreadyCompressedEncoding(savedContentEncoding);
    }
}
