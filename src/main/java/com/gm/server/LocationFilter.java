package com.gm.server;


import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;

public class LocationFilter extends APIServlet implements Filter{
  
  private static final Logger log = Logger.getLogger(LocationFilter.class.getName());



  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    if (request instanceof HttpServletRequest
        && response instanceof HttpServletResponse) {
      execute((HttpServletRequest)request,(HttpServletResponse)response);
    }
  }

  @Override
  public void destroy() {
    // TODO Auto-generated method stub

  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void handle(HttpServletRequest req, HttpServletResponse resp)
      throws ApiException, IOException {
    // TODO Auto-generated method stub
    
  }

}
