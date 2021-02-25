[#ftl output_format="HTML"]
[#include "/WEB-INF/pages/inc/header_setup-bootstrap.ftl"]

<main class="container pt-5">

    <form action="setup2.do" method="post" class="needs-validation">
        <div class="my-3 p-3 bg-body rounded shadow-sm">
            <h4 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 mt-2 text-success text-center">
                [@s.text name="admin.config.setup2.title"/]
            </h4>

            [@s.actionmessage cssClass="alert alert-success list-unstyled mx-md-4 mx-2"/]
            [#if warnings?size>0]
                [#list warnings as w]
                    <div class="alert alert-danger mx-md-4 mx-2" role="alert">
                        ${w!}
                    </div>
                [/#list]
            [/#if]
            [@s.actionerror cssClass="alert alert-danger list-unstyled mx-md-4 mx-2"/]

            <p class="text-muted mx-md-4 mx-2">[@s.text name="admin.config.setup2.welcome"/]</p>

            <input type="hidden" name="setup2" value="true" />
            <input type="hidden" name="ignoreUserValidation" value="${ignoreUserValidation}" />

            <h5 class="text-success mx-md-4 mx-2">
                [@s.text name="admin.config.setup2.administrator"/]
            </h5>
            <p class="text-muted mx-md-4 mx-2">
                [@s.text name="admin.config.setup2.administrator.help"/]
            </p>
            <div class="row g-3 mx-md-4 mx-2 pb-3 mb-2">
                <div class="col-12">
                    <label class="form-label text-muted" for="userEmail">
                        [@s.text name="user.email"/]
                    </label>
                    <input type="email" class="form-control" id="userEmail" name="user.email" [#if user.email??]value="${user.email}" [/#if].>
                    [@s.fielderror id="field-error-userEmail" cssClass="invalid-feedback list-unstyled field-error" fieldName="user.email"/]
                </div>

                <div class="col-md-6">
                    <label class="form-label text-muted" for="userFirstname">
                        [@s.text name="user.firstname"/]
                    </label>
                    <input type="text" class="form-control" id="userFirstname" name="user.firstname" [#if user.firstname??]value="${user.firstname}"[/#if]>
                    [@s.fielderror id="field-error-userFirstname" cssClass="invalid-feedback-display list-unstyled field-error" fieldName="user.firstname"/]
                </div>

                <div class="col-md-6">
                    <label class="form-label text-muted" for="userLastname">
                        [@s.text name="user.lastname"/]
                    </label>
                    <input type="text" class="form-control" id="userLastname" name="user.lastname" [#if user.lastname??]value="${user.lastname}"[/#if]>
                    [@s.fielderror id="field-error-userLastname" cssClass="invalid-feedback list-unstyled field-error" fieldName="user.lastname"/]
                </div>

                <div class="col-md-6">
                    <label class="form-label text-muted" for="userPassword">
                        [@s.text name="user.password"/]
                    </label>
                    <input type="password" class="form-control" id="userPassword" name="user.password" [#if user.password??]value="${user.password}"[/#if]>
                    [@s.fielderror id="field-error-userPassword" cssClass="invalid-feedback list-unstyled field-error" fieldName="user.password"/]
                </div>

                <div class="col-md-6">
                    <label class="form-label text-muted" for="password2">
                        [@s.text name="user.password2"/]
                    </label>
                    <input type="password" class="form-control" id="password2" name="password2" [#if password2??]value="${password2}"[/#if]>
                    [@s.fielderror id="field-error-password2" cssClass="invalid-feedback list-unstyled field-error" fieldName="password2"/]
                </div>
            </div>
        </div>

        <div class="my-3 p-3 bg-body rounded shadow-sm">
            <h5 class="text-success pb-2 mt-2 border-bottom mx-md-4 mx-2">
                [@s.text name="admin.config.setup2.mode.title"/]
            </h5>

            <p class="text-muted mx-md-4 mx-2">[@s.text name="admin.config.setup2.mode.help"/]</p>
            <p class="text-muted mx-md-4 mx-2">[@s.text name="admin.config.setup2.mode.test"/]</p>
            <p class="text-muted mx-md-4 mx-2">[@s.text name="admin.config.setup2.mode.production"/]</p>
            <p class="text-muted mx-md-4 mx-2">[@s.text name="admin.config.setup2.mode"/]</p>

            [#list modes as mode]
                <div class="form-check form-check-inline text-muted mx-md-4 mx-2 pb-2">
                    <input class="form-check-input" type="radio" name="modeSelected" id="mode${mode}" aria-describedby="field-error-mode" [#if mode??]value="${mode}"[/#if] [#if modeSelected?? && mode == modeSelected] checked [/#if]>
                    <label class="form-check-label" for="mode${mode}">
                        ${mode}
                    </label>
                </div>
            [/#list]
            [@s.fielderror id="field-error-modeTest" cssClass="invalid-feedback list-unstyled field-error"]
[#--                [@s.param value="%{'modeSelected'}" /]--]
            [/@s.fielderror]
        </div>

        <div class="my-3 p-3 bg-body rounded shadow-sm">
            <h5 class="text-success pb-2 mx-md-4 mx-2 pt-2 border-bottom">
                [@s.text name="admin.registration.baseURL"/]
            </h5>

            <div class="row g-3 mx-md-3 mx-1">
                <div class="col-md-6">
                    <label class="form-label text-muted" for="baseURL">
                        [@s.text name="admin.config.baseUrl"/]
                    </label>
                    <div class="input-group">
                        <button type="button" class="btn btn-outline-secondary" data-bs-toggle="popover" data-bs-trigger="focus" data-bs-content="[@s.text name="admin.config.baseUrl.help"/]">
                            <svg xmlns="http://www.w3.org/2000/svg"
                                 width="16" height="16"
                                 fill="#0dcaf0" class="bi bi-info-circle" viewBox="0 0 16 16">
                                <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                                <path d="M8.93 6.588l-2.29.287-.082.38.45.083c.294.07.352.176.288.469l-.738 3.468c-.194.897.105 1.319.808 1.319.545 0 1.178-.252 1.465-.598l.088-.416c-.2.176-.492.246-.686.246-.275 0-.375-.193-.304-.533L8.93 6.588zM9 4.5a1 1 0 1 1-2 0 1 1 0 0 1 2 0z"/>
                            </svg>
                        </button>
                        <input type="text" class="form-control" id="baseURL" name="baseURL" [#if baseURL??]value="${baseURL}"[/#if]>
                    </div>
                    [@s.fielderror id="field-error-baseURL" cssClass="invalid-feedback list-unstyled field-error" fieldName="baseURL"/]
                </div>

                <div class="col-md-6">
                    <label class="form-label text-muted" for="proxy">
                        [@s.text name="admin.config.proxy"/]
                    </label>
                    <div class="input-group">
                        <button type="button" class="btn btn-outline-secondary" data-bs-toggle="popover" data-bs-trigger="focus" data-bs-content="[@s.text name="admin.config.proxy.help"/]">
                            <svg xmlns="http://www.w3.org/2000/svg"
                                 width="16" height="16"
                                 fill="#0dcaf0" class="bi bi-info-circle" viewBox="0 0 16 16">
                                <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                                <path d="M8.93 6.588l-2.29.287-.082.38.45.083c.294.07.352.176.288.469l-.738 3.468c-.194.897.105 1.319.808 1.319.545 0 1.178-.252 1.465-.598l.088-.416c-.2.176-.492.246-.686.246-.275 0-.375-.193-.304-.533L8.93 6.588zM9 4.5a1 1 0 1 1-2 0 1 1 0 0 1 2 0z"/>
                            </svg>
                        </button>
                        <input type="text" class="form-control" id="proxy" name="proxy" [#if proxy??]value="${proxy}"[/#if]>
                    </div>
                    [@s.fielderror id="field-error-proxy" cssClass="invalid-feedback list-unstyled field-error" fieldName="proxy"/]
                </div>

                <div class="col-12 pb-3">
                    <button class="btn btn-outline-success" type="submit">
                        [@s.text name="button.save"/]
                    </button>
                </div>
            </div>



        </div>
    </form>
</main>
</div>


[#include "/WEB-INF/pages/inc/footer-bootstrap.ftl"]
