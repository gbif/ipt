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

<#-- sources no mapping uses: under the table, each with "Map to…" and its menu. More than a few are folded into one
     line with their names; filtering and searching look inside it anyway -->
<#assign unusedFoldAbove = 3>
<#macro unusedSources headingKey idPrefix>
    <#if unmappedSources?has_content>
        <#local folded = (unmappedSources?size > unusedFoldAbove)>
        <div class="ov-unused<#if folded> ov-folded</#if>"<#if folded> data-ov-foldable="true"</#if>>
            <div class="ov-unused-h">
                <span><@s.text name=headingKey/></span>
                <#if folded>
                    <span class="ov-unused-names">· ${unmappedSources?map(src -> src.name)?join(", ")}</span>
                    <button type="button" class="ov-link-btn ov-unused-toggle"
                            data-show="<@s.text name='manage.overview.data.showAll'><@s.param>${unmappedSources?size}</@s.param></@s.text>"
                            data-hide="<@s.text name='manage.overview.data.showFewer'/>"><@s.text name='manage.overview.data.showAll'><@s.param>${unmappedSources?size}</@s.param></@s.text></button>
                </#if>
            </div>
            <#list unmappedSources as src>
                <div class="ov-unused-row ov-row" data-ov-unused="true" data-ov-text="${src.name?lower_case}" data-ov-changed="${isSourceChanged(src)?c}" data-ov-unmapped="true" data-ov-problem="${(!src.readable)?c}">
                    <@sourceCell src/>
                    <span class="ov-sp"></span>
                    <#if src.readable && !isDataPackage><button type="button" class="ov-link-btn ov-open-add-mapping"><@s.text name="manage.overview.data.mapTo"/></button></#if>
                    <div class="ov-rowacts"><@sourceMenu src idPrefix + src?index/></div>
                </div>
            </#list>
        </div>
    </#if>
</#macro>

<#-- search and filter chips over the rows, once there are more than fit -->
<#macro dataTools rowCount anyChanged anyProblem>
    <#if (rowCount > rowLimit)>
        <div class="ov-tools">
            <#local placeholder><@s.text name=isDataPackage?then('manage.overview.data.filter.placeholder.dp', 'manage.overview.data.filter.placeholder')/></#local>
            <input type="search" id="ov-data-filter" placeholder="${placeholder}" aria-label="${placeholder}">
            <button type="button" class="ov-chip on" data-ov-filter="all"><@s.text name="manage.overview.data.filter.all"/></button>
            <#if anyChanged><button type="button" class="ov-chip" data-ov-filter="changed"><@s.text name="manage.overview.data.filter.changed"/></button></#if>
            <#if unmappedSources?has_content><button type="button" class="ov-chip" data-ov-filter="unmapped"><@s.text name="manage.overview.data.filter.unmapped"/> ${unmappedSources?size}</button></#if>
            <#if anyProblem><button type="button" class="ov-chip" data-ov-filter="problems"><@s.text name="manage.overview.data.filter.problems"/></button></#if>
        </div>
    </#if>
</#macro>

<#-- "Showing 8 of 20 · Show all", under the rows -->
<#macro dataMore>
    <p class="ov-nomatch text-muted text-smaller" style="display: none;"><@s.text name="manage.overview.data.noMatch"/></p>
    <div class="ov-more" id="ov-data-more" style="display: none;"
         data-showing="<@s.text name='manage.overview.data.showing'/>"
         data-show-all="<@s.text name='manage.overview.data.showAll'/>"
         data-show-fewer="<@s.text name='manage.overview.data.showFewer'/>">
        <span id="ov-data-showing"></span>
        <button type="button" class="ov-link-btn" id="ov-data-expand"></button>
    </div>
</#macro>

<#macro changedTag><span class="ov-chg"><@s.text name="manage.overview.data.changedTag"/></span></#macro>

<#-- rows shown before "Show all"; rows with a problem are always shown -->
<#assign rowLimit = 8>

<#-- what makes a row need attention -->
<#function dwcProblem r>
    <#return ((r.m.source)?? && !r.m.source.readable) || r.m.fields?size == 0>
</#function>
<#function dpProblem m>
    <#local tableName = (m.dataPackageTableSchemaName.name)!"?">
    <#return (missingRequiredFieldsDataPackage!{})?keys?seq_contains(tableName) || ((m.source)?? && !m.source.readable) || dataPackageMappingHasIssues(m)>
</#function>
<#function dpChanged m>
    <#return isMappingChanged(m) || ((m.source)?? && isSourceChanged(m.source))>
</#function>

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

    <#-- when the data last changed. A CHANGED tag only tells something when not every row has it; if every mapping
         changed, that's said here once instead -->
    <#if isDataPackage>
        <#assign mappingsForTags = resource.dataPackageMappings![]>
        <#assign allChanged = isPublished && mappingsForTags?has_content && !mappingsForTags?filter(m -> !isMappingChanged(m))?has_content>
    <#else>
        <#assign allChanged = isPublished && classRows?has_content && !classRows?filter(r -> !isMappingChanged(r.m))?has_content>
    </#if>
    <#assign showChangedTags = !allChanged>
    <div class="ov-sub">
        <#if sourcesModifiedSinceLastPublication || mappingsModifiedSinceLastPublication>
            <#assign dataModified = "">
            <#if sourcesModifiedSinceLastPublication && resource.sourcesModified??><#assign dataModified = resource.sourcesModified></#if>
            <#if mappingsModifiedSinceLastPublication && resource.mappingsModified?? && (!dataModified?is_date || resource.mappingsModified?long > dataModified?long)><#assign dataModified = resource.mappingsModified></#if>
            <#if dataModified?is_date>
                <@s.text name='manage.home.last.modified'/> ${dataModified?datetime?string.medium}.
            </#if>
            <#if allChanged><@s.text name="manage.overview.data.allChanged"><@s.param>${currentVersion}</@s.param></@s.text></#if>
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
        <#-- data packages: one line per mapped table, grouped like the schema, problems first in each group; unused sources last -->
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
            <#assign dpRows = dpRows + [{"m": m, "mid": m_index, "group": groupName, "problem": dpProblem(m)}]>
        </#list>
        <#assign groupOrder = ["Events", "Entities", "Agents", "Joins", "Others", ""]>

        <@dataTools rowCount=dpRows?size + unmappedSources?size anyChanged=(showChangedTags && dpRows?filter(r -> isMappingChanged(r.m))?has_content) || changedSources?has_content anyProblem=dpRows?filter(r -> r.problem)?has_content || unreadableSources?has_content/>

        <div id="ov-data" data-ov-limit="${rowLimit}">
        <#if dpRows?has_content>
        <table class="ov-table">
            <thead>
            <tr>
                <th><@s.text name="manage.overview.data.col.table"/></th>
                <th><@s.text name="manage.overview.data.col.fromSource"/></th>
                <th class="ov-num"><@s.text name="manage.overview.data.col.fields"/></th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <#list groupOrder as groupName>
                <#assign rowsInGroup = dpRows?filter(r -> r.group == groupName)>
                <#assign rowsInGroup = rowsInGroup?filter(r -> r.problem) + rowsInGroup?filter(r -> !r.problem)>
                <#if rowsInGroup?has_content>
                    <#if groupName?has_content><tr class="ov-grp"><td colspan="4">${groupName}</td></tr></#if>
                    <#list rowsInGroup as r>
                        <#assign m = r.m>
                        <#assign tableName = (m.dataPackageTableSchemaName.name)!"?">
                        <#assign sourceName = (m.source.name)!"?">
                        <#assign missingRequired = (missingRequiredFieldsDataPackage!{})?keys?seq_contains(tableName)>
                        <#assign editUrl = "dataPackageMapping.do?r=${resource.shortname}&id=${m.dataPackageSchema.identifier?url}&mid=${r.mid}">
                        <tr class="ov-row" data-ov-text="${(tableName + ' ' + sourceName)?lower_case}" data-ov-changed="${dpChanged(m)?c}" data-ov-unmapped="false" data-ov-problem="${r.problem?c}">
                            <td>
                                <a href="${editUrl}" class="ov-name fw-semibold">${tableName}</a><#if showChangedTags && isMappingChanged(m)><@changedTag/></#if>
                                <#if missingRequired && !m.tableSchemaMissing><@problemNote "manage.overview.data.status.requiredMissing"/></#if>
                                <@dataPackageMappingIssues m/>
                            </td>
                            <td><#if (m.source)??><@sourceCell m.source/><#else>?</#if></td>
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
        </#if>
        <@dataMore/>
        <@unusedSources "manage.overview.data.group.notUsed" "dp-"/>
        </div>

    <#else>
        <#-- Darwin Core Archive: one line per class, core first, then extensions with problems first. A class filled from
             several sources gets one line that opens to a line per source; unused sources last -->
        <#assign classGroups = []>
        <#list classRows as r>
            <#if r?is_first || classRows[r?index - 1].ext.rowType != r.ext.rowType>
                <#assign groupRows = classRows?filter(x -> x.ext.rowType == r.ext.rowType)>
                <#assign classGroups = classGroups + [{"ext": r.ext, "core": r.core, "rows": groupRows, "problem": groupRows?filter(x -> dwcProblem(x))?has_content}]>
            </#if>
        </#list>
        <#assign classGroups = classGroups?filter(g -> g.core) + classGroups?filter(g -> !g.core && g.problem) + classGroups?filter(g -> !g.core && !g.problem)>

        <@dataTools rowCount=classGroups?size + unmappedSources?size anyChanged=(showChangedTags && classRows?filter(r -> isMappingChanged(r.m))?has_content) || changedSources?has_content anyProblem=emptyMappings?has_content || unreadableSources?has_content/>

        <#if (classRows?size > 1)>
            <#assign deleteCoreMappingClass = "delete-core-mapping">
        <#else>
            <#assign deleteCoreMappingClass = "delete-mapping">
        </#if>

        <div id="ov-data" data-ov-limit="${rowLimit}">
        <#if classGroups?has_content>
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
            <#list classGroups as g>
                <#assign groupKey = g.core?then("manage.overview.data.group.core", "manage.overview.data.group.extensions")>
                <#if groupKey != lastGroup>
                    <tr class="ov-grp"><td colspan="4"><@s.text name=groupKey/></td></tr>
                    <#assign lastGroup = groupKey>
                </#if>
                <#assign anyMappingChanged = g.rows?filter(r -> isMappingChanged(r.m))?has_content>
                <#if g.rows?size == 1>
                    <@dwcMappingRow g.rows[0] true/>
                <#else>
                    <#-- several sources: one line for the class, opening to a line per source (open when one has a problem) -->
                    <#assign groupId = "ov-class-" + g?index>
                    <#assign groupText = g.ext.title + " " + g.rows?map(r -> (r.m.source.name)!"")?join(" ")>
                    <tr class="ov-row ov-parent<#if g.problem> open</#if>" id="${groupId}" data-ov-text="${groupText?lower_case}"
                        data-ov-changed="${g.rows?filter(r -> (r.m.source)?? && isSourceChanged(r.m.source))?has_content?c}" data-ov-mapping-changed="${anyMappingChanged?c}"
                        data-ov-unmapped="false" data-ov-problem="${g.problem?c}">
                        <td>
                            <span class="ov-name fw-semibold">${g.ext.title}</span><#if showChangedTags && anyMappingChanged><@changedTag/></#if>
                            <#if g.rows?filter(r -> r.m.fields?size == 0)?has_content><@problemNote "manage.overview.data.status.noTerms"/></#if>
                        </td>
                        <td>
                            <button type="button" class="ov-link-btn ov-toggle-sources" aria-expanded="${g.problem?c}" aria-controls="${groupId}">
                                <@s.text name="manage.overview.data.sourcesCount"><@s.param>${g.rows?size}</@s.param></@s.text>
                            </button>
                        </td>
                        <td></td>
                        <td></td>
                    </tr>
                    <#assign showChildChangedTags = showChangedTags && g.rows?filter(r -> !isMappingChanged(r.m))?has_content>
                    <#list g.rows as r>
                        <@dwcMappingRow r false groupId/>
                    </#list>
                </#if>
            </#list>
            </tbody>
        </table>
        <#else>
            <p class="text-muted text-smaller mb-2"><@s.text name="manage.overview.data.noMappings"/></p>
        </#if>
        <@dataMore/>
        <@unusedSources "manage.overview.data.group.notInArchive" "unused-"/>
        </div>
    </#if>
</section>

<#-- one Darwin Core mapping: a row of its own (withClass), or a line under its class when the class has several sources -->
<#macro dwcMappingRow r withClass parentId="">
    <#local m = r.m>
    <#local editUrl = "mapping.do?r=${resource.shortname}&id=${r.ext.rowType?url}&mid=${r.mid}">
    <#if withClass>
        <tr class="ov-row"<#if (m.source)??> data-ov-text="${(m.source.name + ' ' + r.ext.title)?lower_case}" data-ov-changed="${isSourceChanged(m.source)?c}"</#if> data-ov-mapping-changed="${isMappingChanged(m)?c}" data-ov-unmapped="false" data-ov-problem="${dwcProblem(r)?c}">
    <#else>
        <tr class="ov-child" data-ov-parent="${parentId}">
    </#if>
        <td>
            <#if withClass>
                <a href="${editUrl}" class="ov-name fw-semibold">${r.ext.title}</a><#if showChangedTags && isMappingChanged(m)><@changedTag/></#if>
                <#if m.fields?size == 0><@problemNote "manage.overview.data.status.noTerms"/></#if>
            <#elseif m.fields?size == 0>
                <@problemNote "manage.overview.data.status.noTerms"/>
            <#elseif showChildChangedTags && isMappingChanged(m)>
                <@changedTag/>
            </#if>
        </td>
        <td><#if (m.source)??><@sourceCell m.source/><#else>?</#if></td>
        <td class="ov-num<#if m.fields?size == 0> ov-missing</#if>">${m.fields?size}</td>
        <td class="ov-rowacts">
            <a href="${editUrl}"><@s.text name="button.edit"/></a>
            <a class="peekBtn" href="mappingPeek.do?r=${resource.shortname}&id=${r.ext.rowType?url}&mid=${r.mid}"><@s.text name="button.preview"/></a>
            <div class="dropdown">
                <a href="#" data-bs-toggle="dropdown" aria-expanded="false" title="<@s.text name='manage.overview.data.mappingActions'/>"><@icon name="more_horiz"/></a>
                <ul class="dropdown-menu dropdown-menu-end">
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
</#macro>
