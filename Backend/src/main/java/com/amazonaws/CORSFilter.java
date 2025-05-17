package com.amazonaws;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CORSFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResp = (HttpServletResponse) response;
        httpResp.setHeader("Access-Control-Allow-Origin", "*");
        httpResp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        httpResp.setHeader("Access-Control-Allow-Headers", "Content-Type");

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
