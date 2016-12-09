(function(exports) {

  /*
   * tile.stamen.js v1.3.0.
   * Stamen Design is the rights holder of the OSM-based 'terrain' tile set, which the IPT uses.
   * This tile set is available for free unlimited use under the CC BY 3.0 license.
   * This tile set is compatible with Leaflet and features hill shading, natural vegetation colors, advanced labeling
   * and linework generalization of dual-carriageway roads. For more information about Stamen Design and this tile set
   * see:
   * <http://maps.stamen.com/>
   * <https://en.wikipedia.org/wiki/Stamen_Design>
   */

  var SUBDOMAINS = "a. b. c. d.".split(" "),
    MAKE_PROVIDER = function(layer, type, minZoom, maxZoom) {
      return {
        // use protocol-agnostic URLs
        "url": ["//stamen-tiles-{S}a.ssl.fastly.net/", layer, "/{Z}/{X}/{Y}.", type].join(""),
        "type":         type,
        "subdomains":   SUBDOMAINS.slice(),
        "minZoom":      minZoom,
        "maxZoom":      maxZoom,
        "attribution":  [
          'Map tiles by <a href="http://stamen.com/">Stamen Design</a>, ',
          'under <a href="http://creativecommons.org/licenses/by/3.0">CC BY 3.0</a>. ',
          'Data by <a href="http://openstreetmap.org/">OpenStreetMap</a>, ',
          'under <a href="http://creativecommons.org/licenses/by-sa/3.0">CC BY SA</a>.'
        ].join("")
      };
    },
    PROVIDERS =  {
      "terrain":      MAKE_PROVIDER("terrain", "png", 0, 18)
    };

  var odbl = [
    "terrain"
  ];

  for (var i = 0; i < odbl.length; i++) {
    var key = odbl[i];

    PROVIDERS[key].retina = true;
    PROVIDERS[key].attribution = [
      'Map tiles by <a href="http://stamen.com/">Stamen Design</a>, ',
      'under <a href="http://creativecommons.org/licenses/by/3.0">CC BY 3.0</a>. ',
      'Data by <a href="http://openstreetmap.org">OpenStreetMap</a>, ',
      'under <a href="http://www.openstreetmap.org/copyright">ODbL</a>.'
    ].join("");
  }

  /*
   * Export stamen.tile to the provided namespace.
   */
  exports.stamen = exports.stamen || {};
  exports.stamen.tile = exports.stamen.tile || {};
  exports.stamen.tile.providers = PROVIDERS;
  exports.stamen.tile.getProvider = getProvider;

  /*
   * Get the named provider, or throw an exception if it doesn't exist.
   */
  function getProvider(name) {
    if (name in PROVIDERS) {
      var provider = PROVIDERS[name];

      if (provider.deprecated && console && console.warn) {
        console.warn(name + " is a deprecated style; it will be redirected to its replacement. For performance improvements, please change your reference.");
      }

      return provider;
    } else {
      throw 'No such provider (' + name + ')';
    }
  }

  /*
   * StamenTileLayer for Leaflet
   * <http://leaflet.cloudmade.com/>
   *
   * Tested with version 0.3 and 0.4, but should work on all 0.x releases.
   */
  if (typeof L === "object") {
    L.StamenTileLayer = L.TileLayer.extend({
      initialize: function(name, options) {
        var provider = getProvider(name),
          url = provider.url.replace(/({[A-Z]})/g, function(s) {
            return s.toLowerCase();
          }),
          opts = L.Util.extend({}, options, {
            "minZoom":      provider.minZoom,
            "maxZoom":      provider.maxZoom,
            "subdomains":   provider.subdomains,
            "scheme":       "xyz",
            "attribution":  provider.attribution,
            sa_id:          name
          });
        L.TileLayer.prototype.initialize.call(this, url, opts);
      }
    });

    /*
     * Factory function for consistency with Leaflet conventions
     */
    L.stamenTileLayer = function (options, source) {
      return new L.StamenTileLayer(options, source);
    };
  }

})(typeof exports === "undefined" ? this : exports);
