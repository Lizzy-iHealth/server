package com.gm.server.push;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Pusher {

  // server key configured at https://cloud.google.com/console#/project
  private static final String API_KEY = "AIzaSyBRQQyJK9k-dDAsgAJHS4W9jLDdXdBo75A";
  
  // GCM service URL for web server
  private static final String SERVICE_URL = "https://android.googleapis.com/gcm/send";
  
  // The android devices will sent over their registration ids based on the API_KEY. Our server
  // will associated those registration ids with corresponding logged in users. If we want to send
  // updates to users, we use their registration ids to push messages.
  private final String[] registrationIds;
  
  public Pusher(String... registrationIds) {
    this.registrationIds = registrationIds;
  }
  
  /**
   * 
   * @return push statuses for registration ids 
   * @throws JSONException 
   * @throws IOException 
   */
  public Result[] push(Map<String, String> data) throws JSONException, IOException {
    JSONObject req = new JSONObject();
    req.put("data", new JSONObject(data));
    JSONArray ids = new JSONArray();
    for (String id : registrationIds) {
      ids.put(id);
    }
    req.put("registration_ids", ids);
    // TODO: add other parameters, such as "time_to_live", "delay_while_idle"
    
    String msg = doHttpPost(req.toString());
    
    JSONObject resp = new JSONObject(msg);
    JSONArray array = resp.getJSONArray("results");
    Result[] results = new Result[array.length()];
    for (int i = 0; i < array.length(); ++i) {
      JSONObject obj = array.getJSONObject(i);
      results[i] = new Result(obj.has("error") ? obj.getString("error") : null,
          obj.has("registration_id") ? obj.getString("registration_id") : null);
    }
    return results;
  }
  
  private String doHttpPost(String data) throws IOException {
    byte[] payload = data.getBytes();
    
    HttpURLConnection conn = (HttpURLConnection) new URL(SERVICE_URL).openConnection();
    conn.setRequestProperty("Authorization", "key=" + API_KEY);
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setRequestMethod("POST");
    conn.setDoOutput(true);
    conn.setUseCaches(false);
    // conn.setFixedLengthStreamingMode(payload.length);
    conn.getOutputStream().write(payload);
    conn.getOutputStream().flush();
    int status = conn.getResponseCode();
    return new String(readStream(status == 200 ? conn.getInputStream() : conn.getErrorStream()));
  }
  
  private static byte[] readStream(InputStream in) throws IOException {
    byte[] buf = new byte[1024];
    int count = 0;
    ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
    while ((count = in.read(buf)) != -1)
      out.write(buf, 0, count);
    return out.toByteArray();
  }
  
  public static final class Result {
    private final String updatedRegistrationId;
    private final String error;
    
    public Result(String error, String regId) {
      this.updatedRegistrationId = regId;
      this.error = error;
    }
    
    public boolean isSuccessful() {
      return error == null;
    }
    
    public String getUpdatedRegistrationId() {
      return updatedRegistrationId;
    }
  }
}
