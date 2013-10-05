package com.gm.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.server.model.DAO;
import com.gm.server.model.Token;
import com.gm.server.model.User;
import com.gm.common.net.ErrorCode;
import com.google.common.base.Strings;

public enum API {

  login("/auth/", false) {
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp) throws ApiException {
      String phone = stringNotEmpty(ParamKey.phone.getValue(req), 
          ErrorCode.auth_invalid_phone);
      String password = stringNotEmpty(ParamKey.password.getValue(req), 
          ErrorCode.auth_invalid_password);
      String key = stringNotEmpty(ParamKey.key.getValue(req), 
          ErrorCode.auth_invalid_key_or_secret);
      String secret = stringNotEmpty(ParamKey.secret.getValue(req), 
          ErrorCode.auth_invalid_key_or_secret);
      
      User user = checkNotNull(dao.querySingle("phone", phone, User.class),
          ErrorCode.auth_phone_not_registered);
      check(password.equals(user.getPassword()), ErrorCode.auth_incorrect_password);
      
      user.login(secret, key);
      dao.save(user);
    }
  },

  register("/auth/", false) {
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp) throws ApiException {
      String phone = stringNotEmpty(ParamKey.phone.getValue(req), 
          ErrorCode.auth_invalid_phone);
      String password = stringNotEmpty(ParamKey.password.getValue(req), 
          ErrorCode.auth_invalid_password);
      String token = stringNotEmpty(ParamKey.token.getValue(req), 
          ErrorCode.auth_invalid_token);
      String key = stringNotEmpty(ParamKey.key.getValue(req), 
          ErrorCode.auth_invalid_key_or_secret);
      String secret = stringNotEmpty(ParamKey.secret.getValue(req), 
          ErrorCode.auth_invalid_key_or_secret);
      
      Token tokenStore = checkNotNull(dao.querySingle("phone", phone, Token.class),
          ErrorCode.auth_token_not_sent);
      check(token.equalsIgnoreCase(tokenStore.token), ErrorCode.auth_incorrect_token);
      
      check(!User.existsByPhone(phone), ErrorCode.auth_phone_registered); 
      User user = new User(phone,password,secret,key);

      dao.create(user);
    }
  },

  request_token("/auth/", false) {
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {
      String phone = stringNotEmpty(ParamKey.phone.getValue(req),
          ErrorCode.auth_invalid_phone);
      String msg = "1234"; // TODO: generate random token and send by sms
      Token token = dao.querySingle("phone", phone, Token.class);
      if (token == null) token = new Token(phone, msg);
      token.token = msg;
      dao.save(token);
    }
  };

  public final String url;
  public final boolean requiresHmac;

  private API(String urlPrefix, boolean requiresHmac) {
    url = urlPrefix + name();
    this.requiresHmac = requiresHmac;
  }

  private static final DAO dao = DAO.get();

  static void check(boolean condition, int error) throws ApiException {
    if (!condition)
      throw new ApiException(error);
  }

  static String stringNotEmpty(String string, int error) throws ApiException {
    check(!Strings.isNullOrEmpty(string), error);
    return string;
  }

  static <T> T checkNotNull(T reference, int error) throws ApiException {
    if (reference == null)
      throw new ApiException(error);
    return reference;
  }

  /**
   * Each API enum item will implement this method, handling individual business logic
   * 
   * @param req
   * @param resp
   * @throws ApiException
   * @throws IOException
   */
  public abstract void handle(HttpServletRequest req, HttpServletResponse resp) 
      throws ApiException, IOException;

  /**
   * The common logic for all HTTP servlets.
   * 
   * @param req
   * @param resp
   * @throws IOException
   */
  public void execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    if (requiresHmac) {
      // TODO: check hmac, if it fails, return error code 400
    }
    try {
      handle(req, resp);
      resp.setStatus(HttpServletResponse.SC_OK); // API executed successfully
    } catch (ApiException e) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // API failed on validation
      resp.getWriter().print(e.error);
    } catch (RuntimeException e) {
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);  // Unknown error
      e.printStackTrace(new PrintWriter(resp.getOutputStream())); // TODO: remove me when release
      info(e, "unknow API error %s", e.getMessage());
      throw e;
    }
  }
  
  static final void info(Throwable t, String msg, Object...args) {
    logger.log(Level.INFO, String.format(msg, args), t);
  }
  
  static final void info(String msg, Object...args) {
    logger.log(Level.INFO, String.format(msg, args));
  }
  
  private static final Logger logger = Logger.getLogger(API.class.getName());
}
