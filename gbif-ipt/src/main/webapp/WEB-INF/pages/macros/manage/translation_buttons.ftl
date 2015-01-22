<#macro buttons location>
<div <#if location == "top">class="topButtons"<#else>class="bottomButtons"</#if>>
  <@s.submit cssClass="button" name="save" key="button.save"/>
  <a class="button" href="mapping.do?r=${resource.shortname}&id=${property.extension.rowType?url}&mid=${mid}">
    <input class="button" type="button" value='<@s.text name='button.cancel'/>'/>
  </a>
  <@s.submit cssClass="confirm" name="delete" key="button.delete"/>

  <a class="button" href='translationReload.do?r=${resource.shortname}&mapping=${property.extension.rowType?url}&term=${property.qualname?url}&mid=${mid}&rowtype=${property.extension.rowType?url}'>
    <input class="button" type="button" value='<@s.text name="button.reload"/>'/>
  </a>
  <#if property.vocabulary?has_content>
    <a class="button" href='translationAutomap.do?r=${resource.shortname}&mapping=${property.extension.rowType?url}&rowtype=${property.extension.rowType?url}&term=${property.qualname?url}&mid=${mid}'>
      <input class="button" type="button" value='<@s.text name="button.automap"/>'/>
    </a>
  </#if>
</div>
</#macro>
