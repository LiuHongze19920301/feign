/*
 * Copyright 2012-2023 The Feign Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package feign.querymap;

import feign.Param;
import feign.QueryMapEncoder;
import feign.codec.EncodeException;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 查询map编码器
 * <p>
 * the query map will be generated using java beans accessible getter property as query parameter
 * names.
 * <p>
 * eg: "/uri?name={name}&number={number}"
 * <p>
 * 顺序是不一定能保证的, 如果值为空的话就不会进行传递
 * order of included query parameters not guaranteed, and as usual, if any value is null, it will be
 * left out
 */
public class BeanQueryMapEncoder implements QueryMapEncoder {

    private final Map<Class<?>, ObjectParamMetadata> classToMetadata = new HashMap<>();

    @Override
    public Map<String, Object> encode(Object object) throws EncodeException {
        try {
            // 获取类型元数据, 主要是属性描述器列表
            ObjectParamMetadata metadata = getMetadata(object.getClass());
            Map<String, Object> propertyNameToValue = new HashMap<>();

            for (PropertyDescriptor pd : metadata.objectProperties) {
                // 获取get...方法
                Method method = pd.getReadMethod();
                // 获取get...方法的返回值
                Object value = method.invoke(object);
                if (value != null && value != object) {
                    Param alias = method.getAnnotation(Param.class);
                    String name = alias != null ? alias.value() : pd.getName();
                    propertyNameToValue.put(name, value);
                }
            }
            return propertyNameToValue;
        } catch (IllegalAccessException | IntrospectionException | InvocationTargetException e) {
            throw new EncodeException("Failure encoding object into query map", e);
        }
    }

    /**
     * 获取ObjectParameterMetadata
     */
    private ObjectParamMetadata getMetadata(Class<?> objectType) throws IntrospectionException {
        // 有缓存机制
        ObjectParamMetadata metadata = classToMetadata.get(objectType);
        if (metadata == null) {
            metadata = ObjectParamMetadata.parseObjectType(objectType);
            classToMetadata.put(objectType, metadata);
        }
        return metadata;
    }

    /**
     * 对象参数元数据
     */
    private static class ObjectParamMetadata {

        /**
         * 属性描述器
         */
        private final List<PropertyDescriptor> objectProperties;

        private ObjectParamMetadata(List<PropertyDescriptor> objectProperties) {
            this.objectProperties = Collections.unmodifiableList(objectProperties);
        }

        private static ObjectParamMetadata parseObjectType(Class<?> type)
            throws IntrospectionException {

            // 属性描述器列表
            List<PropertyDescriptor> properties = new ArrayList<>();

            for (PropertyDescriptor pd : Introspector.getBeanInfo(type).getPropertyDescriptors()) {
                // 忽略getClass方法
                boolean isGetterMethod = pd.getReadMethod() != null && !"class".equals(pd.getName());
                // 判断如果是get...方法的话就添加到属性描述器列表中
                if (isGetterMethod) {
                    properties.add(pd);
                }
            }

            return new ObjectParamMetadata(properties);
        }
    }
}