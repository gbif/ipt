/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.utils;

import org.gbif.ipt.action.BaseAction;

import org.apache.logging.log4j.Logger;

public class ActionLogger {

  private final Logger log;
  private final BaseAction action;

  public ActionLogger(Logger log, BaseAction action) {
    this.log = log;
    this.action = action;
  }

  public void error(String message) {
    if (message != null) {
      action.addActionWarning(action.getText(message));
      log.error(action.getText(message) == null ? message : action.getText(message));
    }
  }

  public void error(String message, String[] args) {
    if (message != null) {
      action.addActionWarning(action.getText(message, args));
      log.error(action.getText(message) == null ? message : action.getText(message, args));
    }
  }

  public void error(String message, String[] args, Throwable t) {
    if (message == null) {
      error(t);
    } else {
      action.addActionWarning(action.getText(message, args));
      log.error(action.getText(message) == null ? message : action.getText(message, args), t);
    }
  }

  public void error(String message, Throwable t) {
    if (message == null) {
      error(t);
    } else {
      action.addActionWarning(action.getText(message));
      log.error(action.getText(message) == null ? message : action.getText(message), t);
    }
  }

  public void error(Throwable t) {
    if (t.getMessage() != null) {
      action.addActionWarning(t.getMessage());
    }
    log.error(t);
  }

  public void info(String message) {
    if (message != null) {
      action.addActionMessage(action.getText(message));
      log.info(action.getText(message) == null ? message : action.getText(message));
    }
  }

  public void info(String message, String[] args) {
    if (message != null) {
      action.addActionMessage(action.getText(message, args));
      log.info(action.getText(message) == null ? message : action.getText(message));
    }
  }

  public void info(String message, String[] args, Throwable t) {
    if (message == null) {
      info(t);
    } else {
      action.addActionMessage(action.getText(message, args));
      log.info(action.getText(message) == null ? message : action.getText(message), t);
    }
  }

  public void info(String message, Throwable t) {
    if (message == null) {
      info(t);
    } else {
      action.addActionMessage(action.getText(message));
      log.info(action.getText(message) == null ? message : action.getText(message), t);
    }
  }

  public void info(Throwable t) {
    if (t.getMessage() != null) {
      action.addActionMessage(t.getMessage());
    }
    log.info(t);
  }

  public void warn(String message) {
    if (message != null) {
      action.addActionWarning(action.getText(message));
      log.warn(action.getText(message) == null ? message : action.getText(message));
    }
  }

  public void warn(String message, String[] args) {
    if (message != null) {
      action.addActionWarning(action.getText(message, args));
      log.warn(action.getText(message) == null ? message : action.getText(message));
    }
  }

  public void warn(String message, String[] args, Throwable t) {
    if (message == null) {
      info(t);
    } else {
      action.addActionWarning(action.getText(message, args));
      log.warn(action.getText(message) == null ? message : action.getText(message, args), t);
    }
  }

  public void warn(String message, Throwable t) {
    if (message == null) {
      info(t);
    } else {
      action.addActionWarning(action.getText(message));
      log.warn(action.getText(message) == null ? message : action.getText(message), t);
    }
  }

  public void warn(Throwable t) {
    if (t.getMessage() != null) {
      action.addActionWarning(t.getMessage());
    }
    log.warn(t);
  }
}
