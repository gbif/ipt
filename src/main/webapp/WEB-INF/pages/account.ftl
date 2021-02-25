<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header-bootstrap.ftl">
<title><@s.text name="account.title"/></title>
<#include "/WEB-INF/pages/inc/menu-bootstrap.ftl">

<main class="container pt-5">
    <div class="my-3 p-3 bg-body rounded shadow-sm" id="summary">

        <h4 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-success text-center">
            <@s.text name="account.title"/>
        </h4>

        <p class="text-muted mx-md-4 mx-2">
            <@s.text name="account.intro"/>
        </p>

        <p class="text-muted mx-md-4 mx-2">
            <@s.text name="account.email.cantChange"/>
        </p>
    </div>

    <div class="my-3 p-3 bg-body rounded shadow-sm" id="summary">
        <form class="needs-validation" action="account.do" method="post" novalidate>
            <input type="hidden" name="id" value="${user.email!}" required>

            <div class="row g-3 mx-md-4 mx-2 mt-0 mb-2">
                <div class="col-sm-6">
                    <label for="email" class="form-label"><@s.text name="user.email"/></label>
                    <input type="text" class="form-control" id="email" name="user.email" value="${user.email}" required readonly>
                    <div class="invalid-feedback">
                        <@s.text name="validation.email.required"/>
                    </div>
                    <@s.fielderror id="field-error-email" cssClass="invalid-feedback list-unstyled field-error" fieldName="user.email"/>
                </div>

                <#assign val><@s.text name="user.roles.${user.role?lower_case}"/></#assign>

                <div class="col-sm-6">
                    <label for="roles" class="form-label"><@s.text name="user.role"/></label>
                    <input type="text" class="form-control" id="roles" name="role" value="${val}" disabled>
                </div>

                <div class="col-sm-6">
                    <label for="firstname" class="form-label"><@s.text name="user.firstname"/></label>
                    <input type="text" class="form-control" id="firstname" name="user.firstname" value="${user.firstname}" required>
                    <div class="invalid-feedback">
                        <@s.text name="validation.firstname.required"/>
                    </div>
                </div>

                <div class="col-sm-6">
                    <label for="lastname" class="form-label"><@s.text name="user.lastname"/></label>
                    <input type="text" class="form-control" id="lastname" name="user.lastname" value="${user.lastname}" required>
                    <div class="invalid-feedback">
                        <@s.text name="validation.lastname.required"/>
                    </div>
                </div>

                <div class="col-sm-6">
                    <label for="new-password" class="form-label"><@s.text name="user.password.new"/></label>
                    <input type="password" class="form-control" id="new-password" name="user.password" value="${user.password}">
                    <@s.fielderror id="field-error-new-password" cssClass="invalid-feedback list-unstyled field-error" fieldName="user.password"/>
                </div>

                <div class="col-sm-6">
                    <label for="repeat-password" class="form-label"><@s.text name="user.password2"/></label>
                    <input type="password" class="form-control" id="repeat-password" name="password2">
                    <@s.fielderror id="field-error-repeat-password" cssClass="invalid-feedback list-unstyled field-error" fieldName="password2"/>
                </div>

                <div class="col-12">
                    <@s.submit cssClass="btn btn-outline-success" name="save" key="button.save"/>
                    <@s.submit cssClass="btn btn-outline-secondary" name="cancel" key="button.cancel"/>
                </div>

            </div>
        </form>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer-bootstrap.ftl">
