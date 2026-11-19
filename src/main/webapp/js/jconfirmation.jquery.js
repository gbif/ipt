(function ($) {

    // $(".x").jConfirmAction({...}): asks before a link or button does its job. Kept for the existing calls;
    // the dialog itself is IPT.confirm() (js/dialog.js), so new code calls that directly.
    // $.jConfirmActionOn(".x", {...}) does the same for every .x on the page, also rows a table draws later.
    //
    // titleQuestion: the dialog's title, the actual question; {0} in it is the clicked element's data-name
    // question: one or two sentences of consequence
    // yesAnswer / cancelAnswer: the button labels, verbs
    // checkboxText: a box that must be ticked before the main button works (optional)
    // typeToConfirm: a word that must be typed before the main button works (optional)
    // summary: placeholder of a textarea whose text is copied into #summary (optional)
    // select: a choice that must be made, copied to an input (optional),
    //         e.g. {label: "Type", placeholder: "Select", options: [{value: "A", label: "a"}], target: "#input"}
    // buttonType: "danger" for destructive actions (red button, red icon), anything else for a green button
    // tone: overrides the colour taken from buttonType: "danger", "warn", "info" or "ok"
    // icon: the Material icon in the tile (default: the tone's), e.g. "upload" for publishing
    // processing: show the spinner after confirming a form submit
    function defaults(options) {
        return jQuery.extend({
            titleQuestion: undefined,
            question: undefined,
            yesAnswer: undefined,
            cancelAnswer: undefined,
            checkboxText: undefined,
            typeToConfirm: undefined,
            summary: undefined,
            select: undefined,
            buttonType: "danger",
            tone: undefined,
            icon: undefined,
            processing: false
        }, options);
    }

    // the click handler: asks first, then lets the element do its job (follow the link, submit the form)
    function handler(o) {
        return function (e) {
            var submitBtn = $(this);
            var thisHref = submitBtn.attr("href");

            // a label with a hidden submit inside (users and organisations tables): the click the label passes on
            // to its input bubbles back here; that one must go through
            if (e.target !== this && $(e.target).is("input, button")) {
                return;
            }
            if (submitBtn.attr("jconfirmed")) {
                submitBtn.removeAttr("jconfirmed");
                return;
            }
            e.preventDefault();

            // the clicked element's name, as text: the title is read as HTML
            var name = String(submitBtn.attr("data-name") || "").replace(/[&<>]/g, function (c) {
                return {"&": "&amp;", "<": "&lt;", ">": "&gt;"}[c];
            });
            IPT.confirm({
                tone: o.tone || (o.buttonType === "danger" ? "danger" : "info"),
                icon: o.icon,
                title: o.titleQuestion && o.titleQuestion.replace("{0}", function () { return name; }),
                text: o.question,
                confirm: o.yesAnswer,
                cancel: o.cancelAnswer,
                agreement: o.checkboxText,
                typeToConfirm: o.typeToConfirm,
                summary: o.summary,
                select: o.select
            }).then(function (result) {
                if (!result) {
                    return;
                }
                if (thisHref != null && submitBtn.is("[data-post]") && window.IPT_CSRF) {
                    // the link changes state: send it as a POST with the CSRF token (see csrf.js)
                    window.IPT_CSRF.postTo(thisHref);
                } else if (thisHref != null) {
                    window.location = thisHref;
                } else {
                    if (o.summary !== undefined) {
                        $("#summary").val(result.summary);
                    }
                    if (o.select !== undefined) {
                        $(o.select.target).val(result.select);
                    }
                    submitBtn.attr("jconfirmed", true);
                    submitBtn[0].click();

                    if (o.processing) {
                        var processingDiv = $(".dataTables_processing");
                        processingDiv.show();
                        processingDiv.css("z-index", "1001");
                        var div = document.createElement("div");
                        div.className = "overlay";
                        document.body.appendChild(div);
                    }
                }
            });
        };
    }

    jQuery.fn.jConfirmAction = function (options) {
        var onClick = handler(defaults(options));
        return this.each(function () {
            $(this).on("click", onClick);
        });
    };

    jQuery.jConfirmActionOn = function (selector, options) {
        $(document).on("click", selector, handler(defaults(options)));
    };

})(jQuery);
