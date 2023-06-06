[#ftl output_format="HTML"]

<div class="setup-stepper-root setup-stepper-vertical mt-3">
    [#assign steps = ["Disclaimer", "IPT data directory", "IPT default administrator", "IPT mode", "Public URL", "Installation complete"] ]
    [#list steps as step]
        [#if step_index < setupStepIndex]
            [#assign stepStatus = "completed"]
        [#elseif step_index == setupStepIndex]
            [#assign stepStatus = "active"]
        [#else]
            [#assign stepStatus = "disabled"]
        [/#if]

        [#if step_index != 0]
            <div class="setup-step-connector-root setup-step-connector-vertical disabled">
                <span class="setup-step-connector-line setup-step-connector-line-vertical"></span>
            </div>
        [/#if]

        <div class="setup-step-root setup-step-vertical [#if stepStatus == 'completed']completed[/#if] ">
            <span class="setup-step-label-root setup-step-label-vertical ${stepStatus}">
                <span class="setup-step-label-icon-container ${stepStatus}">
                    <svg class="step-svg-icon-root step-svg-icon-font-size-medium step-icon-root ${stepStatus}" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                        [#if stepStatus == 'completed']
                            <svg class="step-svg-icon-root step-svg-icon-font-size-medium step-icon-root completed" focusable="false" aria-hidden="true" viewBox="0 0 24 24" data-testid="CheckCircleIcon">
                                <path d="M12 0a12 12 0 1 0 0 24 12 12 0 0 0 0-24zm-2 17l-5-5 1.4-1.4 3.6 3.6 7.6-7.6L19 8l-9 9z"></path>
                            </svg>
                        [#else]
                            <circle cx="12" cy="12" r="12"></circle>
                            <text class="step-icon-text" x="12" y="12" text-anchor="middle" dominant-baseline="central">${step_index + 1}</text>
                        [/#if]
                    </svg>
                </span>
                <span class="setup-step-label-label-container">
                    <span class="setup-step-label-label ${stepStatus}">${step}</span>
                </span>
            </span>
        </div>
    [/#list]
</div>
