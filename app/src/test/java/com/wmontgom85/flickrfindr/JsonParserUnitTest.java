package com.wmontgom85.flickrfindr;

import com.wmontgom85.flickrfindr.api.jsonadapter.FlickrJsonAdapter;
import com.wmontgom85.flickrfindr.api.response.ImageSearchResponse;
import com.wmontgom85.flickrfindr.repo.model.FlickrImage;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class JsonParserUnitTest {
    @Test
    public void json_parser_test() {
        String sampleJson = "{\"photos\":{\"page\":1,\"pages\":140,\"perpage\":25,\"total\":\"3500\",\"photo\":[{\"id\":\"48628492297\",\"owner\":\"10655856@N04\",\"secret\":\"f6a837dde1\",\"server\":\"65535\",\"farm\":66,\"title\":\"Geraldine Farrar in Madam Butterfly\",\"ispublic\":1,\"isfriend\":0,\"isfamily\":0},{\"id\":\"48625304623\",\"owner\":\"88326925@N08\",\"secret\":\"fb62685aa3\",\"server\":\"65535\",\"farm\":66,\"title\":\"Bob, Alice and Yaz the Golden Retriever (#86\\/100)\",\"ispublic\":1,\"isfriend\":0,\"isfamily\":0},{\"id\":\"48607064017\",\"owner\":\"150490242@N06\",\"secret\":\"dc7df8c1ae\",\"server\":\"65535\",\"farm\":66,\"title\":\"I Will Minimalist Logo Design Within 24 Hours + For Your Business + Design Unique Minimalist Business Logo Design +\",\"ispublic\":1,\"isfriend\":0,\"isfamily\":0}]}}";

        FlickrJsonAdapter adapt = new FlickrJsonAdapter();

        ImageSearchResponse irs = (ImageSearchResponse) adapt.readFrom(sampleJson);

        Assert.assertEquals(irs.getPage(), 1);
        Assert.assertEquals(irs.getPages(), 140);
        Assert.assertEquals(irs.getPerPage(), 25);
        Assert.assertEquals(irs.getTotal(), 3500);
        Assert.assertEquals(irs.getPhotos().size(), 3);

        FlickrImage img1 = irs.getPhotos().get(0);

        Assert.assertEquals(img1.getId(), "48628492297");
        Assert.assertEquals(img1.getOwner(), "10655856@N04");
        Assert.assertEquals(img1.getSecret(), "f6a837dde1");
        Assert.assertEquals(img1.getServer(), 65535);
        Assert.assertEquals(img1.getFarm(), 66);
        Assert.assertEquals(img1.getTitle(), "Geraldine Farrar in Madam Butterfly");
        Assert.assertEquals(img1.isPublic(), 1);
        Assert.assertEquals(img1.isFriend(), 0);
        Assert.assertEquals(img1.isFamily(), 0);

        String thumb = String.format("https://farm%s.staticflickr.com/%s/%s_%s_m.jpg", img1.getFarm(), img1.getServer(),
                img1.getId(), img1.getSecret());

        String large = String.format("https://farm%s.staticflickr.com/%s/%s_%s_b.jpg", img1.getFarm(), img1.getServer(),
                img1.getId(), img1.getSecret());

        Assert.assertEquals(img1.getThumbnail(), thumb);
        Assert.assertEquals(img1.getLargeImage(), large);
    }
}
