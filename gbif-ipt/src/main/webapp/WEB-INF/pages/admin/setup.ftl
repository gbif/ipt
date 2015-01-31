[#ftl]
[#include "/WEB-INF/pages/inc/header_setup.ftl"]
[#include "/WEB-INF/pages/macros/forms.ftl"]

<div class="grid_18">
		<h1>[@s.text name="admin.config.setup.title"/]</h1>
		
			[@s.actionmessage/]
			[#if warnings?size>0]		    
			 <ul class="warnMessage">
			 [#list warnings as w]
	          <li><span>${w!}</span></li>
			 [/#list]
             </ul>
            [/#if]
			[@s.actionerror/]

  		[@s.form action="setup.do" method="post"]
      <h2 class="subTitle">[@s.text name="admin.config.setup.disclaimer.title"/]&nbsp;<img class="infoImg" src="${baseURL}/images/warning.gif"/></h2>
      <p>[@s.text name="admin.config.setup.disclaimerPart1"/]</p>
      <p>[@s.text name="admin.config.setup.disclaimerPart2"/]</p>
      <p>
        [@s.fielderror]
          [@s.param value="%{'readDisclaimer'}" /]
        [/@s.fielderror]
        [@s.checkbox name="readDisclaimer" value="readDisclaimer" /]&nbsp;
        [@s.text name="admin.config.setup.read"/]
      </p>

      <h2 class="subTitle">[@s.text name="admin.config.server.data.dir"/]</h2>
      <p>[@s.text name="admin.config.setup.welcome"/]</p>
      <p>[@s.text name="admin.config.setup.instructions"/]</p>
      <p>[@s.text name="admin.config.setup.examples"/]</p>
			[@s.textfield key="admin.config.setup.datadir" name="dataDirPath" size="80" required="true"/]
      [@s.submit cssClass="button" name="save" key="button.save"/]
		[/@s.form]
</div>
</div>
[#include "/WEB-INF/pages/inc/footer.ftl"]