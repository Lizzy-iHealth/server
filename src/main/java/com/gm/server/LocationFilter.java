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

import com.gm.server.model.DAO;
import com.gm.server.model.User;
import com.google.appengine.api.datastore.GeoPt;
import com.google.common.base.Joiner;

public class LocationFilter  implements Filter{
  
  private static final Logger log = Logger.getLogger(LocationFilter.class.getName());
  private static final DAO dao = DAO.get();


  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    if (request instanceof HttpServletRequest
        && response instanceof HttpServletResponse) {
     try {
		handle((HttpServletRequest)request,(HttpServletResponse)response);
	} catch (ApiException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    }
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
    // TODO Auto-generated method stub

  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // TODO Auto-generated method stub
    
  }


  public void handle(HttpServletRequest req, HttpServletResponse resp)
      throws ApiException, IOException {
    // TODO Auto-generated method stub
	  String key = ParamKey.key.getValue(req);
    User user = dao.get(key, User.class);
    String contry = req.getHeader("X-AppEngine-Country");
    String region = req.getHeader("X-AppEngine-Region");
    String city = req.getHeader("X-AppEngine-City");
    String latLong[] = req.getHeader("X-AppEngine-CityLatLong").split(",");
    if(latLong!=null&&latLong.length==2){
    GeoPt geo = new GeoPt(Float.parseFloat((latLong[0])),Float.parseFloat(latLong[1]));
    user.setGeo(geo);

    }
 
    Joiner joiner = Joiner.on(",").skipNulls();
    String address = joiner.join(contry, region, city);
 
    user.setCheckinAddress(address);
    dao.save(user);
    
  }

}
