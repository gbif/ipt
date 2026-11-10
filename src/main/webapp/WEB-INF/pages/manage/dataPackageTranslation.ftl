<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.DataPackageFieldTranslationAction" -->
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name="manage.translation.title"/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
    <script src="${baseURL}/js/select2/select2-4.0.13.full.min.js"></script>

    <script>
        $(document).ready(function () {
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
            $("table input").blur(function () {
                $(this).parent().parent().removeClass("highlight")
            });
            //Hack needed for Internet Explorer X.*x
            $('.reload').each(function () {
                $(this).click(function () {
                    window.location = $(this).parent('a').attr('href');
                });
            });
            $('.automap').each(function () {
                $(this).click(function () {
                    window.location = $(this).parent('a').attr('href');
                });
            });
            $('.cancel').each(function () {
                $(this).click(function () {
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
                width: "100%",
                allowClear: true,
                // allow typing values not listed, e.g. when the source has too many distinct values to list them all
                tags: true,
                dropdownCssClass: 'text-smaller',
                theme: 'bootstrap4'
            });

            var fieldType = "${(field.type!)?js_string}";
            var isRequiredConstraint = false;
            var minConstraint;
            var maxConstraint;
            var patternConstraint;

            var numberExpectedMessage = "<@s.text name='validation.number.expected'/>";
            var integerExpectedMessage = "<@s.text name='validation.integer.expected'/>";
            var booleanExpectedMessage = "<@s.text name='validation.boolean.expected'/>";
            var requiredConstraintViolatedMessage = "<@s.text name='validation.required.violated'/>";
            var patternConstraintViolatedMessage = "<@s.text name='validation.pattern.violated'/>";
            var minimumConstraintViolatedMessage = "<@s.text name='validation.minimum.violated'/>";
            var maximumConstraintViolatedMessage = "<@s.text name='validation.maximum.violated'/>";
            var vocabularyValueExpectedMessage = "<@s.text name='manage.translation.vocabulary.select.required'/>";

            <#if (field.constraints.pattern)??>
                patternConstraint = "${field.constraints.pattern?js_string?no_esc}";
            </#if>

            <#if (field.constraints.minimum)??>
                minConstraint = ${field.constraints.minimum?c};
            </#if>

            <#if (field.constraints.maximum)??>
                maxConstraint = ${field.constraints.maximum?c};
            </#if>

            <#if (field.constraints.required)?? && field.constraints.required == true>
                isRequiredConstraint = true;
            </#if>

            function isNumberType(value) {
                return !isNaN(parseFloat(value)) && isFinite(value);
            }

            function isIntegerType(value) {
                return /^-?\d+$/.test(value);
            }

            function isBooleanType(value) {
                return value === "true" || value === "false";
            }

            function matchesPattern(value) {
                try {
                    // a Table Schema pattern must match the whole value
                    return new RegExp("^(?:" + patternConstraint + ")$").test(value);
                } catch (e) {
                    console.log("Invalid regex!");
                    return true;
                }
            }

            // validation, skipped when deleting
            $("#translation-form").submit(function (e) {
                var submitter = e.originalEvent ? e.originalEvent.submitter : null;
                if (submitter && submitter.name === "deleteFlag") {
                    return;
                }

                var isValid = true;
                $('#translation .translated-value').each(function (index, element) {
                    var message = validateValue(element);
                    if (message) {
                        addInputError(element, message);
                        isValid = false;
                    } else {
                        removeInputError(element);
                    }
                });

                if (!isValid) {
                    e.preventDefault();
                }
            });

            /**
             * @return error message if the translated value is invalid, null otherwise
             */
            function validateValue(element) {
                var value = $(element).val();

                // vocabulary value not selected
                if (value === null || value === undefined) {
                    return vocabularyValueExpectedMessage;
                }

                // an empty value leaves the value empty
                if (value === '') {
                    return isRequiredConstraint ? requiredConstraintViolatedMessage : null;
                }

                if (fieldType === 'number' && !isNumberType(value)) {
                    return numberExpectedMessage;
                }
                if (fieldType === 'integer' && !isIntegerType(value)) {
                    return integerExpectedMessage;
                }
                if (fieldType === 'boolean' && !isBooleanType(value)) {
                    return booleanExpectedMessage;
                }
                if (patternConstraint !== undefined && !matchesPattern(value)) {
                    return patternConstraintViolatedMessage;
                }
                if (fieldType === 'number' || fieldType === 'integer') {
                    var number = parseFloat(value);
                    if (minConstraint !== undefined && number < minConstraint) {
                        return minimumConstraintViolatedMessage;
                    }
                    if (maxConstraint !== undefined && number > maxConstraint) {
                        return maximumConstraintViolatedMessage;
                    }
                }

                return null;
            }

            function addInputError(element, message) {
                var errorWrongTypeUl = $('<ul class="invalid-feedback list-unstyled field-error my-1">');
                errorWrongTypeUl.append('<li>')
                errorWrongTypeUl.append($('<span>').text(message))

                $(element).next('.invalid-feedback').remove();
                $(element).addClass("is-invalid");
                $(element).after(errorWrongTypeUl);
            }

            function removeInputError(element) {
                $(element).next('.invalid-feedback').remove();
                $(element).removeClass("is-invalid");
            }
        });
    </script>
    <#assign currentMenu = "manage"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <#macro processDescription description>
        ${description?replace("`(.*?)`", "<code>$1</code>", "r")?replace("\\[(.*)\\]\\((.*)\\)", "<a href='$2'>$1</a>", "r")?no_esc}
    </#macro>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <div class="container-fluid border-bottom">
        <div class="container bg-body border rounded-2 mb-4">
            <div class="container my-3 p-3">
                <div class="text-center fs-smaller">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);"
                         aria-label="breadcrumb">
                        <ol class="breadcrumb justify-content-center mb-0">
                            <li class="breadcrumb-item">
                                <a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a>
                            </li>
                            <li class="breadcrumb-item">
                                <a href="resource?r=${resource.shortname}"><@s.text name="breadcrumb.manage.overview"/></a>
                            </li>
                            <li class="breadcrumb-item">
                                <a href="dataPackageMapping.do?r=${resource.shortname}&id=${(mapping.dataPackageSchema.identifier)!?url}&mid=${mid}">
                                    <@s.text name="breadcrumb.manage.overview.mapping"/>
                                </a>
                            </li>
                            <li class="breadcrumb-item active" aria-current="page">
                                <@s.text name="breadcrumb.manage.overview.mapping.translation"/>
                            </li>
                        </ol>
                    </nav>
                </div>

                <div class="text-center">
                    <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                        <@s.text name="manage.translation.title"/>
                    </h1>
                </div>

                <div class="text-center fs-smaller">
                    <a href="resource.do?r=${resource.shortname}"
                       title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
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
                                    <a class="button btn btn-sm btn-outline-secondary w-100 dropdown-button" href='dataPackageFieldTranslationReload.do?r=${resource.shortname}&field=${field.name!}&mid=${mid}'>
                                        <@s.text name="button.reload"/>
                                    </a>
                                </li>
                                <#if (field.constraints.vocabulary)?has_content>
                                    <li>
                                        <a class="button btn btn-sm btn-outline-secondary w-100 dropdown-button" role="button" href='dataPackageFieldTranslationAutomap.do?r=${resource.shortname}&field=${field.name!}&mid=${mid}'>
                                            <@s.text name="button.automap"/>
                                        </a>
                                    </li>
                                </#if>
                                <li>
                                    <@s.submit form="translation-form" cssClass="confirm btn btn-sm btn-outline-gbif-danger w-100 dropdown-button" name="deleteFlag" key="button.delete"/>
                                </li>
                            </ul>
                        </div>

                        <a class="button btn btn-sm btn-outline-secondary top-button mt-1" role="button" href="dataPackageMapping.do?r=${resource.shortname}&id=${(mapping.dataPackageSchema.identifier)!}&mid=${mid}">
                            <@s.text name='button.cancel'/>
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <main class="container main-content-container">
        <div class="mt-3 p-3">
            <p class="mb-0"><@s.text name="manage.translation.intro"/></p>
        </div>

        <div class="my-3 p-3">
            <h5 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                <@s.text name="manage.translation.property"/> <em>${field.name}</em>
            </h5>

            <#if field.description?has_content>
                <@processDescription field.description />
            <#else>
                <@s.text name="basic.no.description"/>
            </#if>
            <#if field.type??>
                <br/><br/>
                <em><@s.text name="schema.field.type"/>:</em>
                <span>${field.type!}</span>
            </#if>
            <#if field.format?? && field.format != 'default'>
                <br><br/>
                <em><@s.text name="schema.field.format"/></em>
                <code>${field.format}</code>
            </#if>
            <#if field.constraints?? && (field.constraints.unique?? || field.constraints.maximum?? || field.constraints.minimum?? || field.constraints.pattern??)>
                <br/><br/>
                <em><@s.text name="schema.field.constraints"/>:</em>
                <ul>
                    <#if field.constraints.unique??>
                        <li>unique <code>${field.constraints.unique?string}</code></li>
                    </#if>
                    <#if field.constraints.maximum??>
                        <li>maximum <code>${field.constraints.maximum}</code></li>
                    </#if>
                    <#if field.constraints.minimum??>
                        <li>minimum <code>${field.constraints.minimum}</code></li>
                    </#if>
                    <#if field.constraints.pattern??>
                        <li>pattern <code>${field.constraints.pattern}</code></li>
                    </#if>
                </ul>
                <#assign noBrakeForExamples = true />
            <#else>
                <#assign noBrakeForExamples = false />
            </#if>
            <#if (field.example)?has_content>
                <#if !noBrakeForExamples>
                    <br/><br/>
                </#if>
                <em><@s.text name="basic.examples"/></em>:
                <#if field.example?is_collection>
                    <#list field.example as ex>
                        <code>${ex}</code><#sep>, </#sep>
                    </#list>
                <#else>
                    <#if field.example?is_boolean>
                        <code>${field.example?string("true", "false")}</code>
                    <#else>
                        <code>${field.example}</code>
                    </#if>
                </#if>
            </#if>
        </div>

        <div class="my-3 p-3">
            <form id="translation-form" class="translation-form" action="dataPackageFieldTranslation.do" method="post">
                <input type="hidden" name="r" value="${resource.shortname}"/>
                <input type="hidden" name="id" value="${(mapping.dataPackageSchema.identifier)!}"/>
                <input type="hidden" name="mid" value="${mid}"/>
                <input type="hidden" name="field" value="${field.name}"/>

                <div id="translation">
                    <div class="row g-2 border-bottom pb-2">
                        <div class="col-6">
                            <strong><@s.text name="manage.translation.source.value"/></strong>
                        </div>

                        <div class="col-6">
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
                                            <option value="" disabled
                                                    selected><@s.text name="manage.translation.vocabulary"/></option>
                                            <#-- keep a saved value that isn't in the vocabulary, so saving doesn't drop it -->
                                            <#if !(vocabTerms[translated])??>
                                                <option value="${translated}" selected>${translated}</option>
                                            </#if>
                                            <#list vocabTermsKeys as code>
                                                <option value="${code}"
                                                        <#if translated == code>selected</#if> >
                                                    ${vocabTerms[code]}
                                                </option>
                                            </#list>
                                        </select>
                                    </div>
                                <#else>
                                    <div class="col-6">
                                        <input type="text"
                                               placeholder="<@s.text name='manage.translation.empty.value'/>"
                                               class="translated-value form-control" name="tmap['${k}']"
                                               value="${translated}"/>
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
            <div class="col-6">
                <input type="text" placeholder="<@s.text name='manage.translation.empty.value'/>"
                       class="translated-value form-control" value=""/>
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
