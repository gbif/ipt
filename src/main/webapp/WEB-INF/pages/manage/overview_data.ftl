<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.OverviewAction" -->
<#-- Data & mappings section of the resource overview: sources and what they're mapped to in one table. Needs overview_status.ftl -->
<#assign lastModifiedNotSet><@s.text name='basic.lastModified.not.set'/></#assign>

<#-- what a data package mapping needs reviewed for: made against another schema version than the installed one,
     or its table or fields aren't in the installed schema (anymore) -->
<#macro dataPackageMappingIssues m>
    <#local issues = []>
    <#if (m.version!"") != (m.dataPackageSchema.version)!"">
        <#local issue><@s.text name="manage.overview.mappings.outdated"><@s.param>${m.version!"?"}</@s.param></@s.text></#local>
        <#local issues = issues + [issue]>
    </#if>
    <#if m.tableSchemaMissing>
        <#local issue><@s.text name="manage.overview.mappings.tableMissing"/></#local>
        <#local issues = issues + [issue]>
    <#elseif m.unresolvedFieldNames?has_content>
        <#local issue><@s.text name="manage.overview.mappings.fieldsMissing"><@s.param>${m.unresolvedFieldNames?join(", ")}</@s.param></@s.text></#local>
        <#local issues = issues + [issue]>
    </#if>
    <#if issues?has_content>
        <span class="ov-small bad"><@icon name="warning"/> <#list issues as issue>${issue}<#sep>; </#sep></#list></span>
    </#if>
</#macro>

<#macro sourceType src>
    <#if !src.readable>
        <#local cls = "bad">
    <#elseif src.isSqlSource()>
        <#local cls = "sql">
    <#elseif src.isUrlSource()>
        <#local cls = "url">
    <#elseif src.isExcelSource()>
        <#local cls = "xls">
    <#else>
        <#local cls = "">
    </#if>
    <span class="ov-ftype ${cls}"><#if src.isSqlSource()>SQL<#elseif src.isUrlSource()>URL<#elseif src.isExcelSource()>XLS<#else>FILE</#if></span>
</#macro>

<#macro sourceDims src>
    <#if !src.readable>
        <span class="ov-small bad"><@s.text name="manage.source.notReadable"/></span>
    <#elseif src.isSqlSource()>
        <span class="ov-small">${src.getColumns()} <@s.text name="manage.overview.source.columns"/></span>
    <#else>
        <span class="ov-small"><#attempt>${src.formattedFileSize(currentLocale)}<#recover>-</#attempt> · ${src.rows?string.number} <@s.text name="manage.overview.source.rows"/> × ${src.getColumns()} <@s.text name="manage.overview.source.columns"/></span>
    </#if>
</#macro>

<#-- source cell: type, name, changed flag and size -->
<#macro sourceCell src>
    <div class="ov-src">
        <@sourceType src/>
        <div>
            <a href="source.do?r=${resource.shortname}&id=${src.name?url}" class="ov-name">${src.name}</a><#if isSourceChanged(src)><span class="ov-chg"><@s.text name="manage.overview.data.changedTag"/></span></#if>
            <@sourceDims src/>
        </div>
    </div>
</#macro>

<#-- the ⋯ menu for a source: edit, download, delete -->
<#macro sourceMenu src id>
    <div class="dropdown">
        <a href="#" class="ov-name" id="source-menu-${id}" data-bs-toggle="dropdown" aria-expanded="false" title="<@s.text name='manage.overview.data.sourceActions'/>"><@icon name="more_horiz"/></a>
        <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="source-menu-${id}">
            <li><a class="dropdown-item" href="source.do?r=${resource.shortname}&id=${src.name?url}"><@s.text name="manage.overview.data.editSource"/></a></li>
            <#if src.isFileSource() || src.isExcelSource()>
                <li><a class="dropdown-item" href="raw-source.do?r=${resource.shortname}&id=${src.name?url}"><@s.text name="manage.overview.data.downloadSource"/></a></li>
            </#if>
            <li><hr class="dropdown-divider"></li>
            <li><a class="dropdown-item text-gbif-danger delete-source" data-post href="delete-source.do?r=${resource.shortname}&id=${src.name?url}"><@s.text name="manage.overview.data.deleteSource"/></a></li>
        </ul>
    </div>
</#macro>

<#-- a problem with a mapping, under its name -->
<#macro problemNote key>
    <span class="ov-small bad"><@icon name="warning"/> <@s.text name=key/></span>
</#macro>

<#-- sources no mapping uses: under the table, each with "Map to…" and its menu -->
<#macro unusedSources headingKey idPrefix>
    <#if unmappedSources?has_content>
        <div class="ov-unused">
            <div class="ov-unused-h"><@s.text name=headingKey/></div>
            <#list unmappedSources as src>
                <div class="ov-unused-row ov-row" <@filterAttrs src ""/> data-ov-problem="${(!src.readable)?c}">
                    <@sourceCell src/>
                    <span class="ov-sp"></span>
                    <#if src.readable && !isDataPackage><button type="button" class="ov-link-btn ov-open-add-mapping"><@s.text name="manage.overview.data.mapTo"/></button></#if>
                    <div class="ov-rowacts"><@sourceMenu src idPrefix + src?index/></div>
                </div>
            </#list>
        </div>
    </#if>
</#macro>

<#-- data attributes used by the filter chips and the search box -->
<#macro filterAttrs src text>
    data-ov-text="${(src.name + ' ' + text)?lower_case}"
    data-ov-changed="${isSourceChanged(src)?c}"
    data-ov-unmapped="${(!mappedSourceNames?seq_contains(src.name))?c}"
</#macro>

<span class="anchor anchor-overview-page" id="anchor-data"></span>
<section class="ov-card section" id="data">
    <div class="ov-ch">
        <h2>
            <#assign sourcesInfo>
                <@s.text name='manage.overview.source.description1'/>&nbsp;<@s.text name='manage.overview.source.description2'/>&nbsp;<@s.text name='manage.overview.source.description3'><@s.param><@s.text name='button.add'/></@s.param></@s.text><br><br><@s.text name='manage.overview.source.description4'><@s.param><@s.text name="button.connectDB"/></@s.param></@s.text><br><br><#if isDataPackage><@s.text name='manage.overview.mappings.info'/><#else><@s.text name='manage.overview.DwC.Mappings.coretype.description1'/></#if>
            </#assign>
            <@popoverTextInfo sourcesInfo/>
            <#if isDataPackage><@s.text name="manage.overview.data.title.dp"/><#else><@s.text name="manage.overview.data.title"/></#if>
        </h2>
        <span class="ov-sp"></span>
        <a id="add-source-button" class="ov-hact source-action" href="#"><@icon name="add"/> <@s.text name="manage.overview.data.addSource"/></a>
        <#if isDataPackage && resource.sources?has_content>
            <form action='dataPackageMappingSourceNew.do' method='post' class="d-inline">
                <input name="r" type="hidden" value="${resource.shortname}"/>
                <input name="id" type="hidden" value="${resource.dataPackageIdentifier}">
                <button name="add" class="ov-hact" type="submit"><@icon name="add"/> <@s.text name="manage.overview.data.mapTable"/></button>
            </form>
        <#else>
            <a id="add-mapping-button" class="ov-hact mapping-action" href="#"><@icon name="add"/> <#if isDataPackage><@s.text name="manage.overview.data.mapTable"/><#else><@s.text name="manage.overview.data.addMapping"/></#if></a>
        </#if>
    </div>

    <#-- when the data last changed; what the card shows is in the column headings and the (i) -->
    <div class="ov-sub">
        <#if sourcesModifiedSinceLastPublication || mappingsModifiedSinceLastPublication>
            <#assign dataModified = "">
            <#if sourcesModifiedSinceLastPublication && resource.sourcesModified??><#assign dataModified = resource.sourcesModified></#if>
            <#if mappingsModifiedSinceLastPublication && resource.mappingsModified?? && (!dataModified?is_date || resource.mappingsModified?long > dataModified?long)><#assign dataModified = resource.mappingsModified></#if>
            <#if dataModified?is_date>
                <@s.text name='manage.home.last.modified'/> ${dataModified?datetime?string.medium}.
            </#if>
        <#elseif isPublished>
            <@s.text name="manage.overview.notModified"/>.
        </#if>
    </div>

    <#if isDataPackage && !dataPackageMappingsMissing && dataPackageSchemaChanged>
        <p class="text-gbif-danger text-smaller">
            <@s.text name='manage.overview.mappings.schemaChanged'>
                <@s.param><b>${installedSchemaName!"?"}</b></@s.param>
                <@s.param><b>v${outdatedMappingVersions!"?"}</b></@s.param>
                <@s.param><b>v${installedSchemaVersion!"?"}</b></@s.param>
            </@s.text>
        </p>
    </#if>

    <#if !resource.sources?has_content>
        <div class="ov-empty">
            <b><@s.text name="manage.overview.data.empty.title"/></b>
            <@s.text name="manage.overview.data.empty.text"/>
            <div class="mt-2"><button type="button" class="btn btn-sm btn-outline-gbif-primary ov-open-add-source"><@icon name="add"/> <@s.text name="manage.overview.data.addSource"/></button></div>
        </div>

    <#elseif isDataPackage>
        <#-- data packages: one row per mapped table, grouped like the schema; sources no table uses go last -->
        <#assign dpGroups = []>
        <#if isDwcDp>
            <#assign dpGroups = [["Events", dwcDpEvents], ["Entities", dwcDpEntities], ["Agents", dwcDpAgents], ["Joins", dwcDpJoins]]>
        </#if>
        <#assign dpRows = []>
        <#list resource.dataPackageMappings! as m>
            <#assign tableName = (m.dataPackageTableSchemaName.name)!"?">
            <#assign groupName = "">
            <#list dpGroups as g><#if g[1]?seq_contains(tableName)><#assign groupName = g[0]><#break></#if></#list>
            <#if isDwcDp && !groupName?has_content><#assign groupName = "Others"></#if>
            <#assign dpRows = dpRows + [{"m": m, "mid": m_index, "group": groupName}]>
        </#list>
        <#assign groupOrder = ["Events", "Entities", "Agents", "Joins", "Others", ""]>
        <table class="ov-table">
            <thead>
            <tr>
                <th><@s.text name="manage.overview.data.col.table"/></th>
                <th class="ov-num"><@s.text name="manage.overview.data.col.fields"/></th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <#list groupOrder as groupName>
                <#assign rowsInGroup = dpRows?filter(r -> r.group == groupName)>
                <#if rowsInGroup?has_content>
                    <#if groupName?has_content><tr class="ov-grp"><td colspan="3">${groupName}</td></tr></#if>
                    <#list rowsInGroup as r>
                        <#assign m = r.m>
                        <#assign tableName = (m.dataPackageTableSchemaName.name)!"?">
                        <#assign missingRequired = (missingRequiredFieldsDataPackage!{})?keys?seq_contains(tableName)>
                        <#assign editUrl = "dataPackageMapping.do?r=${resource.shortname}&id=${m.dataPackageSchema.identifier?url}&mid=${r.mid}">
                        <tr>
                            <td>
                                <div class="ov-src">
                                    <span class="ov-ftype tbl">TBL</span>
                                    <div>
                                        <a href="${editUrl}" class="ov-name fw-semibold">${tableName}</a><#if isMappingChanged(m)><span class="ov-chg"><@s.text name="manage.overview.data.changedTag"/></span></#if>
                                        <span class="ov-small"><@s.text name="manage.overview.data.fromSource"><@s.param>${(m.source.name)!"?"}</@s.param></@s.text><#if (m.source)?? && isSourceChanged(m.source)><span class="ov-chg"><@s.text name="manage.overview.data.changedTag"/></span></#if></span>
                                        <#if missingRequired && !m.tableSchemaMissing><@problemNote "manage.overview.data.status.requiredMissing"/></#if>
                                        <#if (m.source)?? && !m.source.readable><@problemNote "manage.overview.data.status.unreadable"/></#if>
                                        <@dataPackageMappingIssues m/>
                                    </div>
                                </div>
                            </td>
                            <td class="ov-num">${(m.fields)!?size}</td>
                            <td class="ov-rowacts">
                                <a href="${editUrl}"><@s.text name="button.edit"/></a>
                                <div class="dropdown">
                                    <a href="#" id="dp-mapping-menu-${r.mid}" data-bs-toggle="dropdown" aria-expanded="false" title="<@s.text name='manage.overview.data.mappingActions'/>"><@icon name="more_horiz"/></a>
                                    <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="dp-mapping-menu-${r.mid}">
                                        <#if (m.source.name)??>
                                            <li><a class="dropdown-item" href="source.do?r=${resource.shortname}&id=${m.source.name?url}"><@s.text name="manage.overview.data.editSource"/></a></li>
                                            <li><hr class="dropdown-divider"></li>
                                        </#if>
                                        <li><a class="dropdown-item text-gbif-danger delete-mapping" data-post href="deleteDataPackageMapping.do?r=${resource.shortname}&id=${m.dataPackageSchema.identifier?url}&mid=${r.mid}"><@s.text name="manage.overview.data.deleteMapping"/></a></li>
                                    </ul>
                                </div>
                            </td>
                        </tr>
                    </#list>
                </#if>
            </#list>
            </tbody>
        </table>
        <@unusedSources "manage.overview.data.group.notUsed" "dp-"/>
        <#if installedSchemaVersion?has_content>
            <div class="ov-more"><span></span><span>${installedSchemaName!""} ${installedSchemaVersion}</span></div>
        </#if>

    <#else>
        <#-- Darwin Core Archive: what goes into the archive, by class, and the source each part comes from; unused sources last -->
        <#assign rowLimit = 8>
        <#if (resource.sources?size > rowLimit)>
            <div class="ov-tools">
                <input type="search" id="ov-data-filter" placeholder="<@s.text name='manage.overview.data.filter.placeholder'/>" aria-label="<@s.text name='manage.overview.data.filter.placeholder'/>">
                <button type="button" class="ov-chip on" data-ov-filter="all"><@s.text name="manage.overview.data.filter.all"/></button>
                <#if changedSources?has_content || classRows?filter(r -> isMappingChanged(r.m))?has_content><button type="button" class="ov-chip" data-ov-filter="changed"><@s.text name="manage.overview.data.filter.changed"/></button></#if>
                <#if unmappedSources?has_content><button type="button" class="ov-chip" data-ov-filter="unmapped"><@s.text name="manage.overview.data.filter.unmapped"/> ${unmappedSources?size}</button></#if>
                <#if unreadableSources?has_content || emptyMappings?has_content><button type="button" class="ov-chip" data-ov-filter="problems"><@s.text name="manage.overview.data.filter.problems"/> ${unreadableSources?size + emptyMappings?size}</button></#if>
            </div>
        </#if>

        <#if (classRows?size > 1)>
            <#assign deleteCoreMappingClass = "delete-core-mapping">
        <#else>
            <#assign deleteCoreMappingClass = "delete-mapping">
        </#if>

        <div id="ov-data" data-ov-limit="${rowLimit}">
        <#if classRows?has_content>
        <table class="ov-table">
            <thead>
            <tr>
                <th><@s.text name="manage.overview.data.col.class"/></th>
                <th><@s.text name="manage.overview.data.col.fromSource"/></th>
                <th class="ov-num"><@s.text name="manage.overview.data.col.terms"/></th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <#assign lastGroup = "">
            <#list classRows as r>
                <#assign m = r.m>
                <#assign groupKey = r.core?then("manage.overview.data.group.core", "manage.overview.data.group.extensions")>
                <#if groupKey != lastGroup>
                    <tr class="ov-grp"><td colspan="4"><@s.text name=groupKey/></td></tr>
                    <#assign lastGroup = groupKey>
                </#if>
                <#assign editUrl = "mapping.do?r=${resource.shortname}&id=${r.ext.rowType?url}&mid=${r.mid}">
                <tr class="ov-row"<#if (m.source)??> <@filterAttrs m.source r.ext.title/></#if> data-ov-mapping-changed="${isMappingChanged(m)?c}" data-ov-problem="${((m.source)?? && !m.source.readable || m.fields?size == 0)?c}">
                    <td>
                        <a href="${editUrl}" class="ov-name fw-semibold">${r.ext.title}</a><#if isMappingChanged(m)><span class="ov-chg"><@s.text name="manage.overview.data.changedTag"/></span></#if>
                        <#if m.fields?size == 0><@problemNote "manage.overview.data.status.noTerms"/></#if>
                    </td>
                    <td><#if (m.source)??><@sourceCell m.source/><#else>?</#if></td>
                    <td class="ov-num<#if m.fields?size == 0> ov-missing</#if>">${m.fields?size}</td>
                    <td class="ov-rowacts">
                        <a href="${editUrl}"><@s.text name="button.edit"/></a>
                        <a class="peekBtn" href="mappingPeek.do?r=${resource.shortname}&id=${r.ext.rowType?url}&mid=${r.mid}"><@s.text name="button.preview"/></a>
                        <div class="dropdown">
                            <a href="#" id="mapping-menu-${r?index}" data-bs-toggle="dropdown" aria-expanded="false" title="<@s.text name='manage.overview.data.mappingActions'/>"><@icon name="more_horiz"/></a>
                            <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="mapping-menu-${r?index}">
                                <#if (m.source)??>
                                    <li><a class="dropdown-item" href="source.do?r=${resource.shortname}&id=${m.source.name?url}"><@s.text name="manage.overview.data.editSource"/></a></li>
                                    <#if m.source.isFileSource() || m.source.isExcelSource()>
                                        <li><a class="dropdown-item" href="raw-source.do?r=${resource.shortname}&id=${m.source.name?url}"><@s.text name="manage.overview.data.downloadSource"/></a></li>
                                    </#if>
                                    <li><hr class="dropdown-divider"></li>
                                </#if>
                                <li><a class="dropdown-item text-gbif-danger ${r.core?then(deleteCoreMappingClass, 'delete-mapping')}" data-post href="delete-mapping.do?r=${resource.shortname}&id=${r.ext.rowType?url}&mid=${r.mid}"><@s.text name="manage.overview.data.deleteMapping"/></a></li>
                            </ul>
                        </div>
                    </td>
                </tr>
            </#list>
            </tbody>
        </table>
        <#else>
            <p class="text-muted text-smaller mb-2"><@s.text name="manage.overview.data.noMappings"/></p>
        </#if>
        <@unusedSources "manage.overview.data.group.notInArchive" "unused-"/>
        <p class="ov-nomatch text-muted text-smaller" style="display: none;"><@s.text name="manage.overview.data.noMatch"/></p>
        </div>
        <div class="ov-more" id="ov-data-more" style="display: none;"
             data-showing="<@s.text name='manage.overview.data.showing'/>"
             data-show-all="<@s.text name='manage.overview.data.showAll'/>"
             data-show-fewer="<@s.text name='manage.overview.data.showFewer'/>">
            <span id="ov-data-showing"></span>
            <button type="button" class="ov-link-btn" id="ov-data-expand"></button>
        </div>
    </#if>
</section>
