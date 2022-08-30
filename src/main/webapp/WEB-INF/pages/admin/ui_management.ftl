<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <script src="${baseURL}/js/jquery/jquery-3.5.1.min.js"></script>
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
    <script src="${baseURL}/js/select2/select2-4.0.13.min.js"></script>

    <#assign cssColors = ["#61a861", "#e36370", "#4e565f", "#ffc107", "#78b578", "#71b171", "#ffffff", "#4ba2ce", "#2c79a1", "#4caf50", "#edf7ed", "#1e4620", "#ff9800", "#fff4e5", "#663c00", "#ef5350", "#fdeded", "#5f2120", "aliceblue", "antiquewhite", "aqua", "aquamarine", "azure", "beige", "bisque", "black", "blanchedalmond", "blue", "blueviolet", "brown", "burlywood", "cadetblue", "chartreuse", "chocolate", "coral", "cornflowerblue", "cornsilk", "crimson", "cyan", "darkblue", "darkcyan", "darkgoldenrod", "darkgray", "darkgrey", "darkgreen", "darkkhaki", "darkmagenta", "darkolivegreen", "darkorange", "darkorchid", "darkred", "darksalmon", "darkseagreen", "darkslateblue", "darkslategray", "darkslategrey", "darkturquoise", "darkviolet", "deeppink", "deepskyblue", "dimgray", "dimgrey", "dodgerblue", "firebrick", "floralwhite", "forestgreen", "fuchsia", "gainsboro", "ghostwhite", "gold", "goldenrod", "gray", "grey", "green", "greenyellow", "honeydew", "hotpink", "indianred", "indigo", "ivory", "khaki", "lavender", "lavenderblush", "lawngreen", "lemonchiffon", "lightblue", "lightcoral", "lightcyan", "lightgoldenrodyellow", "lightgray", "lightgrey", "lightgreen", "lightpink", "lightsalmon", "lightseagreen", "lightskyblue", "lightslategray", "lightslategrey", "lightsteelblue", "lightyellow", "lime", "limegreen", "linen", "magenta", "maroon", "mediumaquamarine", "mediumblue", "mediumorchid", "mediumpurple", "mediumseagreen", "mediumslateblue", "mediumspringgreen", "mediumturquoise", "mediumvioletred", "midnightblue", "mintcream", "mistyrose", "moccasin", "navajowhite", "navy", "oldlace", "olive", "olivedrab", "orange", "orangered", "orchid", "palegoldenrod", "palegreen", "paleturquoise", "palevioletred", "papayawhip", "peachpuff", "peru", "pink", "plum", "powderblue", "purple", "rebeccapurple", "red", "rosybrown", "royalblue", "saddlebrown", "salmon", "sandybrown", "seagreen", "seashell", "sienna", "silver", "skyblue", "slateblue", "slategray", "slategrey", "snow", "springgreen", "steelblue", "tan", "teal", "thistle", "tomato", "turquoise", "violet", "wheat", "white", "whitesmoke", "yellow", "yellowgreen", "#ffebee", "#ffcdd2", "#ef9a9a", "#e57373", "#ef5350", "#f44336", "#e53935", "#d32f2f", "#c62828", "#b71c1c", "#ff8a80", "#ff5252", "#ff1744", "#d50000", "#fce4ec", "#f8bbd0", "#f48fb1", "#f06292", "#ec407a", "#e91e63", "#d81b60", "#c2185b", "#ad1457", "#880e4f", "#ff80ab", "#ff4081", "#f50057", "#c51162", "#f3e5f5", "#e1bee7", "#ce93d8", "#ba68c8", "#ab47bc", "#9c27b0", "#8e24aa", "#7b1fa2", "#6a1b9a", "#4a148c", "#ea80fc", "#e040fb", "#d500f9", "#a0f", "#ede7f6", "#d1c4e9", "#b39ddb", "#9575cd", "#7e57c2", "#673ab7", "#5e35b1", "#512da8", "#4527a0", "#311b92", "#b388ff", "#7c4dff", "#651fff", "#6200ea", "#e8eaf6", "#c5cae9", "#9fa8da", "#7986cb", "#5c6bc0", "#3f51b5", "#3949ab", "#303f9f", "#283593", "#1a237e", "#8c9eff", "#536dfe", "#3d5afe", "#304ffe", "#e3f2fd", "#bbdefb", "#90caf9", "#64b5f6", "#42a5f5", "#2196f3", "#1e88e5", "#1976d2", "#1565c0", "#0d47a1", "#82b1ff", "#448aff", "#2979ff", "#2962ff", "#e1f5fe", "#b3e5fc", "#81d4fa", "#4fc3f7", "#29b6f6", "#03a9f4", "#039be5", "#0288d1", "#0277bd", "#01579b", "#80d8ff", "#40c4ff", "#00b0ff", "#0091ea", "#e0f7fa", "#b2ebf2", "#80deea", "#4dd0e1", "#26c6da", "#00bcd4", "#00acc1", "#0097a7", "#00838f", "#006064", "#84ffff", "#18ffff", "#00e5ff", "#00b8d4", "#e0f2f1", "#b2dfdb", "#80cbc4", "#4db6ac", "#26a69a", "#009688", "#00897b", "#00796b", "#00695c", "#004d40", "#a7ffeb", "#64ffda", "#1de9b6", "#00bfa5", "#e8f5e9", "#c8e6c9", "#a5d6a7", "#81c784", "#66bb6a", "#4caf50", "#43a047", "#388e3c", "#2e7d32", "#1b5e20", "#b9f6ca", "#69f0ae", "#00e676", "#00c853", "#f1f8e9", "#dcedc8", "#c5e1a5", "#aed581", "#9ccc65", "#8bc34a", "#7cb342", "#689f38", "#558b2f", "#33691e", "#ccff90", "#b2ff59", "#76ff03", "#64dd17", "#f9fbe7", "#f0f4c3", "#e6ee9c", "#dce775", "#d4e157", "#cddc39", "#c0ca33", "#afb42b", "#9e9d24", "#827717", "#f4ff81", "#eeff41", "#c6ff00", "#aeea00", "#fffde7", "#fff9c4", "#fff59d", "#fff176", "#ffee58", "#ffeb3b", "#fdd835", "#fbc02d", "#f9a825", "#f57f17", "#ffff8d", "#ff0", "#ffea00", "#ffd600", "#fff8e1", "#ffecb3", "#ffe082", "#ffd54f", "#ffca28", "#ffc107", "#ffb300", "#ffa000", "#ff8f00", "#ff6f00", "#ffe57f", "#ffd740", "#ffc400", "#ffab00", "#fff3e0", "#ffe0b2", "#ffcc80", "#ffb74d", "#ffa726", "#ff9800", "#fb8c00", "#f57c00", "#ef6c00", "#e65100", "#ffd180", "#ffab40", "#ff9100", "#ff6d00", "#fbe9e7", "#ffccbc", "#ffab91", "#ff8a65", "#ff7043", "#ff5722", "#f4511e", "#e64a19", "#d84315", "#bf360c", "#ff9e80", "#ff6e40", "#ff3d00", "#dd2c00", "#efebe9", "#d7ccc8", "#bcaaa4", "#a1887f", "#8d6e63", "#795548", "#6d4c41", "#5d4037", "#4e342e", "#3e2723", "#fafafa", "#f5f5f5", "#eee", "#e0e0e0", "#bdbdbd", "#9e9e9e", "#757575", "#616161", "#424242", "#212121", "#eceff1", "#cfd8dc", "#b0bec5", "#90a4ae", "#78909c", "#607d8b", "#546e7a", "#455a64", "#37474f", "#263238"]>
    <#assign colors = {"#61a861": "#61a861", "#e36370": "#e36370", "#4e565f": "#4e565f", "#ffc107": "#ffc107", "#78b578": "#78b578", "#ffffff": "#ffffff", "#4ba2ce": "#4ba2ce", "#f0fff0": "honeydew", "#ff69b4": "hotpink", "#cd5c5c": "indianred", "#4b0082": "indigo", "#fffff0": "ivory"}/>

    <script>
        $(document).ready(function() {
            $('#primaryColor-select').select2({placeholder: 'Select color...', theme: 'bootstrap4'});
            $('#secondaryColor-select').select2({placeholder: 'Select color...', theme: 'bootstrap4'});
            $('#warningColor-select').select2({placeholder: 'Select color...', theme: 'bootstrap4'});
            $('#dangerColor-select').select2({placeholder: 'Select color...', theme: 'bootstrap4'});
            $('#navbarColor-select').select2({placeholder: 'Select color...', theme: 'bootstrap4'});
            $('#navbarLinkColor-select').select2({placeholder: 'Select color...', theme: 'bootstrap4'});
            $('#linkColor-select').select2({placeholder: 'Select color...', theme: 'bootstrap4'});
        });
    </script>
    <script>
        $(document).ready(function(){
            $('body').bind('DOMSubtreeModified', function(e) {
                if (e.target.innerHTML.length > 0 && e.target.classList.contains("select2-results__options")) {
                    if (e.target.innerText === "Searching…") {
                        setTimeout(() => {
                            addColorSquares(e);
                        }, 500);
                    } else {
                        addColorSquares(e);
                    }
                }
            });

            function addColorSquares(e) {
                var optionsUl = e.target;
                var options = optionsUl.querySelectorAll("li");
                options.forEach(function (item, index) {
                    var color = item.innerText;
                    item.innerHTML = '<i class="color-square" style="background: ' + color + '"></i> ' + color;
                });
            }

            $("#primaryColor-colorBox").css("background", "${colorScheme.primaryColor}");
            $("#secondaryColor-colorBox").css("background", "${colorScheme.secondaryColor}");
            $("#warningColor-colorBox").css("background", "${colorScheme.warningColor}");
            $("#dangerColor-colorBox").css("background", "${colorScheme.dangerColor}");
            $("#navbarColor-colorBox").css("background", "${colorScheme.navbarColor}");
            $("#navbarLinkColor-colorBox").css("background", "${colorScheme.navbarLinkColor}");
            $("#linkColor-colorBox").css("background", "${colorScheme.linkColor}");

            // on color select (in select2 component)
            $("#primaryColor-select").on("select2:select", function (e) {
                $("#primaryColor-colorBox").css("background", e.params.data.id)
            });

            $("#secondaryColor-select").on("select2:select", function (e) {
                $("#secondaryColor-colorBox").css("background", e.params.data.id)
            });

            $("#warningColor-select").on("select2:select", function (e) {
                $("#warningColor-colorBox").css("background", e.params.data.id)
            });

            $("#dangerColor-select").on("select2:select", function (e) {
                $("#dangerColor-colorBox").css("background", e.params.data.id)
            });

            $("#navbarColor-select").on("select2:select", function (e) {
                $("#navbarColor-colorBox").css("background", e.params.data.id)
            });

            $("#navbarLinkColor-select").on("select2:select", function (e) {
                $("#navbarLinkColor-colorBox").css("background", e.params.data.id)
            });

            $("#linkColor-select").on("select2:select", function (e) {
                $("#linkColor-colorBox").css("background", e.params.data.id)
            });

            // open color picker on color box click; change color box and select color
            $("#primaryColor-colorBoxWrapper").click(function () {
                $("#primaryColor").click();
            });
            $("#primaryColor").change(function (e) {
                changeColorsOnClick(e);
            });

            $("#secondaryColor-colorBoxWrapper").click(function () {
                $("#secondaryColor").click();
            });
            $("#secondaryColor").change(function (e) {
                changeColorsOnClick(e);
            });

            $("#warningColor-colorBoxWrapper").click(function () {
                $("#warningColor").click();
            });
            $("#warningColor").change(function (e) {
                changeColorsOnClick(e);
            });

            $("#dangerColor-colorBoxWrapper").click(function () {
                $("#dangerColor").click();
            });
            $("#dangerColor").change(function (e) {
                changeColorsOnClick(e);
            });

            $("#navbarColor-colorBoxWrapper").click(function () {
                $("#navbarColor").click();
            });
            $("#navbarColor").change(function (e) {
                changeColorsOnClick(e);
            });

            $("#navbarLinkColor-colorBoxWrapper").click(function () {
                $("#navbarLinkColor").click();
            });
            $("#navbarLinkColor").change(function (e) {
                changeColorsOnClick(e);
            });

            $("#linkColor-colorBoxWrapper").click(function () {
                $("#linkColor").click();
            });
            $("#linkColor").change(function (e) {
                changeColorsOnClick(e);
            });

            function changeColorsOnClick(e) {
                var component = e.target.dataset.targetElementId;
                var pickedColor = $("#" + e.target.id).val();
                var colorSelect = $("#" + component + "-select");

                // set proper color to the color box
                $("#" + component + "-colorBox").css("background", pickedColor);

                // add option to select
                colorSelect.append($('<option/>', {
                    value: pickedColor,
                    text: pickedColor,
                }));

                // Select the option
                colorSelect.val(pickedColor);
                // Notify any JS components that the value changed
                colorSelect.trigger('change');
            }

            // initialize absent values (values chosen from color picker and not present in the list)
            function initializeSelectAbsentValues() {
                <#if !colors['${colorScheme.primaryColor}']??>
                    initializeSelectAbsentValue("primaryColor", "${colorScheme.primaryColor}")
                </#if>

                <#if !colors['${colorScheme.secondaryColor}']??>
                initializeSelectAbsentValue("secondaryColor", "${colorScheme.secondaryColor}")
                </#if>

                <#if !colors['${colorScheme.warningColor}']??>
                initializeSelectAbsentValue("warningColor", "${colorScheme.warningColor}")
                </#if>

                <#if !colors['${colorScheme.dangerColor}']??>
                initializeSelectAbsentValue("dangerColor", "${colorScheme.dangerColor}")
                </#if>

                <#if !colors['${colorScheme.navbarColor}']??>
                initializeSelectAbsentValue("navbarColor", "${colorScheme.navbarColor}")
                </#if>

                <#if !colors['${colorScheme.navbarLinkColor}']??>
                initializeSelectAbsentValue("navbarLinkColor", "${colorScheme.navbarLinkColor}")
                </#if>

                <#if !colors['${colorScheme.linkColor}']??>
                initializeSelectAbsentValue("linkColor", "${colorScheme.linkColor}")
                </#if>
            }

            function initializeSelectAbsentValue(selectId, value) {
                var select = $("#" + selectId + "-select");
                // add option to select
                select.append($('<option/>', {
                    value: value,
                    text: value,
                }));
                // Select the option
                select.val(value);
                // Notify any JS components that the value changed
                select.trigger('change');
            }

            initializeSelectAbsentValues();

            // reset default values
            $("#reset").on("click", function (e) {
                e.preventDefault();

                var $primaryColorSelect = $("#primaryColor-select");
                $primaryColorSelect.val("#61a861");
                $primaryColorSelect.trigger('change');
                $("#primaryColor-colorBox").css("background", "#61a861");

                var $secondaryColorSelect = $("#secondaryColor-select");
                $secondaryColorSelect.val("#4e565f");
                $secondaryColorSelect.trigger('change');
                $("#secondaryColor-colorBox").css("background", "#4e565f");

                var $warningColorSelect = $("#warningColor-select");
                $warningColorSelect.val("#ffc107");
                $warningColorSelect.trigger('change');
                $("#warningColor-colorBox").css("background", "#ffc107");

                var $dangerColorSelect = $("#dangerColor-select");
                $dangerColorSelect.val("#e36370");
                $dangerColorSelect.trigger('change');
                $("#dangerColor-colorBox").css("background", "#e36370");

                var $navbarColorSelect = $("#navbarColor-select");
                $navbarColorSelect.val("#78b578");
                $navbarColorSelect.trigger('change');
                $("#navbarColor-colorBox").css("background", "#78b578");

                var $navbarLinkColorSelect = $("#navbarLinkColor-select");
                $navbarLinkColorSelect.val("#ffffff");
                $navbarLinkColorSelect.trigger('change');
                $("#navbarLinkColor-colorBox").css("background", "#ffffff");

                var $linkColorSelect = $("#linkColor-select");
                $linkColorSelect.val("#4ba2ce");
                $linkColorSelect.trigger('change');
                $("#linkColor-colorBox").css("background", "#4ba2ce");
            })
        });
    </script>
    <title><@s.text name="title"/></title>
    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/macros/forms.ftl">
    <#include "/WEB-INF/pages/inc/menu.ftl">

    <div class="container-fluid bg-body border-bottom">
        <div class="container my-3">
            <#include "/WEB-INF/pages/inc/action_alerts.ftl">
        </div>

        <div class="container my-3 p-3">
            <div class="text-center">
                <div class="text-uppercase fw-bold fs-smaller-2">
                    <span><@s.text name="menu.admin"/></span>
                </div>

                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    <@s.text name="admin.home.manageUI"/>
                </h1>

                <div class="mt-2">
                    <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" form="ui-management-form" name="save" id="save" key="button.save" />
                    <@s.submit cssClass="button btn btn-sm btn-outline-gbif-danger top-button" form="ui-management-form" name="reset" id="reset" key="button.reset" />
                    <@s.submit cssClass="button btn btn-sm btn-outline-secondary top-button" form="ui-management-form" name="cancel" key="button.cancel"/>
                </div>
            </div>
        </div>
    </div>

    <main class="container">
        <div class="my-3 p-3">
            <div id="ui-management-form-wrapper" class="mt-4">
                <form id="ui-management-form" class="needs-validation" action="uiManagement.do" method="post" novalidate>
                    <div class="row g-3">
                        <div class="col-lg-6">
                            <label for="primaryColor-select" class="form-label">
                                <@s.text name="admin.uiManagement.primaryColor"/>
                            </label>
                            <div class="input-group">
                                <select name="colorScheme.primaryColor" id="primaryColor-select" class="form-select">
                                    <#list colors as colorHex, colorName>
                                        <option value="${colorHex}" <#if colorScheme.primaryColor == colorHex>selected</#if> >
                                            ${colorName}
                                        </option>
                                    </#list>
                                </select>
                                <input id="primaryColor" type="color" data-target-element-id="primaryColor" style='opacity:0;width:100px;height:100%;position:absolute;'/>
                                <span id="primaryColor-colorBoxWrapper" class="input-group-append">
                                    <span class="input-group-text colorpicker-input-addon" data-original-title="" title="" tabindex="0">
                                        <i id="primaryColor-colorBox" style="background: rgb(var(--color-gbif-primary));"></i>
                                    </span>
                                </span>
                            </div>
                        </div>

                        <div class="col-lg-6">
                            <label for="secondaryColor-select" class="form-label">
                                <@s.text name="admin.uiManagement.secondaryColor"/>
                            </label>
                            <div class="input-group">
                                <select name="colorScheme.secondaryColor" id="secondaryColor-select" class="form-select">
                                    <#list colors as colorHex, colorName>
                                        <option value="${colorHex}" <#if colorScheme.secondaryColor == colorHex>selected</#if> >
                                            ${colorName}
                                        </option>
                                    </#list>
                                </select>
                                <input id="secondaryColor" type="color" data-target-element-id="secondaryColor" style='opacity:0;width:100px;height:100%;position:absolute;'/>
                                <span id="secondaryColor-colorBoxWrapper" class="input-group-append">
                                    <span class="input-group-text colorpicker-input-addon" data-original-title="" title="" tabindex="0">
                                        <i id="secondaryColor-colorBox" style="background: rgb(var(--color-gbif-secondary));"></i>
                                    </span>
                                </span>
                            </div>
                        </div>

                        <div class="col-lg-6">
                            <label for="warningColor-select" class="form-label">
                                <@s.text name="admin.uiManagement.warningColor"/>
                            </label>
                            <div class="input-group">
                                <select name="colorScheme.warningColor" id="warningColor-select" class="form-select">
                                    <#list colors as colorHex, colorName>
                                        <option value="${colorHex}" <#if colorScheme.warningColor == colorHex>selected</#if> >
                                            ${colorName}
                                        </option>
                                    </#list>
                                </select>
                                <input id="warningColor" type="color" data-target-element-id="warningColor" style='opacity:0;width:100px;height:100%;position:absolute;'/>
                                <span id="warningColor-colorBoxWrapper" class="input-group-append">
                                    <span class="input-group-text colorpicker-input-addon" data-original-title="" title="" tabindex="0">
                                        <i id="warningColor-colorBox" style="background: rgb(var(--color-gbif-warning));"></i>
                                    </span>
                                </span>
                            </div>
                        </div>

                        <div class="col-lg-6">
                            <label for="dangerColor-select" class="form-label">
                                <@s.text name="admin.uiManagement.dangerColor"/>
                            </label>
                            <div class="input-group">
                                <select name="colorScheme.dangerColor" id="dangerColor-select" class="form-select">
                                    <#list colors as colorHex, colorName>
                                        <option value="${colorHex}" <#if colorScheme.dangerColor == colorHex>selected</#if> >
                                            ${colorName}
                                        </option>
                                    </#list>
                                </select>
                                <input id="dangerColor" type="color" data-target-element-id="dangerColor" style='opacity:0;width:100px;height:100%;position:absolute;'/>
                                <span id="dangerColor-colorBoxWrapper" class="input-group-append">
                                    <span class="input-group-text colorpicker-input-addon" data-original-title="" title="" tabindex="0">
                                        <i id="dangerColor-colorBox" style="background: rgb(var(--color-gbif-danger));"></i>
                                    </span>
                                </span>
                            </div>
                        </div>
                    </div>

                    <div class="row g-3 mt-5">
                        <div class="col-lg-6">
                            <label for="navbarColor-select" class="form-label">
                                <@s.text name="admin.uiManagement.navbarColor"/>
                            </label>
                            <div class="input-group">
                                <select name="colorScheme.navbarColor" id="navbarColor-select" class="form-select">
                                    <#list colors as colorHex, colorName>
                                        <option value="${colorHex}" <#if colorScheme.navbarColor == colorHex>selected</#if> >
                                            ${colorName}
                                        </option>
                                    </#list>
                                </select>
                                <input id="navbarColor" type="color" data-target-element-id="navbarColor" style='opacity:0;width:100px;height:100%;position:absolute;'/>
                                <span id="navbarColor-colorBoxWrapper" class="input-group-append">
                                    <span class="input-group-text colorpicker-input-addon" data-original-title="" title="" tabindex="0">
                                        <i id="navbarColor-colorBox" style="background: rgb(var(--navbar-color));"></i>
                                    </span>
                                </span>
                            </div>
                        </div>

                        <div class="col-lg-6">
                            <label for="navbarLinkColor-select" class="form-label">
                                <@s.text name="admin.uiManagement.navbarLinkColor"/>
                            </label>
                            <div class="input-group">
                                <select name="colorScheme.navbarLinkColor" id="navbarLinkColor-select" class="form-select">
                                    <#list colors as colorHex, colorName>
                                        <option value="${colorHex}" <#if colorScheme.navbarLinkColor == colorHex>selected</#if> >
                                            ${colorName}
                                        </option>
                                    </#list>
                                </select>
                                <input id="navbarLinkColor" type="color" data-target-element-id="navbarLinkColor" style='opacity:0;width:100px;height:100%;position:absolute;'/>
                                <span id="navbarLinkColor-colorBoxWrapper" class="input-group-append">
                                    <span class="input-group-text colorpicker-input-addon" data-original-title="" title="" tabindex="0">
                                        <i id="navbarLinkColor-colorBox" style="background: rgb(var(--navbar-link-color));"></i>
                                    </span>
                                </span>
                            </div>
                        </div>
                    </div>

                    <div class="row g-3 mt-5">
                        <div class="col-lg-6">
                            <label for="linkColor-select" class="form-label">
                                <@s.text name="admin.uiManagement.linkColor"/>
                            </label>
                            <div class="input-group">
                                <select name="colorScheme.linkColor" id="linkColor-select" class="form-select">
                                    <#list colors as colorHex, colorName>
                                        <option value="${colorHex}" <#if colorScheme.linkColor == colorHex>selected</#if> >
                                            ${colorName}
                                        </option>
                                    </#list>
                                </select>
                                <input id="linkColor" type="color" data-target-element-id="linkColor" style='opacity:0;width:100px;height:100%;position:absolute;'/>
                                <span id="linkColor-colorBoxWrapper" class="input-group-append">
                                    <span class="input-group-text colorpicker-input-addon" data-original-title="" title="" tabindex="0">
                                        <i id="linkColor-colorBox" style="background: rgb(var(--link-color));"></i>
                                    </span>
                                </span>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </main>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
