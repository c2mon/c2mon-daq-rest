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
package cern.c2mon.daq.rest.config;

import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.client.core.service.ConfigurationService;
import cern.c2mon.daq.config.DaqProperties;
import cern.c2mon.daq.rest.RestMessageHandler;

/**
 * Class is called at the very beginning to configure a new process in C2MON, if not yet done.
 *
 * @author Matthias Braeger
 */
@Configuration
@EnableConfigurationProperties(RestDaqProperties.class)
public class ConfigurationInitializer {

  @Getter
  String equipmentName;

  /**
   * Called by Spring before starting up the DAQ process
   * @param context Spring context
   * @return
   */
  @Bean
  public InitializingBean autoConfigureDAQ(ApplicationContext context) {
    return () -> configureProcessAndEquipment(context);
  }

  private void configureProcessAndEquipment(ApplicationContext context) {
    if (!isAutoConfiguration(context)) {
      return;
    }
    
    ConfigurationService configurationService = context.getBean(ConfigurationService.class);
    String processName = getProcessName(context);

    if (isProcessConfigured(configurationService, processName)) {
      return;
    }

    // P_ is often used as predicate for a process name
    equipmentName = "E_" + processName.replaceFirst("P_", "");

    configurationService.createProcess(processName);
    configurationService.createEquipment(processName, equipmentName, RestMessageHandler.class.getName());
  }

  /**
   * Checks whether the server has already a configuration for the given Process name
   * @param configurationService The C2MON configuration service
   * @param processName name of the DAQ process
   * @return <code>true</code>, if DAQ process is already configured
   */
  private boolean isProcessConfigured(ConfigurationService configurationService, String processName) {
    return configurationService.getProcessNames().stream().anyMatch(p -> p.getProcessName().equals(processName));
  }

  private String getProcessName(ApplicationContext context) {
    DaqProperties properties = context.getBean(DaqProperties.class);
    return properties.getName();
  }
  
  /**
   * Whether auto-configuration is enabled or not
   */
  private boolean isAutoConfiguration(ApplicationContext context) {
    RestDaqProperties properties = context.getBean(RestDaqProperties.class);
    return properties.isAutoConfiguration();
  }
}
