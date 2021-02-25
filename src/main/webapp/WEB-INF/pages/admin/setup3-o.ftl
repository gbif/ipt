[#ftl output_format="HTML"]
[#include "/WEB-INF/pages/inc/header_setup.ftl"]

<div class="grid_18 suffix_6">
  <h1 class="twenty_top">[@s.text name="admin.config.setup3.title"/]</h1>
  <p>[@s.text name="admin.config.setup3.welcome"/]</p>

  [#if warnings?size>0]
    <ul class="warnMessage">
      [#list warnings as w]
        <li><span>${w!}</span></li>
      [/#list]
    </ul>
  [/#if]

  [#include "/WEB-INF/pages/macros/forms.ftl"]
  [@s.form cssClass="topForm half" action="setupComplete" namespace="" includeContext="false"]
	  <div class="buttons">
 	    [@s.submit cssClass="button" name="continue" key="button.continue"/]
	  </div>
  [/@s.form]
</div>

[#include "/WEB-INF/pages/inc/footer.ftl"]
