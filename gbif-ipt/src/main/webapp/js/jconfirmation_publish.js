(function($){

  jQuery.fn.jConfirmPublishAction = function (options) {

    // Some jConfirmPublishAction options
    // headerQuestion : a question for the header.
    // yesAnswer : a text for your question.
    // yesAnswerChecked : a text for Yes answer if checkbox is checked.
    // cancelAnswer : a text for cancelling.
    // checkboxText: a text for the checkbox.
    // checkboxTextOff : a text for the checkbox to disable.
    var theOptions = jQuery.extend ({
      headerQuestion: "Are You Sure?",
      yesAnswer: "Yes",
      yesAnswerChecked: "Yes checked",
      cancelAnswer: "Cancel",
      checkboxText: undefined,
      checkboxTextOff: undefined
    }, options);
    return this.each (function () {

      $(this).bind('click', function(e) {
        var submitBtn = $(this);
        if ($(this).attr("jconfirmed")){
          submitBtn.removeAttr("jconfirmed");
        }else{
          e.preventDefault();
          thisHref = $(this).attr('href');
          var btns = {};
          btns[theOptions.yesAnswer]=function() {
            $( this ).dialog( "close" );
            if (thisHref!=null){
              window.location = thisHref;
            }else{
              submitBtn.attr("jconfirmed", true);
              submitBtn.click();
            }
          };
          btns[theOptions.yesAnswerChecked]=function() {
            $( this ).dialog( "close" );
            if (thisHref!=null){
              window.location = thisHref;
            }else{
              submitBtn.attr("jconfirmed", true);
              submitBtn.click();
            }
          };
          btns[theOptions.cancelAnswer]=function() {
            $( this ).dialog( "close" );
            submitBtn.removeAttr("jconfirmed");
          };

          var content='<p>'+theOptions.headerQuestion+'</p>';
          if(theOptions.checkboxText!=undefined && theOptions.checkboxTextOff!=undefined){
            content =
            '<p>' +
            '<br/>' +
            theOptions.question +
            '<br/><br/>' +
            '<input type="checkbox" key="cbox" id="cbox">' +
            theOptions.checkboxText +
            '<br/>' +
            '<input type="checkbox" key="cboxOff" id="cboxOff">' +
            theOptions.checkboxTextOff +
            '<br/>' +
            '</p>';
          }

          $('#dialog-confirm-publish').html(content);
          $('#cbox').click(function() {
            if($('#cbox').attr("checked")){
              $('.ui-dialog-buttonset button:nth-child(2)').show();
              $('.ui-dialog-buttonset button:first-child').hide();
              // ensure parameter for publication mode gets set using MainUpFreqType Enumeration
              $('#pubMode').val('AUTO_PUBLISH_ON');
              $('#cboxOff').attr("disabled", true);
            } else{
              $('#cboxOff').removeAttr("disabled");
              $('.ui-dialog-buttonset button:first-child').show();
              $('.ui-dialog-buttonset button:nth-child(2)').hide();
            }
          });
          $('#cboxOff').click(function() {
            if($('#cboxOff').attr("checked")){
              $('.ui-dialog-buttonset button:first-child').show();
              $('.ui-dialog-buttonset button:nth-child(2)').hide();
              // ensure parameter for publication mode gets set using MainUpFreqType Enumeration
              $('#pubMode').val('AUTO_PUBLISH_NEVER');
              $('#cbox').attr("disabled", true);
            } else{
              $('#cbox').removeAttr("disabled");
            }
          });
          $('#dialog-confirm-publish').dialog({
            resizable: false,
            modal: true,
            buttons: btns
          });
          // hide alternate yes button
          $('.ui-dialog-buttonset button:nth-child(2)').hide();
        };
      });

    });
  }

})(jQuery);