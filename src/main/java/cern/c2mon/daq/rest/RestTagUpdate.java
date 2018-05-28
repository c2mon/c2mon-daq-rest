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
package cern.c2mon.daq.rest;

import java.util.Map;

import lombok.Data;

/**
 * Bean, representing the JSON message sent via HTTP POST
 * @author Matthias Braeger
 */
@Data
public class RestTagUpdate {
  /** DataTag name. This is a mandatory field! */
  private String name;

  /** The static description of the DataTag (optional) */
  private String description;

  /**
   * Expected update interval (in seconds).
   * In case an update is not received within this interval, the tag will be invalidated.
   * By default, no interval is specified.
   */
  private int postFrequency = -1;

  /** The value type, which can be any raw data type or any complex Java Class name (optional) */
  private String type;

  /** The new tag value as String (Optional) */
  private Object value;

  /** An optional description for the value update */
  private String valueDescription = "";

  /** Optional metadata arguments */
  private Map<String, Object> metadata;

  /**  time in milliseconds (optional) */
  private long timestamp = System.currentTimeMillis();
}
