package com.gm.server;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.server.model.DAO;
import com.gm.server.model.Token;
import com.gm.server.model.User;
import com.google.common.base.Strings;

public enum API {

  login("/auth/", false) {
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp) throws ApiException {
      String phone = stringNotEmpty(ParamKey.phone.getValue(req), 
          Error.auth_invalid_phone);
      String password = stringNotEmpty(ParamKey.password.getValue(req), 
          Error.auth_invalid_password);
      String key = stringNotEmpty(ParamKey.key.getValue(req), 
          Error.auth_invalid_key_or_secret);
      String secret = stringNotEmpty(ParamKey.secret.getValue(req), 
          Error.auth_invalid_key_or_secret);
      
      User user = checkNotNull(dao.querySingle("phone", phone, User.class),
          Error.auth_phone_not_registered);
      check(password.equals(user.getPassword()), Error.auth_incorrect_password);
      
      user.setKey(key);
      user.setSecret(secret);
      dao.save(user);
    }
  },

  register("/auth/", false) {
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp) throws ApiException {
      String phone = stringNotEmpty(ParamKey.phone.getValue(req), 
          Error.auth_invalid_phone);
      String password = stringNotEmpty(ParamKey.password.getValue(req), 
          Error.auth_invalid_password);
      String token = stringNotEmpty(ParamKey.token.getValue(req), 
          Error.auth_invalid_token);
      String key = stringNotEmpty(ParamKey.key.getValue(req), 
          Error.auth_invalid_key_or_secret);
      String secret = stringNotEmpty(ParamKey.secret.getValue(req), 
          Error.auth_invalid_key_or_secret);
      
      check(dao.query(User.class).filterBy(Filters.eq("phone", phone)).prepare().count() == 0,
          Error.auth_phone_registered);
      
      Token tokenStore = checkNotNull(dao.querySingle("phone", phone, Token.class),
          Error.auth_invalid_token);
      check(token.equalsIgnoreCase(tokenStore.token), Error.auth_invalid_token);
      
      User user = new User();
      user.setPassword(password);
      user.setPhone(phone);
      user.setKey(key);
      user.setSecret(secret);
      dao.create(user);
    }
  },

  request_token("/auth/", false) {
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {
      String phone = stringNotEmpty(ParamKey.phone.getValue(req),
          Error.auth_invalid_phone);
      String msg = "1234"; // TODO: generate random token and send by sms
      Token token = new Token(phone, msg);
      dao.save(token);
      resp.getOutputStream().write(msg.getBytes());
    }
  };

  public final String url;
  public final boolean requiresHmac;

  private API(String urlPrefix, boolean requiresHmac) {
    url = urlPrefix + name();
    this.requiresHmac = requiresHmac;
  }

  private static final DAO dao = DAO.get();

  static void check(boolean condition, Error error) throws ApiException {
    if (!condition)
      throw new ApiException(error);
  }

  static String stringNotEmpty(String string, Error error) throws ApiException {
    check(!Strings.isNullOrEmpty(string), error);
    return string;
  }

  static <T> T checkNotNull(T reference, Error error) throws ApiException {
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
      resp.setStatus(200); // API executed successfully
    } catch (ApiException e) {
      resp.setStatus(424); // API failed on validation
      resp.getOutputStream().write(Integer.toString(e.error.code).getBytes());
    } catch (Exception e) {
      resp.setStatus(500);  // Unknown error
      // TODO: log the error
    }
  }
}
