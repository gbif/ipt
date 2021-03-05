(function ($) {

    jQuery.fn.jConfirmActionBootstrap = function (options) {

        // Some jConfirmAction options (limited to customize language) :
        // question : a text for your question.
        // yesAnswer : a text for Yes answer.
        // cancelAnswer : a text for Cancel/No answer.
        // checkboxText: a text for the checkbox needed to confirm for Yes answer (optional)
        // summary: a textarea to enter a summary for the submit
        var theOptions = jQuery.extend({
            question: "Are You Sure ?",
            yesAnswer: "Yes",
            cancelAnswer: "Cancel",
            checkboxText: undefined,
            summary: undefined
        }, options);
        return this.each(function () {

            var content =
                '<div class="modal-dialog modal-dialog-centered">'
                + '<div class="modal-content">'
                + '<div class="modal-header">'
                + '<h5 class="modal-title text-success" id="staticBackdropLabel">'
                + 'Confirmation'
                + '</h5>'
                + '<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>'
                + '</div>'
                + '<div class="modal-body">'
                + '<span>'
                + theOptions.question
                + '</span>'
                + '<div class="modal-footer">'
                + '<button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">'
                + theOptions.cancelAnswer
                + '</button>'
                + '<button type="button" class="btn btn-outline-success">'
                + theOptions.yesAnswer
                + '</button>'
                + '</div>'
                + '</div>'
                + '</div>'
                + '</div>'

            $(this).bind('click', function (e) {
                // alert("Hello! I am an alert box!!")
                $('#dialog-confirm').html(content);

                var myModal = new bootstrap.Modal(document.getElementById('dialog-confirm'), {
                    keyboard: false
                })
                myModal.show()
            });

        });
    }

})(jQuery);
