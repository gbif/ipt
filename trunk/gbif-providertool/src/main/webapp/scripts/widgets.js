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

/*==== Citations ====*/

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

/*==== Agents ====*/

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
      select.attr('id', name);
      select.attr('name', 'eml.associatedParties[' + agentCount + '].address.country');
      select.attr('value', val);
      var oldSelect = e.find('#' + name);
      oldSelect.replaceWith(select);
    } else {
      $(e).attr('id', name);
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

/*==== Physical Data ====*/
/**
 * The PhysicalDataWidget class can be used to encapsulate and display a Physcial Data grouping.
 * 
 */
function PhysicalDataWidget(physicalData) {
  var e = $('#clonePhysicalData').clone();
  var physicalDataCount = PhysicalDataPanel.physicalDataCount();
  e.attr('id', 'physicalData' + physicalDataCount);
  this._element = e;
  e.find('#removeLink').show();
  this._physicalData = physicalData;
  var isPhysicalData = physicalData instanceof PhysicalData;
  var physicalDataProps = PhysicalData.propertyNames();
  for (p in physicalDataProps) {
    var name = physicalDataProps[p];
    var val = '';
    if (isPhysicalData) {
      val = eval('physicalData.' + name + '()');
    } 
    e.find('#' + name).attr('name', 'eml.physicalData[' + physicalDataCount + '].'+ name);
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
 * The PhysicalDataPanel can be used to display a group of physicalData widgets.
 * 
 */
function PhysicalDataPanel() {
  this._elementId = '#physicalDataPanel';
  this._element = $(this._elementId);
  this._physicalDataSelector = "div[id^='physicalData']";
  this._physicalDataIdPattern = /physicalData\d+/;
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
   * Returns the number of physicalData div elements in this physicalData panel.
   * 
   * @return number of physicalData div elements
   */
  function size() {
    return $(this._element).find(this._physicalDataSelector).size();
  }

  function _resize() {
   this._element.find(this._physicalDataSelector).each(function(i) {
     var id =  $(this).attr('id');
     if (id.match(this._physicalDataIdPattern)) {
       $(this).attr('id', 'physicalData' + i);
       name = 'eml.physicalData[' + i + '].';
       props = PhysicalData.propertyNames();
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
   * Adds and displays a physicalData widget and adds a click handler for deleting it.
   */
  function add(widget) {
    if (!(widget instanceof PhysicalDataWidget)) {
      alert('Illegal Argument ' + widget + ': Instance of PhysicalDataWidget expected');
      return;
    }
    var count = this.size();
    var e = widget.element();
    e.attr('id', 'physicalData' + count);
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

PhysicalDataPanel.physicalDataCount = function() {
  return $('#physicalDataPanel').find("div[id^='physicalData']").size();
}

/*==== Curatorial Units ====*/

/**
 * The CuratorialUnitWidget class can be used to encapsulate and display a Curatorial Unit Data grouping.
 * 
 */
function CuratorialUnitWidget(curatorialUnit) {
  var e = $('#cloneCuratorialUnit').clone();
  var curatorialUnitCount = CuratorialUnitPanel.curatorialUnitCount();
  e.attr('id', 'curatorialUnit' + curatorialUnitCount);
  this._element = e;
  e.find('#removeLink').show();
  this._curatorialUnit = curatorialUnit;
  var isCuratorialUnit = curatorialUnit instanceof CuratorialUnit;
  var curatorialUnitProps = CuratorialUnit.propertyNames();
  for (p in curatorialUnitProps) {
    var name = curatorialUnitProps[p];
    var val = '';
    if (isCuratorialUnit) {
      val = eval('curatorialUnit.' + name + '()');
    } 
    e.find('#' + name).attr('name', 'eml.jgtiCuratorialUnits[' + curatorialUnitCount + '].'+ name);
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
 * The CuratorialUnitPanel can be used to display a group of curatorialUnit widgets.
 * 
 */
function CuratorialUnitPanel() {
  this._elementId = '#curatorialUnitPanel';
  this._element = $(this._elementId);
  this._curatorialUnitSelector = "div[id^='curatorialUnit']";
  this._curatorialUnitIdPattern = /curatorialUnit\d+/;
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
   * Returns the number of curatorialUnit div elements in this curatorialUnit panel.
   * 
   * @return number of curatorialUnit div elements
   */
  function size() {
    return $(this._element).find(this._curatorialUnitSelector).size();
  }

  function _resize() {
   this._element.find(this._curatorialUnitSelector).each(function(i) {
     var id =  $(this).attr('id');
     if (id.match(this._curatorialUnitIdPattern)) {
       $(this).attr('id', 'curatorialUnit' + i);
       name = 'eml.jgtiCuratorialUnits[' + i + '].';
       props = CuratorialUnit.propertyNames();
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
   * Adds and displays a curatorialUnit widget and adds a click handler for deleting it.
   */
  function add(widget) {
    if (!(widget instanceof CuratorialUnitWidget)) {
      alert('Illegal Argument ' + widget + ': Instance of CuratorialUnitWidget expected');
      return;
    }
    var count = this.size();
    var e = widget.element();
    e.attr('id', 'curatorialUnit' + count);
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

CuratorialUnitPanel.curatorialUnitCount = function() {
  return $('#curatorialUnitPanel').find("div[id^='curatorialUnit']").size();
}