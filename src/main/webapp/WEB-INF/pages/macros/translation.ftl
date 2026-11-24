<#-- Value translation pages (manage/translation.ftl, manage/dataPackageTranslation.ftl): the source values that have a
     translation, each with what it is published as. Other values are published as they are; the "Add a translation"
     picker lists them (and takes typed values, for sources with more distinct values than the IPT reads). Each row posts
     smap['k'] and tmap['k'] (js/translation.js names tmap on submit: the control, or an empty value when published
     empty), so ValueTranslation keeps what it kept before. -->

<#-- one row; state: "set" (translated), "blank" (published empty) or "none" (just added, nothing chosen yet) -->
<#macro valueRow key source translated="" state="none" notInSource=false>
    <#local hasVocab = (vocabTermsSize > 0)/>
    <div class="tr-row" data-key="${key}" data-state="${state}">
        <input type="hidden" name="smap['${key}']" value="${source}"/>
        <div class="tr-src">
            <#if source?has_content><span class="tr-val">${source}</span><#else><span class="tr-val tr-val-empty"><@s.text name="manage.mapping.field.empty"/></span></#if>
            <#if notInSource><span class="mp-badge t" title="<@s.text name='manage.translation.notInSource.title'/>"><@s.text name="manage.translation.notInSource"/></span></#if>
        </div>
        <@icon name="arrow_forward" class="tr-arrow"/>
        <div class="tr-to">
            <div class="tr-ctl">
                <#if hasVocab>
                    <#-- the vocabulary's options are copied in from #tr-vocab when the select is first used (js/translation.js) -->
                    <select class="tr-input form-select form-select-sm" aria-label="<@s.text name='manage.translation.head.published'/>: ${source}">
                        <option value=""><@s.text name="manage.translation.choose"/></option>
                        <#if translated?has_content>
                            <option value="${translated}" selected><#if (vocabTerms[translated])??>${vocabTerms[translated]}<#else>${translated}</#if></option>
                        </#if>
                    </select>
                <#else>
                    <input type="text" class="tr-input form-control form-control-sm" value="${translated}" placeholder="<@s.text name='manage.translation.type'/>" aria-label="<@s.text name='manage.translation.head.published'/>: ${source}"/>
                </#if>
                <#-- published empty: shown instead of the control -->
                <span class="tr-blank-pill"><@s.text name="manage.translation.blank"/></span>
                <input type="hidden" class="tr-blank" value=""/>
                <button type="button" class="tr-btn tr-blank-btn" title="<@s.text name='manage.translation.blank.set'/>" aria-label="<@s.text name='manage.translation.blank.set'/>"><@icon name="block"/></button>
                <button type="button" class="tr-btn tr-clear-btn" title="<@s.text name='manage.translation.clear'/>" aria-label="<@s.text name='manage.translation.clear'/>"><@icon name="delete"/></button>
            </div>
            <#if hasVocab && translated?has_content && !(vocabTerms[translated])??>
                <div class="mp-msg w tr-server-msg"><@icon name="warning"/><span><@s.text name="manage.translation.notInVocabulary"/></span></div>
            </#if>
            <div class="mp-msg bad tr-problem" hidden></div>
        </div>
    </div>
</#macro>

<#-- the Translations section; the nested content is the form's hidden parameters -->
<#macro valuesSection formAction automapUrl="">
    <span class="mp-anchor" id="anchor-tr-values"></span>
    <section id="tr-values" class="mp-section">
        <div class="mp-sec-head">
            <h2><@s.text name="manage.translation.values.title"/></h2>
            <span><@s.text name="manage.translation.values.desc"/></span>
            <#if automapUrl?has_content>
                <#-- translates the source values not translated yet that match the vocabulary; nothing is saved until Save -->
                <a class="btn btn-sm btn-outline-secondary tr-automap" href="${automapUrl}"><@s.text name="manage.translation.automap"/></a>
            </#if>
        </div>

        <form id="translation-form" action="${formAction}" method="post">
            <#nested>
            <#-- search only where there are enough rows to need it (js/translation.js) -->
            <div class="tr-search" hidden>
                <input type="search" id="tr-q" class="form-control form-control-sm" form="tr-ui" placeholder="<@s.text name='manage.translation.search'/>" aria-label="<@s.text name='manage.translation.search'/>">
            </div>
            <div class="tr-list">
                <div class="tr-head"><span><@s.text name="manage.translation.head.source"/></span><span></span><span><@s.text name="manage.translation.head.published"/></span></div>
                <div id="tr-rows">
                    <#list sourceValuesMap as k, val>
                        <#if (tmap.get(k))??>
                            <#local translated = tmap.get(k)/>
                            <@valueRow key=k source=val translated=translated state=translated?has_content?then("set", "blank") notInSource=action.isNotInSource(k)/>
                        </#if>
                    </#list>
                </div>
                <div class="tr-none-msg" id="tr-nomatch" hidden><span><@s.text name="manage.translation.noMatch"/></span> <button type="button" class="ov-link-btn" id="tr-clear-q"><@s.text name="manage.mapping.fields.clearSearch"/></button></div>
                <div class="tr-none-msg" id="tr-empty" hidden><@s.text name="manage.translation.values.none"/></div>
            </div>
        </form>

        <#-- the source values without a translation; a typed value covers one the IPT didn't read -->
        <div class="tr-add">
            <label class="mp-fl" for="tr-add"><@s.text name="manage.translation.add.label"/></label>
            <div class="tr-add-row">
                <select id="tr-add" form="tr-ui" class="form-select form-select-sm">
                    <option value=""></option>
                    <#list sourceValuesMap as k, val>
                        <#-- the key as the value: an empty source value would otherwise be taken for the placeholder; "(empty)" in
                             brackets, so a typed value "empty" isn't matched to it -->
                        <#if !(tmap.get(k))??><option value="${k}" data-source="${val}"><#if val?has_content>${val}<#else>(<@s.text name="manage.mapping.field.empty"/>)</#if></option></#if>
                    </#list>
                </select>
            </div>
            <div class="mp-msg mu"><@s.text name="manage.translation.add.hint"/></div>
            <div class="mp-msg mu" id="tr-add-msg" hidden></div>
        </div>
    </section>

    <#-- a new row, copied by js/translation.js -->
    <template id="tr-row-template"><@valueRow key="" source=""/></template>
    <#if (vocabTermsSize > 0)>
        <select id="tr-vocab" hidden>
            <option value=""><@s.text name="manage.translation.choose"/></option>
            <#list vocabTermsKeys as code><option value="${code}">${vocabTerms[code]}</option></#list>
        </select>
    </#if>
</#macro>

<#-- texts for js/translation.js -->
<#macro i18n>
    <div id="tr-i18n" hidden
         data-automapped="${automapped?c}"
         data-unsaved="<@s.text name='manage.mapping.savebar.leave'/>"
         data-add-placeholder="<@s.text name='manage.translation.add.placeholder'/>"
         data-add-exists="<@s.text name='manage.translation.add.exists'/>"
         data-empty="<@s.text name='manage.mapping.field.empty'/>"
         data-no-results="${selectNoResultsFound}"
         data-missing="<@s.text name='manage.translation.missing'/>"
         data-msg-number="<@s.text name='validation.number.expected'/>"
         data-msg-integer="<@s.text name='validation.integer.expected'/>"
         data-msg-boolean="<@s.text name='validation.boolean.expected'/>"
         data-msg-required="<@s.text name='validation.required.violated'/>"
         data-msg-pattern="<@s.text name='validation.pattern.violated'/>"
         data-msg-minimum="<@s.text name='validation.minimum.violated'/>"
         data-msg-maximum="<@s.text name='validation.maximum.violated'/>"></div>
</#macro>

<#macro saveBar discardUrl>
    <div class="mp-savebar" id="tr-savebar" aria-hidden="true">
        <div class="container">
            <span class="ov-grow"><@s.text name="manage.mapping.savebar.unsaved"/></span>
            <a class="btn btn-sm btn-outline-secondary" href="${discardUrl}" id="tr-discard"><@s.text name="manage.mapping.savebar.discard"/></a>
            <button type="submit" form="translation-form" class="btn btn-sm btn-gbif-solid" name="save" value="save"><@icon name="save"/> <@s.text name="button.save"/></button>
        </div>
    </div>
</#macro>

<#macro deleteModal fieldName>
    <div class="modal fade" id="tr-delete-modal" tabindex="-1" role="alertdialog" aria-labelledby="tr-delete-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered dlg-alert">
            <div class="modal-content">
                <div class="modal-header">
                    <span class="dlg-tone danger" aria-hidden="true"><@icon name="delete"/></span>
                    <h5 class="modal-title" id="tr-delete-modal-title"><@s.text name="dialog.translation.delete.title"/></h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="<@s.text name='button.cancel'/>"></button>
                </div>
                <div class="modal-body text-smaller">
                    <p class="mb-0"><@s.text name="manage.translation.delete.text"><@s.param>${fieldName}</@s.param></@s.text></p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal"><@s.text name="button.cancel"/></button>
                    <button type="submit" form="translation-form" class="btn btn-sm btn-danger" name="deleteFlag" value="delete"><@s.text name="button.delete"/></button>
                </div>
            </div>
        </div>
    </div>
</#macro>
