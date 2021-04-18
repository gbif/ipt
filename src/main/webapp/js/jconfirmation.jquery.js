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
            summary: undefined
        }, options);
        return this.each(function () {

            $(this).bind('click', function (e) {
                var submitBtn = $(this);
                if ($(this).attr("jconfirmed")) {
                    submitBtn.removeAttr("jconfirmed");
                } else {
                    e.preventDefault();
                    thisHref = $(this).attr('href');

                    var btns = {};
                    btns[theOptions.yesAnswer] = function () {
                        $(this).dialog("close");
                        if (thisHref != null) {
                            window.location = thisHref;
                        } else {
                            submitBtn.attr("jconfirmed", true);
                            var selected = $("#dialogSummary").val();
                            $("#summary").empty().append(selected);
                            submitBtn.click();
                        }
                    };
                    btns[theOptions.cancelAnswer] = function () {
                        $(this).dialog("close");
                        submitBtn.removeAttr("jconfirmed");
                    };

                    var content = '<p>' + theOptions.question + '</p>';

                    if (theOptions.checkboxText !== undefined) {
                        content = '<div class="form-check mb-2">\n' +
                            '<input class="form-check-input" type="checkbox" id="cbox">\n' +
                            '<label class="form-check-label" for="cbox">\n' +
                            theOptions.checkboxText +
                            '</label>\n' +
                            '</div>' + theOptions.question;
                    }

                    if (theOptions.summary !== undefined) {
                        content += '<div class="mt-3"><textarea id="dialogSummary" rows="5" class="dialog-summary form-control" placeholder="' + theOptions.summary + '"/></div>';
                    }

                    $('#dialog-confirm').html(content);

                    $('#cbox').click(function () {
                        if ($('#cbox').prop('checked')) {
                            $('.ui-dialog-buttonset button:first-child').show();
                        } else {
                            $('.ui-dialog-buttonset button:first-child').hide();
                        }

                    });

                    $('#dialog-confirm').dialog({
                        resizable: false,
                        modal: true,
                        buttons: btns,
                        // modal window fixed positioning to prevent page elements from changing position
                        create: function (event, ui) {
                            $(event.target).parent().css('position', 'fixed');
                        },
                        resizeStop: function (event, ui) {
                            var position = [(Math.floor(ui.position.left) - $(window).scrollLeft()),
                                (Math.floor(ui.position.top) - $(window).scrollTop())];
                            $(event.target).parent().css('position', 'fixed');
                            $(dlg).dialog('option', 'position', position);
                        }
                    });

                    // add bootstrap design to modal's title, content and footer
                    var dialog = $('.ui-dialog');
                    dialog.addClass('modal-content');
                    dialog.find('.ui-dialog-titlebar').addClass('modal-header').find('.ui-dialog-titlebar-close').addClass('btn-close');
                    dialog.find('.ui-dialog-title').addClass('modal-title fw-bold').html(theOptions.titleQuestion);
                    dialog.find('.ui-dialog-content').addClass('modal-body');
                    dialog.find('.ui-dialog-buttonpane').addClass('modal-footer');

                    // add bootstrap design to modal buttons
                    $('.ui-dialog-buttonset button:first-child').addClass('btn btn-outline-gbif-primary mx-2');
                    $('.ui-dialog-buttonset button:nth-child(2)').addClass('btn btn-outline-secondary');

                    if (theOptions.checkboxText != undefined) {
                        $('.ui-dialog-buttonset button:first-child').hide();
                    }
                }
            });

        });
    }

})(jQuery);
