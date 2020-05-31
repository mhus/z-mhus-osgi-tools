package de.mhus.osgi.services;

import org.osgi.service.component.annotations.Component;

import io.jaegertracing.Configuration.SenderConfiguration;
import io.jaegertracing.spi.Sender;
import io.jaegertracing.spi.SenderFactory;

@Component
public class JaegerSenderFactory implements SenderFactory {

	@Override
	public Sender getSender(SenderConfiguration senderConfiguration) {
		System.out.println("JaegerSenderFactory.getSender");
		return null;
	}

	@Override
	public String getType() {
		System.out.println("JaegerSenderFactory.getType");
		return null;
	}

}
