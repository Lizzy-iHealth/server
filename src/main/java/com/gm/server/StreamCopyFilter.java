package com.gm.server;

import java.io.ByteArrayOutputStream;
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

public class StreamCopyFilter implements Filter {
  
  private static final Logger log = Logger.getLogger(StreamCopyFilter.class.getName());

  private static class StreamCacheRequest extends HttpServletRequestWrapper {
    
    private static class CacheInputStream extends ServletInputStream {
      
      private final byte[] content;
      private int i;
      
      public CacheInputStream(byte[] content) {
        i = -1;
        this.content = content;
      }

      @Override
      public int read() throws IOException {
        ++i;
        return i < content.length ? content[i] : -1;
      }
      
    }
    
    private final byte[] content;
    private final Hashtable<?, ?> table;

    public StreamCacheRequest(HttpServletRequest request, byte[] content) {
      super(request);
      this.content = content;
      table = HttpUtils.parsePostData(content.length, new CacheInputStream(content));
      System.out.println(table);
    }

    @Override
    public int getContentLength() {
      return content.length;
    }
    
    @Override
    public String getParameter(String name) {
      String[] values = getParameterValues(name);
      return values == null ? null : values[0];
    }
    
    @Override
    public String[] getParameterValues(String name) {
      return (String[]) table.get(name);
    }
    
    @Override
    public ServletInputStream getInputStream() throws IOException {
      return new CacheInputStream(content);
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
