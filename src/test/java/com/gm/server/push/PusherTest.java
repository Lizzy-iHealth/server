package com.gm.server.push;

import static org.junit.Assert.*;

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
  
  @Test
  public void testPushOK() throws Exception {
    Map<String, String> data = new HashMap<String, String>();
    data.put("test", "value");
    
    String ids[] = {"APA91bFWFxgXtR57p3Jj2umYFFV8-U1N9PKKLQydheMybhU_2DxdngHbuYijPRHc1Y2a9dLkhdu9pyLCNd61uRBn9d2i6dggDxjMSkADyAET6rHGCQ9PFQi7HAc_hIsRBA_Z4LAkUddPSH9NxTvIjJZe-ImYHpoNgA"};
    Result[] result = new Pusher(ids).push(data);
    
    assertEquals(ids.length, result.length);
    for (Result r : result) {
      assertTrue(r.isSuccessful());
      assertNull(r.getUpdatedRegistrationId());
    }
  }
}