(function ($) {

    jQuery.fn.jConfirmAction = function (options) {

        // Some jConfirmAction options (limited to customize language) :
        // question : a text for your question.
        // yesAnswer : a text for Yes answer.
        // cancelAnswer : a text for Cancel/No answer.
        // checkboxText: a text for the checkbox needed to confirm for Yes answer (optional)
        // summary: a textarea to enter a summary for the submit
        var theOptions = jQuery.extend({
            titleQuestion: "Are you sure?",
            question: "Are you sure?",
            yesAnswer: "Yes",
            cancelAnswer: "Cancel",
            checkboxText: undefined,
            summary: undefined,
            buttonType: "danger"
        }, options);
        return this.each(function () {

            $(this).bind('click', function (e) {
                var submitBtn = $(this);

                if ($(this).attr("jconfirmed")) {
                    submitBtn.removeAttr("jconfirmed");
                } else {
                    e.preventDefault();
                    thisHref = $(this).attr('href');

                    // get empty modal window
                    var dialogWindow = $("#dialog-confirm");

                    // prepare html content for modal window
                    var content = '<div class="modal-dialog modal-confirm modal-dialog-centered">';
                    content += '<div class="modal-content">';

                    // header
                    content += '<div class="modal-header flex-column">';
                    content += '<div class="icon-box"><i class="confirm-danger-icon">!</i></div>'
                    content += '<h5 class="modal-title w-100" id="staticBackdropLabel">' + theOptions.titleQuestion + '</h5>';
                    content += '<button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">×</button>'
                    content += '</div>';

                    // body
                    content += '<div class="modal-body">';

                    // checkbox if present otherwise question
                    if (theOptions.checkboxText !== undefined) {
                        content += '<div class="form-check mb-2">\n' +
                            '<input id="checkbox-confirm" class="form-check-input" type="checkbox" >\n' +
                            '<label class="form-check-label" for="checkbox-confirm">\n' +
                            theOptions.checkboxText +
                            '</label>\n' +
                            '</div>' + theOptions.question;
                    } else {
                        content += '<p>' + theOptions.question + '</p>';
                    }
                    // append summary if present
                    if (theOptions.summary !== undefined) {
                        content += '<div class="mt-3"><textarea id="dialogSummary" rows="5" class="dialog-summary form-control" placeholder="' + theOptions.summary + '"></textarea></div>';
                    }
                    content += '</div>'

                    // footer
                    content += '<div class="modal-footer justify-content-center">'
                    content += '<button id="cancel-button" type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">' + theOptions.cancelAnswer+ '</button>';
                    content += '<button id="yes-button" type="button" class="btn btn-outline-gbif-' + theOptions.buttonType + '">' + theOptions.yesAnswer + '</button>';
                    content += '</div>';

                    content += '</div>';
                    content += '</div>';

                    // add content to window
                    dialogWindow.html(content);

                    // hide yes button if checkbox present
                    if (theOptions.checkboxText !== undefined) {
                        $("yes-button").hide();
                    }

                    $("#yes-button").on("click", function () {
                        if (thisHref != null) {
                            window.location = thisHref;
                        } else {
                            submitBtn.attr("jconfirmed", true);
                            var selected = $("#dialogSummary").val();
                            $("#summary").empty().append(selected);
                            submitBtn.click();
                        }
                    });

                    $("#cancel-button").on("click", function() {
                        submitBtn.removeAttr("jconfirmed");
                    });

                    // show yes button if checkbox is selected
                    $('#checkbox-confirm').on("click", function () {
                        if ($('#checkbox-confirm').prop('checked')) {
                            $('#yes-button').show();
                        } else {
                            $('#yes-button').hide();
                        }
                    });

                    dialogWindow.modal('show');
                }
            });

        });
    }

})(jQuery);
