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

import org.springframework.web.client.RestClientException;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.rest.address.RestGetAddress;
import cern.c2mon.daq.rest.webaccess.RESTConnector;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataTagQuality;
import cern.c2mon.shared.common.datatag.ValueUpdate;
import cern.c2mon.shared.common.datatag.util.SourceDataTagQualityCode;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;
import cern.c2mon.shared.common.type.TypeConverter;


/**
 * This class managed the Timer threads to for making requests to the
 * Webservice. It creates the timer and adds Task to the Timer which are making
 * the request. Besides creating the task this class also manges the deleting
 * of obsolete tasks.
 *
 * @author Franz Ritter
 */
@Slf4j
public class GetScheduler extends RestScheduler {

  public GetScheduler(IEquipmentMessageSender sender, IEquipmentConfiguration configuration) {
    super(sender, configuration);
  }

  @Override
  public void addTask(Long id) {
    // create task
    RestGetAddress hardwareAddress = (RestGetAddress) this.equipmentConfiguration.getSourceDataTag(id)
        .getHardwareAddress();
    SendRequestTask task = new SendRequestTask(hardwareAddress.getUrl(), id, hardwareAddress.getJsonPathExpression());

    //save the Task in map and add it to the timer
    idToTask.put(id, task);

    timer.schedule(task, hardwareAddress.getFrequency(), hardwareAddress.getFrequency());
  }

  @Override
  public void refreshDataTag(Long id) {
    // get information:
    ISourceDataTag dataTag = this.equipmentConfiguration.getSourceDataTag(id);
    RestGetAddress hardwareAddress = (RestGetAddress) dataTag.getHardwareAddress();

    // http request:
    String restMessage = RESTConnector.sendAndReceiveRequest(hardwareAddress.getUrl());

    // sending the reply to the server:
    equipmentMessageSender.update(id, new ValueUpdate(restMessage, System.currentTimeMillis()));
  }

  /**
   * A instance of the SendRequestTask holds all information for sending a GET
   * request to a webservice.
   */
  class SendRequestTask extends TimerTask {

    private String url;

    private Long id;

    private String jsonPathExpression;

    SendRequestTask(String url, Long id, String jsonPathExpression) {
      this.url = url;
      this.id = id;
      this.jsonPathExpression = jsonPathExpression;
    }

    /**
     * Run method for the given Thread to make a get Request based on the given
     * url to a web service. Since this thread is managed my a Timer the run
     * method will be triggered in a given interval.
     * <p/>
     * After receiving the message from the web service this method also sends
     * the answer from service to the server.
     */
    @Override
    public void run() {

      SourceDataTag sdt = (SourceDataTag) equipmentConfiguration.getSourceDataTags().get(id);
      try {
        Object serverMessage;

        // request to the web service
        String restMessage = RESTConnector.sendAndReceiveRequest(url);
        Class<?> dataType = TypeConverter.getType(sdt.getDataType());

        // convert Message if jsonPathExpression is given
        if (jsonPathExpression != null) {
          serverMessage = JsonPath.parse(restMessage).read(jsonPathExpression, dataType);
        }
        else {
          serverMessage = restMessage;
        }

        // sending the reply to the server
        equipmentMessageSender.update(id, new ValueUpdate(serverMessage, System.currentTimeMillis()));

      } catch (RestClientException e) {
        SourceDataTagQuality tagQuality = new SourceDataTagQuality(SourceDataTagQualityCode.DATA_UNAVAILABLE);
        tagQuality.setDescription("Problem occurred at the REST get-operation (with the tag " + id + ") : "
            + e.getMessage());
        log.warn("Problem occurred at the REST get-operation: " + e.getMessage());
        equipmentMessageSender.update(id, tagQuality);

      }

    }

  }

}
