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
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.service.ConfigurationService;
import cern.c2mon.daq.rest.RestTagUpdate;
import cern.c2mon.shared.client.configuration.ConfigConstants.Status;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.client.metadata.Metadata;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.type.TypeConverter;

/**
 * Provides methods to configure or update on-the-fly tags in C2MON
 * @author Matthias Braeger
 */
@Slf4j
public class TagConfigurer {

  private final ConfigurationService configurationService;

  private final String equipmentName;

  public TagConfigurer(String equipmentName) {
    this.equipmentName = equipmentName;
    this.configurationService = C2monServiceGateway.getConfigurationService();
  }

  /**
   * Constructor for testing purpose only!
   */
  protected TagConfigurer(String equipmentName, ConfigurationService configurationService) {
    this.equipmentName = equipmentName;
    this.configurationService = configurationService;
  }

  /**
   * Creates on the fly a tag in C2MON
   * @param tag The Rest POST message received
   * @return <code>true</code>, if configuration was successful
   */
  public boolean createTag(RestTagUpdate tag) {
    if (tag.getName() == null || tag.getName().isEmpty()) {
      return false;
    }

    log.info("Creating new tag with name {} ...", tag.getName());
    ConfigurationReport report = configurationService.createDataTag(
        equipmentName,
        createConfiguration(tag));

    if (report == null || report.getStatus() == null) {
      return false;
    }

    return report.getStatus() == Status.OK;
  }

  protected DataTag createConfiguration(RestTagUpdate tag) {
    DataTag dataTag = DataTag.create(tag.getName(), getType(tag), getAddress(tag.getPostFrequency())).build();
    dataTag.setDescription(tag.getDescription());

    if (tag.getMetadata() != null && !tag.getMetadata().isEmpty()) {
      Metadata meta = new Metadata();
      meta.setMetadata(tag.getMetadata());
      dataTag.setMetadata(meta);
    }

    return dataTag;
  }

  /**
   * Calculates the correct type of the message
   * @param tag The Tag update
   * @return a class reference
   */
  protected Class<?> getType(RestTagUpdate tag) {
    Class<?> clazz = TypeConverter.getType(tag.getType());

    if (clazz != null) {
      return clazz;
    }
    else if (tag.getValue() == null) {
      return String.class;
    }

    if (tag.getValue() instanceof String) {
      String value = (String) tag.getValue();

      if (StringUtils.isEmpty(value)) {
        return String.class;
      }
      else if (isNumber(value)){
        return Double.class;
      }
      else if (value.equalsIgnoreCase(Boolean.TRUE.toString()) || value.equalsIgnoreCase(Boolean.FALSE.toString())) {
        return Boolean.class;
      }
    }
    else if (tag.getValue() instanceof Number) {
      return Double.class;
    }
    else if (tag.getValue() instanceof Map) {
      return HashMap.class;
    }

    return tag.getValue().getClass();
  }

  private boolean isNumber(String value) {
    try {
      Double.valueOf(value);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  protected DataTagAddress getAddress(int postFrequency) {
    HashMap<String, String> address = new HashMap<>();
    address.put("mode", "POST");

    if (postFrequency > 0) {
      address.put("postFrequency", Integer.toString(postFrequency));
    }

    return new DataTagAddress(address);
  }
}
