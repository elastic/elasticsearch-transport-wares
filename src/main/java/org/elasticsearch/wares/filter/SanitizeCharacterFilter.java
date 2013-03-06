package org.elasticsearch.wares.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Sanitize characters to fix URIs/IRIs so they can consist of
 * all US-ASCII characters and get handled gracefully by broken clients
 * who do not percent encode URIs/IRIs.
 * 
 * As long as http://tools.ietf.org/html/draft-ietf-iri-3987bis-11
 * is not released, this filter will protect URI/IRI sensitive services
 * from clients who are not implementing http://tools.ietf.org/html/rfc2396#section-2.4.3
 * 
 * More info:
 * http://blog.jclark.com/2008/11/what-allowed-in-uri.html
 * 
 */
public final class SanitizeCharacterFilter implements Filter {

    private String[] parameternames = new String[]{"q"};
    /**
     * 
     * These ASCII characters in HTTP parameters are an error in URI/IRI if not
     * percent-encoded. Some clients are broken, so fix it here.
     */
    private final static Map<Character, String> map = new HashMap<Character, String>() {

        {
            put('<', "%3C");
            put('>', "%3E");
            put('[', "%5B");
            put('\\', "%5C");
            put(']', "%5D");
            put('^', "%5E");
            put('`', "%60");
            put('{', "%7B");
            put('|', "%7C");
            put('}', "%7D");
            put('\u007f', "%7F");
        }
    };

    @Override
    public void init(FilterConfig filterConfig) {
        if (filterConfig != null && filterConfig.getInitParameter("parameternames") != null) {
            this.parameternames = filterConfig.getInitParameter("parameternames").split(",");
        }
    }

    private class FilteredRequest extends HttpServletRequestWrapper {

        public FilteredRequest(ServletRequest request) {
            super((HttpServletRequest) request);
        }

        @Override
        public String getQueryString() {
           return sanitize(super.getQueryString());
        }
        
        @Override
        public String getParameter(String paramName) {
            String value = super.getParameter(paramName);
            for (String p : parameternames) {
                if (p.equals(paramName)) {
                    value = sanitize(value);
                }
            }
            return value;
        }

        @Override
        public String[] getParameterValues(String paramName) {
            String values[] = super.getParameterValues(paramName);
            for (String p : parameternames) {
                if (p.equals(paramName)) {
                    for (int index = 0; index < values.length; index++) {
                        values[index] = sanitize(values[index]);
                    }
                }
            }
            return values;
        }

        @Override
        public Map getParameterMap() {
            Map values = super.getParameterMap();
            if (values != null) {
                for (String p : parameternames) {
                    if (values.containsKey(p) && values.get(p) instanceof String) {
                        values.put(p, sanitize((String) values.get(p)));
                    }
                }
            }
            return values;
        }

        private String sanitize(String input) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < input.length(); i++) {
                Character ch = input.charAt(i);
                String mapped = map.get(ch);
                sb.append(mapped != null ? mapped : ch);
            }
            return sb.toString();
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(new FilteredRequest(request), response);
    }

    @Override
    public void destroy() {
    }
}
