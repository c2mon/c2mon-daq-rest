package cern.c2mon.daq.rest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties("c2mon.daq.rest")
public class RestDaqProperties {
  
  /** Enables/disables auto-configuration feature of REST DAQ */
  private boolean autoConfiguration = true;
}
