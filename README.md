Servlet Transport for Elasticsearch
==================================

The wares transport plugin allows to use the REST interface over servlets. You don't really install this plugin,
but instead use the jar file in addition to elasticsearch itself in your web app using the maven repo.

|   Wares Transport Plugin    | elasticsearch         | Release date |
|-----------------------------|-----------------------|:------------:|
| 1.8.0-SNAPSHOT (master)     | 0.90.10 -> 0.90       |              |
| 1.7.0                       | 0.90.0 -> 0.90.9      |  2013-10-07  |
| 1.6.0                       | 0.90.0 -> 0.90.9      |  2013-02-26  |
| 1.5.0                       | 0.19.9 -> 0.20        |  2013-01-28  |
| 1.4.0                       | 0.19.9 -> 0.20        |  2012-08-23  |
| 1.3.0                       | 0.19.0 -> 0.19.8      |  2012-05-16  |
| 1.2.0                       | 0.19.0 -> 0.19.8      |  2012-02-13  |
| 1.1.0                       | 0.19.0 -> 0.19.8      |  2012-02-07  |
| 1.0.0                       | 0.18                  |  2011-12-05  |


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
