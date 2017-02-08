/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.daq.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClientException;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.conf.equipment.IDataTagChanger;
import cern.c2mon.daq.rest.controller.RestController;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataQuality;

/**
 * Entry point of the REST module. The connectToDataSource() method is called
 * at the DAQ startup phase. It initializes the Spring boot context, which
 * triggers the tag subscriptions for all GET REST requests.
 *
 * @author Franz Ritter
 */
@Slf4j
public class RestMessageHandler extends EquipmentMessageHandler {

  /**
   * The class which communicates with the RestServices
   */
  private RequestDelegator requestDelegator;

  @Override
  public void connectToDataSource() throws EqIOException {
    // class initialization
    RestController restController = getContext().getBean(RestController.class);
    log.trace("enter connectToDataSource()");

    requestDelegator = new RequestDelegator(getEquipmentMessageSender(), getEquipmentConfiguration(), restController);

    IDataTagChanger dataTagChanger = new RestDataTagChanger(getEquipmentMessageSender(), requestDelegator);
    getEquipmentConfigurationHandler().setDataTagChanger(dataTagChanger);


    // Adding DataTags to the equipment
    for (ISourceDataTag dataTag : getEquipmentConfiguration().getSourceDataTags().values()) {
      try {
        requestDelegator.addDataTag(dataTag);
      }
      catch (IllegalArgumentException ex) {
        log.warn("DataTag " + dataTag.getId() + " not configurable - Reason: " + ex.getMessage());
        getEquipmentMessageSender().sendInvalidTag(dataTag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, "DataTag " +
                dataTag.getId() + " not configurable - Reason: " + ex.getMessage());
      }
    }

    getEquipmentMessageSender().confirmEquipmentStateOK("successfully connected");
    log.info("connectToDataSource succeeded");
  }

  @Override
  public void disconnectFromDataSource() throws EqIOException {
    log.trace("Entering disconnectFromDataSource method.");

    for (ISourceDataTag dataTag : getEquipmentConfiguration().getSourceDataTags().values()) {
      try {
        requestDelegator.removeDataTag(dataTag);
      }
      catch (IllegalArgumentException ex) {
        log.warn("Problem caused by disconnecting: " + ex.getMessage());
      }
    }

    log.info("Equipment disconnected.");
    log.trace("Leaving disconnectFromDataSource method.");
  }

  @Override
  public void refreshAllDataTags() {
    log.trace("Entering refreshAllDataTags method.");

    for (ISourceDataTag dataTag : getEquipmentConfiguration().getSourceDataTags().values()) {
      try {
        requestDelegator.refreshDataTag(dataTag.getId());
      }
      catch (IllegalArgumentException ex) {
        log.warn("Problem causes by refreshing. Reason: " + ex.getMessage());
        getEquipmentMessageSender().sendInvalidTag(dataTag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, "Problem " +
                "causes by refreshing. Reason: " + ex.getMessage());
      }
      catch (RestClientException ex) {
        log.warn("Connection problem causes by refreshing:: " + ex.getMessage());
        getEquipmentMessageSender().sendInvalidTag(dataTag, SourceDataQuality.DATA_UNAVAILABLE, "Connection problem " +
                "causes by refreshing:: " + ex.getMessage());
      }
    }

    log.trace("Leaving refreshAllDataTags method.");
  }

  @Override
  public void refreshDataTag(long dataTagId) {
    log.trace("Entering refreshDataTag method.");
    try {
      requestDelegator.refreshDataTag(dataTagId);
    }
    catch (IllegalArgumentException ex) {
      log.warn("Problem causes by refreshing. Reason: " + ex.getMessage());
      getEquipmentMessageSender().sendInvalidTag(getEquipmentConfiguration().getSourceDataTag(dataTagId),
              SourceDataQuality.INCORRECT_NATIVE_ADDRESS, "Problem causes by refreshing. Reason: " + ex.getMessage());
    }
    catch (RestClientException ex) {
      log.warn("Connection problem causes by refreshing:: " + ex.getMessage());
      getEquipmentMessageSender().sendInvalidTag(getEquipmentConfiguration().getSourceDataTag(dataTagId),
              SourceDataQuality.DATA_UNAVAILABLE, "Connection problem causes by refreshing:: " + ex.getMessage());
    }

    log.trace("Leaving refreshDataTag method.");
  }
}
