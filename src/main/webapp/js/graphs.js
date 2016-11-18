/*
 * To draw data record line chart on resource homepage.
 */
function drawGreyBars(ob, scale) {
  jQuery("ul li", ob).each(function() {
    var bar = $(this).find("div.grey_bar");
    var value = bar.html();
    $(this).append("<div class='value_label'>" + value + "</div>");
    bar.empty();
    bar.css({width: value * scale});
    bar.show();
  });
}

$.fn.bindGreyBars = function(scale) {
  drawGreyBars($(this), scale);
}