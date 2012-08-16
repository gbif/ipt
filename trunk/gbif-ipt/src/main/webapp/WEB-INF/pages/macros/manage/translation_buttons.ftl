<#macro buttons location>
<div <#if location == "top">class="topButtons"<#else>class="bottomButtons"</#if>>
  <@s.submit cssClass="button" name="save" key="button.save"/>
  <a class="button" href="mapping.do?r=${resource.shortname}&id=${property.extension.rowType}&mid=${mid}">
    <input class="button" type="button" value='<@s.text name='button.cancel'/>'/>
  </a>
  <@s.submit cssClass="confirm" name="delete" key="button.delete"/>

  <a class="button" href='translationReload.do?r=${resource.shortname}&mapping=${property.extension.rowType}&term=${property.qualname}&mid=${mid}&rowtype=${property.extension.rowType}'>
    <input class="button" type="button" value='<@s.text name="button.reload"/>'/>
  </a>
  <#if property.vocabulary?exists>
    <a class="button" href='translationAutomap.do?r=${resource.shortname}&mapping=${property.extension.rowType}&rowtype=${property.extension.rowType}&term=${property.qualname}&mid=${mid}'>
      <input class="button" type="button" value='<@s.text name="button.automap"/>'/>
    </a>
  </#if>
</div>
</#macro>
