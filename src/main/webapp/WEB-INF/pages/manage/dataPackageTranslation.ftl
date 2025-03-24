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
                buttonType: "danger"
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
                $("#item-" + index + " .translatedValue").attr("name", "tmap['" + index + "']");
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

            $("#addNewTranslation").select2({
                placeholder: '${action.getText("manage.translation.select")?js_string}',
                dropdownParent: $('#dialog'),
                width: "100%",
                allowClear: true,
                minimumResultsForSearch: 15,
                dropdownCssClass: 'text-smaller',
                theme: 'bootstrap4'
            });

            var fieldType = "${field.type!}";
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

            <#if (field.constraints.pattern)??>
                // escape slashes to prevent issues with patterns
                patternConstraint = "${field.constraints.pattern?replace('\\', '\\\\')}";
            </#if>

            <#if (field.constraints.minimum)??>
                minConstraint = "${field.constraints.minimum}";
            </#if>

            <#if (field.constraints.maximum)??>
                maxConstraint = "${field.constraints.maximum}";
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

            var isDeleteOperation = false;

            // intercept input submit - and store whether it's a delete operation
            $('.dropdown-menu input[type=submit]').click(function () {
                isDeleteOperation = ($(this)[0].id === "delete");
            });

            // validation
            $("#translation-form").submit(function (e) {
                // Ignore delete
                if (isDeleteOperation) {
                    isDeleteOperation = false;
                    return;
                }

                e.preventDefault();
                var isTypeValid = true;
                var isConstrainsValid = true;

                var translatedValues = $('#translation .translatedValue');

                translatedValues.each(function(index, element) {
                    if (fieldType === 'number') {
                        isTypeValid = validateNumberField(element);
                    } else if (fieldType === 'integer') {
                        isTypeValid = validateIntegerField(element);
                    } else if (fieldType === 'boolean') {
                        isTypeValid = validateBooleanField(element);
                    }

                    if (isRequiredConstraint) {
                        isConstrainsValid = validateRequiredConstraint(element);
                    } else if (patternConstraint) {
                        isConstrainsValid = validatePatternConstraint(element);
                    } else if (minConstraint) {
                        isConstrainsValid = validateMinimumConstraint(element);
                    } else if (maxConstraint) {
                        isConstrainsValid = validateMaximumConstraint(element);
                    }
                });

                if (isTypeValid && isConstrainsValid) {
                    $('#translation-form')[0].submit();
                }
            });

            function validateRequiredConstraint(element) {
                var isValid = true;
                var value = $(element).val();

                if (value === null || value === undefined || value === '') {
                    isValid = false;
                    addInputError(element, requiredConstraintViolatedMessage);
                }

                return isValid;
            }

            function validateNumberField(element) {
                var isValidNumber = true;
                var value = $(element).val();

                if (!isNumberType(value)) {
                    isValidNumber = false;
                    addInputError(element, numberExpectedMessage);
                } else {
                    removeInputError(element);
                }

                return isValidNumber;
            }

            function validateIntegerField(element) {
                var isValidNumber = true;
                var value = $(element).val();

                if (!isIntegerType(value)) {
                    isValidNumber = false;
                    addInputError(element, integerExpectedMessage);
                }

                return isValidNumber;
            }

            function validateBooleanField(element) {
                var isValidBoolean = true;
                var value = $(element).val();

                if (!isBooleanType(value)) {
                    isValidBoolean = false;
                    addInputError(element, booleanExpectedMessage);
                }

                return isValidBoolean;
            }

            function validatePatternConstraint(element) {
                var isValid = true;
                var value = $(element).val();

                var patternConstraintRegex;

                try {
                    patternConstraintRegex = new RegExp(patternConstraint);
                    isValid = patternConstraintRegex.test(value);
                } catch (e) {
                    console.log("Invalid regex!")
                }

                if (!isValid) {
                    addInputError(element, patternConstraintViolatedMessage);
                }

                return isValid;
            }

            function validateMaximumConstraint(element) {
                var isValid = true;
                var value = $(element).val();

                if (value > maxConstraint) {
                    isValid = false;
                    addInputError(element, maximumConstraintViolatedMessage);
                }

                return isValid;
            }

            function validateMinimumConstraint(element) {
                var isValid = true;
                var value = $(element).val();

                if (value < minConstraint) {
                    isValid = false;
                    addInputError(element, minimumConstraintViolatedMessage);
                }

                return isValid;
            }

            function addInputError(element, message) {
                var errorWrongTypeUl = $('<ul class="invalid-feedback list-unstyled field-error my-1">');
                errorWrongTypeUl.append('<li>')
                errorWrongTypeUl.append('<span>' + message + '</span>')

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
        ${description?replace("`(.*?)`", "<code>$1</code>", "r")?replace("\\[(.*)\\]\\((.*)\\)", "<a href='$2'>$1</a>", "r")}
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
                                    <@s.submit form="translation-form" cssClass="confirm btn btn-sm btn-outline-gbif-danger w-100 dropdown-button" name="delete" key="button.delete"/>
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
                                    <input type="text" class="value form-control" value="${sourceValuesMap.get(k)!}"
                                           disabled/>
                                </div>

                                <#if (vocabTermsSize>0)>
                                    <div class="col-6">
                                        <select name="tmapP['${k}']" class="form-select">
                                            <option value="" disabled
                                                    selected><@s.text name="manage.translation.vocabulary"/></option>
                                            <#list vocabTermsKeys as code>
                                                <option value="${code?replace('"','\"')}"
                                                        <#if tmap.get(k) == code>selected</#if> >
                                                    ${vocabTerms[code]}
                                                </option>
                                            </#list>
                                        </select>
                                    </div>
                                <#else>
                                    <div class="col-6">
                                        <input type="text"
                                               placeholder="<@s.text name='manage.translation.empty.value'/>"
                                               class="translatedValue form-control" name="tmap['${k}']"
                                               value="${tmap.get(k)!}"/>
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
                <select name="tmap" class="translatedValue form-select">
                    <option value="" disabled selected><@s.text name="manage.translation.vocabulary"/></option>
                    <#list vocabTermsKeys as code>
                        <option value="${code?replace('"','\"')}">
                            ${vocabTerms[code]}
                        </option>
                    </#list>
                </select>
            </div>
        <#else>
            <div class="col-6">
                <input type="text" placeholder="<@s.text name='manage.translation.empty.value'/>"
                       class="translatedValue form-control" name="tmap" value=""/>
            </div>
        </#if>
    </div>

    <div id="dialog" class="modal fade" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <h5 class="modal-title w-100"
                        id="staticBackdropLabel"><@s.text name="manage.translation.title"/></h5>
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
                    <button id="add-button" type="button"
                            class="btn btn-outline-gbif-primary"><@s.text name="button.add"/></button>
                </div>
            </div>
        </div>
    </div>

    <#include "/WEB-INF/pages/inc/footer.ftl">
