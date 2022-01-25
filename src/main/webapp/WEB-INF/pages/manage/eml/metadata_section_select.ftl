<#ftl output_format="HTML">

<div id="metadata-selector-container" class="container-fluid border-bottom">
    <div class="container">
        <div class="p-3">
            <div>
                <label for="metadata-section" class="form-label">
                    <@s.text name="manage.metadata.section"/>
                </label>
            </div>
            <select name="metadata-section" id="metadata-section" size="1" class="form-select" required="" >
                <#list ["basic", "geocoverage", "taxcoverage","tempcoverage", "keywords", "parties", "project", "methods", "citations", "collections", "physical", "additional"] as it>
                    <option value="${it}" <#if it == currentMetadataPage>selected</#if>>
                        <@s.text name="submenu.${it}"/>
                    </option>
                </#list>
            </select>
        </div>
    </div>
</div>
