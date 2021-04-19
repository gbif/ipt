<#escape x as x?html>
    <#setting number_format="#####.##">
    <#include "/WEB-INF/pages/inc/header.ftl">
    <#include "/WEB-INF/pages/macros/metadata.ftl"/>
    <script type="text/javascript">
        $(document).ready(function(){
            initHelp();

            $('#plus').click(function () {
                var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'))
                var popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
                    return new bootstrap.Popover(popoverTriggerEl)
                })
                var popover = new bootstrap.Popover(document.querySelector('.popover-dismiss'), {
                    trigger: 'focus'
                })
            });
        });
    </script>
    <title><@s.text name='manage.metadata.taxcoverage.title'/></title>
    <#assign auxTopNavbar=true />
    <#assign auxTopNavbarPage = "metadata" />
    <#assign currentMenu="manage"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

<main class="container">
    <div class="row g-3">
        <div class="p-3 bg-body rounded shadow-sm">

            <#include "/WEB-INF/pages/inc/action_alerts.ftl">

            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header text-center">
                <@s.text name='manage.metadata.taxcoverage.title'/>:
                <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
            </h5>

            <p class="mx-md-4 mx-2 mb-0">
                <@s.text name='manage.metadata.taxcoverage.intro'/>
            </p>

            <div id="items" class="mx-md-4 mx-2">
                <!-- Adding the taxonomic coverages that already exists on the file -->
                <#assign next_agent_index=0 />
                <#list eml.taxonomicCoverages as item>
                    <div id='item-${item_index}' class="item border-bottom">
                        <div class="d-flex justify-content-end">
                            <a id="removeLink-${item_index}" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.taxcoverage.item'/> ]</a>
                        </div>

                        <@text  i18nkey="eml.taxonomicCoverages.description" help="i18n" name="eml.taxonomicCoverages[${item_index}].description" />

                        <!-- Taxon list-->
                        <div class="my-2">
                            <@link name="taxonsLink-${item_index}" class="show-taxonList mt-1" value="manage.metadata.taxcoverage.addSeveralTaxa" help="i18n" i18nkey="manage.metadata.taxcoverage.addSeveralTaxa"/>
                        </div>

                        <div id="list-${item_index}" class="half addSeveralTaxa mt-2" style="display:none">
                            <@text i18nkey="eml.taxonomicCoverages.taxonList" help="i18n" name="taxon-list-${item_index}" value="" />
                            <div id="addSeveralTaxaButtons" class="buttons mt-2">
                                <@s.submit cssClass="button btn btn-outline-gbif-primary" name="add-button-${item_index}" key="button.add"/>
                            </div>
                        </div>
                        <div id="subItems" class="mt-2">
                            <#list item.taxonKeywords as subItem>
                                <div id="subItem-${subItem_index}" class="sub-item row g-3 pt-3" >
                                    <div class="col-lg-4 d-flex align-items-end">
                                        <div class="w-100">
                                            <@input i18nkey="eml.taxonomicCoverages.taxonKeyword.scientificName" name="eml.taxonomicCoverages[${item_index}].taxonKeywords[${subItem_index}].scientificName" requiredField=true />
                                        </div>
                                    </div>

                                    <div class="col-lg-3 d-flex align-items-end">
                                        <div class="w-100">
                                            <@input i18nkey="eml.taxonomicCoverages.taxonKeyword.commonName" name="eml.taxonomicCoverages[${item_index}].taxonKeywords[${subItem_index}].commonName" />
                                        </div>
                                    </div>

                                    <div class="col-lg-5 d-flex align-items-end">
                                        <div class="w-100 me-2">
                                            <@select i18nkey="eml.taxonomicCoverages.taxonKeyword.rank"  name="eml.taxonomicCoverages[${item_index}].taxonKeywords[${subItem_index}].rank" options=ranks value="${eml.taxonomicCoverages[item_index].taxonKeywords[subItem_index].rank!?lower_case}"/>
                                        </div>

                                        <div>
                                            <#if (item.taxonKeywords ? size == 1) >
                                                <button id="trash-${item_index}-${subItem_index}" class="btn btn-outline-danger" style="display: none;" role="button"><@s.text name='button.delete'/></button>
                                            <#else>
                                                <button id="trash-${item_index}-${subItem_index}" class="btn btn-outline-danger" role="button"><@s.text name='button.delete'/></button>
                                            </#if>
                                        </div>
                                    </div>


                                </div>
                            </#list>
                        </div>
                        <div class="pb-1 mt-3">
                            <a id="plus-subItem-${item_index}" href="" >
                                <@s.text name='manage.metadata.addnew' /> <@s.text name='manage.metadata.taxcoverage.taxon.item' />
                            </a>
                        </div>
                    </div>
                </#list>
            </div>

            <div class="addNew mx-md-4 mx-2 mt-2">
                <a id="plus" class="plus" href="">
                    <@s.text name='manage.metadata.addnew' /> <@s.text name='manage.metadata.taxcoverage.item' />
                </a>
            </div>

            <div class="row mx-md-3 mx-1 mt-3">
                <div class="col-12">
                    <@s.submit cssClass="button btn btn-outline-gbif-primary" name="save" key="button.save"/>
                    <@s.submit cssClass="button btn btn-outline-secondary" name="cancel" key="button.cancel"/>
                </div>
            </div>

            <!-- internal parameter -->
            <input name="r" type="hidden" value="${resource.shortname}" />

            <!-- The base form that is going to be cloned every time an user click on the 'add' link -->
            <!-- The next divs are hidden. -->
            <div id="baseItem" class="item clearfix" style="display:none">
                <div class="d-flex justify-content-end mt-2">
                    <a id="removeLink" class="removeLink" href="">
                        [ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.taxcoverage.item'/> ]
                    </a>
                </div>

                <@text i18nkey="eml.taxonomicCoverages.description" help="i18n" name="description" />

                <!-- Taxon list-->
                <div class="addNew mt-1">
                    <a id="taxonsLink" class="show-taxonList" href="" >
                        <@s.text name='manage.metadata.taxcoverage.addSeveralTaxa' />
                    </a>
                </div>
                <div id="list" class="mt-2" style="display:none">
                    <@text i18nkey="eml.taxonomicCoverages.taxonList" help="i18n" name="taxon-list" value="" />
                    <div class="buttons taxon-list my-2">
                        <@s.submit cssClass="button btn btn-outline-gbif-primary" name="add-button" key="button.add"/>
                    </div>
                </div>
                <div id="subItems" class="my-2"></div>
                <div class="addNew border-bottom pb-1 mt-1">
                    <a id="plus-subItem" href="" >
                        <@s.text name='manage.metadata.addnew' /> <@s.text name='manage.metadata.taxcoverage.taxon.item' />
                    </a>
                </div>
            </div>

            <div id="subItem-9999" class="sub-item row g-3 pt-3" style="display:none">
                <div class="col-lg-4 d-flex align-items-end">
                    <div class="w-100">
                        <@input i18nkey="eml.taxonomicCoverages.taxonKeyword.scientificName" name="scientificName" requiredField=true />
                    </div>
                </div>

                <div class="col-lg-3 d-flex align-items-end">
                    <div class="w-100">
                        <@input i18nkey="eml.taxonomicCoverages.taxonKeyword.commonName" name="commonName" />
                    </div>
                </div>

                <div class="col-lg-5 d-flex align-items-end">
                    <div class="w-100 me-2">
                        <@select i18nkey="eml.taxonomicCoverages.taxonKeyword.rank"  name="rank" options=ranks />
                    </div>

                    <div>
                        <button id="trash" class="btn btn-outline-danger" role="button"><@s.text name='button.delete'/></button>
                    </div>
                </div>
            </div>

        </div>
    </div>
</main>
    </form>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
