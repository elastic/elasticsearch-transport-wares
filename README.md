Servlet Transport for Elasticsearch
==================================

The wares transport plugin allows to use the REST interface over servlets. You don't really install this plugin,
but instead use the jar file in addition to elasticsearch itself in your web app using the maven repo:

```xml
<dependency>
    <groupId>org.elasticsearch</groupId>
    <artifactId>elasticsearch-transport-wares</artifactId>
    <version>2.6.0</version>
</dependency>
```

| elasticsearch | Wares Transport Plugin | Documentation                                                                           |
|---------------|------------------------|-----------------------------------------------------------------------------------------|
| master        |  Build from source     | See below                                                                               |
|    es-1.7              |     2.7.0         | [2.7.0](https://github.com/elastic/elasticsearch-transport-wares/tree/v2.7.0/#version-270-for-elasticsearch-17)                  |
| es-1.6        |     2.6.0              | [2.6.0](https://github.com/elastic/elasticsearch-transport-wares/tree/v2.6.0/) |
| es-1.5        |     2.5.0              | [2.5.0](https://github.com/elastic/elasticsearch-transport-wares/tree/v2.5.0/) |
| es-1.4        |     2.4.1              | [2.4.1](https://github.com/elastic/elasticsearch-transport-wares/tree/v2.4.1/)          |
| es-1.3        |     2.3.0              | [2.3.0](https://github.com/elastic/elasticsearch-transport-wares/tree/v2.3.0/)          |
| es-1.2        |     2.2.0              | [2.2.0](https://github.com/elastic/elasticsearch-transport-wares/tree/v2.2.0/)          |
| es-1.0        |     2.0.0.RC1          | [2.0.0.RC1](https://github.com/elastic/elasticsearch-transport-wares/tree/v2.0.0.RC1/)  |
| es-0.90       |     1.8.0              | [1.8.0](https://github.com/elastic/elasticsearch-transport-wares/tree/v1.8.0/)          |


Tomcat configuration (CORS filter)
----------------------------------

The [Tomcat configuration](http://tomcat.apache.org/tomcat-7.0-doc/config/filter.html#CORS_Filter) to allow CORS is:

```xml
<filter>
  <filter-name>CorsFilter</filter-name>
  <filter-class>org.apache.catalina.filters.CorsFilter</filter-class>
</filter>
<filter-mapping>
  <filter-name>CorsFilter</filter-name>
  <url-pattern>/*</url-pattern>
</filter-mapping>
```

Node resource
-------------

The node is registered as a servlet context attribute under `elasticsearchNode` so it is easily accessible from other web resources if needed.

You can also preregister your own node using `elasticsearchNode` servlet context attribute. It will be used by the NodeServlet.


License
-------

    This software is licensed under the Apache 2 license, quoted below.

    Copyright 2009-2014 Elasticsearch <http://www.elasticsearch.org>

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy of
    the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
    License for the specific language governing permissions and limitations under
    the License.
