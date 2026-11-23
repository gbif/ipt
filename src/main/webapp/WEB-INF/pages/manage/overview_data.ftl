<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.OverviewAction" -->
<#-- Data & mappings section of the resource overview: sources and what they're mapped to in one table. Needs overview_status.ftl -->
<#assign lastModifiedNotSet><@s.text name='basic.lastModified.not.set'/></#assign>

<#-- what a data package mapping needs reviewed for, as on its mapping page: a table not in the installed schema stops
     publishing (error); made against another schema version, or fields the installed schema doesn't have, which are
     published without them (warning) -->
<#macro dataPackageMappingIssues m>
    <#local warnings = []>
    <#if (m.version!"") != (m.dataPackageSchema.version)!"">
        <#local issue><@s.text name="manage.overview.mappings.outdated"><@s.param>${m.version!"?"}</@s.param><@s.param>${(m.dataPackageSchema.version)!"?"}</@s.param></@s.text></#local>
        <#local warnings = warnings + [issue]>
    </#if>
    <#if m.tableSchemaMissing>
        <span class="ov-small bad"><@icon name="error"/> <@s.text name="manage.overview.mappings.tableMissing"/></span>
    <#elseif m.unresolvedFieldNames?has_content>
        <#local issue><@s.text name="manage.overview.mappings.fieldsMissing"><@s.param>${m.unresolvedFieldNames?join(", ")}</@s.param></@s.text></#local>
        <#local warnings = warnings + [issue]>
    </#if>
    <#if warnings?has_content>
        <span class="ov-small warn"><@icon name="warning"/> <#list warnings as issue>${issue}<#sep>; </#sep></#list></span>
    </#if>
</#macro>

<#-- the source's kind as a grey badge with an icon; red when the source can't be read -->
<#macro sourceType src>
    <#if src.isSqlSource()>
        <#local iconName = "dns_outlined" kindLabel = "SQL">
    <#elseif src.isUrlSource()>
        <#local iconName = "link_outlined" kindLabel = "URL">
    <#elseif src.isExcelSource()>
        <#local iconName = "grid_on_outlined" kindLabel = "XLS">
    <#else>
        <#local iconName = "description_outlined" kindLabel = "FILE">
    </#if>
    <span class="ov-ftype<#if !src.readable> bad</#if>"><@icon name=iconName/>${kindLabel}</span>
</#macro>

<#macro sourceDims src>
    <#if !src.readable>
        <span class="ov-small bad"><@s.text name="manage.source.notReadable"/></span>
    <#elseif src.isSqlSource()>
        <span class="ov-small">${src.getColumns()} <@s.text name="manage.overview.source.columns"/><#if liveSourceNames?seq_contains(src.name)> · <@s.text name="manage.overview.source.live"/></#if></span>
    <#else>
        <span class="ov-small"><#attempt>${src.formattedFileSize(currentLocale)}<#recover>-</#attempt> · ${src.rows?string.number} <@s.text name="manage.overview.source.rows"/> · ${src.getColumns()} <@s.text name="manage.overview.source.columns"/><#if liveSourceNames?seq_contains(src.name)> · <@s.text name="manage.overview.source.live"/></#if></span>
    </#if>
</#macro>

<#-- source cell: type, name, changed flag and size -->
<#-- menuId: when given, the source's menu follows its name. showDims: the size line, left out where the source shows
     again in the same section -->
<#macro sourceCell src menuId="" showDims=true>
    <div class="ov-src">
        <@sourceType src/>
        <div>
            <a href="source.do?r=${resource.shortname}&id=${src.name?url}" class="ov-name">${src.name}</a><#if isSourceChanged(src)><span class="ov-chg"><@s.text name="manage.overview.data.changedTag"/></span></#if><#if menuId?has_content><@sourceMenu src menuId/></#if>
            <#if showDims><@sourceDims src/></#if>
        </div>
    </div>
</#macro>

<#-- the actions after a table or source name: icons on wide screens, a ⋯ menu on narrow ones (overview.css).
     items: icon, text, href; optional cls (extra class, e.g. peekBtn) and name (data-name); a delete has del, its
     confirm class -->
<#macro nameActions id titleKey items>
    <span class="ov-name-acts">
        <#list items as i>
            <a href="${i.href}" title="<@s.text name=i.text/>" aria-label="<@s.text name=i.text/>" class="<#if i.del??>ov-act-del ${i.del}</#if> ${i.cls!}"<#if i.name??> data-name="${i.name}"</#if><#if i.del??> data-post</#if>><@icon name=i.icon/></a>
        </#list>
    </span>
    <div class="dropdown ov-name-menu">
        <a href="#" class="ov-name" id="${id}" data-bs-toggle="dropdown" aria-expanded="false" title="<@s.text name=titleKey/>"><@icon name="more_horiz"/></a>
        <ul class="dropdown-menu" aria-labelledby="${id}">
            <#list items as i>
                <#if i.del??><li><hr class="dropdown-divider"></li></#if>
                <li><a class="dropdown-item<#if i.del??> text-gbif-danger ${i.del}</#if> ${i.cls!}" href="${i.href}"<#if i.name??> data-name="${i.name}"</#if><#if i.del??> data-post</#if>><@s.text name=i.text/></a></li>
            </#list>
        </ul>
    </div>
</#macro>

<#-- a source's actions: edit, download (files), delete -->
<#macro sourceMenu src id>
    <#local items = [{"icon": "edit", "text": "manage.overview.data.editSource", "href": "source.do?r=${resource.shortname}&id=${src.name?url}"}]>
    <#if src.isFileSource() || src.isExcelSource()>
        <#local items = items + [{"icon": "download", "text": "manage.overview.data.downloadSource", "href": "raw-source.do?r=${resource.shortname}&id=${src.name?url}"}]>
    </#if>
    <#local items = items + [{"icon": "delete", "text": "manage.overview.data.deleteSource", "href": "delete-source.do?r=${resource.shortname}&id=${src.name?url}", "del": unmappedSources?map(x -> x.name)?seq_contains(src.name)?then("delete-unused-source", "delete-source"), "name": src.name}]>
    <@nameActions id="source-menu-" + id titleKey="manage.overview.data.sourceActions" items=items/>
</#macro>

<#-- a problem with a mapping, under its name: an error stops publishing, a warning doesn't -->
<#macro problemNote key level="error">
    <span class="ov-small <#if level == "error">bad<#else>warn</#if>"><@icon name=level/> <@s.text name=key/></span>
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
            <#-- a grid of small cards: mapping them starts from "Map tables" / "Add mapping" above -->
            <div class="ov-unused-grid">
            <#list unmappedSources as src>
                <div class="ov-unused-row ov-row" data-ov-unused="true" data-ov-text="${src.name?lower_case}" data-ov-changed="${isSourceChanged(src)?c}" data-ov-unmapped="true" data-ov-problem="${(!src.readable)?c}">
                    <@sourceCell src idPrefix + src?index/>
                </div>
            </#list>
            </div>
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

<#-- shown when search or a filter matches nothing -->
<#macro noMatch>
    <p class="ov-nomatch text-muted text-smaller" style="display: none;"><@s.text name="manage.overview.data.noMatch"/></p>
</#macro>

<#macro changedTag><span class="ov-chg"><@s.text name="manage.overview.data.changedTag"/></span></#macro>

<#-- search and filter chips show above this many rows -->
<#assign rowLimit = 8>

<#-- how the sections of the table start on load (they can always be folded and opened by hand). Folded, the heading
     lists each mapping as "source -> table"; opened, the rows show.
     The core of the data starts open: the DwC-A core and DwC-DP events. Supporting tables start folded: DwC-DP
     assertions and identifiers, and joins. The others (entities, extensions) fold when they have more mappings than
     foldAbove -->
<#assign foldAbove = 5>
<#assign alwaysOpen = ["core", "events"]>
<#assign alwaysFolded = ["assertions", "joins"]>

<#-- how bad a mapping's problem is, as its note under the name says: an error stops publishing, a warning doesn't -->
<#function dpLevel m>
    <#local tableName = (m.dataPackageTableSchemaName.name)!"?">
    <#if m.tableSchemaMissing || (missingRequiredFieldsDataPackage!{})?keys?seq_contains(tableName) || ((m.source)?? && !m.source.readable)>
        <#return "error">
    </#if>
    <#if dataPackageMappingHasIssues(m)><#return "warning"></#if>
    <#return "">
</#function>
<#function dwcLevel m>
    <#if (m.source)?? && !m.source.readable><#return "error"></#if>
    <#if m.fields?size == 0><#return "warning"></#if>
    <#return "">
</#function>

<#-- a section heading: name, counts, and when folded a link per mapping. items: name, source, href, level, changed -->
<#macro sectionHeading id label items countKey>
    <#local folded = !alwaysOpen?seq_contains(id) && (alwaysFolded?seq_contains(id) || items?size > foldAbove)>
    <#local errors = items?filter(i -> i.level == "error")?size>
    <#local warnings = items?filter(i -> i.level == "warning")?size>
    <#local changed = items?filter(i -> i.changed)?size>
    <tr class="ov-grp ov-sec<#if folded> ov-closed</#if>" data-ov-sec="${id}">
        <td colspan="2">
            <div class="ov-sec-h">
                <button type="button" class="ov-sec-toggle" aria-expanded="${(!folded)?c}"><@icon name="expand_more"/><span>${label}</span></button>
                <span class="ov-sec-sum">
                    <span><@s.text name=countKey><@s.param>${items?size}</@s.param></@s.text></span>
                    <#if (errors > 0)><span class="bad"><@icon name="error"/><@s.text name="manage.overview.data.section.needFixing"><@s.param>${errors}</@s.param></@s.text></span></#if>
                    <#if (warnings > 0)><span class="warn"><@icon name="warning"/><@s.text name="manage.overview.data.section.warnings"><@s.param>${warnings}</@s.param></@s.text></span></#if>
                    <#if (changed > 0)><span class="chg"><@s.text name="manage.overview.data.section.changed"><@s.param>${changed}</@s.param></@s.text></span></#if>
                </span>
            </div>
            <div class="ov-sec-maps">
                <#list items as i>
                    <a class="ov-map<#if i.level?has_content> ${i.level}</#if>" href="${i.href}"><#if i.level?has_content><@icon name=i.level/></#if><span class="ov-map-src">${i.source}</span><span class="ov-map-ar">&rarr;</span><b>${i.name}</b></a>
                </#list>
            </div>
        </td>
    </tr>
</#macro>

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
            <@popoverTextInfo sourcesInfo true/>
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
        <#-- starting over: delete every mapping, or every source and so every mapping -->
        <#if resource.sources?has_content>
            <div class="dropdown">
                <button class="ov-hmenu" type="button" id="data-menu" data-bs-toggle="dropdown" aria-expanded="false" title="<@s.text name='manage.overview.data.actions'/>" aria-label="<@s.text name='manage.overview.data.actions'/>">
                    <@icon name="more_horiz"/>
                </button>
                <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="data-menu">
                    <#if resource.hasAnyMapping()>
                        <li><a class="dropdown-item text-gbif-danger delete-all-mappings" href="resource-deleteAllMappings.do?r=${resource.shortname}" data-post><@s.text name="manage.overview.data.deleteAllMappings"/></a></li>
                    </#if>
                    <li><a class="dropdown-item text-gbif-danger delete-all-sources" href="resource-deleteAllSources.do?r=${resource.shortname}" data-post><@s.text name="manage.overview.data.deleteAllSources"/></a></li>
                </ul>
            </div>
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
        <#elseif isPublished && !liveSources?has_content>
            <@s.text name="manage.overview.notModified"/>.
        </#if>
        <#if isPublished>${liveSourcesNote}</#if>
    </div>

    <#if !resource.sources?has_content>
        <div class="ov-empty">
            <b><@s.text name="manage.overview.data.empty.title"/></b>
            <@s.text name="manage.overview.data.empty.text"/>
            <div class="mt-2"><button type="button" class="btn btn-sm btn-outline-gbif-primary ov-open-add-source"><@icon name="add"/> <@s.text name="manage.overview.data.addSource"/></button></div>
        </div>

    <#elseif isDataPackage>
        <#-- data packages: one line per mapped table, grouped like the schema, problems first in each group; unused sources last -->
        <#-- DwC-DP: grouped by the table's keys (DwcDpTableGroup); a table the schema doesn't have is an entity -->
        <#assign dpRows = []>
        <#list resource.dataPackageMappings! as m>
            <#assign groupName = "">
            <#if isDwcDp>
                <#assign groupName = m.tableSchemaMissing?then("entities", (m.tableSchema.dwcDpGroup.key)!"entities")>
            </#if>
            <#assign dpRows = dpRows + [{"m": m, "mid": m_index, "group": groupName, "problem": dpProblem(m)}]>
        </#list>
        <#assign groupOrder = ["events", "entities", "assertions", "joins", ""]>

        <@dataTools rowCount=dpRows?size + unmappedSources?size anyChanged=(showChangedTags && dpRows?filter(r -> isMappingChanged(r.m))?has_content) || changedSources?has_content anyProblem=dpRows?filter(r -> r.problem)?has_content || unreadableSources?has_content/>

        <div id="ov-data">
        <#if dpRows?has_content>
        <#-- the table name links to the mapping and the source name to the source; each is followed by its menu -->
        <table class="ov-table ov-split">
            <thead>
            <tr>
                <th><@s.text name="manage.overview.data.col.table"/></th>
                <th><@s.text name="manage.overview.data.col.fromSource"/></th>
            </tr>
            </thead>
            <tbody>
            <#list groupOrder as groupName>
                <#assign rowsInGroup = dpRows?filter(r -> r.group == groupName)>
                <#assign rowsInGroup = rowsInGroup?filter(r -> r.problem) + rowsInGroup?filter(r -> !r.problem)>
                <#if rowsInGroup?has_content>
                    <#-- other data packages have no groups: their tables aren't folded -->
                    <#assign secId = groupName?lower_case>
                    <#if groupName?has_content>
                        <#assign secItems = []>
                        <#list rowsInGroup as r>
                            <#assign secItems = secItems + [{"name": (r.m.dataPackageTableSchemaName.name)!"?", "source": (r.m.source.name)!"?",
                                "href": "dataPackageMapping.do?r=${resource.shortname}&id=${r.m.dataPackageSchema.identifier?url}&mid=${r.mid}",
                                "level": dpLevel(r.m), "changed": showChangedTags && isMappingChanged(r.m)}]>
                        </#list>
                        <#assign secLabel><@s.text name="manage.overview.data.group.${groupName}"/></#assign>
                        <@sectionHeading id=secId label=secLabel items=secItems countKey="manage.overview.data.section.tables"/>
                    </#if>
                    <#-- a section where every table changed says so in its heading only -->
                    <#assign rowTags = showChangedTags && !(groupName?has_content && !rowsInGroup?filter(x -> !isMappingChanged(x.m))?has_content)>
                    <#assign sourcesShown = []>
                    <#list rowsInGroup as r>
                        <#assign m = r.m>
                        <#assign tableName = (m.dataPackageTableSchemaName.name)!"?">
                        <#assign sourceName = (m.source.name)!"?">
                        <#assign missingRequired = (missingRequiredFieldsDataPackage!{})?keys?seq_contains(tableName)>
                        <#assign editUrl = "dataPackageMapping.do?r=${resource.shortname}&id=${m.dataPackageSchema.identifier?url}&mid=${r.mid}">
                        <tr class="ov-row" data-ov-sec="${secId}" data-ov-text="${(tableName + ' ' + sourceName)?lower_case}" data-ov-changed="${dpChanged(m)?c}" data-ov-unmapped="false" data-ov-problem="${r.problem?c}">
                            <td>
                                <a href="${editUrl}" class="ov-name fw-semibold">${tableName}</a><#if rowTags && isMappingChanged(m)><@changedTag/></#if><@nameActions id="dp-mapping-menu-" + r.mid?c titleKey="manage.overview.data.mappingActions" items=[
                                    {"icon": "edit", "text": "manage.overview.data.editMapping", "href": editUrl},
                                    {"icon": "delete", "text": "manage.overview.data.deleteMapping", "href": "deleteDataPackageMapping.do?r=${resource.shortname}&id=${m.dataPackageSchema.identifier?url}&mid=${r.mid}", "del": "delete-mapping", "name": tableName}]/>
                                <div class="ov-small"><@s.text name="manage.overview.data.fieldsMapped"><@s.param>${(m.fields![])?filter(f -> f.mapped && !m.unresolvedFieldNames?seq_contains((f.field.name)!""))?size}</@s.param><@s.param><#if m.tableSchemaMissing>?<#else>${(m.tableSchema.fields)!?size}</#if></@s.param></@s.text></div>
                                <#if missingRequired && !m.tableSchemaMissing><@problemNote "manage.overview.data.status.requiredMissing"/></#if>
                                <@dataPackageMappingIssues m/>
                            </td>
                            <td><#if (m.source)??><@sourceCell src=m.source menuId="dp-" + r.mid?c showDims=!sourcesShown?seq_contains(m.source.name)/><#assign sourcesShown = sourcesShown + [m.source.name]><#else>?</#if></td>
                        </tr>
                    </#list>
                </#if>
            </#list>
            </tbody>
        </table>
        </#if>
        <@noMatch/>
        <@unusedSources "manage.overview.data.group.notUsed" "dp-unused-"/>
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

        <@dataTools rowCount=classRows?size + unmappedSources?size anyChanged=(showChangedTags && classRows?filter(r -> isMappingChanged(r.m))?has_content) || changedSources?has_content anyProblem=emptyMappings?has_content || unreadableSources?has_content/>

        <#-- deleting the only core mapping deletes the extension mappings too (Resource.deleteMapping) -->
        <#if (classRows?filter(r -> r.core)?size == 1) && (classRows?size > 1)>
            <#assign deleteCoreMappingClass = "delete-core-mapping">
        <#else>
            <#assign deleteCoreMappingClass = "delete-mapping">
        </#if>

        <div id="ov-data">
        <#if classGroups?has_content>
        <table class="ov-table ov-split">
            <thead>
            <tr>
                <th><@s.text name="manage.overview.data.col.class"/></th>
                <th><@s.text name="manage.overview.data.col.fromSource"/></th>
            </tr>
            </thead>
            <tbody>
            <#assign lastGroup = "">
            <#list classGroups as g>
                <#assign groupKey = g.core?then("manage.overview.data.group.core", "manage.overview.data.group.extensions")>
                <#if groupKey != lastGroup>
                    <#assign secId = g.core?then("core", "extensions")>
                    <#assign secItems = []>
                    <#list classGroups?filter(x -> x.core == g.core) as sg>
                        <#list sg.rows as r>
                            <#assign secItems = secItems + [{"name": r.ext.title, "source": (r.m.source.name)!"?",
                                "href": "mapping.do?r=${resource.shortname}&id=${r.ext.rowType?url}&mid=${r.mid}",
                                "level": dwcLevel(r.m), "changed": showChangedTags && isMappingChanged(r.m)}]>
                        </#list>
                    </#list>
                    <#assign secLabel><@s.text name=groupKey/></#assign>
                    <@sectionHeading id=secId label=secLabel items=secItems countKey="manage.overview.data.section.mappings"/>
                    <#-- a section where every mapping changed says so in its heading only -->
                    <#assign rowTags = showChangedTags && secItems?filter(i -> !i.changed)?has_content>
                    <#assign sourcesShown = []>
                    <#assign lastGroup = groupKey>
                </#if>
                <#-- a row per mapping; a class mapped from several sources has a row per source, named alike -->
                <#list g.rows as r>
                    <@dwcMappingRow r/>
                </#list>
            </#list>
            </tbody>
        </table>
        <#else>
            <p class="text-muted text-smaller mb-2"><@s.text name="manage.overview.data.noMappings"/></p>
        </#if>
        <@noMatch/>
        <@unusedSources "manage.overview.data.group.notInArchive" "unused-"/>
        </div>
    </#if>
</section>

<#-- one Darwin Core mapping -->
<#macro dwcMappingRow r>
    <#local m = r.m>
    <#local editUrl = "mapping.do?r=${resource.shortname}&id=${r.ext.rowType?url}&mid=${r.mid}">
    <tr class="ov-row" data-ov-sec="${secId}"<#if (m.source)??> data-ov-text="${(m.source.name + ' ' + r.ext.title)?lower_case}" data-ov-changed="${isSourceChanged(m.source)?c}"</#if> data-ov-mapping-changed="${isMappingChanged(m)?c}" data-ov-unmapped="false" data-ov-problem="${dwcProblem(r)?c}">
        <td>
            <#local items = [
                {"icon": "edit", "text": "manage.overview.data.editMapping", "href": editUrl},
                {"icon": "visibility", "text": "button.preview", "href": "mappingPeek.do?r=${resource.shortname}&id=${r.ext.rowType?url}&mid=${r.mid}",
                    "cls": "peekBtn", "name": r.ext.title + (m.source??)?then(" \x2190 " + (m.source.name)!"", "")},
                {"icon": "delete", "text": "manage.overview.data.deleteMapping", "href": "delete-mapping.do?r=${resource.shortname}&id=${r.ext.rowType?url}&mid=${r.mid}",
                    "del": r.core?then(deleteCoreMappingClass, "delete-mapping"), "name": r.ext.title}]>
            <#local termsLine><@s.text name="manage.overview.data.termsMapped"><@s.param>${m.fields?size}</@s.param><@s.param>${(r.ext.properties)!?size}</@s.param></@s.text></#local>
            <a href="${editUrl}" class="ov-name fw-semibold">${r.ext.title}</a><#if rowTags && isMappingChanged(m)><@changedTag/></#if><@nameActions id="dwc-mapping-menu-" + r.mid?c + "-" + r.ext.rowType?url items=items titleKey="manage.overview.data.mappingActions"/>
            <div class="ov-small">${termsLine}</div>
            <#if m.fields?size == 0><@problemNote "manage.overview.data.status.noTerms" "warning"/></#if>
        </td>
        <td><#if (m.source)??><@sourceCell src=m.source menuId="dwc-" + r.mid?c + "-" + r.ext.rowType?url showDims=!sourcesShown?seq_contains(m.source.name)/><#assign sourcesShown = sourcesShown + [m.source.name]><#else>?</#if></td>
    </tr>
</#macro>
