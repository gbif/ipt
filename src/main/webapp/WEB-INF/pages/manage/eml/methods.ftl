    <#setting number_format="#####.##">
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.metadata.methods.title'/></title>
    <script>
        $(document).ready(function () {
            $('#metadata-section').change(function () {
                var metadataSection = $('#metadata-section').find(':selected').val()
                $(location).attr('href', 'metadata-' + metadataSection + '.do?r=${resource.shortname!r!}');
            });

            // scroll to the first error, if any
            var invalid = $(".is-invalid").first();
            if (invalid.length) {
                $('body, html').animate({scrollTop: invalid.offset().top - 120});
            }

            makeSureResourceParameterIsPresentInURL('${resource.shortname}');
        });
    </script>
    <#assign currentMetadataPage = "methods"/>
    <#assign currentMenu="manage"/>
    <#include "/WEB-INF/pages/macros/icon.ftl"/>
    <#assign materialHelpIcons = true/>
    <link rel="stylesheet" href="${baseURL}/styles/overview.css">
    <link rel="stylesheet" href="${baseURL}/styles/contacts.css">
    <link rel="stylesheet" href="${baseURL}/styles/eml.css">
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <#function t key>
        <#return action.getText(key)/>
    </#function>

    <#macro icn path>
        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="${path}"/></svg>
    </#macro>

    <#--
      One method step. The script numbers the cards and sets the textarea id/name (eml.methodSteps[i]) after every add,
      remove and move; the template (index "") has no posted errors.
    -->
    <#macro stepCard index="" value="">
        <#local name = index?has_content?then("eml.methodSteps[" + index + "]", "")/>
        <#local posted = index?has_content?then((action.fieldErrors[name])![], [])/>
        <div class="ms-step<#if posted?has_content> invalid</#if>" data-step>
            <div class="ms-step-head">
                <span class="party-grip" title="${t('eml.party.dragToReorder')}" aria-hidden="true">
                    <@icn "M9 4a2 2 0 1 1 0 4 2 2 0 0 1 0-4zm6 0a2 2 0 1 1 0 4 2 2 0 0 1 0-4zM9 10a2 2 0 1 1 0 4 2 2 0 0 1 0-4zm6 0a2 2 0 1 1 0 4 2 2 0 0 1 0-4zm-6 6a2 2 0 1 1 0 4 2 2 0 0 1 0-4zm6 0a2 2 0 1 1 0 4 2 2 0 0 1 0-4z"/>
                </span>
                <label class="ms-step-title" for="${name}" data-step-title><#if index?has_content>${t('manage.metadata.methods.step')?replace('{0}', (index + 1)?c)}</#if></label>
                <span class="party-order">
                    <button type="button" class="party-icon-btn" data-act="up" aria-label="${t('eml.party.moveUp')}" title="${t('eml.party.moveUp')}"><@icn "M7.41 15.41 12 10.83l4.59 4.58L18 14l-6-6-6 6z"/></button>
                    <button type="button" class="party-icon-btn" data-act="down" aria-label="${t('eml.party.moveDown')}" title="${t('eml.party.moveDown')}"><@icn "M7.41 8.59 12 13.17l4.59-4.58L18 10l-6 6-6-6z"/></button>
                </span>
                <button type="button" class="party-icon-btn party-icon-danger" data-act="remove" aria-label="${t('manage.metadata.methods.removeStep')}" title="${t('manage.metadata.methods.removeStep')}"><@icn "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></button>
            </div>
            <textarea class="form-control" rows="4" id="${name}" name="${name}">${value}</textarea>
            <#if posted?has_content>
                <ul id="field-error-${name}" class="invalid-feedback list-unstyled field-error d-block mb-0"><#list posted as message><li>${message}</li></#list></ul>
            </#if>
        </div>
    </#macro>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <form id="methods-form" class="needs-validation track-unsaved" action="metadata-${section}.do" method="post" novalidate>
        <#include "eml_heading.ftl"/>

        <#include "metadata_section_select.ftl"/>

        <div class="container-fluid bg-body">
            <div class="container bd-layout main-content-container">
                <main class="bd-main">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "eml_sidebar.ftl"/>
                    </div>

                    <div class="bd-content">
                        <div class="my-md-3 p-3" id="s-steps" data-toc="${t('manage.metadata.methods.steps')}">
                            <@textinline name="manage.metadata.methods.steps" i18nkey="eml.methodSteps" help="i18n"/>
                            <p class="party-intro"><@s.text name="manage.metadata.methods.steps.intro"/></p>

                            <div id="ms-steps" class="ms-steps">
                                <#list (eml.methodSteps)![] as step>
                                    <@stepCard index=step?index value=step!""/>
                                </#list>
                            </div>
                            <#-- sampling or quality control without any step (EML needs one) -->
                            <#assign stepsErrors = (action.fieldErrors["eml.methodSteps"])![]/>
                            <#if stepsErrors?has_content>
                                <ul class="list-unstyled text-gbif-danger small mb-0 mt-1"><#list stepsErrors as message><li>${message}</li></#list></ul>
                            </#if>
                            <p id="ms-none" class="geo-hint mt-0" hidden><@s.text name="manage.metadata.methods.noStep"/></p>
                            <a id="ms-add" href="" class="geo-add">
                                <@icon name="add"/> <span><@s.text name="manage.metadata.methods.addStep"/></span>
                            </a>
                            <template id="ms-step-template"><@stepCard/></template>
                        </div>

                        <div class="my-3 p-3" id="s-sampling" data-toc="${t('manage.metadata.methods.sampling')}">
                            <@textinline name="manage.metadata.methods.sampling"/>
                            <p class="party-intro"><@s.text name="manage.metadata.methods.sampling.intro"/></p>
                            <div class="row g-3">
                                <div class="col-12">
                                    <@text name="eml.studyExtent" help="i18n" rows=3/>
                                </div>
                                <div class="col-12">
                                    <@text name="eml.sampleDescription" help="i18n"/>
                                </div>
                            </div>
                        </div>

                        <div class="my-3 p-3" id="s-quality" data-toc="${t('eml.qualityControl')}">
                            <@textinline name="eml.qualityControl" help="i18n"/>
                            <p class="party-intro"><@s.text name="manage.metadata.methods.qualityControl.intro"/></p>
                            <#-- the heading names the field; the label stays for screen readers -->
                            <label class="visually-hidden" for="eml.qualityControl"><@s.text name="eml.qualityControl"/></label>
                            <textarea class="form-control" rows="4" id="eml.qualityControl" name="eml.qualityControl">${eml.qualityControl!}</textarea>
                            <@s.fielderror id="field-error-eml.qualityControl" cssClass="invalid-feedback list-unstyled field-error my-1" fieldName="eml.qualityControl"/>
                        </div>

                        <!-- internal parameter -->
                        <input name="r" type="hidden" value="${resource.shortname}" />
                    </div>
                </main>
            </div>
        </div>

        <#include "eml_savebar.ftl"/>
    </form>

    <#-- texts for the script -->
    <div id="ms-i18n" hidden
         data-step="${t('manage.metadata.methods.step')}"
         data-removed="${t('eml.party.removed')}"
         data-moved="${t('eml.party.moved')}"></div>

    <#include "/WEB-INF/pages/manage/eml/unsaved_changes_modal.ftl">

    <div class="party-toast" id="party-toast" role="status" aria-live="polite"></div>

    <#include "/WEB-INF/pages/inc/footer.ftl">

    <script>
        (function () {
            const form = document.getElementById('methods-form');
            const list = document.getElementById('ms-steps');
            const none = document.getElementById('ms-none');
            const text = document.getElementById('ms-i18n').dataset;

            function steps() {
                return Array.from(list.querySelectorAll(':scope > [data-step]'));
            }

            function changed() {
                // tells the unsaved changes warning (footer.ftl) that something changed
                form.dispatchEvent(new Event('input', {bubbles: true}));
            }

            function toast(message) {
                const el = document.getElementById('party-toast');
                el.textContent = message;
                el.classList.add('show');
                clearTimeout(toast.timer);
                toast.timer = setTimeout(() => el.classList.remove('show'), 2500);
            }

            // numbers, names and the up/down state follow the order on the page
            function reindex() {
                const all = steps();
                all.forEach((step, i) => {
                    const area = step.querySelector('textarea');
                    const name = 'eml.methodSteps[' + i + ']';
                    area.id = name;
                    area.name = name;
                    const title = step.querySelector('[data-step-title]');
                    title.htmlFor = name;
                    title.textContent = text.step.replace('{0}', i + 1);
                    step.querySelector('[data-act=up]').disabled = i === 0;
                    step.querySelector('[data-act=down]').disabled = i === all.length - 1;
                    // a field error belongs to the place it was posted for, so it goes when the step moves
                    const error = step.querySelector('.field-error');
                    if (error && error.id !== 'field-error-' + name) {
                        error.remove();
                        step.classList.remove('invalid');
                        area.classList.remove('is-invalid');
                    }
                });
                none.hidden = all.length > 0;
            }

            document.getElementById('ms-add').addEventListener('click', e => {
                e.preventDefault();
                const card = document.getElementById('ms-step-template').content.firstElementChild.cloneNode(true);
                list.appendChild(card);
                reindex();
                card.querySelector('textarea').focus();
                changed();
            });

            list.addEventListener('click', e => {
                const button = e.target.closest('button[data-act]');
                if (!button) {
                    return;
                }
                const step = button.closest('[data-step]');
                const act = button.dataset.act;
                if (act === 'remove') {
                    const number = steps().indexOf(step) + 1;
                    step.remove();
                    reindex();
                    toast(text.removed + ' ' + text.step.replace('{0}', number));
                } else if (act === 'up' && step.previousElementSibling) {
                    list.insertBefore(step, step.previousElementSibling);
                    reindex();
                    button.disabled ? step.querySelector('[data-act=down]').focus() : button.focus();
                    toast(text.moved + ' ' + (steps().indexOf(step) + 1));
                } else if (act === 'down' && step.nextElementSibling) {
                    list.insertBefore(step.nextElementSibling, step);
                    reindex();
                    button.disabled ? step.querySelector('[data-act=up]').focus() : button.focus();
                    toast(text.moved + ' ' + (steps().indexOf(step) + 1));
                } else {
                    return;
                }
                changed();
            });

            // empty steps are left out, so a blank card never blocks saving
            form.addEventListener('submit', () => {
                steps().forEach(step => {
                    if (!step.querySelector('textarea').value.trim()) {
                        step.remove();
                    }
                });
                reindex();
            });

            if (typeof sortable === 'function') {
                const instance = sortable(list, {
                    forcePlaceholderSize: true,
                    placeholderClass: 'border',
                    handle: '.party-grip',
                    items: '[data-step]'
                });
                instance[0].addEventListener('sortupdate', () => {
                    reindex();
                    changed();
                });
            }

            reindex();
        })();
    </script>
