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
package feign.optionals;

import feign.Response;
import feign.Util;
import feign.codec.Decoder;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;

/**
 * 解码的结果可能是一个Optional类型
 */
public final class OptionalDecoder implements Decoder {

    /**
     * 委托的解码器, delegate pattern
     */
    final Decoder delegate;

    public OptionalDecoder(Decoder delegate) {
        Objects.requireNonNull(delegate, "Decoder must not be null. ");
        this.delegate = delegate;
    }

    @Override
    public Object decode(Response response, Type type) throws IOException {
        if (!isOptional(type)) {
            // 如果不是Optional类型, 则直接委托给delegate解码器进行解码
            return delegate.decode(response, type);
        }
        // 判断响应状态码是否为404或者204
        if (response.status() == 404 || response.status() == 204) {
            return Optional.empty();
        }
        // 解析Optional类型最后一个参数的类型
        Type enclosedType = Util.resolveLastTypeParameter(type, Optional.class);
        return Optional.ofNullable(delegate.decode(response, enclosedType));
    }

    /**
     * 判断是否是Optional类型
     * 如果是Optional类型的肯定是ParameterizedType参数化类型的
     * 如果是Optional类型必须是参数化类型 e.g. Optional<Abc>
     * 参数化类型的rawType必须是Optional类型
     */
    static boolean isOptional(Type type) {
        // Optional类型的返回肯定是ParameterizedType参数化类型的
        if (!(type instanceof ParameterizedType)) {
            return false;
        }
        ParameterizedType parameterizedType = (ParameterizedType) type;
        // 参数化类型判断原始类型是否是Optional类型
        return parameterizedType.getRawType().equals(Optional.class);
    }
}
