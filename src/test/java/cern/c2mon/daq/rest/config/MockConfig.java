package cern.c2mon.daq.rest.config;

import org.easymock.EasyMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import cern.c2mon.client.core.jms.JmsProxy;

@Configuration
public class MockConfig {
  
  @Bean
  @Primary
  public JmsProxy jmsProxy() {
    return EasyMock.createNiceMock(JmsProxy.class);
  }
}
