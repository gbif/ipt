package org.gbif.provider.webapp.action;

import java.util.ArrayList;
import java.util.List;

import org.gbif.provider.model.User;
import org.gbif.provider.util.RequestUtil;
import org.springframework.mail.MailException;
import org.springframework.security.userdetails.UsernameNotFoundException;

/**
 * Action class to send password hints to registered users.
 *
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 */
public class PasswordHintAction extends BaseAppfuseAction {
    private static final long serialVersionUID = -4037514607101222025L;
    private String username;

    /**
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Execute sending the password hint via e-mail.
     *
     * @return success if username works, input if not
     */
    public String execute() {
        List<String> args = new ArrayList<String>();

        // ensure that the username has been sent
        if (username == null) {
            log.warn("Username not specified, notifying user that it's a required field.");

            args.add(getText("user.username"));
            addActionError(getText("errors.requiredField", args));
            return INPUT;
        }

        if (log.isDebugEnabled()) {
            log.debug("Processing Password Hint...");
        }

        // look up the user's information
        try {
            User user = userManager.getUserByUsername(username);
            String hint = user.getPasswordHint();

            if (hint == null || hint.trim().equals("")) {
                log.warn("User '" + username + "' found, but no password hint exists.");
                addActionError(getText("login.passwordHint.missing"));
                return INPUT;
            }

            StringBuffer msg = new StringBuffer();
            msg.append("Your password hint is: ").append(hint);
            msg.append("\n\nLogin at: ").append(RequestUtil.getAppURL(getRequest()));

            mailMessage.setTo(user.getEmail());
            String subject = '[' + getText("webapp.name") + "] " + getText("user.passwordHint");
            mailMessage.setSubject(subject);
            mailMessage.setText(msg.toString());
            mailEngine.send(mailMessage);
            
            args.add(username);
            args.add(user.getEmail());
            
            saveMessage(getText("login.passwordHint.sent", args));
        } catch (UsernameNotFoundException e) {
            log.warn(e.getMessage());
            args.add(username);
            addActionError(getText("login.passwordHint.error", args));
            getSession().setAttribute("errors", getActionErrors());
            return INPUT;
        } catch (MailException me) {
            addActionError(me.getCause().getLocalizedMessage());
            getSession().setAttribute("errors", getActionErrors());
            return INPUT;
        }

        return SUCCESS;
    }
}
