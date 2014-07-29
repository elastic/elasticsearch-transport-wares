Servlet Transport for Elasticsearch
==================================

The wares transport plugin allows to use the REST interface over servlets. You don't really install this plugin,
but instead use the jar file in addition to elasticsearch itself in your web app using the maven repo:

```xml
<dependency>
    <groupId>org.elasticsearch</groupId>
    <artifactId>elasticsearch-transport-wares</artifactId>
    <version>2.4.0-SNAPSHOT</version>
</dependency>
```

* For master elasticsearch versions, look at [master branch](https://github.com/elasticsearch/elasticsearch-transport-wares/tree/master).
* For 1.x.x elasticsearch versions, look at [es-1.x branch](https://github.com/elasticsearch/elasticsearch-transport-wares/tree/es-1.x).
* For 1.3.x elasticsearch versions, look at [es-1.3 branch](https://github.com/elasticsearch/elasticsearch-transport-wares/tree/es-1.3).
* For 1.2.x elasticsearch versions, look at [es-1.2 branch](https://github.com/elasticsearch/elasticsearch-transport-wares/tree/es-1.2).
* For 0.90.x elasticsearch versions, look at [es-0.90 branch](https://github.com/elasticsearch/elasticsearch-transport-wares/tree/es-0.90).

|   Wares Transport Plugin    | elasticsearch         | Release date |
|-----------------------------|-----------------------|:------------:|
| 2.4.0-SNAPSHOT              | 1.x                   |  XXXX-XX-XX  |

Please read documentation relative to the version you are using:

* [2.4.0-SNAPSHOT](https://github.com/elasticsearch/elasticsearch-transport-wares/blob/es-1.x/README.md)

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
