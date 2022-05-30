/*
 * To draw data record line chart on resource homepage.
 */
function drawRecordBars(ob, scale) {
  jQuery("#record_graph_inner .row", ob).each(function() {
    var bar = $(this).find("div.color-bar");
    var value = bar.html();
    $(this).append("<div class='col-lg-1 ps-0 ms-lg-2 value_label'>" + value + "</div>");
    bar.empty();
    bar.css({width: value * scale});
    bar.show();
  });
}

$.fn.bindRecordBars = function(scale) {
  drawRecordBars($(this), scale);
}
