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

function displayProcessing() {
    var processingDiv = $(".dataTables_processing");
    processingDiv.show();
    processingDiv.css("z-index", "1001");
    var div= document.createElement("div");
    div.className += "overlay";
    document.body.appendChild(div);
}

function hideProcessing() {
    $(".dataTables_processing").hide();
    $(".overlay").remove();
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

function makeSureResourceParameterIsPresentInURL(resourceShortname) {
    const currentUrl = window.location.href;
    const url = new URL(currentUrl);

    const searchParams = url.searchParams;

    if (!searchParams.has('r')) {
        searchParams.set('r', resourceShortname);
        window.history.replaceState({}, '', url.toString());
    }
}

function formatFileSize(longSize, decimalPos = 2, strLocale = 'en-GB', inseparableDelimiter = false) {
    const delimiter = inseparableDelimiter ? '\u00A0' : ' '; // &nbsp;

    const formatter = new Intl.NumberFormat(strLocale, {
        maximumFractionDigits: decimalPos >= 0 ? decimalPos : undefined
    });

    let val = longSize / 1_000_000_000;
    if (val > 1) {
        return formatter.format(val) + delimiter + 'GB';
    }

    val = longSize / 1_000_000;
    if (val > 1) {
        return formatter.format(val) + delimiter + 'MB';
    }

    val = longSize / 1_000;
    if (val > 1) {
        return formatter.format(val) + delimiter + 'KB';
    }

    return formatter.format(longSize) + delimiter + 'bytes';
}
