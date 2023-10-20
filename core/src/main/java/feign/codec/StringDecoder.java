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
package feign.codec;

import feign.Response;
import feign.Util;

import java.io.IOException;
import java.lang.reflect.Type;

import static java.lang.String.format;

/**
 * 基于字符串的解码器
 */
public class StringDecoder implements Decoder {

    @Override
    public Object decode(Response response, Type type) throws IOException {
        // 获取响应信息的body
        Response.Body body = response.body();
        // 判断响应状态码是否为404或者204, 或者body是否为空
        if (response.status() == 404 || response.status() == 204 || body == null) {
            return null;
        }
        // 判断响应体是否是字符串类型
        if (String.class.equals(type)) {
            return Util.toString(body.asReader(Util.UTF_8));
        }
        throw new DecodeException(response.status(),
            format("%s is not a type supported by this decoder.", type), response.request());
    }
}
