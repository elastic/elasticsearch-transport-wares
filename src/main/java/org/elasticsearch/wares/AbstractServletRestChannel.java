/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.wares;

import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Base implementation of RestChannel responsible for mappig a
 * RestResponse to an HttpServletResponse.
 */
abstract class AbstractServletRestChannel implements RestChannel {

    final RestRequest restRequest;

    protected AbstractServletRestChannel(RestRequest restRequest) {
        this.restRequest = restRequest;
    }

    @Override
    public void sendResponse(RestResponse response) {
        HttpServletResponse resp = getServletResponse();
        resp.setStatus(response.status().getStatus());
        resp.setContentType(response.contentType());
        String opaque = restRequest.header("X-Opaque-Id");
        if (opaque != null) {
            resp.addHeader("X-Opaque-Id", opaque);
        }
        try {
            int contentLength = response.contentLength();
            if (response.prefixContent() != null) {
                contentLength += response.prefixContentLength();
            }
            if (response.suffixContent() != null) {
                contentLength += response.suffixContentLength();
            }

            resp.setContentLength(contentLength);

            ServletOutputStream out = resp.getOutputStream();
            if (response.prefixContent() != null) {
                out.write(response.prefixContent(), 0, response.prefixContentLength());
            }
            out.write(response.content(), 0, response.contentLength());
            if (response.suffixContent() != null) {
                out.write(response.suffixContent(), 0, response.suffixContentLength());
            }
            out.close();
        } catch (IOException e) {
            errorOccured(e);
        } finally {
            finish();
        }
    }

    /**
     * Provides the HttpServletResponse to send the response to.
     */
    protected abstract HttpServletResponse getServletResponse();

    /**
     * Is invoked if an error occurs.
     *
     * @param e the exception caught in {@link #sendResponse}.
     */
    protected abstract void errorOccured(IOException e);

    /**
     * Called after the response has been processed.
     */
    protected abstract void finish();
}
