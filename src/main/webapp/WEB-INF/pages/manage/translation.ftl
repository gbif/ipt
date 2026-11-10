<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.TranslationAction" -->
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="manage.translation.title"/></title>
<script src="${baseURL}/js/jconfirmation.jquery.js"></script>
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
<script src="${baseURL}/js/select2/select2-4.0.13.full.min.js"></script>

<script>
    $(document).ready(function(){
        $('.confirm').jConfirmAction({
            titleQuestion: "<@s.text name="basic.confirm"/>",
            yesAnswer: "<@s.text name="basic.yes"/>",
            cancelAnswer: "<@s.text name="basic.no"/>",
            buttonType: "danger",
            baseUrl: "${baseURL}"
        });
        $("table input").focus(function () {
            $(this).parent().parent().addClass("highlight");
        });
        $("table input").blur(function() {
            $(this).parent().parent().removeClass("highlight")
        });
        //Hack needed for Internet Explorer X.*x
        $('.reload').each(function() {
            $(this).click(function() {
                window.location = $(this).parent('a').attr('href');
            });
        });
        $('.automap').each(function() {
            $(this).click(function() {
                window.location = $(this).parent('a').attr('href');
            });
        });
        $('.cancel').each(function() {
            $(this).click(function() {
                window.location = $(this).parent('a').attr('href');
            });
        });
        // end hack

        // keys for typed source values, which have none yet
        var newKeyIndex = 0;

        $('#plus').click(function (e) {
            e.preventDefault();
            $("#dialog").modal('show');
        });

        $("#add-button").on("click", function () {
            addNewTranslation();
        });

        function addNewTranslation() {
            var select = $('#addNewTranslation');
            // get selected option from dropdown
            var selected = select.find(":selected");
            var value = selected.val();

            if (value) {
                if (!isTranslated(value)) {
                    var key = selected.attr("data-key") || ("n" + (++newKeyIndex));
                    addNewItem(value, key);
                }
                // remove this option from dropdown - translation already added
                selected.remove();
                select.val(null).trigger('change');
            }
        }

        function isTranslated(value) {
            return $('#translation .source-value').filter(function () {
                return $(this).val() === value;
            }).length > 0;
        }

        function addNewItem(value, key) {
            // clone template and temporarily hide it
            var newItem = $('#baseItem').clone();
            newItem.hide();

            newItem.attr("id", "item-" + key);
            newItem.find(".value").val(value);
            newItem.find(".source-value").attr("name", "smap['" + key + "']").val(value);
            newItem.find(".translated-value").attr("name", "tmap['" + key + "']");

            // add it to translation div and show it
            $("#translation").append(newItem);
            newItem.slideDown('slow');
        }

        $("#translation").on("click", ".removeLink", function (e) {
            e.preventDefault();

            var item = $(this).closest(".item");
            var key = item.attr("id").substring("item-".length);

            item.slideUp('slow', function () {
                // return the source value back to select options
                var value = item.find(".source-value").val();
                var o = new Option(value, value);
                $(o).attr("data-key", key);
                $("#addNewTranslation").append(o);

                item.remove();
            });
        });

        $("#addNewTranslation").select2({
            placeholder: '${action.getText("manage.translation.select")?js_string?no_esc}',
            dropdownParent: $('#dialog'),
            language: {
                noResults: function () {
                    return '${selectNoResultsFound}';
                }
            },
            width: "100%",
            allowClear: true,
            // allow typing values not listed, e.g. when the source has too many distinct values to list them all
            tags: true,
            dropdownCssClass: 'text-smaller',
            theme: 'bootstrap4'
        });

        // a row without a vocabulary value wouldn't be saved; not checked when deleting
        $("#translation-form").submit(function (e) {
            var submitter = e.originalEvent ? e.originalEvent.submitter : null;
            if (submitter && submitter.name === "deleteFlag") {
                return;
            }

            var selects = $('#translation select.translated-value');
            selects.removeClass("is-invalid").next('.invalid-feedback').remove();
            var unselected = selects.filter(function () {
                return $(this).val() === null;
            });
            if (unselected.length) {
                unselected.addClass("is-invalid").after(function () {
                    return $('<div class="invalid-feedback">')
                        .text('${action.getText("manage.translation.vocabulary.select.required")?js_string?no_esc}');
                });
                e.preventDefault();
            }
        });
    });
</script>
<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <div class="container-fluid border-bottom">
        <div class="container bg-body border rounded-2 mb-4">
            <div class="container my-3 p-3">
                <div class="text-center fs-smaller">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb justify-content-center mb-0">
                            <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                            <li class="breadcrumb-item"><a href="resource?r=${resource.shortname}"><@s.text name="breadcrumb.manage.overview"/></a></li>
                            <li class="breadcrumb-item"><a href="mapping.do?r=${resource.shortname}&id=${property.extension.rowType?url}&mid=${mid}"><@s.text name="breadcrumb.manage.overview.mapping"/></a></li>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.mapping.translation"/></li>
                        </ol>
                    </nav>
                </div>

                <div class="text-center">
                    <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                        <@s.text name="manage.translation.title"/>
                    </h1>
                </div>

                <div class="text-center fs-smaller">
                    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                </div>

                <div class="mt-2 text-center">
                    <div>
                        <@s.submit form="translation-form" cssClass="button btn btn-sm btn-outline-gbif-primary top-button mt-1" name="save" key="button.save"/>

                        <div class="btn-group btn-group-sm" role="group">
                            <button id="btnGroup" type="button" class="btn btn-sm btn-outline-gbif-primary dropdown-toggle align-self-start top-button" data-bs-toggle="dropdown" aria-expanded="false">
                                <@s.text name="button.options"/>
                            </button>
                            <ul class="dropdown-menu" aria-labelledby="btnGroup" style="">
                                <li>
                                    <a class="button btn btn-sm btn-outline-secondary w-100 dropdown-button" href='translationReload.do?r=${resource.shortname}&mapping=${property.extension.rowType?url}&term=${property.qualname?url}&mid=${mid}&rowtype=${property.extension.rowType?url}'>
                                        <@s.text name="button.reload"/>
                                    </a>
                                </li>
                                <#if property.vocabulary?has_content>
                                    <li>
                                        <a class="button btn btn-sm btn-outline-secondary w-100 dropdown-button" role="button" href='translationAutomap.do?r=${resource.shortname}&mapping=${property.extension.rowType?url}&rowtype=${property.extension.rowType?url}&term=${property.qualname?url}&mid=${mid}'>
                                            <@s.text name="button.automap"/>
                                        </a>
                                    </li>
                                </#if>
                                <li>
                                    <@s.submit form="translation-form" cssClass="confirm btn btn-sm btn-outline-gbif-danger w-100 dropdown-button" name="deleteFlag" key="button.delete"/>
                                </li>
                            </ul>
                        </div>

                        <a class="button btn btn-sm btn-outline-secondary top-button mt-1" role="button" href="mapping.do?r=${resource.shortname}&id=${property.extension.rowType?url}&mid=${mid}">
                            <@s.text name='button.cancel'/>
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <main class="container main-content-container">
        <div class="my-3 p-3">
            <p><@s.text name="manage.translation.intro"/></p>
        </div>

        <div class="my-3 p-3">
            <h5 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                <@s.text name="manage.translation.property"/> <em>${property.name}</em>
            </h5>

            <span class="fst-italic">${property.description!}</span>

            <#if property.vocabulary?has_content>
                <div class="callout callout-info text-smaller">
                    <strong><@s.text name="manage.translation.vocabulary.required"/></strong>
                </div>
                <p>
                    <a href="vocabulary.do?id=${property.vocabulary.uriString}" class="no-text-decoration" target="_blank">
                        <i class="bi bi-book"></i>
                        <span>${property.vocabulary.title!property.vocabulary.uriString}</span>
                    </a>
                </p>
                <p>
                    <span class="fst-italic">${property.vocabulary.description!}</span>
                </p>
            </#if>
        </div>

        <div class="my-3 p-3">
            <form id="translation-form" class="translation-form" action="translation.do" method="post">
                <input type="hidden" name="r" value="${resource.shortname}"/>
                <input type="hidden" name="rowtype" value="${property.extension.rowType}"/>
                <input type="hidden" name="mid" value="${mid}"/>
                <input type="hidden" name="term" value="${property.qualname}"/>

                <div id="translation">
                    <div class="row g-2 border-bottom pb-2">
                        <div class="col-6">
                            <strong><@s.text name="manage.translation.source.value"/></strong>
                        </div>

                        <div class="col-6" >
                            <strong><@s.text name="manage.translation.translated.value"/></strong>
                        </div>
                    </div>

                    <#list sourceValuesMap as k, val>
                        <#if (tmap.get(k))??>
                            <#assign translated = tmap.get(k)/>
                            <div id="item-${k}" class="item row g-2 border-bottom pb-2">
                                <div class="d-flex justify-content-end mt-3">
                                    <a class="removeLink text-smaller" href="">
                                            <span>
                                                <svg viewBox="0 0 24 24" class="link-icon">
                                                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                </svg>
                                            </span>
                                        <span><@s.text name="manage.translation.remove"/></span>
                                    </a>
                                </div>

                                <div class="col-6">
                                    <input type="hidden" class="source-value" name="smap['${k}']" value="${val}"/>
                                    <input type="text" class="value form-control" value="${val}" disabled/>
                                    <#if action.isNotInSource(k)>
                                        <div class="form-text"><@s.text name="manage.translation.notInSource"/></div>
                                    </#if>
                                </div>

                                <#if (vocabTermsSize>0)>
                                    <div class="col-6">
                                        <select name="tmap['${k}']" class="translated-value form-select">
                                            <option value="" disabled selected><@s.text name="manage.translation.vocabulary"/></option>
                                            <#-- keep a saved value that isn't in the vocabulary, so saving doesn't drop it -->
                                            <#if !(vocabTerms[translated])??>
                                                <option value="${translated}" selected>${translated}</option>
                                            </#if>
                                            <#list vocabTermsKeys as code>
                                                <option value="${code}" <#if translated == code>selected</#if> >
                                                    ${vocabTerms[code]}
                                                </option>
                                            </#list>
                                        </select>
                                    </div>
                                <#else>
                                    <div class="col-6">
                                        <input type="text" placeholder="<@s.text name='manage.translation.empty.value'/>" class="translated-value form-control" name="tmap['${k}']" value="${translated}"/>
                                    </div>
                                </#if>
                            </div>
                        </#if>
                    </#list>
                </div>
            </form>

            <div class="row g-2 mt-0 text-smaller">
                <div class="col-12">
                    <div class="d-flex">
                        <a id="plus" href="">
                            <span>
                                <svg viewBox="0 0 24 24" class="link-icon">
                                    <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                </svg>
                            </span>
                            <span><@s.text name="manage.translation.addNew"/></span>
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </main>

<div id="baseItem" class="item row g-2 border-bottom pb-2" style="display:none">
    <div class="d-flex justify-content-end mt-3">
        <a class="removeLink text-smaller" href="">
            <span>
                <svg viewBox="0 0 24 24" class="link-icon">
                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                </svg>
            </span>
            <span><@s.text name="manage.translation.remove"/></span>
        </a>
    </div>
    <div class="col-6">
        <input type="hidden" class="source-value" value=""/>
        <input type="text" class="value form-control" value="" disabled/>
    </div>

    <#if (vocabTermsSize>0)>
        <div class="col-6">
            <select class="translated-value form-select">
                <option value="" disabled selected><@s.text name="manage.translation.vocabulary"/></option>
                <#list vocabTermsKeys as code>
                    <option value="${code}">
                        ${vocabTerms[code]}
                    </option>
                </#list>
            </select>
        </div>
    <#else>
        <div class="col-6" >
            <input type="text" placeholder="<@s.text name='manage.translation.empty.value'/>" class="translated-value form-control" value=""/>
        </div>
    </#if>
</div>

<div id="dialog" class="modal fade" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
    <div class="modal-dialog modal-confirm modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header flex-column">
                <img src="${baseURL}/images/logo-modal-success.png" alt="Success" class="modal-image" />
            </div>
            <div class="modal-body">
                <h5 class="modal-title w-100" id="staticBackdropLabel"><@s.text name="manage.translation.title"/></h5>
                <div>
                    <select name="addNewTranslation" id="addNewTranslation" class="form-select">
                        <option value="" disabled selected><@s.text name="manage.translation.select"/></option>
                        <#list sourceValuesMap as key, val>
                            <#if !(tmap.get(key))??>
                                <option data-key="${key}" value="${val}">${val}</option>
                            </#if>
                        </#list>
                    </select>
                </div>
            </div>
            <div class="modal-footer justify-content-center">
                <button id="add-button" type="button" class="btn btn-sm btn-outline-gbif-primary"><@s.text name="button.add"/></button>
                <button id="cancel-button" type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal"><@s.text name="button.cancel"/></button>
            </div>
        </div>
    </div>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
