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



function CitationWidget(citation) {
  this.elem = $('#cloneCitation').clone();
  var elem = this.elem;
  var citationCount = CitationPanel.size();
  elem.attr('id', 'citation' + citationCount);
  elem.find('#removeLink').show();
  $(elem).find('#citation').attr('name', 'eml.bibliographicCitations[' + citationCount + ']');
  $(elem).find('#citation').attr('value', citation);
}

function CitationPanel() {
  this.id = '#citationsPanel';
  this.elem = $(this.id);
  this.citationSelector = "div[id^='citation']";
  this.citationIdPattern = /citation\d+/;
  this.add = add;
  this.resize = resize;
  
  function add(widget) {
    if (!(widget instanceof CitationWidget)) {
      alert('Illegal argument. Expected a CitationWidget');
      return;
    }
    var size = CitationPanel.size();
    var elem = $(widget.elem);
    elem.attr('id', 'citation' + size);
    var _this = this;
    elem.find('#removeLink').click(function() {
      var id = '#' + $(this).parent().parent().attr('id');
      $($(id)).remove();
      _this.resize();
    });
    elem.appendTo(this.elem);
    elem.show();
    return false;
  }
  
  function resize() {
    this.elem.find(this.citationSelector).each(function(i) {
      var id =  $(this).attr('id');
      if (id.match(this.citationIdPattern)) {
        var elem = $(this);
        $(elem).attr('id', 'citation' + i);
        var name = 'eml.bibliographicCitations[' + i + ']';
        $(elem).attr('name', name);
      }
    });
  }  
}


CitationPanel.size = function() {
  return $('#citationsPanel').find("div[id^='citation']").size();
}

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
    } else {       
      $(select).attr('id', name);
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

function _resize() {
   this._element.find(this._agentSelector).each(function(i) {
     var id =  $(this).attr('id');
     if (id.match(this._agentIdPattern)) {
       $(this).attr('id', 'agent' + i);
       name = 'eml.associatedParties[' + i + '].';
       props = Agent.propertyNames();
       var addressNames = Address.propertyNames();
       for (p in props) {
         var prop = props[p];
         var isAddress = jQuery.inArray(prop, addressNames);
         isAddress = isAddress >= 0 ? true : false;
         if (isAddress) {
           $(this).find('#' + prop).attr('name', 'eml.associatedParties[' + i + '].address.'+ prop);
         } else {
           $(this).find('#' + prop).attr('name', name + prop);
         }
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

/*==== TemporalCoverage ====*/

/**
 * The TemporalCoverageWidget class can be used to encapsulate and display a TemporalCoverage.
 * 
 */
function TemporalCoverageWidget(temporalCoverage) {
  var e = $('#cloneTemporalCoverage').clone();
  var temporalCoverageCount = TemporalCoveragePanel.temporalCoverageCount();
  e.attr('id', 'temporalCoverage' + temporalCoverageCount);
  this._element = e;
  e.find('#removeLink').show();
  this._temporalCoverage = temporalCoverage;
  var isTemporalCoverage = temporalCoverage instanceof TemporalCoverage;
  var temporalCoverageProps = TemporalCoverage.propertyNames();
  for (p in temporalCoverageProps) {
    var name = temporalCoverageProps[p];
    var val = '';
    if (isTemporalCoverage) {
      val = eval('temporalCoverage.' + name + '()');
    } 
    e.find('#' + name).attr('name', 'eml.temporalCoverages[' + temporalCoverageCount + '].'+ name);
    e.find('#' + name).attr('value', val);
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
 * The TemporalCoveragePanel can be used to display a group of temporal coverage widgets.
 * 
 */
function TemporalCoveragePanel() {
  this._elementId = '#temporalCoveragePanel';
  this._element = $(this._elementId);
  this._temporalCoverageSelector = "div[id^='temporalCoverage']";
  this._temporalCoverageIdPattern = /temporalCoverage\d+/;
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
   * Returns the number of temporalCoverage div elements in this temporalCoverage panel.
   * 
   * @return number of temporalCoverage div elements
   */
  function size() {
    return $(this._element).find(this._temporalCoverageSelector).size();
  }

  function _resize() {
   this._element.find(this._temporalCoverageSelector).each(function(i) {
     var id =  $(this).attr('id');
     if (id.match(this._temporalCoverageIdPattern)) {
       $(this).attr('id', 'temporalCoverage' + i);
       name = 'eml.temporalCoverages[' + i + '].';
       props = TemporalCoverage.propertyNames();
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
   * Adds and displays a temporalCoverage widget and adds a click handler for deleting it.
   */
  function add(widget) {
    if (!(widget instanceof TemporalCoverageWidget)) {
      alert('Illegal Argument ' + widget + ': Instance of TemporalCoverageWidget expected');
      return;
    }
    var count = this.size();
    var e = widget.element();
    e.attr('id', 'temporalCoverage' + count);
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

TemporalCoveragePanel.temporalCoverageCount = function() {
  return $('#temporalCoveragePanel').find("div[id^='temporalCoverage']").size();
}

/*==== GeospatialCoverage ====*/

/*==== TaxonomicCoverage ====*/

/*==== Sampling Method ====*/
/**
 * The SamplingMethodWidget class can be used to encapsulate and display an SamplingMethod.
 * 
 */
function SamplingMethodWidget(samplingMethod) {
  var e = $('#cloneSamplingMethods').clone();
  var samplingMethodCount = SamplingMethodPanel.samplingMethodCount();
  e.attr('id', 'samplingMethod' + samplingMethodCount);
  this._element = e;
  e.find('#removeLink').show();
  this._samplingMethod = samplingMethod;
  var isSamplingMethod = samplingMethod instanceof SamplingMethod;
  var samplingMethodProps = SamplingMethod.propertyNames();
  for (p in samplingMethodProps) {
    var name = samplingMethodProps[p];
    var val = '';
    if (isSamplingMethod) {
      val = eval('samplingMethod.' + name + '()');
    } 
    e.find('#' + name).attr('name', 'eml.samplingMethods[' + samplingMethodCount + '].'+ name);
    e.find('#' + name).attr('value', val);
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
 * The SamplingMethodPanel can be used to display a group of sampling method widgets.
 * 
 */
function SamplingMethodPanel() {
  this._elementId = '#samplingMethodPanel';
  this._element = $(this._elementId);
  this._samplingMethodSelector = "div[id^='samplingMethod']";
  this._samplingMethodIdPattern = /samplingMethod\d+/;
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
   * Returns the number of samplingMethod div elements in this samplingMethod panel.
   * 
   * @return number of samplingMethod div elements
   */
  function size() {
    return $(this._element).find(this._samplingMethodSelector).size();
  }

  function _resize() {
   this._element.find(this._samplingMethodSelector).each(function(i) {
     var id =  $(this).attr('id');
     if (id.match(this._samplingMethodIdPattern)) {
       $(this).attr('id', 'samplingMethod' + i);
       name = 'eml.samplingMethods[' + i + '].';
       props = SamplingMethod.propertyNames();
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
   * Adds and displays a samplingMethod widget and adds a click handler for deleting it.
   */
  function add(widget) {
    if (!(widget instanceof SamplingMethodWidget)) {
      alert('Illegal Argument ' + widget + ': Instance of SamplingMethodWidget expected');
      return;
    }
    var count = this.size();
    var e = widget.element();
    e.attr('id', 'samplingMethod' + count);
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

SamplingMethodPanel.samplingMethodCount = function() {
  return $('#samplingMethodPanel').find("div[id^='samplingMethod']").size();
}