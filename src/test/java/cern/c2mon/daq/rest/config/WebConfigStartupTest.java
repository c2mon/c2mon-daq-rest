/*******************************************************************************
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
 ******************************************************************************/

package cern.c2mon.daq.rest.config;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import cern.c2mon.daq.DaqStartup;

/**
 * Created by fritter on 23/05/16.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = DaqStartup.class)
@ComponentScan({"cern.c2mon.daq.rest"})
@TestPropertySource(properties = {
    "c2mon.daq.rest.autoConfiguration=false"
   ,"logging.level.root=trace"
   ,"spring.main.allow-bean-definition-overriding=true"
})
public class WebConfigStartupTest {
	@Autowired 
	Environment env;
	
	@Autowired
	ApplicationContext context;
	
	@Test
	public void testStartup() {
		assertTrue(context.containsBean("autoConfigureDAQ"));
		assertTrue(env.getProperty("c2mon.daq.rest.autoConfiguration").contentEquals("false"));
	}
}
