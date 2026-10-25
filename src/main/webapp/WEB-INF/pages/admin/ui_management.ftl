<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.UIManagementAction" -->
    <#include "/WEB-INF/pages/inc/header.ftl">
    <script src="${baseURL}/js/ajaxfileupload.js"></script>
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
    <script src="${baseURL}/js/select2/select2-4.0.13.min.js"></script>

    <#assign colors = {"#61a861": "GBIF green", "#e36370": "GBIF red", "#4e565f": "GBIF grey", "#575757": "GBIF font grey", "#ffc108": "GBIF yellow", "#78b578": "GBIF navbar green", "#4ba2ce": "GBIF light blue", "#f8f9fa": "GBIF footer grey", "#f0f8ff": "alice blue", "#faebd7": "antique white", "#00ffff": "aqua", "#7fffd4": "aquamarine", "#f0ffff": "azure", "#f5f5dc": "beige", "#ffe4c4": "bisque", "#000000": "black", "#ffebcd": "blanched almond", "#0000ff": "blue", "#8a2be2": "blue violet", "#a52a2a": "brown", "#deb887": "burly wood", "#5f9ea0": "cadet blue", "#7fff00": "chartreuse", "#d2691e": "chocolate", "#ff7f50": "coral", "#6495ed": "cornflower blue", "#fff8dc": "cornsilk", "#dc143c": "crimson", "#00ffff": "cyan", "#00008b": "dark blue", "#008b8b": "dark cyan", "#b8860b": "dark golden rod", "#a9a9a9": "dark gray", "#a9a9a9": "dark grey", "#006400": "dark green", "#bdb76b": "dark khaki", "#8b008b": "dark magenta", "#556b2f": "dark olive green", "#ff8c00": "dark orange", "#9932cc": "dark orchid", "#8b0000": "dark red", "#e9967a": "dark salmon", "#8fbc8f": "dark sea green", "#483d8b": "dark slate blue", "#2f4f4f": "dark slate gray", "#2f4f4f": "dark slate grey", "#00ced1": "dark turquoise", "#9400d3": "dark violet", "#ff1493": "deep pink", "#00bfff": "deep sky blue", "#696969": "dim gray", "#696969": "dim grey", "#1e90ff": "dodger blue", "#b22222": "fire brick", "#fffaf0": "floral white", "#228b22": "forest green", "#ff00ff": "fuchsia", "#dcdcdc": "gainsboro", "#f8f8ff": "ghost white", "#ffd700": "gold", "#daa520": "golden rod", "#808080": "gray", "#808080": "grey", "#008000": "green", "#adff2f": "green yellow", "#f0fff0": "honey dew", "#ff69b4": "hot pink", "#cd5c5c": "indian red", "#4b0082": "indigo", "#fffff0": "ivory", "#f0e68c": "khaki", "#e6e6fa": "lavender", "#fff0f5": "lavender blush", "#7cfc00": "lawn green", "#fffacd": "lemon chiffon", "#add8e6": "light blue", "#f08080": "light coral", "#e0ffff": "light cyan", "#fafad2": "light golden rod yellow", "#d3d3d3": "light gray", "#d3d3d3": "light grey", "#90ee90": "light green", "#ffb6c1": "light pink", "#ffa07a": "light salmon", "#20b2aa": "light sea green", "#87cefa": "light sky blue", "#778899": "light slate gray", "#778899": "light slate grey", "#b0c4de": "light steel blue", "#ffffe0": "light yellow", "#00ff00": "lime", "#32cd32": "lime green", "#faf0e6": "linen", "#ff00ff": "magenta", "#800000": "maroon", "#66cdaa": "medium aqua marine", "#0000cd": "medium blue", "#ba55d3": "medium orchid", "#9370db": "medium purple", "#3cb371": "medium sea green", "#7b68ee": "medium slate blue", "#00fa9a": "medium spring green", "#48d1cc": "medium turquoise", "#c71585": "medium violet red", "#191970": "midnight blue", "#f5fffa": "mint cream", "#ffe4e1": "misty rose", "#ffe4b5": "moccasin", "#ffdead": "navajo white", "#000080": "navy", "#fdf5e6": "old lace", "#808000": "olive", "#6b8e23": "olive drab", "#ffa500": "orange", "#ff4500": "orange red", "#da70d6": "orchid", "#eee8aa": "pale golden rod", "#98fb98": "pale green", "#afeeee": "pale turquoise", "#db7093": "pale violet red", "#ffefd5": "papaya whip", "#ffdab9": "peach puff", "#cd853f": "peru", "#ffc0cb": "pink", "#dda0dd": "plum", "#b0e0e6": "powder blue", "#800080": "purple", "#663399": "rebecca purple", "#ff0000": "red", "#bc8f8f": "rosy brown", "#4169e1": "royal blue", "#8b4513": "saddle brown", "#fa8072": "salmon", "#f4a460": "sandy brown", "#2e8b57": "sea green", "#fff5ee": "sea shell", "#a0522d": "sienna", "#c0c0c0": "silver", "#87ceeb": "sky blue", "#6a5acd": "slate blue", "#708090": "slate gray", "#708090": "slate grey", "#fffafa": "snow", "#00ff7f": "spring green", "#4682b4": "steel blue", "#d2b48c": "tan", "#008080": "teal", "#d8bfd8": "thistle", "#ff6347": "tomato", "#40e0d0": "turquoise", "#ee82ee": "violet", "#f5deb3": "wheat", "#ffffff": "white", "#f5f5f5": "white smoke", "#ffff00": "yellow", "#9acd32": "yellow green", "#ffebee": "md-red-50", "#ffcdd2": "md-red-100", "#ef9a9a": "md-red-200", "#e57373": "md-red-300", "#ef5350": "md-red-400", "#f44336": "md-red-500", "#e53935": "md-red-600", "#d32f2f": "md-red-700", "#c62828": "md-red-800", "#b71c1c": "md-red-900", "#ff8a80": "md-red-A100", "#ff5252": "md-red-A200", "#ff1744": "md-red-A400", "#d50000": "md-red-A700", "#fce4ec": "md-pink-50", "#f8bbd0": "md-pink-100", "#f48fb1": "md-pink-200", "#f06292": "md-pink-300", "#ec407a": "md-pink-400", "#e91e63": "md-pink-500", "#d81b60": "md-pink-600", "#c2185b": "md-pink-700", "#ad1457": "md-pink-800", "#880e4f": "md-pink-900", "#ff80ab": "md-pink-A100", "#ff4081": "md-pink-A200", "#f50057": "md-pink-A400", "#c51162": "md-pink-A700", "#f3e5f5": "md-purple-50", "#e1bee7": "md-purple-100", "#ce93d8": "md-purple-200", "#ba68c8": "md-purple-300", "#ab47bc": "md-purple-400", "#9c27b0": "md-purple-500", "#8e24aa": "md-purple-600", "#7b1fa2": "md-purple-700", "#6a1b9a": "md-purple-800", "#4a148c": "md-purple-900", "#ea80fc": "md-purple-A100", "#e040fb": "md-purple-A200", "#d500f9": "md-purple-A400", "#a0f": "md-purple-A700", "#ede7f6": "md-deep-purple-50", "#d1c4e9": "md-deep-purple-100", "#b39ddb": "md-deep-purple-200", "#9575cd": "md-deep-purple-300", "#7e57c2": "md-deep-purple-400", "#673ab7": "md-deep-purple-500", "#5e35b1": "md-deep-purple-600", "#512da8": "md-deep-purple-700", "#4527a0": "md-deep-purple-800", "#311b92": "md-deep-purple-900", "#b388ff": "md-deep-purple-A100", "#7c4dff": "md-deep-purple-A200", "#651fff": "md-deep-purple-A400", "#6200ea": "md-deep-purple-A700", "#e8eaf6": "md-indigo-50", "#c5cae9": "md-indigo-100", "#9fa8da": "md-indigo-200", "#7986cb": "md-indigo-300", "#5c6bc0": "md-indigo-400", "#3f51b5": "md-indigo-500", "#3949ab": "md-indigo-600", "#303f9f": "md-indigo-700", "#283593": "md-indigo-800", "#1a237e": "md-indigo-900", "#8c9eff": "md-indigo-A100", "#536dfe": "md-indigo-A200", "#3d5afe": "md-indigo-A400", "#304ffe": "md-indigo-A700", "#e3f2fd": "md-blue-50", "#bbdefb": "md-blue-100", "#90caf9": "md-blue-200", "#64b5f6": "md-blue-300", "#42a5f5": "md-blue-400", "#2196f3": "md-blue-500", "#1e88e5": "md-blue-600", "#1976d2": "md-blue-700", "#1565c0": "md-blue-800", "#0d47a1": "md-blue-900", "#82b1ff": "md-blue-A100", "#448aff": "md-blue-A200", "#2979ff": "md-blue-A400", "#2962ff": "md-blue-A700", "#e1f5fe": "md-light-blue-50", "#b3e5fc": "md-light-blue-100", "#81d4fa": "md-light-blue-200", "#4fc3f7": "md-light-blue-300", "#29b6f6": "md-light-blue-400", "#03a9f4": "md-light-blue-500", "#039be5": "md-light-blue-600", "#0288d1": "md-light-blue-700", "#0277bd": "md-light-blue-800", "#01579b": "md-light-blue-900", "#80d8ff": "md-light-blue-A100", "#40c4ff": "md-light-blue-A200", "#00b0ff": "md-light-blue-A400", "#0091ea": "md-light-blue-A700", "#e0f7fa": "md-cyan-50", "#b2ebf2": "md-cyan-100", "#80deea": "md-cyan-200", "#4dd0e1": "md-cyan-300", "#26c6da": "md-cyan-400", "#00bcd4": "md-cyan-500", "#00acc1": "md-cyan-600", "#0097a7": "md-cyan-700", "#00838f": "md-cyan-800", "#006064": "md-cyan-900", "#84ffff": "md-cyan-A100", "#18ffff": "md-cyan-A200", "#00e5ff": "md-cyan-A400", "#00b8d4": "md-cyan-A700", "#e0f2f1": "md-teal-50", "#b2dfdb": "md-teal-100", "#80cbc4": "md-teal-200", "#4db6ac": "md-teal-300", "#26a69a": "md-teal-400", "#009688": "md-teal-500", "#00897b": "md-teal-600", "#00796b": "md-teal-700", "#00695c": "md-teal-800", "#004d40": "md-teal-900", "#a7ffeb": "md-teal-A100", "#64ffda": "md-teal-A200", "#1de9b6": "md-teal-A400", "#00bfa5": "md-teal-A700", "#e8f5e9": "md-green-50", "#c8e6c9": "md-green-100", "#a5d6a7": "md-green-200", "#81c784": "md-green-300", "#66bb6a": "md-green-400", "#4caf50": "md-green-500", "#43a047": "md-green-600", "#388e3c": "md-green-700", "#2e7d32": "md-green-800", "#1b5e20": "md-green-900", "#b9f6ca": "md-green-A100", "#69f0ae": "md-green-A200", "#00e676": "md-green-A400", "#00c853": "md-green-A700", "#f1f8e9": "md-light-green-50", "#dcedc8": "md-light-green-100", "#c5e1a5": "md-light-green-200", "#aed581": "md-light-green-300", "#9ccc65": "md-light-green-400", "#8bc34a": "md-light-green-500", "#7cb342": "md-light-green-600", "#689f38": "md-light-green-700", "#558b2f": "md-light-green-800", "#33691e": "md-light-green-900", "#ccff90": "md-light-green-A100", "#b2ff59": "md-light-green-A200", "#76ff03": "md-light-green-A400", "#64dd17": "md-light-green-A700", "#f9fbe7": "md-lime-50", "#f0f4c3": "md-lime-100", "#e6ee9c": "md-lime-200", "#dce775": "md-lime-300", "#d4e157": "md-lime-400", "#cddc39": "md-lime-500", "#c0ca33": "md-lime-600", "#afb42b": "md-lime-700", "#9e9d24": "md-lime-800", "#827717": "md-lime-900", "#f4ff81": "md-lime-A100", "#eeff41": "md-lime-A200", "#c6ff00": "md-lime-A400", "#aeea00": "md-lime-A700", "#fffde7": "md-yellow-50", "#fff9c4": "md-yellow-100", "#fff59d": "md-yellow-200", "#fff176": "md-yellow-300", "#ffee58": "md-yellow-400", "#ffeb3b": "md-yellow-500", "#fdd835": "md-yellow-600", "#fbc02d": "md-yellow-700", "#f9a825": "md-yellow-800", "#f57f17": "md-yellow-900", "#ffff8d": "md-yellow-A100", "#ff0": "md-yellow-A200", "#ffea00": "md-yellow-A400", "#ffd600": "md-yellow-A700", "#fff8e1": "md-amber-50", "#ffecb3": "md-amber-100", "#ffe082": "md-amber-200", "#ffd54f": "md-amber-300", "#ffca28": "md-amber-400", "#ffc107": "md-amber-500", "#ffb300": "md-amber-600", "#ffa000": "md-amber-700", "#ff8f00": "md-amber-800", "#ff6f00": "md-amber-900", "#ffe57f": "md-amber-A100", "#ffd740": "md-amber-A200", "#ffc400": "md-amber-A400", "#ffab00": "md-amber-A700", "#fff3e0": "md-orange-50", "#ffe0b2": "md-orange-100", "#ffcc80": "md-orange-200", "#ffb74d": "md-orange-300", "#ffa726": "md-orange-400", "#ff9800": "md-orange-500", "#fb8c00": "md-orange-600", "#f57c00": "md-orange-700", "#ef6c00": "md-orange-800", "#e65100": "md-orange-900", "#ffd180": "md-orange-A100", "#ffab40": "md-orange-A200", "#ff9100": "md-orange-A400", "#ff6d00": "md-orange-A700", "#fbe9e7": "md-deep-orange-50", "#ffccbc": "md-deep-orange-100", "#ffab91": "md-deep-orange-200", "#ff8a65": "md-deep-orange-300", "#ff7043": "md-deep-orange-400", "#ff5722": "md-deep-orange-500", "#f4511e": "md-deep-orange-600", "#e64a19": "md-deep-orange-700", "#d84315": "md-deep-orange-800", "#bf360c": "md-deep-orange-900", "#ff9e80": "md-deep-orange-A100", "#ff6e40": "md-deep-orange-A200", "#ff3d00": "md-deep-orange-A400", "#dd2c00": "md-deep-orange-A700", "#efebe9": "md-brown-50", "#d7ccc8": "md-brown-100", "#bcaaa4": "md-brown-200", "#a1887f": "md-brown-300", "#8d6e63": "md-brown-400", "#795548": "md-brown-500", "#6d4c41": "md-brown-600", "#5d4037": "md-brown-700", "#4e342e": "md-brown-800", "#3e2723": "md-brown-900", "#fafafa": "md-grey-50", "#f5f5f5": "md-grey-100", "#eee": "md-grey-200", "#e0e0e0": "md-grey-300", "#bdbdbd": "md-grey-400", "#9e9e9e": "md-grey-500", "#757575": "md-grey-600", "#616161": "md-grey-700", "#424242": "md-grey-800", "#212121": "md-grey-900", "#eceff1": "md-blue-grey-50", "#cfd8dc": "md-blue-grey-100", "#b0bec5": "md-blue-grey-200", "#90a4ae": "md-blue-grey-300", "#78909c": "md-blue-grey-400", "#607d8b": "md-blue-grey-500", "#546e7a": "md-blue-grey-600", "#455a64": "md-blue-grey-700", "#37474f": "md-blue-grey-800", "#263238": "md-blue-grey-900"}/>

    <script>
        $(document).ready(function() {
            // show a color square before the color name
            function formatColorOption(option) {
                if (!option.id) {
                    return option.text;
                }
                return $('<span/>')
                    .append($('<i class="color-square"/>').css('background', option.id))
                    .append(document.createTextNode(' ' + option.text.trim()));
            }

            ['primaryColor', 'navbarColor', 'navbarLinkColor', 'navbarGbifLogoColor', 'navbarActiveTabColor', 'linkColor'].forEach(function (component) {
                $('#' + component + '-select').select2({
                    placeholder: 'Select color',
                    language: {
                        noResults: function () {
                            return '${selectNoResultsFound}';
                        }
                    },
                    templateResult: formatColorOption,
                    theme: 'bootstrap4'
                });
            });
        });
    </script>
    <script>
        $(document).ready(function(){
            $("#primaryColor-colorBox").css("background", "${colorScheme.primaryColor}");
            $("#navbarColor-colorBox").css("background", "${colorScheme.navbarColor}");
            $("#navbarLinkColor-colorBox").css("background", "${colorScheme.navbarLinkColor}");
            $("#navbarGbifLogoColor-colorBox").css("background", "${colorScheme.navbarGbifLogoColor!}");
            $("#navbarActiveTabColor-colorBox").css("background", "${colorScheme.navbarActiveTabColor!}");
            $("#linkColor-colorBox").css("background", "${colorScheme.linkColor}");

            // on color select (in select2 component)
            $("#primaryColor-select").on("select2:select", function (e) {
                $("#primaryColor-colorBox").css("background", e.params.data.id);
                applyPrimaryColor(e.params.data.id);
            });

            $("#navbarColor-select").on("select2:select", function (e) {
                $("#navbarColor-colorBox").css("background", e.params.data.id);
                applyNavbarColor(e.params.data.id);
            });

            $("#navbarLinkColor-select").on("select2:select", function (e) {
                $("#navbarLinkColor-colorBox").css("background", e.params.data.id);
                applyNavbarLinkColor(e.params.data.id);
            });

            $("#navbarGbifLogoColor-select").on("select2:select", function (e) {
                $("#navbarGbifLogoColor-colorBox").css("background", e.params.data.id);
                applyNavbarGbifLogoColor(e.params.data.id);
            });

            $("#navbarActiveTabColor-select").on("select2:select", function (e) {
                $("#navbarActiveTabColor-colorBox").css("background", e.params.data.id);
                applyNavbarActiveTabColor(e.params.data.id);
            });

            $("#linkColor-select").on("select2:select", function (e) {
                $("#linkColor-colorBox").css("background", e.params.data.id);
                applyLinkColor(e.params.data.id);
            });

            // open color picker on color box click; change color box and select color
            $("#primaryColor-colorBoxWrapper").click(function () {
                $("#primaryColor").click();
            });
            $("#primaryColor").change(function (e) {
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

            $("#navbarGbifLogoColor-colorBoxWrapper").click(function () {
                $("#navbarGbifLogoColor").click();
            });
            $("#navbarGbifLogoColor").change(function (e) {
                changeColorsOnClick(e);
            });

            $("#navbarActiveTabColor-colorBoxWrapper").click(function () {
                $("#navbarActiveTabColor").click();
            });
            $("#navbarActiveTabColor").change(function (e) {
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
                // Apply changes live
                if (component === 'primaryColor') {
                    applyPrimaryColor(pickedColor);
                } else if (component === 'linkColor') {
                    applyLinkColor(pickedColor);
                } else if (component === 'navbarColor') {
                    applyNavbarColor(pickedColor);
                } else if (component === 'navbarLinkColor') {
                    applyNavbarLinkColor(pickedColor);
                } else if (component === 'navbarGbifLogoColor') {
                    applyNavbarGbifLogoColor(pickedColor);
                } else if (component === 'navbarActiveTabColor') {
                    applyNavbarActiveTabColor(pickedColor);
                }
            }

            function applyPrimaryColor(hex) {
                const rgb = hexToRgb(hex);
                document.documentElement.style.setProperty('--color-gbif-primary', rgb);
            }

            function applyLinkColor(hex) {
                const rgb = hexToRgb(hex);
                document.documentElement.style.setProperty('--link-color', rgb);
            }

            function applyNavbarColor(hex) {
                const rgb = hexToRgb(hex);
                document.documentElement.style.setProperty('--navbar-color', rgb);
            }

            function applyNavbarLinkColor(hex) {
                const rgb = hexToRgb(hex);
                document.documentElement.style.setProperty('--navbar-link-color', rgb);
            }

            function applyNavbarGbifLogoColor(hex) {
                const rgb = hexToRgb(hex);
                document.documentElement.style.setProperty('--navbar-gbif-logo-color', rgb);
            }

            function applyNavbarActiveTabColor(hex) {
                const rgb = hexToRgb(hex);
                document.documentElement.style.setProperty('--navbar-active-tab-color', rgb);
            }

            // initialize absent values (values chosen from color picker and not present in the list)
            function initializeSelectAbsentValues() {
                <#if !colors['${colorScheme.primaryColor}']??>
                initializeSelectAbsentValue("primaryColor", "${colorScheme.primaryColor}")
                </#if>

                <#if !colors['${colorScheme.navbarColor}']??>
                initializeSelectAbsentValue("navbarColor", "${colorScheme.navbarColor}")
                </#if>

                <#if !colors['${colorScheme.navbarLinkColor}']??>
                initializeSelectAbsentValue("navbarLinkColor", "${colorScheme.navbarLinkColor}")
                </#if>

                <#if !colors['${colorScheme.navbarGbifLogoColor}']??>
                initializeSelectAbsentValue("navbarGbifLogoColor", "${colorScheme.navbarGbifLogoColor}")
                </#if>

                <#if !colors['${colorScheme.navbarActiveTabColor}']??>
                initializeSelectAbsentValue("navbarActiveTabColor", "${colorScheme.navbarActiveTabColor}")
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
                applyPrimaryColor("#61a861");

                var $navbarColorSelect = $("#navbarColor-select");
                $navbarColorSelect.val("#78b578");
                $navbarColorSelect.trigger('change');
                $("#navbarColor-colorBox").css("background", "#78b578");
                applyNavbarColor("#78b578");

                var $navbarLinkColorSelect = $("#navbarLinkColor-select");
                $navbarLinkColorSelect.val("#ffffff");
                $navbarLinkColorSelect.trigger('change');
                $("#navbarLinkColor-colorBox").css("background", "#ffffff");
                applyNavbarLinkColor("#ffffff")

                var $navbarGbifLogoColorSelect = $("#navbarGbifLogoColor-select");
                $navbarGbifLogoColorSelect.val("#ffffff");
                $navbarGbifLogoColorSelect.trigger('change');
                $("#navbarGbifLogoColor-colorBox").css("background", "#ffffff");
                applyNavbarGbifLogoColor("#ffffff")

                var $navbarActiveTabColorSelect = $("#navbarActiveTabColor-select");
                $navbarActiveTabColorSelect.val("#ffffff");
                $navbarActiveTabColorSelect.trigger('change');
                $("#navbarActiveTabColor-colorBox").css("background", "#ffffff");
                applyNavbarActiveTabColor("#ffffff");

                var $linkColorSelect = $("#linkColor-select");
                $linkColorSelect.val("#4ba2ce");
                $linkColorSelect.trigger('change');
                $("#linkColor-colorBox").css("background", "#4ba2ce");
                applyLinkColor("#4ba2ce");
            })

            function urlExists(url, cb) {
                jQuery.ajax({
                    url: url,
                    dataType: 'text',
                    type: 'GET',
                    complete: function (xhr) {
                        if (typeof cb === 'function')
                            cb.apply(this, [xhr.status]);
                    }
                });
            }

            $("#buttonUpload").click(function (event) {
                event.preventDefault()
                return ajaxFileUpload();
            });

            $("#buttonRemove").click(function () {
                $("#applogo").remove();
                $("#file").val('');
                $("#removeLogo").val('true');
            });

            function ajaxFileUpload() {
                $.ajaxFileUpload
                (
                    {
                        url: 'appLogoUpload.do',
                        secureuri: false,
                        fileElementId: 'file',
                        dataType: 'json',
                        done: function (data, status) {
                            if (typeof (data.error) != 'undefined') {
                                if (data.error !== '') {
                                    alert(data.error);
                                } else {
                                    alert(data.msg);
                                }
                            }
                        },
                        fail: function (data, status, e) {
                            alert(e);
                        }
                    }
                )
                return false;
            }

            urlExists('${baseURL}/appLogo.do', function (status) {
                if (status === 404) {
                    // hide logo file
                    $("#applogo-image").hide();
                }
            });

            function hexToRgb(hex) {
                hex = hex.replace(/^#/, '');

                // Parse the hex values for red, green, and blue
                const r = parseInt(hex.slice(0, 2), 16);
                const g = parseInt(hex.slice(2, 4), 16);
                const b = parseInt(hex.slice(4, 6), 16);

                return  r + ',' + g + ',' + b;
            }
        });
    </script>
    <title><@s.text name="title"/></title>
    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/macros/forms.ftl">
    <#include "/WEB-INF/pages/inc/menu.ftl">

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <div class="container-fluid bg-body border-bottom">
        <div class="container bg-body border rounded-2 mb-4">
            <div class="container my-3 p-3">
                <div class="text-center">
                    <div class="fs-smaller">
                        <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                            <ol class="breadcrumb justify-content-center mb-0">
                                <li class="breadcrumb-item"><a href="${baseURL}/admin/"><@s.text name="breadcrumb.admin"/></a></li>
                                <li class="breadcrumb-item"><@s.text name="breadcrumb.admin.ui"/></li>
                            </ol>
                        </nav>
                    </div>

                    <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                        <@s.text name="admin.home.manageUI"/>
                    </h1>

                    <div class="mt-2">
                        <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" form="ui-management-form" name="save" id="save" key="button.save" />
                        <@s.submit cssClass="button btn btn-sm btn-outline-gbif-danger top-button" form="ui-management-form" name="reset" id="reset" key="button.reset" />
                        <a href="${baseURL}/admin/" class="button btn btn-sm btn-outline-secondary top-button">
                            <@s.text name="button.back"/>
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <main class="container main-content-container">
        <div class="my-3 p-3">
            <div id="ui-management-form-wrapper" class="mt-4">
                <form id="ui-management-form" class="needs-validation" action="uiManagement.do" method="post" novalidate>
                    <@s.hidden name="removeLogo" id="removeLogo" value="false" required="true" />

                    <!-- App Logo -->
                    <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fs-5 fw-400">
                        <@s.text name="admin.uiManagement.logo"/>
                    </h4>
                    <div id="logofields" class="row g-3 mb-5">
                        <div class="col-lg-6">
                            <label for="file" class="form-label">
                                <@s.text name="admin.uiManagement.iptLogo"/>
                            </label>
                            <@s.file cssClass="form-control my-1" name="file"/>
                            <a href="#" class="action-link-button action-link-button-primary" id="buttonUpload">
                                <svg class="action-link-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                    <path d="M9 16h6v-6h4l-7-7-7 7h4zm-4 2h14v2H5z"></path>
                                </svg>
                                <@s.text name="button.upload"/>
                            </a>
                            <a href="#" class="action-link-button action-link-button-danger" id="buttonRemove">
                                <svg class="action-link-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                </svg>
                                <@s.text name="button.remove"/>
                            </a>
                        </div>

                        <div class="col-lg-3 d-flex justify-content-start align-items-center">
                            <div id="applogo">
                                <img id="applogo-image" src="${baseURL}/appLogo.do" />
                            </div>
                        </div>
                    </div>

                    <!-- App colors -->
                    <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fs-5 fw-400">
                        <@s.text name="admin.uiManagement.colors"/>
                    </h4>
                    <div class="row g-3">
                        <div class="col-lg-6">
                            <label for="primaryColor-select" class="form-label">
                                <@s.text name="admin.uiManagement.primaryColor"/> <span class="text-gbif-danger">&#42;</span>
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

                        <!-- Links -->
                        <div class="col-lg-6">
                            <label for="linkColor-select" class="form-label">
                                <@s.text name="admin.uiManagement.linkColor"/> <span class="text-gbif-danger">&#42;</span>
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

                        <!-- Navigation bar -->
                        <div class="col-lg-6">
                            <label for="navbarColor-select" class="form-label">
                                <@s.text name="admin.uiManagement.navbarColor"/> <span class="text-gbif-danger">&#42;</span>
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
                                <@s.text name="admin.uiManagement.navbarLinkColor"/> <span class="text-gbif-danger">&#42;</span>
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

                        <div class="col-lg-6">
                            <label for="navbarGbifLogoColor-select" class="form-label">
                                <@s.text name="admin.uiManagement.navbarGbifLogoColor"/> <span class="text-gbif-danger">&#42;</span>
                            </label>
                            <div class="input-group">
                                <select name="colorScheme.navbarGbifLogoColor" id="navbarGbifLogoColor-select" class="form-select">
                                    <#list colors as colorHex, colorName>
                                        <option value="${colorHex}" <#if colorScheme.navbarGbifLogoColor == colorHex>selected</#if> >
                                            ${colorName}
                                        </option>
                                    </#list>
                                </select>
                                <input id="navbarGbifLogoColor" type="color" data-target-element-id="navbarGbifLogoColor" style='opacity:0;width:100px;height:100%;position:absolute;'/>
                                <span id="navbarGbifLogoColor-colorBoxWrapper" class="input-group-append">
                                    <span class="input-group-text colorpicker-input-addon" data-original-title="" title="" tabindex="0">
                                        <i id="navbarGbifLogoColor-colorBox" style="background: rgb(var(--navbar-link-color));"></i>
                                    </span>
                                </span>
                            </div>
                        </div>


                        <div class="col-lg-6">
                            <label for="navbarActiveTabColor-select" class="form-label">
                                <@s.text name="admin.uiManagement.navbarActiveTabColor"/> <span class="text-gbif-danger">&#42;</span>
                            </label>
                            <div class="input-group">
                                <select name="colorScheme.navbarActiveTabColor" id="navbarActiveTabColor-select" class="form-select">
                                    <#list colors as colorHex, colorName>
                                        <option value="${colorHex}" <#if colorScheme.navbarActiveTabColor == colorHex>selected</#if> >
                                            ${colorName}
                                        </option>
                                    </#list>
                                </select>
                                <input id="navbarActiveTabColor" type="color" data-target-element-id="navbarActiveTabColor" style='opacity:0;width:100px;height:100%;position:absolute;'/>
                                <span id="navbarActiveTabColor-colorBoxWrapper" class="input-group-append">
                                    <span class="input-group-text colorpicker-input-addon" data-original-title="" title="" tabindex="0">
                                        <i id="navbarActiveTabColor-colorBox" style="background: rgb(var(--navbar-link-color));"></i>
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
