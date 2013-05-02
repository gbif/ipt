<!-- Represents metadata section on resource overview page -->
<div class="resourceOverview" id="metadata">
    <div class="titleOverview">
        <div class="head">
        <@s.text name='manage.overview.metadata'/>
        </div>
        <div class="actions">
            <form action='metadata-basic.do' method='get'>
                <input name="r" type="hidden" value="${resource.shortname}"/>
            <@s.submit name="edit" key="button.edit"/>
            </form>
        </div>
    <#if missingMetadata>
        <p class="warn">
          <@s.text name='manage.overview.missing.metadata'/>
        </p>
    </#if>
    </div>
    <div class="bodyOverview">
        <p>
        <#assign no_description><@s.text name='manage.overview.no.description'/></#assign>
			<@description resource.description!no_description 100/>
        </p>

        <div class="details">
            <table>
            <#if resource.eml.subject?has_content>
                <tr>
                    <th><@s.text name='portal.resource.summary.keywords'/></th>
                    <td><@description resource.eml.subject!no_description 90/></td>
                </tr>
            </#if>
            <#assign text>
              <#list resource.eml.taxonomicCoverages as tc>
                <#list tc.taxonKeywords as k>
                ${k.scientificName!}<#if k_has_next>, </#if>
                </#list>
                <#if tc_has_next>; </#if>
              </#list>
            </#assign>
            <#if resource.eml.taxonomicCoverages?has_content>
                <tr>
                    <th><@s.text name='portal.resource.summary.taxcoverage'/></th>
                    <td><@description text!no_description 90/></td>
                </tr>
            </#if>
            <#assign text>
              <#list resource.eml.geospatialCoverages as geo>
              ${geo.description!}<#if geo_has_next>; </#if>
              </#list>
            </#assign>
            <#if resource.eml.geospatialCoverages?has_content>
                <tr>
                    <th><@s.text name='portal.resource.summary.geocoverage'/></th>
                    <td><@description text!no_description 90/></td>
                </tr></#if>
            </table>
        </div>
    <#if resource.coreType?has_content && resource.coreType==metadataType>
        <div>
            <img class="info" src="${baseURL}/images/info.gif"/>
            <em><@s.text name='manage.overview.source.hidden'><@s.param><a
                    href="${baseURL}/manage/metadata-basic.do?r=${resource.shortname}&amp;edit=Edit"><@s.text name="submenu.basic"/></a></@s.param></@s.text>
            </em>
        </div>
    </#if>

    </div>
</div>