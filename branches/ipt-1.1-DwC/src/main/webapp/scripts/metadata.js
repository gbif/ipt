/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 * A generic error handler. Simply alerts the errorMsg.
 * 
 * @param errorMsg the error msg
 * 
 */
function handleError(errorMsg) {
  alert("IPT Error: " + errorMsg);
}

/**
 * The Widget class can be used to encapsulate a DIV element.
 * 
 * @param elementId div element id
 * 
 */
function Widget(elementId) {
  this.elementId = '#' + elementId;
  this.setVisible = setVisible;

  /**
   * Sets the visibility of the widget.
   * 
   * @param isVisible true or false
   */
  function setVisible(isVisible) {
    if (isVisible) {
      $(this.elementId).show();
    } else {
      $(this.elementId).hide();
    }
  }
}

/**
 * The TextBox class can be used to encapsulate a textbox form element.
 * 
 * @param elementId form element id
 * 
 */
function TextBox(elementId) {
  this.elementId = '#' + elementId;
  this.setReadOnly = setReadOnly;
  this.setAutoComplete = setAutoComplete;
  this.setValue = setValue;
  this.getValue = getValue;

  /**
   * Returns the value of this textbox.
   */
  function getValue() {
    return $(this.elementId).val();
  }

  /**
   * Sets the value of this textbox.
   * 
   * @param value the textbox value to set
   */
  function setValue(value) {
    $(this.elementId).val(value);
  }

  /**
   * Adds autocomplete functionality to this text box.
   * 
   * @param data the autocomplete data in JSON format
   * @param callback the callback when an item is selected
   */
  function setAutoComplete(data, callback) {
    $(this.elementId).autocomplete(data, {
      width : 340,
      minChars : 1,
      mustMatch : false,
      matchContains : true,
      formatItem : function(row, i, max) {
        return row.name;
      },
      formatResult : function(row) {
        return row.name;
      }
    }).result(function(event, selectedData, formatted) {
      callback.onSuccess(selectedData);
    });
  }

  /**
   * Sets this textbox to read-onlu.
   * 
   * @param readOnly true or false
   */
  function setReadOnly(readOnly) {
    $(this.elementId).attr("readOnly", readOnly);
  }
}

/**
 * The AsyncCallback class can be used to encapsulate callback methods for
 * handling async success and failure.
 * 
 * @param onSuccess function that handles success
 * @param onFailure function that handles failure
 */
function AsyncCallback(onSuccess, onFailure) {
  this.onSuccess = onSuccess;
  this.onFailure = onFailure;
}

/**
 * The RegistryServiceAsyc class provides async access to the Registry API.
 * 
 * @param registryUrl the registry API URL
 */
function RegistryServiceAsync(registryUrl) {
  this.registryUrl = registryUrl;
  this.loadAllOrgsAsync = loadAllOrgsAsync;
  this.loadOrgAsync = loadOrgAsync;
  this.cachedOrgs = null;

  /**
   * Asynchronously loads an organization by key.
   * 
   * @param orgKey the organization key
   * @param callback the AsyncCallback
   */
  function loadOrgAsync(orgKey, callback) {
    try {
      url = this.registryUrl + orgKey + '.json';
      $.getJSON(url, function(json) {
        callback.onSuccess(json);
      });
    } catch (error) {
      callback.onFailure(error);
    }
  }

  /**
   * Asynchronously loads all organizations.
   * 
   * @param callback the AsyncCallback
   */
  function loadAllOrgsAsync(callback) {
    try {
      url = this.registryUrl + '.json';
      if (this.cachedOrgs == null) {
        $.getJSON(url, function(json) {
          this.cachedOrgs = json;
          callback.onSuccess(json);
        });
      } else {
        callback.onSuccess(this.cachedOrgs);
      }

    } catch (error) {
      callback.onFailure(error);
    }
  }
}