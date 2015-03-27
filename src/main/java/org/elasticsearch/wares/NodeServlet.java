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

import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.http.netty.NettyHttpServerTransport;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.node.internal.InternalNode;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.concurrent.CountDownLatch;

/**
 * A servlet that can be used to dispatch requests to elasticsearch. A {@link Node} will be started, reading
 * config from either <tt>/WEB-INF/elasticsearch.json</tt> or <tt>/WEB-INF/elasticsearch.yml</tt> but, by default,
 * with its internal HTTP interface disabled.
 * <p/>
 * <p>The node is registered as a servlet context attribute under <tt>elasticsearchNode</tt> so it is easily
 * accessible from other web resources if needed.
 * <p/>
 * <p>The servlet can be registered under a prefix URI, and it will automatically adjust to handle it.
 */
public class NodeServlet extends HttpServlet {

    public static String NODE_KEY = "elasticsearchNode";
    public static String NAME_PREFIX = "org.elasticsearch.";

    protected Node node;

    protected RestController restController;
    
    protected boolean detailedErrorsEnabled;

    @Override
    public void init() throws ServletException {
        final Object nodeAttribute = getServletContext().getAttribute(NODE_KEY);
        if (nodeAttribute == null || !(nodeAttribute instanceof InternalNode)) {
            if (nodeAttribute != null) {
                getServletContext().log(
                        "Warning: overwriting attribute with key \"" + NODE_KEY + "\" and type \""
                                + nodeAttribute.getClass().getName() + "\".");
            }
            getServletContext().log("Initializing elasticsearch Node '" + getServletName() + "'");
            ImmutableSettings.Builder settings = ImmutableSettings.settingsBuilder();
    
            InputStream resourceAsStream = getServletContext().getResourceAsStream("/WEB-INF/elasticsearch.json");
            if (resourceAsStream != null) {
                settings.loadFromStream("/WEB-INF/elasticsearch.json", resourceAsStream);
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
    
            resourceAsStream = getServletContext().getResourceAsStream("/WEB-INF/elasticsearch.yml");
            if (resourceAsStream != null) {
                settings.loadFromStream("/WEB-INF/elasticsearch.yml", resourceAsStream);
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
    
            Enumeration<String> enumeration = getServletContext().getAttributeNames();
    
            while (enumeration.hasMoreElements()) {
                String key = enumeration.nextElement();
    
                if (key.startsWith(NAME_PREFIX)) {
                    Object attribute = getServletContext().getAttribute(key);
    
                    if (attribute != null)
                        attribute = attribute.toString();
    
                    settings.put(key.substring(NAME_PREFIX.length()), (String) attribute);
                }
            }
    
            if (settings.get("http.enabled") == null) {
                settings.put("http.enabled", false);
            }
    
            node = NodeBuilder.nodeBuilder().settings(settings).node();
            getServletContext().setAttribute(NODE_KEY, node);
        } else {
            getServletContext().log("Using pre-initialized elasticsearch Node '" + getServletName() + "'");
            this.node = (InternalNode) nodeAttribute;
        }
        restController = ((InternalNode) node).injector().getInstance(RestController.class);        
        detailedErrorsEnabled = ((InternalNode) this.node).settings().getAsBoolean(NettyHttpServerTransport.SETTING_HTTP_DETAILED_ERRORS_ENABLED, true);
    }

    @Override
    public void destroy() {
        if (node != null) {
            getServletContext().removeAttribute(NODE_KEY);
            node.close();
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletRestRequest request = new ServletRestRequest(req);
        ServletRestChannel channel = new ServletRestChannel(request, resp, this.detailedErrorsEnabled);
        try {
            restController.dispatchRequest(request, channel);
            channel.latch.await();
        } catch (Exception e) {
            throw new IOException("failed to dispatch request", e);
        }
        if (channel.sendFailure != null) {
            throw channel.sendFailure;
        }
    }

    static class ServletRestChannel extends AbstractServletRestChannel {

        final HttpServletResponse resp;

        final CountDownLatch latch;

        IOException sendFailure;

        ServletRestChannel(RestRequest restRequest, HttpServletResponse resp, boolean detailedErrorsEnabled) {
            super(restRequest, detailedErrorsEnabled);
            this.resp = resp;
            this.latch = new CountDownLatch(1);
        }

        @Override
        protected HttpServletResponse getServletResponse() {
            return resp;
        }

        @Override
        protected void errorOccured(IOException e) {
            sendFailure = e;
        }

        @Override
        protected void finish() {
            latch.countDown();
        }
    }
}
