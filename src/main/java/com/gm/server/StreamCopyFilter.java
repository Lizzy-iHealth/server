package com.gm.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

public class StreamCopyFilter implements Filter {
  
  private static final Logger log = Logger.getLogger(StreamCopyFilter.class.getName());

  private static class StreamCacheRequest extends HttpServletRequestWrapper {
    
    private final byte[] content;

    public StreamCacheRequest(HttpServletRequest request, byte[] content) {
      super(request);
      this.content = content;
    }

    @Override
    public int getContentLength() {
      return content.length;
    }
    
    @Override
    public ServletInputStream getInputStream() throws IOException {
      return new ServletInputStream() {
        
        private int i = -1;
        
        @Override
        public int read() throws IOException {
          ++i;
          return i < content.length ? content[i] : -1;
        }
      };
    }
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // TODO Auto-generated method stub

  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    if (request instanceof HttpServletRequest
        && response instanceof HttpServletResponse) {
      byte[] content = readStream(request.getInputStream());
      log.info("read " + new String(content));
      chain.doFilter(new StreamCacheRequest((HttpServletRequest) request, content), response);
    } else {
      chain.doFilter(request, response);
    }
  }

  @Override
  public void destroy() {
    // TODO Auto-generated method stub

  }

  private static byte[] readStream(InputStream in) throws IOException {
    byte[] buf = new byte[1024];
    int count = 0;
    ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
    while ((count = in.read(buf)) != -1)
      out.write(buf, 0, count);
    return out.toByteArray();
  }

}
