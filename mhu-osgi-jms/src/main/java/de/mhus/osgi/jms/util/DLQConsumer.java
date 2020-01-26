/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.jms.util;

import javax.jms.JMSException;
import javax.jms.Message;

import de.mhus.lib.core.logging.Log;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.lib.jms.JmsDestination;
import de.mhus.lib.jms.SendNoAnswerException;
import de.mhus.lib.jms.ServerJms;
import de.mhus.osgi.api.jms.JmsDataSource;

public class DLQConsumer extends ServerJms {

    Log log = Log.getLog(DLQConsumer.class);

    public DLQConsumer() throws JMSException {
        this((JmsConnection) null);
    }

    public DLQConsumer(JmsDataSource con) throws JMSException {
        this(con.getConnection());
    }

    @SuppressWarnings("resource")
    public DLQConsumer(JmsConnection con) {
        super(new JmsDestination("ActiveMQ.DLQ", false).setConnection(con));
    }

    @Override
    public void receivedOneWay(Message msg) throws JMSException {
        log.w("Lost Message", msg);
    }

    @Override
    public Message received(Message msg) throws JMSException {
        log.w("Lost Message", msg);
        throw new SendNoAnswerException();
    }
}
