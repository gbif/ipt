/*
 * To draw data record line chart on resource homepage.
 */
function drawGreyBars(ob, scale) {
  var total = $("#coreCount");
  var totalValue = total.html();

  jQuery("ul li", ob).each(function() {
    var bar = $(this).find("div.grey_bar");
    var value = bar.html();
    var coverage = Math.floor(value / totalValue * 100) / 100;
    $(this).append("<div class='value_label'>" + coverage + "x (" + value + ")" + "</div>");
    bar.empty();
    bar.css({width: value * scale});
    bar.show();
  });
}

$.fn.bindGreyBars = function(scale) {
  drawGreyBars($(this), scale);
}