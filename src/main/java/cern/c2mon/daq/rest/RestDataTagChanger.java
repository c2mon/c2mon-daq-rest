/******************************************************************************
 * Copyright (C) 2010-2018 CERN. All rights not expressly granted are reserved.
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

import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.common.conf.equipment.IDataTagChanger;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataTagQuality;
import cern.c2mon.shared.common.datatag.util.SourceDataTagQualityCode;
import cern.c2mon.shared.daq.config.ChangeReport;

/**
 * @author Franz Ritter
 */

/**
 * Handles the DataTagChange operations triggered by a new Configuration.
 */
@Slf4j
public class RestDataTagChanger implements IDataTagChanger {

  private IEquipmentMessageSender equipmentMessageSender;


  private RequestDelegator requestDelegator;

  public RestDataTagChanger(IEquipmentMessageSender sender, RequestDelegator requestDelegator) {
    this.equipmentMessageSender = sender;
    this.requestDelegator = requestDelegator;
  }

  @Override
  public void onAddDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
    log.trace("Entering onAddDataTag method.");

    try {
      requestDelegator.addDataTag(sourceDataTag);
      changeReport.appendInfo("URL successful tested and added");
    }
    catch (IllegalArgumentException ex) {
      log.warn("DataTag #{} not configurable - Reason: {}", sourceDataTag.getId(), ex.getMessage());
      equipmentMessageSender.update(sourceDataTag.getId(), 
          new SourceDataTagQuality(SourceDataTagQualityCode.INCORRECT_NATIVE_ADDRESS, 
              "Error configuring the provided address - Reason: " + ex.getMessage()));
      
      changeReport.appendError("DataTag " + sourceDataTag.getId() + " cannot be added to the Equipment - Reason: " + ex.getMessage());
    }

    log.trace("Leaving onAddDataTag method.");
  }

  @Override
  public void onRemoveDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
    log.trace("Entering onRemoveDataTag method.");

    try {
      requestDelegator.removeDataTag(sourceDataTag);
    }
    catch (IllegalArgumentException ex) {
      log.warn("Problem caused by removing of DataTag " + sourceDataTag.getId() + ": " + ex.getMessage());
      changeReport.appendWarn("Problem caused by removing of the DataTag " + sourceDataTag.getId() + ": " + ex
              .getMessage());
    }

    log.trace("Leaving onRemoveDataTag method.");
  }

  @Override
  public void onUpdateDataTag(ISourceDataTag sourceDataTag, ISourceDataTag oldSourceDataTag, ChangeReport
          changeReport) {

    try {
      requestDelegator.updateDataTag(sourceDataTag, oldSourceDataTag);
    }
    catch (IllegalArgumentException ex) {
      log.warn("Problem caused by updating of of DataTag " + sourceDataTag.getId() + ": " + ex.getMessage
              ());
      changeReport.appendError("Problem caused by updating of of DataTag " + sourceDataTag.getId() + ": " + ex
              .getMessage());
    }
  }
}
