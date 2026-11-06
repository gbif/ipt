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
        <span class="ov-small bad"><i class="bi bi-exclamation-triangle-fill"></i> <#list issues as issue>${issue}<#sep>; </#sep></#list></span>
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
<#macro sourceCell src bold=true>
    <div class="ov-src">
        <@sourceType src/>
        <div>
            <a href="source.do?r=${resource.shortname}&id=${src.name?url}" class="ov-name<#if bold> fw-semibold<#else> font-monospace</#if>">${src.name}</a><#if isSourceChanged(src)><span class="ov-chg"><@s.text name="manage.overview.data.changedTag"/></span></#if>
            <@sourceDims src/>
        </div>
    </div>
</#macro>

<#-- the ⋯ menu for a source: edit, download, delete -->
<#macro sourceMenu src id>
    <div class="dropdown">
        <a href="#" class="ov-name" id="source-menu-${id}" data-bs-toggle="dropdown" aria-expanded="false" title="<@s.text name='manage.overview.data.sourceActions'/>"><i class="bi bi-three-dots"></i></a>
        <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="source-menu-${id}">
            <li><a class="dropdown-item" href="source.do?r=${resource.shortname}&id=${src.name?url}"><@s.text name="manage.overview.data.editSource"/></a></li>
            <#if src.isFileSource() || src.isExcelSource()>
                <li><a class="dropdown-item" href="raw-source.do?r=${resource.shortname}&id=${src.name?url}"><@s.text name="manage.overview.data.downloadSource"/></a></li>
            </#if>
            <li><hr class="dropdown-divider"></li>
            <li><a class="dropdown-item text-gbif-danger delete-source" href="delete-source.do?r=${resource.shortname}&id=${src.name?url}"><@s.text name="manage.overview.data.deleteSource"/></a></li>
        </ul>
    </div>
</#macro>

<#macro statusPill cls key>
    <span class="ov-pill ${cls}"><@s.text name=key/></span>
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
        <#if resource.sources?has_content>
            <span class="ov-pill grey"><@s.text name="manage.overview.data.sourcesCount"><@s.param>${resource.sources?size}</@s.param></@s.text></span>
            <#if isDataPackage>
                <span class="ov-pill grey"><@s.text name="manage.overview.data.tablesCount"><@s.param>${(resource.dataPackageMappings![])?size}</@s.param></@s.text></span>
            <#else>
                <span class="ov-pill grey"><@s.text name="manage.overview.data.mappingsCount"><@s.param>${classRows?size}</@s.param></@s.text></span>
            </#if>
            <#if unmappedSources?has_content>
                <span class="ov-pill warn"><@s.text name="manage.overview.data.notMappedCount"><@s.param>${unmappedSources?size}</@s.param></@s.text></span>
            </#if>
            <#if unreadableSources?has_content>
                <span class="ov-pill bad"><@s.text name="manage.overview.data.unreadableCount"><@s.param>${unreadableSources?size}</@s.param></@s.text></span>
            </#if>
        </#if>
        <span class="ov-sp"></span>
        <a id="add-source-button" class="btn btn-sm btn-outline-gbif-primary source-action" href="#"><i class="bi bi-plus"></i> <@s.text name="manage.overview.data.addSource"/></a>
        <#if isDataPackage && resource.sources?has_content>
            <form action='dataPackageMappingSourceNew.do' method='post' class="d-inline">
                <input name="r" type="hidden" value="${resource.shortname}"/>
                <input name="id" type="hidden" value="${resource.dataPackageIdentifier}">
                <button name="add" class="btn btn-sm btn-outline-gbif-primary" type="submit"><i class="bi bi-plus"></i> <@s.text name="manage.overview.data.mapTable"/></button>
            </form>
        <#else>
            <a id="add-mapping-button" class="btn btn-sm btn-outline-gbif-primary mapping-action" href="#"><i class="bi bi-plus"></i> <#if isDataPackage><@s.text name="manage.overview.data.mapTable"/><#else><@s.text name="manage.overview.data.addMapping"/></#if></a>
        </#if>
    </div>

    <div class="ov-sub">
        <#if isDataPackage>
            <@s.text name="manage.overview.data.sub.dp"/>
        <#else>
            <span class="ov-sub-class"><@s.text name="manage.overview.data.sub.class"/></span>
            <span class="ov-sub-source" style="display: none;"><@s.text name="manage.overview.data.sub.source"/></span>
        </#if>
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
            <div class="mt-2"><button type="button" class="btn btn-sm btn-outline-gbif-primary ov-open-add-source"><i class="bi bi-plus"></i> <@s.text name="manage.overview.data.addSource"/></button></div>
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
                <th><@s.text name="manage.overview.data.col.fields"/></th>
                <th><@s.text name="manage.overview.data.col.status"/></th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <#list groupOrder as groupName>
                <#assign rowsInGroup = dpRows?filter(r -> r.group == groupName)>
                <#if rowsInGroup?has_content>
                    <#if groupName?has_content><tr class="ov-grp"><td colspan="4">${groupName}</td></tr></#if>
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
                                        <a href="${editUrl}" class="ov-name fw-semibold">${tableName}</a>
                                        <span class="ov-small"><@s.text name="manage.overview.data.fromSource"><@s.param>${(m.source.name)!"?"}</@s.param></@s.text><#if (m.source)?? && isSourceChanged(m.source)><span class="ov-chg"><@s.text name="manage.overview.data.changedTag"/></span></#if></span>
                                        <@dataPackageMappingIssues m/>
                                    </div>
                                </div>
                            </td>
                            <td class="text-nowrap"><@s.text name='manage.overview.mappings.fields'><@s.param>${(m.fields)!?size}</@s.param></@s.text></td>
                            <td>
                                <#if m.tableSchemaMissing || missingRequired>
                                    <@statusPill "bad" "manage.overview.data.status.requiredMissing"/>
                                <#elseif (m.source)?? && !m.source.readable>
                                    <@statusPill "bad" "manage.overview.data.status.unreadable"/>
                                <#elseif dataPackageMappingHasIssues(m)>
                                    <@statusPill "warn" "manage.overview.data.status.needsReview"/>
                                <#elseif isMappingChanged(m) || ((m.source)?? && isSourceChanged(m.source))>
                                    <@statusPill "warn" "manage.overview.data.status.changed"/>
                                <#else>
                                    <@statusPill "ok" "manage.overview.data.status.mapped"/>
                                </#if>
                            </td>
                            <td class="ov-rowacts">
                                <a href="${editUrl}"><@s.text name="button.edit"/></a>
                                <div class="dropdown">
                                    <a href="#" id="dp-mapping-menu-${r.mid}" data-bs-toggle="dropdown" aria-expanded="false" title="<@s.text name='manage.overview.data.mappingActions'/>"><i class="bi bi-three-dots"></i></a>
                                    <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="dp-mapping-menu-${r.mid}">
                                        <#if (m.source.name)??>
                                            <li><a class="dropdown-item" href="source.do?r=${resource.shortname}&id=${m.source.name?url}"><@s.text name="manage.overview.data.editSource"/></a></li>
                                            <li><hr class="dropdown-divider"></li>
                                        </#if>
                                        <li><a class="dropdown-item text-gbif-danger delete-mapping" href="deleteDataPackageMapping.do?r=${resource.shortname}&id=${m.dataPackageSchema.identifier?url}&mid=${r.mid}"><@s.text name="manage.overview.data.deleteMapping"/></a></li>
                                    </ul>
                                </div>
                            </td>
                        </tr>
                    </#list>
                </#if>
            </#list>
            <#if unmappedSources?has_content>
                <tr class="ov-grp"><td colspan="4"><@s.text name="manage.overview.data.group.notUsed"/></td></tr>
                <#list unmappedSources as src>
                    <tr>
                        <td><@sourceCell src/></td>
                        <td></td>
                        <td><#if src.readable><@statusPill "warn" "manage.overview.data.status.notMapped"/><#else><@statusPill "bad" "manage.overview.data.status.unreadable"/></#if></td>
                        <td class="ov-rowacts"><@sourceMenu src "dp-" + src?index/></td>
                    </tr>
                </#list>
            </#if>
            </tbody>
        </table>
        <#if installedSchemaVersion?has_content>
            <div class="ov-more"><span></span><span>${installedSchemaName!""} ${installedSchemaVersion}</span></div>
        </#if>

    <#else>
        <#-- Darwin Core Archive: by class (what goes into the archive) or by source (what was uploaded) -->
        <#assign rowLimit = 8>
        <#assign showTools = (resource.sources?size > rowLimit)>
        <div class="ov-tools">
            <#if showTools>
                <input type="search" id="ov-data-filter" placeholder="<@s.text name='manage.overview.data.filter.placeholder'/>" aria-label="<@s.text name='manage.overview.data.filter.placeholder'/>">
                <button type="button" class="ov-chip on" data-ov-filter="all"><@s.text name="manage.overview.data.filter.all"/> ${resource.sources?size}</button>
                <#if changedSources?has_content><button type="button" class="ov-chip" data-ov-filter="changed"><@s.text name="manage.overview.data.filter.changed"/> ${changedSources?size}</button></#if>
                <#if unmappedSources?has_content><button type="button" class="ov-chip" data-ov-filter="unmapped"><@s.text name="manage.overview.data.filter.unmapped"/> ${unmappedSources?size}</button></#if>
                <#if unreadableSources?has_content || emptyMappings?has_content><button type="button" class="ov-chip" data-ov-filter="problems"><@s.text name="manage.overview.data.filter.problems"/> ${unreadableSources?size + emptyMappings?size}</button></#if>
            </#if>
            <span class="flex-grow-1"></span>
            <span class="ov-seg" role="group" aria-label="<@s.text name='manage.overview.data.groupBy'/>">
                <span><@s.text name="manage.overview.data.groupBy"/></span>
                <button type="button" class="on" data-ov-group="class"><@s.text name="manage.overview.data.group.byClass"/></button>
                <button type="button" data-ov-group="source"><@s.text name="manage.overview.data.group.bySource"/></button>
            </span>
        </div>

        <#if (classRows?size > 1)>
            <#assign deleteCoreMappingClass = "delete-core-mapping">
        <#else>
            <#assign deleteCoreMappingClass = "delete-mapping">
        </#if>

        <#-- by class -->
        <table class="ov-table ov-limited" id="ov-table-class" data-ov-limit="${rowLimit}">
            <thead>
            <tr>
                <th><@s.text name="manage.overview.data.col.class"/></th>
                <th><@s.text name="manage.overview.data.col.fromSource"/></th>
                <th><@s.text name="manage.overview.data.col.terms"/></th>
                <th><@s.text name="manage.overview.data.col.status"/></th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <#assign lastGroup = "">
            <#list classRows as r>
                <#assign m = r.m>
                <#assign groupKey = r.core?then("manage.overview.data.group.core", "manage.overview.data.group.extensions")>
                <#if groupKey != lastGroup>
                    <tr class="ov-grp"><td colspan="5"><@s.text name=groupKey/></td></tr>
                    <#assign lastGroup = groupKey>
                </#if>
                <#assign editUrl = "mapping.do?r=${resource.shortname}&id=${r.ext.rowType?url}&mid=${r.mid}">
                <tr class="ov-row"<#if (m.source)??> <@filterAttrs m.source r.ext.title/> data-ov-problem="${(!m.source.readable || m.fields?size == 0)?c}"</#if>>
                    <td>
                        <a href="${editUrl}" class="ov-name fw-semibold">${r.ext.title}</a><#if r.core><span class="ov-core"><@s.text name="manage.overview.data.coreTag"/></span></#if>
                    </td>
                    <td><#if (m.source)??><@sourceCell m.source false/><#else>?</#if></td>
                    <td class="text-nowrap"><@s.text name='manage.overview.mappings.terms'><@s.param>${m.fields?size}</@s.param></@s.text></td>
                    <td>
                        <#if (m.source)?? && !m.source.readable>
                            <@statusPill "bad" "manage.overview.data.status.unreadable"/>
                        <#elseif m.fields?size == 0>
                            <@statusPill "warn" "manage.overview.data.status.noTerms"/>
                        <#elseif (m.source)?? && isSourceChanged(m.source)>
                            <@statusPill "warn" "manage.overview.data.status.sourceChanged"/>
                        <#elseif isMappingChanged(m)>
                            <@statusPill "warn" "manage.overview.data.status.changed"/>
                        <#else>
                            <@statusPill "ok" "manage.overview.data.status.mapped"/>
                        </#if>
                    </td>
                    <td class="ov-rowacts">
                        <a href="${editUrl}"><@s.text name="button.edit"/></a>
                        <a class="peekBtn" href="mappingPeek.do?r=${resource.shortname}&id=${r.ext.rowType?url}&mid=${r.mid}"><@s.text name="button.preview"/></a>
                        <div class="dropdown">
                            <a href="#" id="mapping-menu-${r?index}" data-bs-toggle="dropdown" aria-expanded="false" title="<@s.text name='manage.overview.data.mappingActions'/>"><i class="bi bi-three-dots"></i></a>
                            <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="mapping-menu-${r?index}">
                                <#if (m.source)??>
                                    <li><a class="dropdown-item" href="source.do?r=${resource.shortname}&id=${m.source.name?url}"><@s.text name="manage.overview.data.editSource"/></a></li>
                                    <#if m.source.isFileSource() || m.source.isExcelSource()>
                                        <li><a class="dropdown-item" href="raw-source.do?r=${resource.shortname}&id=${m.source.name?url}"><@s.text name="manage.overview.data.downloadSource"/></a></li>
                                    </#if>
                                    <li><hr class="dropdown-divider"></li>
                                </#if>
                                <li><a class="dropdown-item text-gbif-danger ${r.core?then(deleteCoreMappingClass, 'delete-mapping')}" href="delete-mapping.do?r=${resource.shortname}&id=${r.ext.rowType?url}&mid=${r.mid}"><@s.text name="manage.overview.data.deleteMapping"/></a></li>
                            </ul>
                        </div>
                    </td>
                </tr>
            </#list>
            <#if unmappedSources?has_content>
                <tr class="ov-grp"><td colspan="5"><@s.text name="manage.overview.data.group.notInArchive"/></td></tr>
                <#list unmappedSources as src>
                    <tr class="ov-row" <@filterAttrs src ""/> data-ov-problem="${(!src.readable)?c}">
                        <td><button type="button" class="ov-link-btn ov-open-add-mapping"><@s.text name="manage.overview.data.mapTo"/></button></td>
                        <td><@sourceCell src false/></td>
                        <td></td>
                        <td><#if src.readable><@statusPill "warn" "manage.overview.data.status.notMapped"/><#else><@statusPill "bad" "manage.overview.data.status.unreadable"/></#if></td>
                        <td class="ov-rowacts"><@sourceMenu src "class-" + src?index/></td>
                    </tr>
                </#list>
            </#if>
            <#if !classRows?has_content && !unmappedSources?has_content>
                <tr><td colspan="5" class="text-muted"><@s.text name="manage.overview.data.noMappings"/></td></tr>
            </#if>
            <tr class="ov-nomatch" style="display: none;"><td colspan="5" class="text-muted"><@s.text name="manage.overview.data.noMatch"/></td></tr>
            </tbody>
        </table>

        <#-- by source -->
        <table class="ov-table ov-limited" id="ov-table-source" data-ov-limit="${rowLimit}" style="display: none;">
            <thead>
            <tr>
                <th><@s.text name="manage.overview.data.col.source"/></th>
                <th><@s.text name="manage.overview.data.col.mappedTo"/></th>
                <th><@s.text name="manage.overview.data.col.status"/></th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <#list resource.sources as src>
                <#assign srcRows = classRows?filter(r -> (r.m.source.name)! == src.name)>
                <tr class="ov-row" <@filterAttrs src srcRows?map(r -> r.ext.title)?join(" ")/> data-ov-problem="${(!src.readable || srcRows?filter(r -> r.m.fields?size == 0)?has_content)?c}">
                    <td><@sourceCell src/></td>
                    <td>
                        <#if srcRows?has_content>
                            <div class="ov-maps">
                                <#list srcRows as r>
                                    <span><a class="ov-name" href="mapping.do?r=${resource.shortname}&id=${r.ext.rowType?url}&mid=${r.mid}">${r.ext.title}</a><#if r.core><span class="ov-core"><@s.text name="manage.overview.data.coreTag"/></span></#if>
                                        <span class="text-muted text-smaller">· <@s.text name='manage.overview.mappings.terms'><@s.param>${r.m.fields?size}</@s.param></@s.text></span></span>
                                </#list>
                            </div>
                        <#else>
                            <button type="button" class="ov-link-btn ov-open-add-mapping"><@s.text name="manage.overview.data.mapTo"/></button>
                        </#if>
                    </td>
                    <td>
                        <#if !src.readable>
                            <@statusPill "bad" "manage.overview.data.status.unreadable"/>
                        <#elseif !srcRows?has_content>
                            <@statusPill "warn" "manage.overview.data.status.notMapped"/>
                        <#elseif srcRows?filter(r -> r.m.fields?size == 0)?has_content>
                            <@statusPill "warn" "manage.overview.data.status.incomplete"/>
                        <#elseif isSourceChanged(src)>
                            <@statusPill "warn" "manage.overview.data.status.changed"/>
                        <#else>
                            <@statusPill "ok" "manage.overview.data.status.mapped"/>
                        </#if>
                    </td>
                    <td class="ov-rowacts">
                        <a href="source.do?r=${resource.shortname}&id=${src.name?url}"><@s.text name="button.edit"/></a>
                        <@sourceMenu src "src-" + src?index/>
                    </td>
                </tr>
            </#list>
            <tr class="ov-nomatch" style="display: none;"><td colspan="4" class="text-muted"><@s.text name="manage.overview.data.noMatch"/></td></tr>
            </tbody>
        </table>
        <div class="ov-more" id="ov-data-more" style="display: none;"
             data-showing="<@s.text name='manage.overview.data.showing'/>"
             data-show-all="<@s.text name='manage.overview.data.showAll'/>"
             data-show-fewer="<@s.text name='manage.overview.data.showFewer'/>">
            <span id="ov-data-showing"></span>
            <button type="button" class="ov-link-btn" id="ov-data-expand"></button>
        </div>
    </#if>
</section>
