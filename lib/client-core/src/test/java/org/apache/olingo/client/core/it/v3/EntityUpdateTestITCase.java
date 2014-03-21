/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.client.core.it.v3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.LinkedHashMap;
import org.apache.olingo.client.api.communication.ODataClientErrorException;
import org.apache.olingo.client.api.communication.header.HeaderName;
import org.apache.olingo.client.api.communication.header.ODataHeaderValues;
import org.apache.olingo.client.api.communication.request.UpdateType;
import org.apache.olingo.client.api.communication.request.cud.ODataEntityUpdateRequest;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntityRequest;
import org.apache.olingo.client.api.communication.response.ODataEntityUpdateResponse;
import org.apache.olingo.client.api.domain.ODataEntity;
import org.apache.olingo.client.api.format.ODataPubFormat;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.junit.Test;

/**
 * This is the unit test class to check entity update operations.
 */
public class EntityUpdateTestITCase extends AbstractTestITCase {

  protected String getServiceRoot() {
    return testStaticServiceRootURL;
  }

  @Test
  public void mergeAsAtom() {
    final ODataPubFormat format = ODataPubFormat.ATOM;
    final URI uri = client.getURIBuilder(getServiceRoot()).
            appendEntitySetSegment("Product").appendKeySegment(-10).build();
    final String etag = getETag(uri);
    final ODataEntity merge = client.getObjectFactory().newEntity(TEST_PRODUCT_TYPE);
    merge.setEditLink(uri);
    updateEntityDescription(format, merge, UpdateType.MERGE, etag);
  }

  @Test
  public void mergeAsJSON() {
    final ODataPubFormat format = ODataPubFormat.JSON_FULL_METADATA;
    final URI uri = client.getURIBuilder(getServiceRoot()).
            appendEntitySetSegment("Product").appendKeySegment(-10).build();
    final String etag = getETag(uri);
    final ODataEntity merge = client.getObjectFactory().newEntity(TEST_PRODUCT_TYPE);
    merge.setEditLink(uri);
    updateEntityDescription(format, merge, UpdateType.MERGE, etag);
  }

  @Test
  public void patchAsAtom() {
    final ODataPubFormat format = ODataPubFormat.ATOM;
    final URI uri = client.getURIBuilder(getServiceRoot()).
            appendEntitySetSegment("Product").appendKeySegment(-10).build();
    final String etag = getETag(uri);
    final ODataEntity patch = client.getObjectFactory().newEntity(TEST_PRODUCT_TYPE);
    patch.setEditLink(uri);
    updateEntityDescription(format, patch, UpdateType.PATCH, etag);
  }

  @Test
  public void patchAsJSON() {
    final ODataPubFormat format = ODataPubFormat.JSON_FULL_METADATA;
    final URI uri = client.getURIBuilder(getServiceRoot()).
            appendEntitySetSegment("Product").appendKeySegment(-10).build();
    final String etag = getETag(uri);
    final ODataEntity patch = client.getObjectFactory().newEntity(TEST_PRODUCT_TYPE);
    patch.setEditLink(uri);
    updateEntityDescription(format, patch, UpdateType.PATCH, etag);
  }

  @Test
  public void replaceAsAtom() {
    final ODataPubFormat format = ODataPubFormat.ATOM;
    final ODataEntity changes = read(format, client.getURIBuilder(getServiceRoot()).
            appendEntitySetSegment("Car").appendKeySegment(14).build());
    updateEntityDescription(format, changes, UpdateType.REPLACE);
  }

  @Test
  public void replaceAsJSON() {
    final ODataPubFormat format = ODataPubFormat.JSON_FULL_METADATA;
    final ODataEntity changes = read(format, client.getURIBuilder(getServiceRoot()).
            appendEntitySetSegment("Car").appendKeySegment(14).build());
    updateEntityDescription(format, changes, UpdateType.REPLACE);
  }

  @Test
  public void patchLinkAsAtom() {
    patchLink(ODataPubFormat.ATOM);
  }

  @Test
  public void patchLinkAsJSON() {
    patchLink(ODataPubFormat.JSON_FULL_METADATA);
  }

  public void patchLink(final ODataPubFormat format) {
    final URI uri = client.getURIBuilder(getServiceRoot()).
            appendEntitySetSegment("Customer").appendKeySegment(-10).build();

    final ODataEntity patch =
            client.getObjectFactory().newEntity("Microsoft.Test.OData.Services.AstoriaDefaultService.Customer");
    patch.setEditLink(uri);

    // ---------------------------------------
    // Update to CustomerInfo(12)
    // ---------------------------------------
    URI customerInfoURI = client.getURIBuilder(getServiceRoot()).
            appendEntitySetSegment("CustomerInfo").appendKeySegment(12).build();

    patch.addLink(client.getObjectFactory().newEntityNavigationLink("Info", customerInfoURI));

    update(UpdateType.PATCH, patch, format, null);

    customerInfoURI = client.getURIBuilder(getServiceRoot()).
            appendEntitySetSegment("Customer").appendKeySegment(-10).appendNavigationSegment("Info").build();

    ODataEntityRequest req = client.getRetrieveRequestFactory().getEntityRequest(customerInfoURI);
    req.setFormat(format);

    ODataEntity newInfo = req.execute().getBody();

    assertEquals(Integer.valueOf(12),
            newInfo.getProperty("CustomerInfoId").getPrimitiveValue().<Integer>toCastValue());
    // ---------------------------------------

    // ---------------------------------------
    // Restore to CustomerInfo(11)
    // ---------------------------------------
    patch.getNavigationLinks().clear();

    customerInfoURI = client.getURIBuilder(getServiceRoot()).
            appendEntitySetSegment("CustomerInfo").appendKeySegment(11).build();
    read(format, customerInfoURI);

    patch.addLink(client.getObjectFactory().newEntityNavigationLink("Info", customerInfoURI));

    update(UpdateType.PATCH, patch, format, null);

    customerInfoURI = client.getURIBuilder(getServiceRoot()).
            appendEntitySetSegment("Customer").appendKeySegment(-10).appendNavigationSegment("Info").build();

    req = client.getRetrieveRequestFactory().getEntityRequest(customerInfoURI);
    req.setFormat(format);

    newInfo = req.execute().getBody();

    assertEquals(Integer.valueOf(11),
            newInfo.getProperty("CustomerInfoId").getPrimitiveValue().<Integer>toCastValue());
    // ---------------------------------------
  }

  private ODataEntityUpdateRequest buildMultiKeyUpdateReq(final ODataPubFormat format) {
    final LinkedHashMap<String, Object> multiKey = new LinkedHashMap<String, Object>();
    multiKey.put("FromUsername", "1");
    multiKey.put("MessageId", -10);
    final ODataEntity message = read(format, client.getURIBuilder(getServiceRoot()).
            appendEntitySetSegment("Message").appendKeySegment(multiKey).build());
    message.getAssociationLinks().clear();
    message.getNavigationLinks().clear();

    final boolean before = message.getProperty("IsRead").getPrimitiveValue().<Boolean>toCastValue();
    message.getProperties().remove(message.getProperty("IsRead"));
    message.getProperties().add(client.getObjectFactory().newPrimitiveProperty("IsRead",
            client.getPrimitiveValueBuilder().setValue(!before).
            setType(EdmPrimitiveTypeKind.Boolean).build()));

    return client.getCUDRequestFactory().getEntityUpdateRequest(UpdateType.MERGE, message);
  }

  private void mergeMultiKey(final ODataPubFormat format) {
    final ODataEntityUpdateResponse res = buildMultiKeyUpdateReq(format).execute();
    assertEquals(204, res.getStatusCode());
  }

  @Test
  public void mergeMultiKeyAsAtom() {
    mergeMultiKey(ODataPubFormat.ATOM);
  }

  @Test
  public void mergeMultiKeyAsJSON() {
    mergeMultiKey(ODataPubFormat.JSON_FULL_METADATA);
  }

  @Test
  public void updateReturnContent() {
    final ODataEntityUpdateRequest req = buildMultiKeyUpdateReq(client.getConfiguration().getDefaultPubFormat());
    req.setPrefer(ODataHeaderValues.preferReturnContent);

    final ODataEntityUpdateResponse res = req.execute();
    assertEquals(200, res.getStatusCode());
    assertEquals(ODataHeaderValues.preferReturnContent,
            res.getHeader(HeaderName.preferenceApplied).iterator().next());
    assertNotNull(res.getBody());
  }

  @Test
  public void concurrentModification() {
    final URI uri = client.getURIBuilder(getServiceRoot()).
            appendEntitySetSegment("Product").appendKeySegment(-10).build();
    String etag = getETag(uri);
    final ODataEntity product = client.getObjectFactory().newEntity(TEST_PRODUCT_TYPE);
    product.setEditLink(uri);
    updateEntityStringProperty("BaseConcurrency",
            client.getConfiguration().getDefaultPubFormat(), product, UpdateType.MERGE, etag);

    try {
      etag += "-invalidone";
      updateEntityStringProperty("BaseConcurrency",
              client.getConfiguration().getDefaultPubFormat(), product, UpdateType.MERGE, etag);
      fail();
    } catch (ODataClientErrorException e) {
      assertEquals(412, e.getStatusLine().getStatusCode());
    }
  }
}
