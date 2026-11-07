/*
 * Sends the CSRF token of the session (meta tag "ipt-csrf-token", see CsrfInterceptor) with every request that
 * changes state:
 * - adds a hidden "iptCsrfToken" field to same-origin POST forms when they are submitted, by the user or by scripts
 * - adds the "X-CSRF-Token" header to same-origin jQuery AJAX requests other than GET/HEAD/OPTIONS
 * - turns links marked with data-post into POST requests: <a href="delete-source.do?r=..." data-post>
 * Other scripts can use window.IPT_CSRF, e.g. to set the header on a plain XMLHttpRequest.
 * Without a logged-in user there's no meta tag and nothing is added.
 */
(function () {
    var PARAM = "iptCsrfToken";
    var HEADER = "X-CSRF-Token";
    var SAFE_METHODS = /^(GET|HEAD|OPTIONS|TRACE)$/i;

    function token() {
        var meta = document.querySelector('meta[name="ipt-csrf-token"]');
        return meta ? meta.getAttribute("content") : null;
    }

    function isSameOrigin(url) {
        try {
            return new URL(url, document.baseURI).origin === window.location.origin;
        } catch (e) {
            return false;
        }
    }

    // attributes rather than form.method/form.action: inputs named "method" or "action" shadow those properties
    function addToken(form, submitter) {
        var t = token();
        if (!t || !(form instanceof HTMLFormElement)) {
            return;
        }
        var method = (submitter && submitter.getAttribute("formmethod")) || form.getAttribute("method") || "get";
        var action = (submitter && submitter.getAttribute("formaction")) || form.getAttribute("action") || window.location.href;
        if (method.toLowerCase() !== "post" || !isSameOrigin(action)) {
            return;
        }
        var input = form.querySelector('input[name="' + PARAM + '"]');
        if (!input) {
            input = document.createElement("input");
            input.type = "hidden";
            input.name = PARAM;
            form.appendChild(input);
        }
        input.value = t;
    }

    function postTo(url) {
        var form = document.createElement("form");
        form.setAttribute("method", "post");
        form.setAttribute("action", url);
        form.style.display = "none";
        document.body.appendChild(form);
        form.submit();
    }

    // forms submitted by the user or with requestSubmit()
    document.addEventListener("submit", function (e) {
        addToken(e.target, e.submitter);
    }, true);

    // form.submit() doesn't fire the submit event
    var nativeSubmit = HTMLFormElement.prototype.submit;
    HTMLFormElement.prototype.submit = function () {
        addToken(this);
        return nativeSubmit.apply(this, arguments);
    };

    // links that change state; handlers that ask for confirmation first (jConfirmAction) prevent the default and
    // call IPT_CSRF.postTo themselves once confirmed
    document.addEventListener("click", function (e) {
        var link = e.target instanceof Element ? e.target.closest("a[data-post]") : null;
        if (!link || e.defaultPrevented || e.button !== 0) {
            return;
        }
        e.preventDefault();
        postTo(link.href);
    });

    if (window.jQuery) {
        window.jQuery.ajaxPrefilter(function (options, originalOptions, xhr) {
            var t = token();
            if (t && !options.crossDomain && !SAFE_METHODS.test(options.type || "GET")) {
                xhr.setRequestHeader(HEADER, t);
            }
        });
    }

    window.IPT_CSRF = {
        param: PARAM,
        header: HEADER,
        token: token,
        postTo: postTo
    };
})();
