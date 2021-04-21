<#macro buttons>
    <div class="mx-md-4 mx-2">
        <@s.submit cssClass="button btn btn-outline-gbif-primary mt-1" name="save" key="button.save"/>

        <a class="button btn btn-outline-secondary ignore-link-color mt-1" role="button" href="mapping.do?r=${resource.shortname}&id=${property.extension.rowType?url}&mid=${mid}">
            <@s.text name='button.cancel'/>
        </a>

        <@s.submit cssClass="confirm btn btn-outline-gbif-danger mt-1" name="delete" key="button.delete"/>

        <a class="button btn btn-outline-secondary ignore-link-color mt-1" href='translationReload.do?r=${resource.shortname}&mapping=${property.extension.rowType?url}&term=${property.qualname?url}&mid=${mid}&rowtype=${property.extension.rowType?url}'>
            <@s.text name="button.reload"/>
        </a>

        <#if property.vocabulary?has_content>
            <a class="button btn btn-outline-secondary ignore-link-color mt-1" role="button" href='translationAutomap.do?r=${resource.shortname}&mapping=${property.extension.rowType?url}&rowtype=${property.extension.rowType?url}&term=${property.qualname?url}&mid=${mid}'>
                <@s.text name="button.automap"/>
            </a>
        </#if>
    </div>
</#macro>
