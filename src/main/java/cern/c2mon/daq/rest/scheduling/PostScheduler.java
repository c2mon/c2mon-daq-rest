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
package cern.c2mon.daq.rest.scheduling;

import java.util.TimerTask;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.rest.address.RestPostAddress;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataTagQuality;
import cern.c2mon.shared.common.datatag.SourceDataTagQualityCode;
import cern.c2mon.shared.common.datatag.ValueUpdate;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;

/**
 * @author Franz Ritter
 */
@Slf4j
public class PostScheduler extends RestScheduler {

  public PostScheduler(IEquipmentMessageSender sender, IEquipmentConfiguration configuration) {
    super(sender, configuration);
  }

  /**
   * If the Controller received successfully a message from a client this
   * method handles all actions which needs to be done in that case. After
   * receiving a message the ReceiverTask of the id must be restarted.
   * Furthermore value must be cast to the given DataType of the
   * corresponding DataTag.
   * <p/>
   * If this things are successful the scheduler sends the value to the server
   * and gives a HttpStatus.OK return.
   *
   * @param id    The id of the corresponding DataTag which this message belongs to.
   * @param valueUpdate The Value for the DataTag
   *
   * @return Status based on the success of the processing of the value.
   */
  public HttpStatus sendValueToServer(Long id, ValueUpdate valueUpdate) {
    // send message if the id is known to the server(and daq)
    if (this.contains(id)) {
      ISourceDataTag tag = equipmentConfiguration.getSourceDataTag(id);
      RestPostAddress address = getAddress(id);
      ReceiverTask newTask = new ReceiverTask(id);

      equipmentMessageSender.update(id, valueUpdate);

      // reset the timer for the tag
      if (idToTask.get(id).cancel()) {
        idToTask.put(id, newTask);
        timer.purge();
        timer.schedule(newTask, address.getFrequency());
      }
      else if (address.getFrequency() != null) {
        idToTask.put(id, newTask);
        timer.schedule(newTask, address.getFrequency());
      }

      if (equipmentConfiguration.getSourceDataTag(id).getCurrentValue().getQuality().getQualityCode() == SourceDataTagQualityCode.UNSUPPORTED_TYPE) {
        log.warn("Value '{}' for tag #{} could not be converted to tag data type {}", valueUpdate, id, tag.getDataType());
        return HttpStatus.BAD_REQUEST;
      }
      return HttpStatus.OK;
    }
    else {
      log.warn("Received message for tag id #{} which is unknown.", id);
      return HttpStatus.BAD_REQUEST;
    }
  }
  
  public HttpStatus sendValueToServer(String name, ValueUpdate valueUpdate) {
    try {
      return sendValueToServer(getIdByName(name), valueUpdate);
    } catch (Exception e) {
      log.warn("Unexpected Problem. Received a message with identifier {}", name, e);
      return HttpStatus.BAD_REQUEST;
    }
  }

  public Long getIdByName(String name) {
    try {
      return equipmentConfiguration.getSourceDataTagIdByName(name);
    }
    catch (IllegalArgumentException e) {
      log.warn("Received message for tag {} which is unknown.", name);
      throw e;
    }
  }
  
  public boolean tagExist(String name) {
    try {
      equipmentConfiguration.getSourceDataTagIdByName(name);
      return true;
    }
    catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  public void addTask(Long id) {
    RestPostAddress address = getAddress(id);
    ReceiverTask task = new ReceiverTask(id);

    idToTask.put(id, task);

    // only add the task to the timer if the frequency ist set.
    if (address.getFrequency() != null) {
      timer.schedule(task, address.getFrequency());
    }
  }

  @Override
  /**
   * Simply send the last received tag value again
   */
  public void refreshDataTag(Long id) {
    log.info("Refresh of data tag not possible for POST address");
  }

  private RestPostAddress getAddress(Long id) {
    return (RestPostAddress) this.equipmentConfiguration.getSourceDataTag(id).getHardwareAddress();
  }

  /**
   * A instance of the SendRequestTask holds all information for sending a invalid message to the equipment.
   */
  class ReceiverTask extends TimerTask {

    private Long id;

    ReceiverTask(Long id) {
      this.id = id;
    }

    /**
     * This method is called after the interval of this task expires. If the
     * interval expire the client did not send a post message with the given
     * id to the REST daq. Because of that the daq thinks that the data is
     * invalid and an invalid message is end to the server.
     */
    @Override
    public void run() {
      // sending the reply to the server
      SourceDataTagQuality tagQuality = new SourceDataTagQuality(SourceDataTagQualityCode.DATA_UNAVAILABLE);
      tagQuality.setDescription("No value received in the given time interval of the DataTag-" + id);
      equipmentMessageSender.update(id, tagQuality);
    }
  }
}
