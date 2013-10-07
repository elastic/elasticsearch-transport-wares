Servlet Transport for ElasticSearch
==================================

The wares transport plugin allows to use the REST interface over servlets. You don't really install this plugin,
but instead use the jar file in addition to elasticsearch itself in your web app using the maven repo.

<table>
	<thead>
		<tr>
			<td>Servlet Transport Plugin</td>
			<td>ElasticSearch</td>
			<td>Release date</td>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>master (1.8.0-SNAPSHOT)</td>
			<td>0.90 -> master</td>
			<td></td>
		</tr>
		<tr>
			<td>1.7.0</td>
			<td>0.90 -> master</td>
			<td>07/10/2013</td>
		</tr>
		<tr>
			<td>1.6.0</td>
			<td>0.90 -> master</td>
			<td>26/02/2013</td>
		</tr>
		<tr>
			<td>1.5.0</td>
			<td>0.19.9 -> 0.90</td>
			<td>28/01/2013</td>
		</tr>
		<tr>
			<td>1.4.0</td>
			<td>0.19.9 -> 0.90</td>
			<td>23/08/2012</td>
		</tr>
		<tr>
			<td>1.3.0</td>
			<td>0.19.0 -> 0.19.8</td>
			<td>16/05/2012</td>
		</tr>
		<tr>
			<td>1.2.0</td>
			<td>0.19.0 -> 0.19.8</td>
			<td>13/02/2012</td>
		</tr>
		<tr>
			<td>1.1.0</td>
			<td>0.19.0 -> 0.19.8</td>
			<td>07/02/2012</td>
		</tr>
		<tr>
			<td>1.0.0</td>
			<td>0.18</td>
			<td>05/12/2011</td>
		</tr>
	</tbody>
</table>


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

    Copyright 2009-2012 Shay Banon and ElasticSearch <http://www.elasticsearch.org>

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy of
    the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
    License for the specific language governing permissions and limitations under
    the License.
