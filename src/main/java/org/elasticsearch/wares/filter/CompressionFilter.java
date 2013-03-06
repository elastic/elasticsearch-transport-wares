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
package org.elasticsearch.wares.filter;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.wares.filter.common.AbstractFilter;
import org.elasticsearch.wares.filter.common.Constants;
import org.elasticsearch.wares.filter.compression.CompressedHttpServletRequestWrapper;
import org.elasticsearch.wares.filter.compression.CompressedHttpServletResponseWrapper;
import org.elasticsearch.wares.filter.compression.EncodedStreamsFactory;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.elasticsearch.wares.filter.common.Constants.CONTENT_ENCODING_IDENTITY;
import static org.elasticsearch.wares.filter.common.Constants.DEFAULT_COMPRESSION_SIZE_THRESHOLD;
import static org.elasticsearch.wares.filter.common.Constants.HTTP_ACCEPT_ENCODING_HEADER;
import static org.elasticsearch.wares.filter.common.Constants.HTTP_CONTENT_ENCODING_HEADER;
import static org.elasticsearch.wares.filter.common.Utils.readInt;


/**
 * Servlet Filter implementation class CompressionFilter to handle compressed requests
 * and also respond with compressed contents supporting gzip, compress or
 * deflate compression encoding.
 * Visit http://code.google.com/p/webutilities/wiki/CompressionFilter for more details.
 *
 * @author rpatil
 */
public class CompressionFilter extends AbstractFilter {

    /**
     * Logger
     */
    private static final ESLogger LOGGER = Loggers.getLogger(CompressionFilter.class);

    /**
     * The threshold number of bytes) to compress
     */
    private int compressionThreshold = DEFAULT_COMPRESSION_SIZE_THRESHOLD;

    /**
     * To mark the request that it is processed
     */
    private static final String PROCESSED_ATTR = CompressionFilter.class.getName() + ".PROCESSED";

    /**
     * To mark the request that response compressed
     */
    private static final String COMPRESSED_ATTR = CompressionFilter.class.getName() + ".COMPRESSED";

    /**
     * Threshold
     */
    private static final String INIT_PARAM_COMPRESSION_THRESHOLD = "compressionThreshold";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        int compressionMinSize = readInt(filterConfig.getInitParameter(INIT_PARAM_COMPRESSION_THRESHOLD), this.compressionThreshold);
        if (compressionMinSize > 0) { // priority given to configured value
            this.compressionThreshold = compressionMinSize;
        }
        LOGGER.trace("Filter initialized with: {}:{}", new Object[]{
            INIT_PARAM_COMPRESSION_THRESHOLD, String.valueOf(this.compressionThreshold)});
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        ServletRequest req = getRequest(request);
        ServletResponse resp = getResponse(request, response);
        request.setAttribute(PROCESSED_ATTR, Boolean.TRUE);
        chain.doFilter(req, resp);
        if (resp instanceof CompressedHttpServletResponseWrapper) {
            CompressedHttpServletResponseWrapper compressedResponseWrapper = (CompressedHttpServletResponseWrapper) resp;
            try {
                compressedResponseWrapper.close();  //so that stream is finished and closed.
            } catch (IOException ex) {
                LOGGER.error("Response was already closed: ", ex.toString());
            }
            if (compressedResponseWrapper.isCompressed()) {
                req.setAttribute(COMPRESSED_ATTR, Boolean.TRUE);
            }
        }
    }

    private ServletRequest getRequest(ServletRequest request) {
        if (!(request instanceof HttpServletRequest)) {
            LOGGER.trace("No Compression: non http request");
            return request;
        }
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String contentEncoding = httpRequest.getHeader(HTTP_CONTENT_ENCODING_HEADER);
        if (contentEncoding == null) {
            LOGGER.trace("No Compression: Request content encoding is: {}", contentEncoding);
            return request;
        }
        if (!EncodedStreamsFactory.isRequestContentEncodingSupported(contentEncoding)) {
            LOGGER.trace("No Compression: unsupported request content encoding: {}", contentEncoding);
            return request;
        }
        LOGGER.debug("Decompressing request: content encoding : {}", contentEncoding);
        return new CompressedHttpServletRequestWrapper(httpRequest, EncodedStreamsFactory.getFactoryForContentEncoding(contentEncoding));
    }

    private String getAppropriateContentEncoding(String acceptEncoding) {
        if (acceptEncoding == null) return null;
        String contentEncoding = null;
        if (CONTENT_ENCODING_IDENTITY.equals(acceptEncoding.trim())) {
            return contentEncoding; //no encoding to be applied
        }
        String[] clientAccepts = acceptEncoding.split(",");
        //!TODO select best encoding (based on q) when multiple encoding are accepted by client
        //@see http://stackoverflow.com/questions/3225136/http-what-is-the-preferred-accept-encoding-for-gzip-deflate
        for (String accepts : clientAccepts) {
            if (CONTENT_ENCODING_IDENTITY.equals(accepts.trim())) {
                return contentEncoding;
            } else if (EncodedStreamsFactory.SUPPORTED_ENCODINGS.containsKey(accepts.trim())) {
                contentEncoding = accepts; //get first matching encoding
                break;
            }
        }
        return contentEncoding;
    }

    private ServletResponse getResponse(ServletRequest request, ServletResponse response) {
        if (response.isCommitted() || request.getAttribute(PROCESSED_ATTR) != null) {
            LOGGER.trace("No Compression: Response committed or filter has already been applied");
            return response;
        }
        if (!(response instanceof HttpServletResponse) || !(request instanceof HttpServletRequest)) {
            LOGGER.trace("No Compression: non http request/response");
            return response;
        }
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String acceptEncoding = httpRequest.getHeader(HTTP_ACCEPT_ENCODING_HEADER);
        String contentEncoding = getAppropriateContentEncoding(acceptEncoding);
        if (contentEncoding == null) {
            LOGGER.trace("No Compression: Accept encoding is : {}", acceptEncoding);
            return response;
        }
        String requestURI = httpRequest.getRequestURI();
        if (!isURLAccepted(requestURI)) {
            LOGGER.trace("No Compression: For path: ", requestURI);
            return response;
        }
        String userAgent = httpRequest.getHeader(Constants.HTTP_USER_AGENT_HEADER);
        if (!isUserAgentAccepted(userAgent)) {
            LOGGER.trace("No Compression: For User-Agent: {}", userAgent);
            return response;
        }
        EncodedStreamsFactory encodedStreamsFactory = EncodedStreamsFactory.getFactoryForContentEncoding(contentEncoding);
        LOGGER.debug("Compressing response: content encoding : {}", contentEncoding);
        return new CompressedHttpServletResponseWrapper(httpResponse, encodedStreamsFactory, contentEncoding, compressionThreshold, this);
    }

}
