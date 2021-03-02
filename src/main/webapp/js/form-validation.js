// Example starter JavaScript for disabling form submissions if there are invalid fields
(function () {
    'use strict'

    // Fetch all the forms we want to apply custom Bootstrap validation styles to
    var forms = document.querySelectorAll('.needs-validation')

    // Loop over them and prevent submission
    Array.prototype.slice.call(forms)
        .forEach(function (form) {
            // validate 'field-errors' from backend
            var fieldErrors = form.querySelectorAll('.field-error')
            Array.prototype.slice.call(fieldErrors)
                .forEach(function (fieldError) {
                    var elementId = fieldError.id.split('field-error-').pop()
                    var element = document.getElementById(elementId)
                    if (element) {
                        element.classList.add('is-invalid')
                    }
                })

            // validate radio 'field-errors' from backend
            var radioErrors = form.querySelectorAll('.radio-error')
            Array.prototype.slice.call(radioErrors)
                .forEach(function (radioError) {
                    // display feedback
                    radioError.style.cssText += ';display:block !important;'

                    // get radio name from classes
                    // id does not work!
                    var radioName = undefined
                    Array.prototype.slice.call(radioError.classList)
                        .forEach(function (radioErrorClass) {
                        if (radioErrorClass.startsWith('radio-name-')) {
                            radioName = radioErrorClass.split('radio-name-').pop()
                        }
                    })

                    // find radios by name and make them invalid
                    if (radioName) {
                        var radios = document.getElementsByName(radioName)
                        if (radios) {
                            Array.prototype.slice.call(radios)
                                .forEach(function (radio) {
                                    radio.classList.add('is-invalid')
                                })
                        }
                    }
                })

            // standard validation
            form.addEventListener('submit', function (event) {
                var inputs = form.querySelectorAll('.form-control, .form-select')

                Array.prototype.slice.call(inputs)
                    .forEach(function (input) {
                        if (!input.checkValidity()) {
                            event.preventDefault()
                            event.stopPropagation()
                            input.classList.add('is-invalid')
                        }
                    })
            })
        })
})()
