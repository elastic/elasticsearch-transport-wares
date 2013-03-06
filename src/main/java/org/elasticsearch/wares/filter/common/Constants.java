/*
 * Copyright 2010-2011 Rajendra Patil
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.elasticsearch.wares.filter.common;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * Common Class to hold the public static constant so that to share across the project
 *
 * @author rpatil
 */
public interface Constants {

    String TYPE_JS = "js";

    String TYPE_CSS = "css";

    String DEFAULT_CHARSET = "UTF-8";

    String EXT_JS = ".js";

    String EXT_JSON = ".json";

    String EXT_CSS = ".css";

    String MIME_OCTET_STREAM = "application/octet-stream";

    String MIME_JS = "text/javascript";

    String MIME_JSON = "application/json";

    String MIME_CSS = "text/css";

    String HEADER_EXPIRES = "Expires";

    String HEADER_LAST_MODIFIED = "Last-Modified";

    String PARAM_EXPIRE_CACHE = "_expirecache_";

    String PARAM_RESET_CACHE = "_resetcache_";

    String PARAM_SKIP_CACHE = "_skipcache_";

    String PARAM_DEBUG = "_dbg_";

    long DEFAULT_EXPIRES_MINUTES = 7 * 24 * 60; //7 days

    String DEFAULT_CACHE_CONTROL = "public";//

    int DEFAULT_COMPRESSION_SIZE_THRESHOLD = 128 * 1024; //128KB

    String HTTP_VARY_HEADER = "Vary";

    String HTTP_ACCEPT_ENCODING_HEADER = "Accept-Encoding";

    String HTTP_CONTENT_ENCODING_HEADER = "Content-Encoding";

    String HTTP_CACHE_CONTROL_HEADER = "Cache-Control";

    String HTTP_CONTENT_LENGTH_HEADER = "Content-Length";

    String HTTP_CONTENT_TYPE_HEADER = "Content-Type";

    String HTTP_ETAG_HEADER = "ETag";

    String HTTP_IF_NONE_MATCH_HEADER = "If-None-Match";

    String HTTP_IF_MODIFIED_SINCE = "If-Modified-Since";

    String CONTENT_ENCODING_GZIP = "gzip";

    String CONTENT_ENCODING_COMPRESS = "compress";

    String CONTENT_ENCODING_DEFLATE = "deflate";

    String CONTENT_ENCODING_IDENTITY = "identity";

    String HTTP_USER_AGENT_HEADER = "User-Agent";

    //HTTP dates are in one of these format
    //@see http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html

    String DATE_PATTERN_RFC_1123 = "EEE, dd MMM yyyy HH:mm:ss z";

    String DATE_PATTERN_RFC_1036 = "EEEEEEEEE, dd-MMM-yy HH:mm:ss z";

    String DATE_PATTERN_ANSI_C = "EEE MMM d HH:mm:ss yyyy";

    String DATE_PATTERN_HTTP_HEADER ="EEE, dd MMM yyyy HH:mm:ss zzz";

    String HEADER_X_OPTIMIZED_BY = "X-Optimized-By";

    String X_OPTIMIZED_BY_VALUE = "http://webutilities.googlecode.com";

    //HTTP locale - US
    Locale DEFAULT_LOCALE_US = Locale.US;

    //HTTP timeZone - GMT
    TimeZone DEFAULT_ZONE_GMT = TimeZone.getTimeZone("GMT");

    Pattern CSS_IMG_URL_PATTERN = Pattern.compile("[uU][rR][lL]\\s*\\(\\s*['\"]?([^('|\")]*)['\"]?\\s*\\)");

    //Map that holds Image path -> CSS files path that refers it
    Map<String, List<String>> CSS_IMG_REFERENCES = new HashMap();

}
