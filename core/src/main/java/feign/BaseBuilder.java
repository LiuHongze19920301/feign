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

import feign.Feign.ResponseMappingDecoder;
import feign.Logger.NoOpLogger;
import feign.Request.Options;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.querymap.FieldQueryMapEncoder;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static feign.ExceptionPropagationPolicy.NONE;

/**
 * 自限定泛型
 *
 * @param <B>
 */
public abstract class BaseBuilder<B extends BaseBuilder<B>> {

    private final B thisB;

    /**
     * 请求拦截器列表
     */
    protected final List<RequestInterceptor> requestInterceptors =
        new ArrayList<>();

    /**
     * 响应拦截器
     */
    protected ResponseInterceptor responseInterceptor = ResponseInterceptor.DEFAULT;

    /**
     * 日志级别
     */
    protected Logger.Level logLevel = Logger.Level.NONE;

    /**
     * 契约
     */
    protected Contract contract = new Contract.Default();

    /**
     * 重试机制
     */
    protected Retryer retryer = new Retryer.Default();

    /**
     * logger
     */
    protected Logger logger = new NoOpLogger();

    /**
     * 编码器
     */
    protected Encoder encoder = new Encoder.Default();

    /**
     * 解码器
     */
    protected Decoder decoder = new Decoder.Default();

    /**
     * 是否在解码后关闭
     */
    protected boolean closeAfterDecode = true;

    /**
     * 查询参数编码器
     */
    protected QueryMapEncoder queryMapEncoder = new FieldQueryMapEncoder();

    /**
     * 错误解码器
     */
    protected ErrorDecoder errorDecoder = new ErrorDecoder.Default();

    /**
     * 请求选项
     */
    protected Options options = new Options();

    /**
     * 调用处理器工厂
     */
    protected InvocationHandlerFactory invocationHandlerFactory =
        new InvocationHandlerFactory.Default();

    /**
     * 是否忽略404
     */
    protected boolean dismiss404;

    /**
     * 异常传播策略
     */
    protected ExceptionPropagationPolicy propagationPolicy = NONE;

    /**
     * 增强能力列表
     */
    protected List<Capability> capabilities = new ArrayList<>();


    @SuppressWarnings("unchecked")
    public BaseBuilder() {
        super();
        thisB = (B) this;
    }

    public B logLevel(Logger.Level logLevel) {
        this.logLevel = logLevel;
        return thisB;
    }

    public B contract(Contract contract) {
        this.contract = contract;
        return thisB;
    }

    public B retryer(Retryer retryer) {
        this.retryer = retryer;
        return thisB;
    }

    public B logger(Logger logger) {
        this.logger = logger;
        return thisB;
    }

    public B encoder(Encoder encoder) {
        this.encoder = encoder;
        return thisB;
    }

    public B decoder(Decoder decoder) {
        this.decoder = decoder;
        return thisB;
    }

    /**
     * This flag indicates that the response should not be automatically closed upon completion of
     * decoding the message. This should be set if you plan on processing the response into a
     * lazy-evaluated construct, such as a {@link java.util.Iterator}.
     *
     * </p>
     * Feign standard decoders do not have built in support for this flag. If you are using this flag,
     * you MUST also use a custom Decoder, and be sure to close all resources appropriately somewhere
     * in the Decoder (you can use {@link Util#ensureClosed} for convenience).
     *
     * @since 9.6
     */
    public B doNotCloseAfterDecode() {
        this.closeAfterDecode = false;
        return thisB;
    }

    public B queryMapEncoder(QueryMapEncoder queryMapEncoder) {
        this.queryMapEncoder = queryMapEncoder;
        return thisB;
    }

    /**
     * Allows to map the response before passing it to the decoder.
     */
    public B mapAndDecode(ResponseMapper mapper, Decoder decoder) {
        this.decoder = new ResponseMappingDecoder(mapper, decoder);
        return thisB;
    }

    /**
     * This flag indicates that the {@link #decoder(Decoder) decoder} should process responses with
     * 404 status, specifically returning null or empty instead of throwing {@link FeignException}.
     * <p>
     * <p/>
     * All first-party (ex gson) decoders return well-known empty values defined by
     * {@link Util#emptyValueOf}. To customize further, wrap an existing {@link #decoder(Decoder)
     * decoder} or make your own.
     * <p>
     * <p/>
     * This flag only works with 404, as opposed to all or arbitrary status codes. This was an
     * explicit decision: 404 -> empty is safe, common and doesn't complicate redirection, retry or
     * fallback policy. If your server returns a different status for not-found, correct via a custom
     * {@link #client(Client) client}.
     *
     * @since 11.9
     */
    public B dismiss404() {
        this.dismiss404 = true;
        return thisB;
    }


    /**
     * This flag indicates that the {@link #decoder(Decoder) decoder} should process responses with
     * 404 status, specifically returning null or empty instead of throwing {@link FeignException}.
     * <p>
     * <p/>
     * All first-party (ex gson) decoders return well-known empty values defined by
     * {@link Util#emptyValueOf}. To customize further, wrap an existing {@link #decoder(Decoder)
     * decoder} or make your own.
     * <p>
     * <p/>
     * This flag only works with 404, as opposed to all or arbitrary status codes. This was an
     * explicit decision: 404 -> empty is safe, common and doesn't complicate redirection, retry or
     * fallback policy. If your server returns a different status for not-found, correct via a custom
     * {@link #client(Client) client}.
     *
     * @since 8.12
     * @deprecated
     */
    @Deprecated
    public B decode404() {
        this.dismiss404 = true;
        return thisB;
    }


    public B errorDecoder(ErrorDecoder errorDecoder) {
        this.errorDecoder = errorDecoder;
        return thisB;
    }

    public B options(Options options) {
        this.options = options;
        return thisB;
    }

    /**
     * Adds a single request interceptor to the builder.
     */
    public B requestInterceptor(RequestInterceptor requestInterceptor) {
        this.requestInterceptors.add(requestInterceptor);
        return thisB;
    }

    /**
     * Sets the full set of request interceptors for the builder, overwriting any previous
     * interceptors.
     */
    public B requestInterceptors(Iterable<RequestInterceptor> requestInterceptors) {
        this.requestInterceptors.clear();
        for (RequestInterceptor requestInterceptor : requestInterceptors) {
            this.requestInterceptors.add(requestInterceptor);
        }
        return thisB;
    }

    /**
     * Adds a single response interceptor to the builder.
     */
    public B responseInterceptor(ResponseInterceptor responseInterceptor) {
        this.responseInterceptor = responseInterceptor;
        return thisB;
    }


    /**
     * Allows you to override how reflective dispatch works inside of Feign.
     */
    public B invocationHandlerFactory(InvocationHandlerFactory invocationHandlerFactory) {
        this.invocationHandlerFactory = invocationHandlerFactory;
        return thisB;
    }

    public B exceptionPropagationPolicy(ExceptionPropagationPolicy propagationPolicy) {
        this.propagationPolicy = propagationPolicy;
        return thisB;
    }

    public B addCapability(Capability capability) {
        this.capabilities.add(capability);
        return thisB;
    }

    /**
     * 增强处理逻辑
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected B enrich() {
        if (capabilities.isEmpty()) {
            return thisB;
        }

        getFieldsToEnrich().forEach(field -> {
            // 扩大访问权限
            field.setAccessible(true);
            try {
                final Object originalValue = field.get(thisB);
                final Object enriched;
                // 列表的增强逻辑
                if (originalValue instanceof List) {
                    Type ownerType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    enriched = ((List) originalValue).stream()
                        .map(value -> Capability.enrich(value, (Class<?>) ownerType, capabilities))
                        .collect(Collectors.toList());
                } else {
                    // 普通字段的增强逻辑
                    enriched = Capability.enrich(originalValue, field.getType(), capabilities);
                }
                field.set(thisB, enriched);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException("Unable to enrich field " + field, e);
            } finally {
                // 收回访问权限
                field.setAccessible(false);
            }
        });

        return thisB;
    }

    List<Field> getFieldsToEnrich() {
        return Util.allFields(getClass())
            .stream()
            // exclude anything generated by compiler
            // 非编译器合成字段
            .filter(field -> !field.isSynthetic())
            // and capabilities itself
            // 过滤掉capabilities字段
            .filter(field -> !Objects.equals(field.getName(), "capabilities"))
            // and thisB helper field
            // 过滤掉thisB字段
            .filter(field -> !Objects.equals(field.getName(), "thisB"))
            // skip primitive types
            // 过滤掉原始类型
            .filter(field -> !field.getType().isPrimitive())
            // skip enumerations
            // 过滤掉枚举类型
            .filter(field -> !field.getType().isEnum())
            .collect(Collectors.toList());
    }


}
