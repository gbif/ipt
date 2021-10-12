var timeout = 500;
var closetimer = 0;
var ddmenuitem = 0;
$(document).ready(function () {
    $("#showMore").on("click", function (e) {
        e.preventDefault();
        var $parentTarget = $(e.target).parent();
        var showLess = $parentTarget.html();
        $parentTarget.html($parentTarget.next("#hiddenContent").html());
        $parentTarget.next("#hiddenContent").html(showLess);
    });
    $("#showLess").on("click", function (e) {
        e.preventDefault();
        var $parentTarget = $(e.target).parent();
        var showMore = $parentTarget.html();
        $parentTarget.html($parentTarget.next("#hiddenContent").html());
        $parentTarget.next("#hiddenContent").html(showMore);
    });
});

function str(x) {
    if (x != null && x.length > 0) return x;
    return '';
}

function isTrueOrNull(x) {
    return x == null || x === true;

}

function initForm() {
    $("input.form-reset").one("click", function () {
        $(this).val("");
    });
}

function initCalendar(context) {
    if (context === undefined) context = "";

    $(context + " .calendarInfo").each(function (i) {
        $(this).find("input").datepicker({
            format: 'yyyy-mm-dd',
            autoclose: true,
            orientation: "bottom"
        });
    });
}

function initInfoPopovers(item) {
    var popoverTriggerList = [].slice.call(item.querySelectorAll('[data-bs-toggle="popover"]'))
    var popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl)
    })
    try {
        var popover = new bootstrap.Popover(document.querySelector('.popover-dismiss'), {
            trigger: 'focus'
        })
    } catch (TypeError) {
    }
}

function jsddm_open() {
    jsddm_canceltimer();
    jsddm_close();
    ddmenuitem = $(this).find('ul').css('visibility', 'visible');
}

function jsddm_close() {
    if (ddmenuitem) ddmenuitem.css('visibility', 'hidden');
}

function jsddm_timer() {
    closetimer = window.setTimeout(jsddm_close, timeout);
}

function jsddm_canceltimer() {
    if (closetimer) {
        window.clearTimeout(closetimer);
        closetimer = null;
    }
}

function initMenu() {
    // Simple Drop-Down Menu
    // taken from http://javascript-array.com/scripts/jquery_simple_drop_down_menu/
    $('#language-menu > li').bind('mouseover', jsddm_open);
    $('#language-menu > li').bind('mouseout', jsddm_timer);
}
