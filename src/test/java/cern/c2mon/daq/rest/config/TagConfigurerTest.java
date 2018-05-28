/******************************************************************************
 * Copyright (C) 2010-2018 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.daq.rest.config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.client.core.service.ConfigurationService;
import cern.c2mon.daq.rest.RestTagUpdate;
import cern.c2mon.shared.client.configuration.ConfigConstants.Status;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TagConfigurerTest {

  private TagConfigurer tc;
  private ConfigurationService configurationService;

  @Before
  public void before() {
    configurationService = EasyMock.createNiceMock(ConfigurationService.class);
    tc = new TagConfigurer("E_test", configurationService);
  }

  @Test
  public void testGetType() {
    RestTagUpdate update = new RestTagUpdate();

    update.setValue(null);
    assertEquals(String.class, tc.getType(update));

    update.setValue("string");
    assertEquals(String.class, tc.getType(update));

    update.setValue("123");
    assertEquals(Double.class, tc.getType(update));

    update.setValue("123.2342");
    assertEquals(Double.class, tc.getType(update));

    update.setValue("123 2342");
    assertEquals(String.class, tc.getType(update));

    update.setValue("true");
    assertEquals(Boolean.class, tc.getType(update));

    update.setValue("TRUE");
    assertEquals(Boolean.class, tc.getType(update));

    update.setValue("false");
    assertEquals(Boolean.class, tc.getType(update));

    update.setValue("FALSE");
    assertEquals(Boolean.class, tc.getType(update));

    update.setValue(123);
    assertEquals(Double.class, tc.getType(update));

    update.setValue(123.234f);
    assertEquals(Double.class, tc.getType(update));

    update.setValue(123.234d);
    assertEquals(Double.class, tc.getType(update));

    update.setValue(123L);
    assertEquals(Double.class, tc.getType(update));

    update.setValue(123L);
    assertEquals(Double.class, tc.getType(update));

    Map<String, Object> jsonMap = new LinkedHashMap<>();
    jsonMap.put("foo", "bar");
    jsonMap.put("number", 123);
    jsonMap.put("boolean", true);
    update.setValue(jsonMap);
    assertEquals(HashMap.class, tc.getType(update));

    // test with type specified

    update.setValue("123");
    update.setType("String");
    assertEquals(String.class, tc.getType(update));

    // Case sensitive problem. Best effort instead which results in Double
    update.setType("string");
    assertEquals(Double.class, tc.getType(update));

    update.setType("sTrInG");
    assertEquals(Double.class, tc.getType(update));

    update.setType("Double");
    assertEquals(Double.class, tc.getType(update));

    update.setType("Float");
    assertEquals(Float.class, tc.getType(update));

    update.setType("Integer");
    assertEquals(Integer.class, tc.getType(update));

    update.setType("Boolean");
    assertEquals(Boolean.class, tc.getType(update));

    update.setType("java.util.HashMap");
    assertEquals(HashMap.class, tc.getType(update));

    update.setType("cern.c2mon.daq.rest.RestTagUpdate");
    assertEquals(RestTagUpdate.class, tc.getType(update));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateConfigurationWithEmptyArguments() {
    RestTagUpdate update = new RestTagUpdate();
    tc.createConfiguration(update);
  }

  @Test
  public void testCreateConfiguration() {
    RestTagUpdate update = new RestTagUpdate();
    update.setName("tagName");

    DataTag expected = DataTag.create("tagName", String.class, tc.getAddress(update.getPostFrequency())).build();
    DataTag actual = tc.createConfiguration(update);
    assertEquals(expected, actual);
    assertEquals("Expect 1 parameters in address", 1, actual.getAddress().getAddressParameters().size());
    assertNull("Expect no metadata", actual.getMetadata());

    update.setPostFrequency(20);
    expected = DataTag.create("tagName", String.class, tc.getAddress(update.getPostFrequency())).build();
    actual = tc.createConfiguration(update);
    assertEquals(expected, actual);
    assertEquals("Expect 2 parameters in address", 2, actual.getAddress().getAddressParameters().size());
    assertNull("Expect no metadata", actual.getMetadata());


    Map<String, Object> metadata = new LinkedHashMap<>();
    metadata.put("foo", "bar");
    metadata.put("number", 123);
    metadata.put("boolean", true);
    update.setMetadata(metadata);;
    actual = tc.createConfiguration(update);
    assertEquals("Expect 2 parameters in address", 2, actual.getAddress().getAddressParameters().size());
    assertEquals("Expect 3 metadata", 3, actual.getMetadata().getMetadata().size());
    assertEquals("Expect data type String", String.class.getName(), actual.getDataType());
  }

  @Test
  public void testcreateTag() {
    RestTagUpdate update = new RestTagUpdate();
    assertFalse(tc.createTag(update));

    update.setName("");
    assertFalse(tc.createTag(update));

    update.setName(null);
    assertFalse(tc.createTag(update));

    // Successful create
    update.setName("tagName");
    ConfigurationReport report = new ConfigurationReport();
    report.setStatus(Status.OK);
    EasyMock.expect(configurationService.createDataTag("E_test", tc.createConfiguration(update))).andReturn(report);
    EasyMock.replay(configurationService);
    assertTrue(tc.createTag(update));
    EasyMock.verify(configurationService);
    EasyMock.reset(configurationService);

    // Failure in create
    report = new ConfigurationReport();
    report.setStatus(Status.FAILURE);
    EasyMock.expect(configurationService.createDataTag("E_test", tc.createConfiguration(update))).andReturn(report);
    EasyMock.replay(configurationService);
    assertFalse(tc.createTag(update));
    EasyMock.verify(configurationService);
    EasyMock.reset(configurationService);
  }
}
