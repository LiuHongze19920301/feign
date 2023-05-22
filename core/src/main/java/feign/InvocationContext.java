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
package feign;

import static feign.FeignException.errorReading;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 调用上下文,封装了解码器,返回类型,响应 这种编码方式需要注意,整合零散的信息放到统一的上下文中进行处理
 */
public class InvocationContext {

  private final Decoder decoder;
  private final Type returnType;
  private final Response response;

  InvocationContext(Decoder decoder, Type returnType, Response response) {
    this.decoder = decoder;
    this.returnType = returnType;
    this.response = response;
  }

  public Object proceed() {
    try {
      return decoder.decode(response, returnType);
    } catch (final FeignException e) {
      throw e;
    } catch (final RuntimeException e) {
      throw new DecodeException(response.status(), e.getMessage(), response.request(), e);
    } catch (IOException e) {
      throw errorReading(response.request(), response, e);
    }
  }

  public Decoder decoder() {
    return decoder;
  }

  public Type returnType() {
    return returnType;
  }

  public Response response() {
    return response;
  }

}
