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

import org.elasticsearch.rest.RestRequest;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
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

    static class AsyncServletRestChannel extends AbstractServletRestChannel {

        final AsyncContext asyncContext;

        AsyncServletRestChannel(RestRequest restRequest, AsyncContext asyncContext) {
            super(restRequest);
            this.asyncContext = asyncContext;
        }

        @Override
        protected HttpServletResponse getServletResponse() {
            return (HttpServletResponse) asyncContext.getResponse();
        }

        @Override
        protected void errorOccured(IOException e) {
            getServletResponse().setStatus(500);
        }

        @Override
        protected void finish() {
            asyncContext.complete();
        }
    }
}
