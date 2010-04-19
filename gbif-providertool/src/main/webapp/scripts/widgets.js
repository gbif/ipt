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
 * The AgentWidget class can be used to encapsulate and display an Agent.
 * 
 */
function AgentWidget(agent) {
  var e = $('#cloneAgent').clone();
  var agentCount = AgentPanel.agentCount();
  e.attr('id', 'agent' + agentCount);
  this._element = e;
  e.find('#removeLink').show();
  this._agent = agent;
  var isAgent = agent instanceof Agent;
  var agentProps = Agent.propertyNames();
  var addressNames = Address.propertyNames();
  for (p in agentProps) {
    var name = agentProps[p];
    var val = '';
    if (isAgent) {
      val = eval('agent.' + name + '()');
    } 
    if (name == 'country') {      
      var select = countriesSelect.clone();
      $(e).find('#' + name).replaceWith(select);
      $(select).attr('name', 'eml.associatedParties[' + agentCount + '].address.country');
      $(select).attr('value', val);
      $(select).attr('id', name);
    } else {       
      var isAddress = jQuery.inArray(name, addressNames);
      isAddress = isAddress >= 0 ? true : false;
      if (isAddress) {
        e.find('#' + name).attr('name', 'eml.associatedParties[' + agentCount + '].address.'+ name);
        e.find('#' + name).attr('value', val);
      } else {
        e.find('#' + name).attr('name', 'eml.associatedParties[' + agentCount + '].'+ name);
        e.find('#' + name).attr('value', val);
      }
    }
  }  
  this._elementId = '';
  this.element = element;
  
  function element(val) {
    if (!val) {
      return this._element;
    } else { 
      this._element = val;
      return this;
    }
  }
}

/**
 * The AgentPanel can be used to display a group of agent widgets.
 * 
 */
function AgentPanel() {
  this._elementId = '#agentPanel';
  this._element = $(this._elementId);
  this._agentSelector = "div[id^='agent']";
  this._agentIdPattern = /agent\d+/;
  this.add = add;
  this._remove = _remove;
  this._resize = _resize;
  this.size = size;
  this.element = element;
  
  function element(val) {
    if (!val) {
      return this._element;
    } else { 
      this._element = val;
      return this;
    }
  }
  
  /**
   * Returns the number of agent div elements in this agent panel.
   * 
   * @return number of agent div elements
   */
  function size() {
    return $(this._element).find(this._agentSelector).size();
  }

  /**
   * Renumbers all agent element ids and name attribute values sequentially
   * starting from 0.
   */
  function _resize() {
    this._element.find(this._agentSelector).each(function(index) {
      var id =  $(this).attr('id');
      if (id.match(this._agentIdPattern)) {
        $(this).attr('id', 'agent' + index);
        name = 'eml.associatedParties[' + index + '].';
        props = Agent.propertyNames();
        for (p in props) {
          var prop = props[p];
          $(this).find('#' + prop).attr('name', name + prop);
        }
      }    
    });
  }

  /**
   * Removes the element from the DOM.
   */
  function _remove(element) {
    $(element).remove();    
  }
  
  /**
   * Adds and displays an agent widget and adds a click handler for deleting it.
   */
  function add(widget) {
    if (!(widget instanceof AgentWidget)) {
      alert('Illegal Argument ' + widget + ': Instance of AgentWidget expected');
      return;
    }
    var count = this.size();
    var e = widget.element();
    e.attr('id', 'agent' + count);
    var _this = this;
    e.find('#removeLink').click(function() {      
      var id = '#' + $(this).parent().parent().attr('id');
      _this._remove($(id));
      _this._resize();
    });
    e.appendTo(this._element);
    e.show();
    return false;
  }
}

AgentPanel.agentCount = function() {
  return $('#agentPanel').find("div[id^='agent']").size();
}