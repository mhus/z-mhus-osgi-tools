/**
 * Copyright (C) 2018 Mike Hummel (mh@mhus.de)
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
package de.mhus.osgi.api.util;

import java.util.function.BiConsumer;

import org.osgi.framework.ServiceReference;

public class MServiceTracker<T> extends AbstractServiceTracker<T> {

    private BiConsumer<ServiceReference<T>, T> addAction;
    private BiConsumer<ServiceReference<T>, T> removeAction;

    public MServiceTracker(
            Class<T> clazz,
            BiConsumer<ServiceReference<T>, T> addAction,
            BiConsumer<ServiceReference<T>, T> removeAction) {
        super(clazz);
        this.addAction = addAction;
        this.removeAction = removeAction;
    }

    @Override
    protected void removeService(ServiceReference<T> reference, T service) {
        removeAction.accept(reference, service);
    }

    @Override
    protected void addService(ServiceReference<T> reference, T service) {
        addAction.accept(reference, service);
    }
}
