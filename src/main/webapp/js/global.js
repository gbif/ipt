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

    // TODO ripple does not work for inputs
    // document.querySelectorAll('.btn').forEach(setRippleElement);
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

function setRippleElement(e) {
    const dot = document.createElement('SPAN');
    const largestSide = Math.sqrt(
        Math.pow(e.offsetWidth, 2) +
        Math.pow(e.offsetHeight, 2)
    );
    const dotSize = Math.ceil((largestSide * 2) / 100);
    var rippleColor = 'rgba(78, 86, 95, 0.5)';

    if (e.classList.contains('btn-outline-gbif-primary')) {
        rippleColor = 'rgba(97, 168, 97, 0.5)';
    } else if (e.classList.contains("btn-outline-gbif-danger")) {
        rippleColor = 'rgba(227, 99, 112, 0.5)';
    }

    dot.style = `
                position: absolute;
                left: 0;
                top: 0;
                width: ${dotSize}px;
                height: ${dotSize}px;
                z-index: 3;
                border-radius: 50%;
                background: ${rippleColor};
                transform: translate(-50%);
                opacity: 0.5;
                animation: ripple 1s ease-out forwards;
            `;

    e.style.position = 'relative';
    e.style.overflow = 'hidden';

    e.addEventListener('click', ({pageX, pageY, currentTarget}) => {
        const x = ((pageX - currentTarget.offsetLeft) * 100) / currentTarget.offsetWidth;
        const y = ((pageY - currentTarget.offsetTop) * 100) / currentTarget.offsetHeight;

        e.appendChild(dot);

        dot.style.left = x + '%';
        dot.style.top = y + '%';
    });
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
