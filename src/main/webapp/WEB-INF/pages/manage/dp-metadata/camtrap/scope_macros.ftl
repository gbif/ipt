<#ftl output_format="HTML">
<#-- Geographic, taxonomic and temporal scope pages: where the scope comes from (source cards as on the EML Coverage
     page, styles/eml.css .geo-mode) and the inferring state. dpScope() in js/dp-metadata.js switches the parts. -->

<#-- the two source cards and the posted setting; page: geographic, taxonomic or temporal -->
<#macro scopeModes field page infer>
    <input type="hidden" id="scope-infer" name="${field}" value="${infer?c}"/>
    <div class="geo-modes" role="radiogroup" aria-label="<@s.text name='manage.datapackagemetadata.camtrap.scope.source'/>">
        <label class="geo-mode" data-mode="inferred">
            <input type="radio" class="form-check-input flex-shrink-0" name="scopeMode" value="inferred"<#if infer> checked</#if>>
            <span class="flex-grow-1">
                <span class="geo-mode-label"><@s.text name="manage.datapackagemetadata.camtrap.scope.inferred"/></span>
                <span class="geo-mode-desc"><@s.text name="manage.datapackagemetadata.camtrap.${page}.inferred.help"/></span>
            </span>
        </label>
        <label class="geo-mode" data-mode="stored">
            <input type="radio" class="form-check-input flex-shrink-0" name="scopeMode" value="stored"<#if !infer> checked</#if>>
            <span class="flex-grow-1">
                <span class="geo-mode-label"><@s.text name="manage.datapackagemetadata.camtrap.scope.stored"/></span>
                <span class="geo-mode-desc"><@s.text name="manage.datapackagemetadata.camtrap.${page}.stored.help"/></span>
            </span>
        </label>
    </div>
</#macro>

<#-- when the data was last inferred and Re-infer, or why inferring failed (a warning: publishing goes on without it) -->
<#macro inferBar scope page>
    <#local failed = (scope.errors)?has_content/>
    <div id="scope-infer-bar" class="<#if failed>ov-banner warn compact cov-infer-warn<#else>geo-infer-bar</#if>">
        <#if failed>
            <span class="ov-ic"><@icon name="warning"/></span>
            <span class="ov-grow">
                <#list scope.errors as error>
                    <span class="d-block"><@s.text name="${error}"/></span>
                </#list>
            </span>
        <#elseif (scope.inferred)!false>
            <span class="flex-grow-1"><@s.text name="manage.metadata.geocoverage.inferredOn"><@s.param>${(inferredMetadata.lastModified?datetime?string.medium)!}</@s.param></@s.text></span>
        <#else>
            <span class="flex-grow-1"><@s.text name="eml.warning.reinfer"/></span>
        </#if>
        <a id="re-infer-link" href="camtrap-metadata-${page}.do?r=${resource.shortname}&amp;reinferMetadata=true" class="<#if failed>align-self-center<#else>metadata-action-link custom-link</#if>">
            <#if !failed><@icon name="autorenew" class="link-icon-primary"/> </#if><span><@s.text name="datapackagemetadata.reinfer"/></span>
        </a>
    </div>
    <#include "/WEB-INF/pages/inc/inferring_status.ftl"/>
</#macro>
