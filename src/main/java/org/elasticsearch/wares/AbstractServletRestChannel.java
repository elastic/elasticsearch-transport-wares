/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
 * Base implementation of RestChannel responsible for mapping a
 * RestResponse to an HttpServletResponse.
 */
abstract class AbstractServletRestChannel extends RestChannel {

    final RestRequest restRequest;

    protected AbstractServletRestChannel(RestRequest restRequest) {
        super(restRequest);
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
            resp.setContentLength(response.content().length());
            ServletOutputStream out = resp.getOutputStream();
            response.content().writeTo(out);
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
