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
package feign.utils;

import java.util.HashSet;
import java.util.Set;

/**
 * 异常信息相关的工具类
 */
public class ExceptionUtils {
    /**
     * 根据异常信息获取异常的根异常
     * Introspects the {@link Throwable} to obtain the root cause.
     *
     * <p>
     * This method walks through the exception chain to the last element, "root" of the tree, using
     * {@link Throwable#getCause()}, and returns that exception.
     *
     * @param throwable the throwable to get the root cause for, may be null
     * @return the root cause of the {@link Throwable}, {@code null} if null throwable input
     */
    public static Throwable getRootCause(Throwable throwable) {
        // short circuit for null input
        if (throwable == null) {
            return null;
        }
        Throwable rootCause = throwable;
        // this is to avoid infinite loops for recursive cases
        // 使用Set进行去重逻辑处理
        final Set<Throwable> seenThrowables = new HashSet<>();
        seenThrowables.add(rootCause);
        while ((rootCause.getCause() != null && !seenThrowables.contains(rootCause.getCause()))) {
            // 添加当前异常的cause到Set中
            seenThrowables.add(rootCause.getCause());
            // 向根进行递归处理
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }

    /**
     * 根据异常信息获取异常的根异常, 使用递归方式
     *
     * @param throwable 异常信息
     * @return 根异常
     */
    public static Throwable getRootCauseRecur(Throwable throwable) {
        return doGetRootCauseRecur(throwable, new HashSet<>());
    }

    public static Throwable doGetRootCauseRecur(Throwable throwable, Set<Throwable> seen) {
        if (null == throwable) {
            return null;
        }
        if (null == throwable.getCause() || !seen.add(throwable.getCause())) {
            return throwable;
        }
        return doGetRootCauseRecur(throwable.getCause(), seen);
    }
}
