			</div>
			<div id="footer">
				<ul>
					<li>Version ${cfg.version!"???"}</li>
					<li><a href="http://code.google.com/p/gbif-providertoolkit/"><@s.text name="footer.projectHome"/></a></li>
					<li><a href="http://code.google.com/p/gbif-providertoolkit/issues/list"><@s.text name="footer.bugReport"/></a></li>
					<li>&copy; 2010 <a href="http://www.gbif.org">GBIF</a></li>
				</ul>
				<#if ms?exists>
				<ul>
					<li>${ms.resource}</li>
				</ul>
				<ul>
				  <#list ms.config.sources as s>
					<li>${s}</li>
				  </#list>
				</ul>
				<ul>
				  <#list ms.config.extensions as e>
					<li>${e}</li>
				  </#list>
				</ul>
				</#if>
			</div>			
		</div>
	</body>
</html>