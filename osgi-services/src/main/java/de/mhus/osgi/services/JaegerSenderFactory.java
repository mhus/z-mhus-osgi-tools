/**
 * Copyright (C) 2020 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
