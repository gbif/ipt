<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.CamtrapMetadataAction" -->
    <#include "/WEB-INF/pages/macros/icon.ftl"/>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.datapackagemetadata.camtrap.keywords.title'/></title>
    <link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
    <link rel="stylesheet" href="${baseURL}/styles/overview.css">
    <link rel="stylesheet" href="${baseURL}/styles/eml.css">
    <#assign currentMenu="manage"/>
    <#assign currentMetadataPage = "keywords"/>
    <#assign savebarAction = "camtrap-metadata-keywords"/>
    <#assign materialHelpIcons = true/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <#-- one keyword: a chip, posted as metadata.keywords[i] (renumbered by the script) -->
    <#macro keywordChip index keyword>
        <span class="kw-chip"><span>${keyword}</span><button type="button" class="kw-chip-x" aria-label="${action.getText('manage.metadata.basic.keywords.removeKeyword', [keyword])}"><@icon name="close"/></button><input type="hidden" name="metadata.keywords[${index}]" value="${keyword}"></span>
    </#macro>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <form id="keywords-form" class="needs-validation track-unsaved" action="camtrap-metadata-${section}.do" method="post" novalidate>
        <input type="hidden" name="r" value="${resource.shortname}" />

        <#include "metadata_heading.ftl"/>

        <div class="container-fluid bg-body">
            <div class="container bd-layout main-content-container">
                <main class="bd-main">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "metadata_sidebar.ftl"/>
                    </div>

                    <div class="bd-content">
                        <div class="my-md-3 p-3">
                            <p class="party-intro text-smaller text-muted"><@s.text name="datapackagemetadata.keywords.intro"/></p>
                            <label class="form-label text-smaller" for="kw-entry"><@s.text name="datapackagemetadata.keywords"/></label>
                            <div class="form-control kw-chips" id="kw-chips">
                                <#list (metadata.keywords)![] as keyword>
                                    <#if keyword?has_content><@keywordChip index=keyword?index keyword=keyword/></#if>
                                </#list>
                                <input type="text" class="kw-entry" id="kw-entry" placeholder="${action.getText('manage.metadata.basic.keywords.placeholder')}">
                            </div>
                            <@s.fielderror cssClass="invalid-feedback d-block list-unstyled my-1" fieldName="metadata.keywords"/>
                        </div>
                    </div>
                </main>
            </div>
        </div>

        <#include "/WEB-INF/pages/manage/eml/eml_savebar.ftl"/>
    </form>

    <#include "/WEB-INF/pages/manage/eml/unsaved_changes_modal.ftl">

    <#include "/WEB-INF/pages/inc/footer.ftl">

    <script>
        (function () {
            // keywords as chips: Enter or a comma adds the typed text, Backspace in the empty box removes the last one
            var box = document.getElementById("kw-chips");
            var entry = document.getElementById("kw-entry");
            var removeLabel = "${action.getText('manage.metadata.basic.keywords.removeKeyword', ['{0}'])?js_string?no_esc}";
            var closeIcon = '<@icon name="close"/>';

            function changed() {
                box.dispatchEvent(new Event("input", {bubbles: true}));
            }
            function chips() {
                return Array.prototype.slice.call(box.querySelectorAll(".kw-chip"));
            }
            function renumber() {
                chips().forEach(function (chip, index) {
                    chip.querySelector("input").name = "metadata.keywords[" + index + "]";
                });
            }
            function chip(text) {
                var el = document.createElement("span");
                el.className = "kw-chip";
                var label = document.createElement("span");
                label.textContent = text;
                var x = document.createElement("button");
                x.type = "button";
                x.className = "kw-chip-x";
                x.setAttribute("aria-label", removeLabel.replace("{0}", text));
                x.innerHTML = closeIcon;
                var input = document.createElement("input");
                input.type = "hidden";
                input.value = text;
                el.appendChild(label);
                el.appendChild(x);
                el.appendChild(input);
                return el;
            }
            function flush() {
                var existing = chips().map(function (el) { return el.querySelector("input").value.toLowerCase(); });
                var added = false;
                entry.value.split(",").forEach(function (word) {
                    word = word.trim();
                    if (word && existing.indexOf(word.toLowerCase()) === -1) {
                        entry.before(chip(word));
                        existing.push(word.toLowerCase());
                        added = true;
                    }
                });
                entry.value = "";
                if (added) {
                    renumber();
                    changed();
                }
            }

            entry.addEventListener("keydown", function (e) {
                if (e.key === "Enter" || e.key === ",") {
                    e.preventDefault();
                    flush();
                } else if (e.key === "Backspace" && !entry.value) {
                    var last = entry.previousElementSibling;
                    if (last && last.classList.contains("kw-chip")) {
                        last.remove();
                        renumber();
                        changed();
                    }
                }
            });
            entry.addEventListener("paste", function () {
                setTimeout(flush, 0);
            });
            entry.addEventListener("blur", flush);
            box.addEventListener("click", function (e) {
                var x = e.target.closest(".kw-chip-x");
                if (x) {
                    x.closest(".kw-chip").remove();
                    renumber();
                    changed();
                    entry.focus();
                } else if (e.target === box) {
                    entry.focus();
                }
            });
            // a keyword still being typed is saved too
            document.getElementById("keywords-form").addEventListener("submit", flush);

            renumber();
            makeSureResourceParameterIsPresentInURL('${resource.shortname}');
        })();
    </script>
