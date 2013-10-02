/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.doma.jdbc.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link EntityType} の骨格実装です。
 * 
 * @author taedium
 * 
 */
public abstract class AbstractEntityType<E> implements EntityType<E> {

    /**
     * インスタンスを構築します。
     */
    protected AbstractEntityType() {
    }

    @Override
    public Map<String, Object> getCopy(E entity) {
        List<EntityPropertyType<E, ?>> propertyTypes = getEntityPropertyTypes();
        Map<String, Object> properties = new HashMap<String, Object>(
                propertyTypes.size());
        for (EntityPropertyType<E, ?> p : propertyTypes) {
            properties.put(p.getName(), p.getCopy(entity));
        }
        return properties;
    }

}
