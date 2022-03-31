<#macro buttons>
    <div class="mx-md-4 mx-2">
        <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" name="save" key="button.save"/>

        <a class="button btn btn-sm btn-outline-secondary top-button" role="button" href="mapping.do?r=${resource.shortname}&id=${property.extension.rowType?url}&mid=${mid}">
            <@s.text name='button.cancel'/>
        </a>

        <@s.submit cssClass="confirm btn btn-sm btn-outline-gbif-danger top-button" name="delete" key="button.delete"/>

        <a class="button btn btn-sm btn-outline-secondary top-button" href='translationReload.do?r=${resource.shortname}&mapping=${property.extension.rowType?url}&term=${property.qualname?url}&mid=${mid}&rowtype=${property.extension.rowType?url}'>
            <@s.text name="button.reload"/>
        </a>

        <#if property.vocabulary?has_content>
            <a class="button btn btn-sm btn-outline-secondary top-button" role="button" href='translationAutomap.do?r=${resource.shortname}&mapping=${property.extension.rowType?url}&rowtype=${property.extension.rowType?url}&term=${property.qualname?url}&mid=${mid}'>
                <@s.text name="button.automap"/>
            </a>
        </#if>
    </div>
</#macro>
