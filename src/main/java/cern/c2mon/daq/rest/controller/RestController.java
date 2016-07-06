/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.daq.rest.controller;

import cern.c2mon.daq.rest.scheduling.PostScheduler;
import cern.c2mon.shared.common.type.TypeConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * This class is responsible for getting 'REST-POST' requests from clients.
 *
 * @author Franz Ritter
 */
@Controller
@Slf4j
public class RestController {

  private PostScheduler postScheduler;

  /**
   * The method receives 'rest-post' queries.
   * In order to ensure that the message decoding is done right the user of the post query needs to specify the
   * header.
   * The Header needs to be 'Content-Type: text/plain' or 'text/json'.
   * The safest way to use this post is to use the type 'plane'.
   *
   * In order to send the message itself the data must be specified in the body of the HTTP request.
   *
   * @param identifier The identifier of the tag. This can either be the name of the tag or the id.
   * @param value      Tha value of the message which need to be specified in the body.
   * @return The status of the request. If the request was successful to the server the request will be HttpStatus.OK.
   */
  @RequestMapping(value = "/tags/{identifier}", method = RequestMethod.POST)
  public HttpStatus postHandler(@PathVariable("identifier") String identifier, @RequestBody String value) {
    Long tagId;

    // Check if the incoming identifier is Long - if not we assume it is a string(name)
    try {
      tagId = Long.parseLong(identifier);

      // Number caste failed: check DataTags for names
    } catch (NumberFormatException e) {
      tagId = postScheduler.getIdByName(identifier);

    } catch (Exception e) {
      log.warn("Unexpected Problem: Received a message with the identifier:" + identifier + ":", e);
      return HttpStatus.BAD_REQUEST;
    }

    HttpStatus status = postScheduler.sendValueToServer(tagId, value);
    return status;
  }

  public void setPostScheduler(PostScheduler scheduler) {
    this.postScheduler = scheduler;
  }

}
