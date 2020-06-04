package cern.c2mon.daq.rest;
/******************************************************************************
 * Copyright (C) 2010- CERN. All rights not expressly granted are reserved.
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

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;

import cern.c2mon.daq.rest.config.WebConfigStartupTest;
import cern.c2mon.daq.rest.webaccess.RESTConnector;
import cern.c2mon.daq.test.GenericMessageHandlerTest;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by fritter on 22/01/16.
 */

@UseHandler(RestMessageHandler.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebConfigStartupTest.class)
@WebAppConfiguration
@TestPropertySource(properties = {
    "c2mon.daq.rest.autoConfiguration=false"
})
@Slf4j
public class RestMessageHandlerTest extends GenericMessageHandlerTest implements ApplicationContextAware {

  // reference to the instance of the handler to test
  RestMessageHandler theHandler;

  ApplicationContext context;

  @Override
  protected void beforeTest() throws Exception {
    log.info("entering beforeTest()..");

    // cast the reference (declared in the parent class) to the expected type
    theHandler = (RestMessageHandler) msgHandler;
    theHandler.setContext(context);
//    theHandler.setRequestDelegator(new RequestDelegator(theHandler.getEquipmentMessageSender(), theHandler.getEquipmentConfiguration(), theHandler.getEquipmentLogger(RESTMessageHandler.class), restController));

    log.info("leaving beforeTest()");
  }

  @Override
  protected void afterTest() throws Exception {
    log.info("entering afterTest()..");

//    theHandler.disconnectFromDataSource();

    log.info("leaving afterTest()");

  }

  @UseConf("e_rest_test1.xml")
  @Test
  @DirtiesContext
  public void restGetCommFaultSuccessful() {
    // messageSender mock setup
    Capture<Long> id = EasyMock.newCapture();
    Capture<String> tagName = EasyMock.newCapture();
    Capture<Boolean> val = EasyMock.newCapture();
    Capture<String> msg = EasyMock.newCapture();

    messageSender.sendCommfaultTag(EasyMock.captureLong(id), EasyMock.capture(tagName), EasyMock.captureBoolean(val), EasyMock.capture(msg));
    expectLastCall().once();

    // record the mock
    replay(messageSender);

    // call your handler's connectToDataSource() - in real operation the DAQ core will do it!
    try {
      theHandler.connectToDataSource();
    } catch (EqIOException e) {
      e.printStackTrace();
    }
    try {
      Thread.sleep(1_000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    verify(messageSender);

    // check commFault sending
    assertEquals(107211L, id.getValue().longValue());
    assertEquals(true, val.getValue());
    assertEquals("successfully connected", msg.getValue());

  }

  @UseConf("e_rest_test1.xml")
  @Test
  @DirtiesContext
  public void restGetSendSuccessful() throws InterruptedException {
    // create junit captures for the tag id, value and message (for the commmfault tag)
    Capture<SourceDataTagValue> sdtv = EasyMock.newCapture();

    messageSender.sendCommfaultTag(107211L, "E_REST_REST1:COMM_FAULT", true, "successfully connected");
    expectLastCall().once();
    messageSender.addValue(EasyMock.capture(sdtv));
    expectLastCall().once();
    replay(messageSender);

    // rest mock setup:
    MockRestServiceServer mockServer = MockRestServiceServer.createServer(RESTConnector.getRestTemplate());
    mockServer.expect(requestTo("http://www.testaddress.org/")).andExpect(method(HttpMethod.GET)).andRespond(withSuccess("resultSuccess", MediaType.TEXT_PLAIN));

    // call yout handler's connectToDataSource() - in real operation the DAQ core will do it!
    try {

      theHandler.connectToDataSource();
      Thread.sleep(6_000);

    } catch (EqIOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    verify(messageSender);
    mockServer.verify();

    assertEquals(sdtv.getValue().getId().longValue(), 54675L);
    assertEquals(sdtv.getValue().getValue(), "resultSuccess");
  }

  @UseConf("e_rest_test3.xml")
  @Test
  @DirtiesContext
  public void restGetWithExpressionSendSuccessful() throws InterruptedException {
    // create junit captures for the tag id, value and message (for the commmfault tag)
    Capture<SourceDataTagValue> sdtv = EasyMock.newCapture();

    messageSender.sendCommfaultTag(107211L, "E_REST_REST1:COMM_FAULT",true, "successfully connected");
    expectLastCall().once();
    messageSender.addValue(EasyMock.capture(sdtv));
    expectLastCall().once();
    replay(messageSender);

    // reply get Message + mock setup:
    MockRestServiceServer mockServer = MockRestServiceServer.createServer(RESTConnector.getRestTemplate());

    String jsonMessage = "{" +
        "        \"id\": 1701," +
        "        \"name\": \"Max Mustermann\"," +
        "        \"age\": 31" +
        "        }";

    mockServer.expect(requestTo("http://www.testaddress.org/")).andExpect(method(HttpMethod.GET)).andRespond(withSuccess(jsonMessage, MediaType.TEXT_PLAIN));

    // call your handler's connectToDataSource() - in real operation the DAQ core will do it!
    try {

      theHandler.connectToDataSource();
      Thread.sleep(6_000);

    } catch (EqIOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    verify(messageSender);
    mockServer.verify();

    assertEquals(sdtv.getValue().getId().longValue(), 54675L);
    assertEquals(sdtv.getValue().getValue(), 1701L);
  }

  @UseConf("e_rest_test2.xml")
  @Test
  @DirtiesContext
  public void restPostCommFaultSuccessful() {
    // messageSender mock setup
    Capture<Long> id = EasyMock.newCapture();
    Capture<String> tagName = EasyMock.newCapture();
    Capture<Boolean> val = EasyMock.newCapture();
    Capture<String> msg = EasyMock.newCapture();

    messageSender.sendCommfaultTag(EasyMock.captureLong(id), EasyMock.capture(tagName), EasyMock.captureBoolean(val), EasyMock.capture(msg));
    expectLastCall().once();

    // record the mock
    replay(messageSender);

    try {
      theHandler.connectToDataSource();
    } catch (EqIOException e) {
      e.printStackTrace();
    }
    verify(messageSender);

    // check commFault sending
    assertEquals(107211L, id.getValue().longValue());
    assertEquals(true, val.getValue());
    assertEquals("successfully connected", msg.getValue());
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.context = applicationContext;
  }
}
