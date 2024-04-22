/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.struts2;

import org.gbif.ipt.config.AppConfig;

import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import com.google.inject.Inject;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

public class DefaultLocaleInterceptor extends AbstractInterceptor {

    private static final long serialVersionUID = -5106569324133156303L;
    @Inject
    private AppConfig cfg;

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        Locale locale = Optional.ofNullable(cfg.getDefaultLocale())
                .filter(StringUtils::isNotEmpty)
                .map(Locale::new)
                .filter(cfg::isSupportedLocale)
                .orElse(Locale.UK);

        invocation.getInvocationContext().setLocale(locale);

        return invocation.invoke();
    }
}
