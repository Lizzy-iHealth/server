package com.gm.server.push;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.gm.server.push.Pusher.Result;

public class PusherTest {

  @Test
  public void testPush() throws Exception {
    Map<String, String> data = new HashMap<String, String>();
    data.put("test", "value");
    
    String ids[] = {"1", "2"};
    Result[] result = new Pusher(ids).push(data);
    
    assertEquals(ids.length, result.length);
    for (Result r : result) {
      assertFalse(r.isSuccessful());
      assertNull(r.getUpdatedRegistrationId());
    }
  }
}