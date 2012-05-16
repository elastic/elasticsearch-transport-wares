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
import org.elasticsearch.rest.support.RestUtils;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Same as {@link NodeServlet} just uses async features.
 * <p/>
 * TODO: Needs to be properly tested, do we really use AsyncContext correctly?
 */
public class AsyncNodeServlet extends NodeServlet {

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final AsyncContext asyncContext = req.startAsync();
        ServletRestRequest request = new ServletRestRequest(req);
        AsyncServletRestChannel channel = new AsyncServletRestChannel(request, asyncContext);
        restController.dispatchRequest(request, channel);
    }

    static class AsyncServletRestChannel implements RestChannel {

        final RestRequest restRequest;

        final AsyncContext asyncContext;

        AsyncServletRestChannel(RestRequest restRequest, AsyncContext asyncContext) {
            this.restRequest = restRequest;
            this.asyncContext = asyncContext;
        }

        @Override
        public void sendResponse(RestResponse response) {
            HttpServletResponse resp = (HttpServletResponse) asyncContext.getResponse();
            resp.setContentType(response.contentType());
            if (RestUtils.isBrowser(restRequest.header("User-Agent"))) {
                resp.addHeader("Access-Control-Allow-Origin", "*");
                if (restRequest.method() == RestRequest.Method.OPTIONS) {
                    // also add more access control parameters
                    resp.addHeader("Access-Control-Max-Age", "1728000");
                    resp.addHeader("Access-Control-Allow-Methods", "PUT, DELETE");
                    resp.addHeader("Access-Control-Allow-Headers", "X-Requested-With");
                }
            }
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
                resp.setStatus(500);
            } finally {
                asyncContext.complete();
            }
        }
    }
}
