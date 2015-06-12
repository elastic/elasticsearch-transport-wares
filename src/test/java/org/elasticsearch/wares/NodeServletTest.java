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


import org.elasticsearch.node.Node;
import org.elasticsearch.test.ElasticsearchTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;

import static org.hamcrest.Matchers.*;

public class NodeServletTest extends ElasticsearchTestCase {

    protected ServletTester tester;

    @Before
    public void initServletTester() throws Exception {
        String tmpPath = tmpPaths()[0];
        tester = new ServletTester();
        tester.setContextPath("/elasticsearch");
        tester.addServlet(NodeServlet.class, "/*");
        tester.setAttribute(NodeServlet.NAME_PREFIX + "path.home", tmpPath);
        tester.setAttribute(NodeServlet.NAME_PREFIX + "node.name", "wares-node");
        tester.setAttribute(NodeServlet.NAME_PREFIX + "cluster.name", "wares-cluster");
        tester.start();
    }

    @After
    public void destroyServletTester() throws Exception {
        if (tester != null) {
            tester.stop();
            // We need to wait a while for all threads to be stopped
            Thread.sleep(1000L);
        }
    }

    @Test
    public void elasticsearchRunning() throws Exception {
        elasticsearchEndpointTester("/elasticsearch/", 200, "cluster_name");
        elasticsearchEndpointTester("/elasticsearch/_cat/", 200, "_cat/master");
        elasticsearchEndpointTester("/elasticsearch/_cat/health", 200, "wares-cluster");
        elasticsearchEndpointTester("/elasticsearch/_cat/nodes", 200, "wares-node");
        Object attribute = tester.getContext().getServletContext().getContext("/elasticsearch").getAttribute(NodeServlet.NODE_KEY);
        assertThat(attribute, instanceOf(Node.class));
    }

    protected void elasticsearchEndpointTester(String url, int expectedCode, String expectedContent) throws Exception {
        HttpTester request = new HttpTester();
        request.setMethod("GET");
        request.setHeader("Host","tester");
        request.setURI(url);
        request.setVersion("HTTP/1.0");

        HttpTester response = new HttpTester();
        response.parse(tester.getResponses(request.generate()));

        assertThat(response.getStatus(), is(expectedCode));
        assertThat(response.getContent(), containsString(expectedContent));
    }

}
