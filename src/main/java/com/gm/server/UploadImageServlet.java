package com.gm.server;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.model.Rpc.Thumbnail;
import com.gm.common.model.Rpc.Thumbnail.Builder;
import com.gm.common.net.ErrorCode;
import com.gm.server.model.User;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

public class UploadImageServlet extends APIServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
  private ImagesService imagesService = ImagesServiceFactory.getImagesService();
  
  //Input: url: /social/upload_image, name="image"
  public void handle(HttpServletRequest req, HttpServletResponse resp)
      throws ApiException, IOException {
 
    String key = stringNotEmpty(ParamKey.key.getValue(req),
        ErrorCode.auth_invalid_key_or_secret);

    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(req);
    List<BlobKey> blobKeys = blobs.get("image");
    checkNotNull(blobKeys,ErrorCode.social_upload_fail);
    BlobKey blobKey = blobKeys.get(0);
    checkNotNull(blobKey,ErrorCode.social_upload_fail);
    
    User user = dao.get(KeyFactory.stringToKey(key), User.class);
    ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);
    Thumbnail.Builder thumbnail = Thumbnail.newBuilder()
    							.setBlobKey(blobKey.getKeyString())
    							.setLargeUrl(imagesService.getServingUrl(options.imageSize(256)))
    							.setSmallUrl(imagesService.getServingUrl(options.imageSize(32)));
	user.setThumbnail(thumbnail);
	info("thumbnail created"+ thumbnail.getLargeUrl() + " and "+ thumbnail.getSmallUrl());
    dao.save(user);
  }
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
	//  this.requiresHmac = false;
    execute(req, resp);
  }
}

