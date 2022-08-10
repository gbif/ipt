<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.TranslationAction" -->
<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="manage.translation.title"/></title>
<script src="${baseURL}/js/jconfirmation.jquery.js"></script>

<script>
    $(document).ready(function(){
        $('.confirm').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>", buttonType: "danger"});
        $("table input").focus(function() {
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

        $('#plus').click(function (e) {
            e.preventDefault();
            showAddNewTranslationModal();
        });

        function addNewTranslation() {
            // get selected option from dropdown
            var selected = $('#addNewTranslation').find(":selected");
            // get selected option's id
            var optionId = selected.attr("id");

            if (optionId) {
                // get index from id (e.g. option-k2, k2 is the index)
                var elementIndex = optionId.split("option-")[1];
                // get value from option
                var value = selected.val()

                // add new line (item)
                addNewItem(value, elementIndex);
            }
        }

        function addNewItem(value, index) {
            // clone template and temporarily hide it
            var newItem = $('#baseItem').clone();
            newItem.hide();

            // add it to translation div
            $("#translation").append(newItem)

            // show item
            newItem.slideDown('slow');

            // set values and properties
            setItemValueAndIndex(newItem, value, index);
        }

        function setItemValueAndIndex(item, value, index) {
            // set id with index
            item.attr("id", "item-" + index);
            // set id to item's remove link
            $("#item-" + index + " .removeLink").attr("id", "removeLink-" + index);
            // set value to input
            $("#item-" + index + " .value").val(value);
            // set name attribute of the translated input
            $("#item-" + index + " .translated-value").attr("name", "tmap['" + index + "']");
            // remove this option from dropdown - translation already added
            $("#option-" + index).remove();

            // initialize remove link
            $("#removeLink-" + index).click(function (e) {
                e.preventDefault();
                remove(e);
            });
        }

        $(".removeLink").click(function (e) {
            e.preventDefault();
            remove(e);
        });

        function remove(e) {
            e.preventDefault();

            // get clicked target
            var $target = $(e.target);
            // make sure it's a link
            if (!$target.is('a')) {
                $target = $(e.target).closest('a');
            }

            // get element index
            var index = $target.attr("id").split("-")[1];

            // remove element by index
            $('#item-' + index).slideUp('slow', function () {
                // find deleted value and return it back to select options
                var value = $(this).find("input.value").val();
                var o = new Option(value, value);
                o.id = "option-" + index;
                $("#addNewTranslation").append(o);

                // remove
                $(this).remove();
            });
        }

        function showAddNewTranslationModal() {
            var dialogWindow = $("#dialog");

            $("#add-button").on("click", function () {
                addNewTranslation();
            });

            dialogWindow.modal('show');
        }

    });
</script>
<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

    <div class="container-fluid bg-body border-bottom">
        <div class="container my-3">
            <#include "/WEB-INF/pages/inc/action_alerts.ftl">
        </div>

        <div class="container my-3 p-3">
            <div class="text-center text-uppercase fw-bold fs-smaller-2">
                <@s.text name="basic.resource"/>
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
                    <@s.submit form="translation" cssClass="button btn btn-sm btn-outline-gbif-primary top-button mt-1" name="save" key="button.save"/>

                    <@s.submit form="translation" cssClass="confirm btn btn-sm btn-outline-gbif-danger top-button mt-1" name="delete" key="button.delete"/>

                    <a class="button btn btn-sm btn-outline-secondary top-button mt-1" role="button" href="mapping.do?r=${resource.shortname}&id=${property.extension.rowType?url}&mid=${mid}">
                        <@s.text name='button.cancel'/>
                    </a>

                    <a class="button btn btn-sm btn-outline-secondary top-button mt-1" href='translationReload.do?r=${resource.shortname}&mapping=${property.extension.rowType?url}&term=${property.qualname?url}&mid=${mid}&rowtype=${property.extension.rowType?url}'>
                        <@s.text name="button.reload"/>
                    </a>

                    <#if property.vocabulary?has_content>
                        <a class="button btn btn-sm btn-outline-secondary top-button mt-1" role="button" href='translationAutomap.do?r=${resource.shortname}&mapping=${property.extension.rowType?url}&rowtype=${property.extension.rowType?url}&term=${property.qualname?url}&mid=${mid}'>
                            <@s.text name="button.automap"/>
                        </a>
                    </#if>
                </div>
            </div>
        </div>
    </div>

    <main class="container">
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
            <form id="translation" class="translation-form" action="translation.do" method="post">
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

                    <#list sourceValuesMap?keys as k>
                        <#if (tmap.get(k))??>
                            <div id="item-${k}" class="item row g-2 border-bottom pb-2">
                                <div class="d-flex justify-content-end mt-3">
                                    <a id="removeLink-${k}" class="removeLink text-smaller" href="">
                                            <span>
                                                <svg viewBox="0 0 24 24" class="link-icon">
                                                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                </svg>
                                            </span>
                                        <span><@s.text name="manage.translation.remove"/></span>
                                    </a>
                                </div>

                                <div class="col-6">
                                    <input type="text" class="value form-control" value="${sourceValuesMap.get(k)!}" disabled/>
                                </div>

                                <#if (vocabTermsSize>0)>
                                    <div class="col-6">
                                        <select name="tmap['${k}']" class="form-select">
                                            <option value="" disabled selected><@s.text name="manage.translation.vocabulary"/></option>
                                            <#list vocabTermsKeys as code>
                                                <option value="${code?replace('"','\"')}" <#if tmap.get(k) == code>selected</#if> >
                                                    ${vocabTerms[code]}
                                                </option>
                                            </#list>
                                        </select>
                                    </div>
                                <#else>
                                    <div class="col-6">
                                        <input type="text" placeholder="<@s.text name='manage.translation.empty.value'/>" class="translatedValue form-control" name="tmap['${k}']" value="${tmap.get(k)!}"/>
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
        <a id="removeLink" class="removeLink text-smaller" href="">
            <span>
                <svg viewBox="0 0 24 24" class="link-icon">
                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                </svg>
            </span>
            <span><@s.text name="manage.translation.remove"/></span>
        </a>
    </div>
    <div class="col-6">
        <input type="text" class="value form-control" value="" disabled/>
    </div>

    <#if (vocabTermsSize>0)>
        <div class="col-6">
            <select name="tmap" class="translated-value form-select">
                <option value="" disabled selected><@s.text name="manage.translation.vocabulary"/></option>
                <#list vocabTermsKeys as code>
                    <option value="${code?replace('"','\"')}">
                        ${vocabTerms[code]}
                    </option>
                </#list>
            </select>
        </div>
    <#else>
        <div class="col-6" >
            <input type="text" placeholder="<@s.text name='manage.translation.empty.value'/>" class="translated-value form-control" name="tmap" value=""/>
        </div>
    </#if>
</div>

<div id="dialog" class="modal fade" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
    <div class="modal-dialog modal-confirm modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header flex-column">
                <h5 class="modal-title w-100" id="staticBackdropLabel"><@s.text name="manage.translation.title"/></h5>
                <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">×</button>
            </div>
            <div class="modal-body">
                <div>
                    <select name="addNewTranslation" id="addNewTranslation" class="form-select">
                        <option value="" disabled selected><@s.text name="manage.translation.select"/></option>
                        <#list sourceValuesMap as key, val>
                            <#if tmap?? && tmap['${key}']??>
                            <#else>
                                <option id="option-${key}" value="${val}">
                                    ${val}
                                </option>
                            </#if>
                        </#list>
                    </select>
                </div>
            </div>
            <div class="modal-footer justify-content-center">
                <button id="add-button" type="button" class="btn btn-outline-gbif-primary"><@s.text name="button.add"/></button>
            </div>
        </div>
    </div>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
