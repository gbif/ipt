<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <script src="${baseURL}/js/jquery/jquery-3.5.1.min.js"></script>
    <script src="${baseURL}/js/ajaxfileupload.js"></script>
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
    <script src="${baseURL}/js/select2/select2-4.0.13.min.js"></script>

    <#assign colors = {"#61a861": "GBIF green", "#e36370": "GBIF red", "#4e565f": "GBIF grey", "#ffc108": "GBIF yellow", "#78b578": "GBIF navbar green", "#4ba2ce": "GBIF light blue", "#f0f8ff": "alice blue", "#faebd7": "antique white", "#00ffff": "aqua", "#7fffd4": "aquamarine", "#f0ffff": "azure", "#f5f5dc": "beige", "#ffe4c4": "bisque", "#000000": "black", "#ffebcd": "blanched almond", "#0000ff": "blue", "#8a2be2": "blue violet", "#a52a2a": "brown", "#deb887": "burly wood", "#5f9ea0": "cadet blue", "#7fff00": "chartreuse", "#d2691e": "chocolate", "#ff7f50": "coral", "#6495ed": "cornflower blue", "#fff8dc": "cornsilk", "#dc143c": "crimson", "#00ffff": "cyan", "#00008b": "dark blue", "#008b8b": "dark cyan", "#b8860b": "dark golden rod", "#a9a9a9": "dark gray", "#a9a9a9": "dark grey", "#006400": "dark green", "#bdb76b": "dark khaki", "#8b008b": "dark magenta", "#556b2f": "dark olive green", "#ff8c00": "dark orange", "#9932cc": "dark orchid", "#8b0000": "dark red", "#e9967a": "dark salmon", "#8fbc8f": "dark sea green", "#483d8b": "dark slate blue", "#2f4f4f": "dark slate gray", "#2f4f4f": "dark slate grey", "#00ced1": "dark turquoise", "#9400d3": "dark violet", "#ff1493": "deep pink", "#00bfff": "deep sky blue", "#696969": "dim gray", "#696969": "dim grey", "#1e90ff": "dodger blue", "#b22222": "fire brick", "#fffaf0": "floral white", "#228b22": "forest green", "#ff00ff": "fuchsia", "#dcdcdc": "gainsboro", "#f8f8ff": "ghost white", "#ffd700": "gold", "#daa520": "golden rod", "#808080": "gray", "#808080": "grey", "#008000": "green", "#adff2f": "green yellow", "#f0fff0": "honey dew", "#ff69b4": "hot pink", "#cd5c5c": "indian red", "#4b0082": "indigo", "#fffff0": "ivory", "#f0e68c": "khaki", "#e6e6fa": "lavender", "#fff0f5": "lavender blush", "#7cfc00": "lawn green", "#fffacd": "lemon chiffon", "#add8e6": "light blue", "#f08080": "light coral", "#e0ffff": "light cyan", "#fafad2": "light golden rod yellow", "#d3d3d3": "light gray", "#d3d3d3": "light grey", "#90ee90": "light green", "#ffb6c1": "light pink", "#ffa07a": "light salmon", "#20b2aa": "light sea green", "#87cefa": "light sky blue", "#778899": "light slate gray", "#778899": "light slate grey", "#b0c4de": "light steel blue", "#ffffe0": "light yellow", "#00ff00": "lime", "#32cd32": "lime green", "#faf0e6": "linen", "#ff00ff": "magenta", "#800000": "maroon", "#66cdaa": "medium aqua marine", "#0000cd": "medium blue", "#ba55d3": "medium orchid", "#9370db": "medium purple", "#3cb371": "medium sea green", "#7b68ee": "medium slate blue", "#00fa9a": "medium spring green", "#48d1cc": "medium turquoise", "#c71585": "medium violet red", "#191970": "midnight blue", "#f5fffa": "mint cream", "#ffe4e1": "misty rose", "#ffe4b5": "moccasin", "#ffdead": "navajo white", "#000080": "navy", "#fdf5e6": "old lace", "#808000": "olive", "#6b8e23": "olive drab", "#ffa500": "orange", "#ff4500": "orange red", "#da70d6": "orchid", "#eee8aa": "pale golden rod", "#98fb98": "pale green", "#afeeee": "pale turquoise", "#db7093": "pale violet red", "#ffefd5": "papaya whip", "#ffdab9": "peach puff", "#cd853f": "peru", "#ffc0cb": "pink", "#dda0dd": "plum", "#b0e0e6": "powder blue", "#800080": "purple", "#663399": "rebecca purple", "#ff0000": "red", "#bc8f8f": "rosy brown", "#4169e1": "royal blue", "#8b4513": "saddle brown", "#fa8072": "salmon", "#f4a460": "sandy brown", "#2e8b57": "sea green", "#fff5ee": "sea shell", "#a0522d": "sienna", "#c0c0c0": "silver", "#87ceeb": "sky blue", "#6a5acd": "slate blue", "#708090": "slate gray", "#708090": "slate grey", "#fffafa": "snow", "#00ff7f": "spring green", "#4682b4": "steel blue", "#d2b48c": "tan", "#008080": "teal", "#d8bfd8": "thistle", "#ff6347": "tomato", "#40e0d0": "turquoise", "#ee82ee": "violet", "#f5deb3": "wheat", "#ffffff": "white", "#f5f5f5": "white smoke", "#ffff00": "yellow", "#9acd32": "yellow green", "#ffebee": "md-red-50", "#ffcdd2": "md-red-100", "#ef9a9a": "md-red-200", "#e57373": "md-red-300", "#ef5350": "md-red-400", "#f44336": "md-red-500", "#e53935": "md-red-600", "#d32f2f": "md-red-700", "#c62828": "md-red-800", "#b71c1c": "md-red-900", "#ff8a80": "md-red-A100", "#ff5252": "md-red-A200", "#ff1744": "md-red-A400", "#d50000": "md-red-A700", "#fce4ec": "md-pink-50", "#f8bbd0": "md-pink-100", "#f48fb1": "md-pink-200", "#f06292": "md-pink-300", "#ec407a": "md-pink-400", "#e91e63": "md-pink-500", "#d81b60": "md-pink-600", "#c2185b": "md-pink-700", "#ad1457": "md-pink-800", "#880e4f": "md-pink-900", "#ff80ab": "md-pink-A100", "#ff4081": "md-pink-A200", "#f50057": "md-pink-A400", "#c51162": "md-pink-A700", "#f3e5f5": "md-purple-50", "#e1bee7": "md-purple-100", "#ce93d8": "md-purple-200", "#ba68c8": "md-purple-300", "#ab47bc": "md-purple-400", "#9c27b0": "md-purple-500", "#8e24aa": "md-purple-600", "#7b1fa2": "md-purple-700", "#6a1b9a": "md-purple-800", "#4a148c": "md-purple-900", "#ea80fc": "md-purple-A100", "#e040fb": "md-purple-A200", "#d500f9": "md-purple-A400", "#a0f": "md-purple-A700", "#ede7f6": "md-deep-purple-50", "#d1c4e9": "md-deep-purple-100", "#b39ddb": "md-deep-purple-200", "#9575cd": "md-deep-purple-300", "#7e57c2": "md-deep-purple-400", "#673ab7": "md-deep-purple-500", "#5e35b1": "md-deep-purple-600", "#512da8": "md-deep-purple-700", "#4527a0": "md-deep-purple-800", "#311b92": "md-deep-purple-900", "#b388ff": "md-deep-purple-A100", "#7c4dff": "md-deep-purple-A200", "#651fff": "md-deep-purple-A400", "#6200ea": "md-deep-purple-A700", "#e8eaf6": "md-indigo-50", "#c5cae9": "md-indigo-100", "#9fa8da": "md-indigo-200", "#7986cb": "md-indigo-300", "#5c6bc0": "md-indigo-400", "#3f51b5": "md-indigo-500", "#3949ab": "md-indigo-600", "#303f9f": "md-indigo-700", "#283593": "md-indigo-800", "#1a237e": "md-indigo-900", "#8c9eff": "md-indigo-A100", "#536dfe": "md-indigo-A200", "#3d5afe": "md-indigo-A400", "#304ffe": "md-indigo-A700", "#e3f2fd": "md-blue-50", "#bbdefb": "md-blue-100", "#90caf9": "md-blue-200", "#64b5f6": "md-blue-300", "#42a5f5": "md-blue-400", "#2196f3": "md-blue-500", "#1e88e5": "md-blue-600", "#1976d2": "md-blue-700", "#1565c0": "md-blue-800", "#0d47a1": "md-blue-900", "#82b1ff": "md-blue-A100", "#448aff": "md-blue-A200", "#2979ff": "md-blue-A400", "#2962ff": "md-blue-A700", "#e1f5fe": "md-light-blue-50", "#b3e5fc": "md-light-blue-100", "#81d4fa": "md-light-blue-200", "#4fc3f7": "md-light-blue-300", "#29b6f6": "md-light-blue-400", "#03a9f4": "md-light-blue-500", "#039be5": "md-light-blue-600", "#0288d1": "md-light-blue-700", "#0277bd": "md-light-blue-800", "#01579b": "md-light-blue-900", "#80d8ff": "md-light-blue-A100", "#40c4ff": "md-light-blue-A200", "#00b0ff": "md-light-blue-A400", "#0091ea": "md-light-blue-A700", "#e0f7fa": "md-cyan-50", "#b2ebf2": "md-cyan-100", "#80deea": "md-cyan-200", "#4dd0e1": "md-cyan-300", "#26c6da": "md-cyan-400", "#00bcd4": "md-cyan-500", "#00acc1": "md-cyan-600", "#0097a7": "md-cyan-700", "#00838f": "md-cyan-800", "#006064": "md-cyan-900", "#84ffff": "md-cyan-A100", "#18ffff": "md-cyan-A200", "#00e5ff": "md-cyan-A400", "#00b8d4": "md-cyan-A700", "#e0f2f1": "md-teal-50", "#b2dfdb": "md-teal-100", "#80cbc4": "md-teal-200", "#4db6ac": "md-teal-300", "#26a69a": "md-teal-400", "#009688": "md-teal-500", "#00897b": "md-teal-600", "#00796b": "md-teal-700", "#00695c": "md-teal-800", "#004d40": "md-teal-900", "#a7ffeb": "md-teal-A100", "#64ffda": "md-teal-A200", "#1de9b6": "md-teal-A400", "#00bfa5": "md-teal-A700", "#e8f5e9": "md-green-50", "#c8e6c9": "md-green-100", "#a5d6a7": "md-green-200", "#81c784": "md-green-300", "#66bb6a": "md-green-400", "#4caf50": "md-green-500", "#43a047": "md-green-600", "#388e3c": "md-green-700", "#2e7d32": "md-green-800", "#1b5e20": "md-green-900", "#b9f6ca": "md-green-A100", "#69f0ae": "md-green-A200", "#00e676": "md-green-A400", "#00c853": "md-green-A700", "#f1f8e9": "md-light-green-50", "#dcedc8": "md-light-green-100", "#c5e1a5": "md-light-green-200", "#aed581": "md-light-green-300", "#9ccc65": "md-light-green-400", "#8bc34a": "md-light-green-500", "#7cb342": "md-light-green-600", "#689f38": "md-light-green-700", "#558b2f": "md-light-green-800", "#33691e": "md-light-green-900", "#ccff90": "md-light-green-A100", "#b2ff59": "md-light-green-A200", "#76ff03": "md-light-green-A400", "#64dd17": "md-light-green-A700", "#f9fbe7": "md-lime-50", "#f0f4c3": "md-lime-100", "#e6ee9c": "md-lime-200", "#dce775": "md-lime-300", "#d4e157": "md-lime-400", "#cddc39": "md-lime-500", "#c0ca33": "md-lime-600", "#afb42b": "md-lime-700", "#9e9d24": "md-lime-800", "#827717": "md-lime-900", "#f4ff81": "md-lime-A100", "#eeff41": "md-lime-A200", "#c6ff00": "md-lime-A400", "#aeea00": "md-lime-A700", "#fffde7": "md-yellow-50", "#fff9c4": "md-yellow-100", "#fff59d": "md-yellow-200", "#fff176": "md-yellow-300", "#ffee58": "md-yellow-400", "#ffeb3b": "md-yellow-500", "#fdd835": "md-yellow-600", "#fbc02d": "md-yellow-700", "#f9a825": "md-yellow-800", "#f57f17": "md-yellow-900", "#ffff8d": "md-yellow-A100", "#ff0": "md-yellow-A200", "#ffea00": "md-yellow-A400", "#ffd600": "md-yellow-A700", "#fff8e1": "md-amber-50", "#ffecb3": "md-amber-100", "#ffe082": "md-amber-200", "#ffd54f": "md-amber-300", "#ffca28": "md-amber-400", "#ffc107": "md-amber-500", "#ffb300": "md-amber-600", "#ffa000": "md-amber-700", "#ff8f00": "md-amber-800", "#ff6f00": "md-amber-900", "#ffe57f": "md-amber-A100", "#ffd740": "md-amber-A200", "#ffc400": "md-amber-A400", "#ffab00": "md-amber-A700", "#fff3e0": "md-orange-50", "#ffe0b2": "md-orange-100", "#ffcc80": "md-orange-200", "#ffb74d": "md-orange-300", "#ffa726": "md-orange-400", "#ff9800": "md-orange-500", "#fb8c00": "md-orange-600", "#f57c00": "md-orange-700", "#ef6c00": "md-orange-800", "#e65100": "md-orange-900", "#ffd180": "md-orange-A100", "#ffab40": "md-orange-A200", "#ff9100": "md-orange-A400", "#ff6d00": "md-orange-A700", "#fbe9e7": "md-deep-orange-50", "#ffccbc": "md-deep-orange-100", "#ffab91": "md-deep-orange-200", "#ff8a65": "md-deep-orange-300", "#ff7043": "md-deep-orange-400", "#ff5722": "md-deep-orange-500", "#f4511e": "md-deep-orange-600", "#e64a19": "md-deep-orange-700", "#d84315": "md-deep-orange-800", "#bf360c": "md-deep-orange-900", "#ff9e80": "md-deep-orange-A100", "#ff6e40": "md-deep-orange-A200", "#ff3d00": "md-deep-orange-A400", "#dd2c00": "md-deep-orange-A700", "#efebe9": "md-brown-50", "#d7ccc8": "md-brown-100", "#bcaaa4": "md-brown-200", "#a1887f": "md-brown-300", "#8d6e63": "md-brown-400", "#795548": "md-brown-500", "#6d4c41": "md-brown-600", "#5d4037": "md-brown-700", "#4e342e": "md-brown-800", "#3e2723": "md-brown-900", "#fafafa": "md-grey-50", "#f5f5f5": "md-grey-100", "#eee": "md-grey-200", "#e0e0e0": "md-grey-300", "#bdbdbd": "md-grey-400", "#9e9e9e": "md-grey-500", "#757575": "md-grey-600", "#616161": "md-grey-700", "#424242": "md-grey-800", "#212121": "md-grey-900", "#eceff1": "md-blue-grey-50", "#cfd8dc": "md-blue-grey-100", "#b0bec5": "md-blue-grey-200", "#90a4ae": "md-blue-grey-300", "#78909c": "md-blue-grey-400", "#607d8b": "md-blue-grey-500", "#546e7a": "md-blue-grey-600", "#455a64": "md-blue-grey-700", "#37474f": "md-blue-grey-800", "#263238": "md-blue-grey-900"}/>

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
        let colorsReversed = {"GBIF green": "#61a861", "GBIF red": "#e36370", "GBIF grey": "#4e565f", "GBIF yellow": "#ffc108", "GBIF navbar green": "#78b578", "GBIF light blue": "#4ba2ce", "alice blue": "#f0f8ff", "antique white": "#faebd7", "aqua": "#00ffff", "aquamarine": "#7fffd4", "azure": "#f0ffff", "beige": "#f5f5dc", "bisque": "#ffe4c4", "black": "#000000", "blanched almond": "#ffebcd", "blue": "#0000ff", "blue violet": "#8a2be2", "brown": "#a52a2a", "burly wood": "#deb887", "cadet blue": "#5f9ea0", "chartreuse": "#7fff00", "chocolate": "#d2691e", "coral": "#ff7f50", "cornflower blue": "#6495ed", "cornsilk": "#fff8dc", "crimson": "#dc143c", "cyan": "#00ffff", "dark blue": "#00008b", "dark cyan": "#008b8b", "dark golden rod": "#b8860b", "dark gray": "#a9a9a9", "dark grey": "#a9a9a9", "dark green": "#006400", "dark khaki": "#bdb76b", "dark magenta": "#8b008b", "dark olive green": "#556b2f", "dark orange": "#ff8c00", "dark orchid": "#9932cc", "dark red": "#8b0000", "dark salmon": "#e9967a", "dark sea green": "#8fbc8f", "dark slate blue": "#483d8b", "dark slate gray": "#2f4f4f", "dark slate grey": "#2f4f4f", "dark turquoise": "#00ced1", "dark violet": "#9400d3", "deep pink": "#ff1493", "deep sky blue": "#00bfff", "dim gray": "#696969", "dim grey": "#696969", "dodger blue": "#1e90ff", "fire brick": "#b22222", "floral white": "#fffaf0", "forest green": "#228b22", "fuchsia": "#ff00ff", "gainsboro": "#dcdcdc", "ghost white": "#f8f8ff", "gold": "#ffd700", "golden rod": "#daa520", "gray": "#808080", "grey": "#808080", "green": "#008000", "green yellow": "#adff2f", "honey dew": "#f0fff0", "hot pink": "#ff69b4", "indian red": "#cd5c5c", "indigo": "#4b0082", "ivory": "#fffff0", "khaki": "#f0e68c", "lavender": "#e6e6fa", "lavender blush": "#fff0f5", "lawn green": "#7cfc00", "lemon chiffon": "#fffacd", "light blue": "#add8e6", "light coral": "#f08080", "light cyan": "#e0ffff", "light golden rod yellow": "#fafad2", "light gray": "#d3d3d3", "light grey": "#d3d3d3", "light green": "#90ee90", "light pink": "#ffb6c1", "light salmon": "#ffa07a", "light sea green": "#20b2aa", "light sky blue": "#87cefa", "light slate gray": "#778899", "light slate grey": "#778899", "light steel blue": "#b0c4de", "light yellow": "#ffffe0", "lime": "#00ff00", "lime green": "#32cd32", "linen": "#faf0e6", "magenta": "#ff00ff", "maroon": "#800000", "medium aqua marine": "#66cdaa", "medium blue": "#0000cd", "medium orchid": "#ba55d3", "medium purple": "#9370db", "medium sea green": "#3cb371", "medium slate blue": "#7b68ee", "medium spring green": "#00fa9a", "medium turquoise": "#48d1cc", "medium violet red": "#c71585", "midnight blue": "#191970", "mint cream": "#f5fffa", "misty rose": "#ffe4e1", "moccasin": "#ffe4b5", "navajo white": "#ffdead", "navy": "#000080", "old lace": "#fdf5e6", "olive": "#808000", "olive drab": "#6b8e23", "orange": "#ffa500", "orange red": "#ff4500", "orchid": "#da70d6", "pale golden rod": "#eee8aa", "pale green": "#98fb98", "pale turquoise": "#afeeee", "pale violet red": "#db7093", "papaya whip": "#ffefd5", "peach puff": "#ffdab9", "peru": "#cd853f", "pink": "#ffc0cb", "plum": "#dda0dd", "powder blue": "#b0e0e6", "purple": "#800080", "rebecca purple": "#663399", "red": "#ff0000", "rosy brown": "#bc8f8f", "royal blue": "#4169e1", "saddle brown": "#8b4513", "salmon": "#fa8072", "sandy brown": "#f4a460", "sea green": "#2e8b57", "sea shell": "#fff5ee", "sienna": "#a0522d", "silver": "#c0c0c0", "sky blue": "#87ceeb", "slate blue": "#6a5acd", "slate gray": "#708090", "slate grey": "#708090", "snow": "#fffafa", "spring green": "#00ff7f", "steel blue": "#4682b4", "tan": "#d2b48c", "teal": "#008080", "thistle": "#d8bfd8", "tomato": "#ff6347", "turquoise": "#40e0d0", "violet": "#ee82ee", "wheat": "#f5deb3", "white": "#ffffff", "white smoke": "#f5f5f5", "yellow": "#ffff00", "yellow green": "#9acd32", "md-red-50": "#ffebee", "md-red-100": "#ffcdd2", "md-red-200": "#ef9a9a", "md-red-300": "#e57373", "md-red-400": "#ef5350", "md-red-500": "#f44336", "md-red-600": "#e53935", "md-red-700": "#d32f2f", "md-red-800": "#c62828", "md-red-900": "#b71c1c", "md-red-A100": "#ff8a80", "md-red-A200": "#ff5252", "md-red-A400": "#ff1744", "md-red-A700": "#d50000", "md-pink-50": "#fce4ec", "md-pink-100": "#f8bbd0", "md-pink-200": "#f48fb1", "md-pink-300": "#f06292", "md-pink-400": "#ec407a", "md-pink-500": "#e91e63", "md-pink-600": "#d81b60", "md-pink-700": "#c2185b", "md-pink-800": "#ad1457", "md-pink-900": "#880e4f", "md-pink-A100": "#ff80ab", "md-pink-A200": "#ff4081", "md-pink-A400": "#f50057", "md-pink-A700": "#c51162", "md-purple-50": "#f3e5f5", "md-purple-100": "#e1bee7", "md-purple-200": "#ce93d8", "md-purple-300": "#ba68c8", "md-purple-400": "#ab47bc", "md-purple-500": "#9c27b0", "md-purple-600": "#8e24aa", "md-purple-700": "#7b1fa2", "md-purple-800": "#6a1b9a", "md-purple-900": "#4a148c", "md-purple-A100": "#ea80fc", "md-purple-A200": "#e040fb", "md-purple-A400": "#d500f9", "md-purple-A700": "#a0f", "md-deep-purple-50": "#ede7f6", "md-deep-purple-100": "#d1c4e9", "md-deep-purple-200": "#b39ddb", "md-deep-purple-300": "#9575cd", "md-deep-purple-400": "#7e57c2", "md-deep-purple-500": "#673ab7", "md-deep-purple-600": "#5e35b1", "md-deep-purple-700": "#512da8", "md-deep-purple-800": "#4527a0", "md-deep-purple-900": "#311b92", "md-deep-purple-A100": "#b388ff", "md-deep-purple-A200": "#7c4dff", "md-deep-purple-A400": "#651fff", "md-deep-purple-A700": "#6200ea", "md-indigo-50": "#e8eaf6", "md-indigo-100": "#c5cae9", "md-indigo-200": "#9fa8da", "md-indigo-300": "#7986cb", "md-indigo-400": "#5c6bc0", "md-indigo-500": "#3f51b5", "md-indigo-600": "#3949ab", "md-indigo-700": "#303f9f", "md-indigo-800": "#283593", "md-indigo-900": "#1a237e", "md-indigo-A100": "#8c9eff", "md-indigo-A200": "#536dfe", "md-indigo-A400": "#3d5afe", "md-indigo-A700": "#304ffe", "md-blue-50": "#e3f2fd", "md-blue-100": "#bbdefb", "md-blue-200": "#90caf9", "md-blue-300": "#64b5f6", "md-blue-400": "#42a5f5", "md-blue-500": "#2196f3", "md-blue-600": "#1e88e5", "md-blue-700": "#1976d2", "md-blue-800": "#1565c0", "md-blue-900": "#0d47a1", "md-blue-A100": "#82b1ff", "md-blue-A200": "#448aff", "md-blue-A400": "#2979ff", "md-blue-A700": "#2962ff", "md-light-blue-50": "#e1f5fe", "md-light-blue-100": "#b3e5fc", "md-light-blue-200": "#81d4fa", "md-light-blue-300": "#4fc3f7", "md-light-blue-400": "#29b6f6", "md-light-blue-500": "#03a9f4", "md-light-blue-600": "#039be5", "md-light-blue-700": "#0288d1", "md-light-blue-800": "#0277bd", "md-light-blue-900": "#01579b", "md-light-blue-A100": "#80d8ff", "md-light-blue-A200": "#40c4ff", "md-light-blue-A400": "#00b0ff", "md-light-blue-A700": "#0091ea", "md-cyan-50": "#e0f7fa", "md-cyan-100": "#b2ebf2", "md-cyan-200": "#80deea", "md-cyan-300": "#4dd0e1", "md-cyan-400": "#26c6da", "md-cyan-500": "#00bcd4", "md-cyan-600": "#00acc1", "md-cyan-700": "#0097a7", "md-cyan-800": "#00838f", "md-cyan-900": "#006064", "md-cyan-A100": "#84ffff", "md-cyan-A200": "#18ffff", "md-cyan-A400": "#00e5ff", "md-cyan-A700": "#00b8d4", "md-teal-50": "#e0f2f1", "md-teal-100": "#b2dfdb", "md-teal-200": "#80cbc4", "md-teal-300": "#4db6ac", "md-teal-400": "#26a69a", "md-teal-500": "#009688", "md-teal-600": "#00897b", "md-teal-700": "#00796b", "md-teal-800": "#00695c", "md-teal-900": "#004d40", "md-teal-A100": "#a7ffeb", "md-teal-A200": "#64ffda", "md-teal-A400": "#1de9b6", "md-teal-A700": "#00bfa5", "md-green-50": "#e8f5e9", "md-green-100": "#c8e6c9", "md-green-200": "#a5d6a7", "md-green-300": "#81c784", "md-green-400": "#66bb6a", "md-green-500": "#4caf50", "md-green-600": "#43a047", "md-green-700": "#388e3c", "md-green-800": "#2e7d32", "md-green-900": "#1b5e20", "md-green-A100": "#b9f6ca", "md-green-A200": "#69f0ae", "md-green-A400": "#00e676", "md-green-A700": "#00c853", "md-light-green-50": "#f1f8e9", "md-light-green-100": "#dcedc8", "md-light-green-200": "#c5e1a5", "md-light-green-300": "#aed581", "md-light-green-400": "#9ccc65", "md-light-green-500": "#8bc34a", "md-light-green-600": "#7cb342", "md-light-green-700": "#689f38", "md-light-green-800": "#558b2f", "md-light-green-900": "#33691e", "md-light-green-A100": "#ccff90", "md-light-green-A200": "#b2ff59", "md-light-green-A400": "#76ff03", "md-light-green-A700": "#64dd17", "md-lime-50": "#f9fbe7", "md-lime-100": "#f0f4c3", "md-lime-200": "#e6ee9c", "md-lime-300": "#dce775", "md-lime-400": "#d4e157", "md-lime-500": "#cddc39", "md-lime-600": "#c0ca33", "md-lime-700": "#afb42b", "md-lime-800": "#9e9d24", "md-lime-900": "#827717", "md-lime-A100": "#f4ff81", "md-lime-A200": "#eeff41", "md-lime-A400": "#c6ff00", "md-lime-A700": "#aeea00", "md-yellow-50": "#fffde7", "md-yellow-100": "#fff9c4", "md-yellow-200": "#fff59d", "md-yellow-300": "#fff176", "md-yellow-400": "#ffee58", "md-yellow-500": "#ffeb3b", "md-yellow-600": "#fdd835", "md-yellow-700": "#fbc02d", "md-yellow-800": "#f9a825", "md-yellow-900": "#f57f17", "md-yellow-A100": "#ffff8d", "md-yellow-A200": "#ff0", "md-yellow-A400": "#ffea00", "md-yellow-A700": "#ffd600", "md-amber-50": "#fff8e1", "md-amber-100": "#ffecb3", "md-amber-200": "#ffe082", "md-amber-300": "#ffd54f", "md-amber-400": "#ffca28", "md-amber-500": "#ffc107", "md-amber-600": "#ffb300", "md-amber-700": "#ffa000", "md-amber-800": "#ff8f00", "md-amber-900": "#ff6f00", "md-amber-A100": "#ffe57f", "md-amber-A200": "#ffd740", "md-amber-A400": "#ffc400", "md-amber-A700": "#ffab00", "md-orange-50": "#fff3e0", "md-orange-100": "#ffe0b2", "md-orange-200": "#ffcc80", "md-orange-300": "#ffb74d", "md-orange-400": "#ffa726", "md-orange-500": "#ff9800", "md-orange-600": "#fb8c00", "md-orange-700": "#f57c00", "md-orange-800": "#ef6c00", "md-orange-900": "#e65100", "md-orange-A100": "#ffd180", "md-orange-A200": "#ffab40", "md-orange-A400": "#ff9100", "md-orange-A700": "#ff6d00", "md-deep-orange-50": "#fbe9e7", "md-deep-orange-100": "#ffccbc", "md-deep-orange-200": "#ffab91", "md-deep-orange-300": "#ff8a65", "md-deep-orange-400": "#ff7043", "md-deep-orange-500": "#ff5722", "md-deep-orange-600": "#f4511e", "md-deep-orange-700": "#e64a19", "md-deep-orange-800": "#d84315", "md-deep-orange-900": "#bf360c", "md-deep-orange-A100": "#ff9e80", "md-deep-orange-A200": "#ff6e40", "md-deep-orange-A400": "#ff3d00", "md-deep-orange-A700": "#dd2c00", "md-brown-50": "#efebe9", "md-brown-100": "#d7ccc8", "md-brown-200": "#bcaaa4", "md-brown-300": "#a1887f", "md-brown-400": "#8d6e63", "md-brown-500": "#795548", "md-brown-600": "#6d4c41", "md-brown-700": "#5d4037", "md-brown-800": "#4e342e", "md-brown-900": "#3e2723", "md-grey-50": "#fafafa", "md-grey-100": "#f5f5f5", "md-grey-200": "#eee", "md-grey-300": "#e0e0e0", "md-grey-400": "#bdbdbd", "md-grey-500": "#9e9e9e", "md-grey-600": "#757575", "md-grey-700": "#616161", "md-grey-800": "#424242", "md-grey-900": "#212121", "md-blue-grey-50": "#eceff1", "md-blue-grey-100": "#cfd8dc", "md-blue-grey-200": "#b0bec5", "md-blue-grey-300": "#90a4ae", "md-blue-grey-400": "#78909c", "md-blue-grey-500": "#607d8b", "md-blue-grey-600": "#546e7a", "md-blue-grey-700": "#455a64", "md-blue-grey-800": "#37474f", "md-blue-grey-900": "#263238"};

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
                    if (!color.includes("md-")) {
                        item.innerHTML = '<i class="color-square" style="background: ' + colorsReversed[color.trim()] + '"></i> ' + color;
                    } else {
                        <#noparse>
                        let prependedAndTrimmed = "--" + color.trim();
                        let colorVar = `var(${prependedAndTrimmed})`;
                        </#noparse>
                        item.innerHTML = '<i class="color-square" style="background: ' + colorVar + '"></i> ' + color;
                    }
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
                $warningColorSelect.val("#ffc108");
                $warningColorSelect.trigger('change');
                $("#warningColor-colorBox").css("background", "#ffc108");

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
                    <@s.hidden name="removeLogo" id="removeLogo" value="false" required="true" />

                    <!-- App Logo -->
                    <div id="logofields" class="row g-3">
                        <div class="col-lg-6">
                            <@s.label for="file" class="form-label text-smaller" value="IPT logo" />
                            <@s.file cssClass="form-control my-1" name="file"/>
                            <a href="#" class="button btn btn-outline-gbif-primary" id="buttonUpload">
                                <@s.text name="button.upload"/>
                            </a>
                            <a href="#" class="button btn btn-outline-gbif-danger" id="buttonRemove">
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
                    <div class="row g-3 mt-5">
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
