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
  this.showType = showType;
  var temporalCoverageType = e.find('#type').attr('value');
  this.showType(e, temporalCoverageType);
  
  function element(val) {
    if (!val) {
      return this._element;
    } else { 
      this._element = val;
      return this;
    }
  }
  
  function showType(element, type) {
    var id = element.attr('id');
    var evalue = element.attr('value');
    if (type == 'SINGLE_DATE') {
      // Update UI for Single Date selection.
      $(element).find('#startDateDiv').show();
      $(element).find('#endDateDiv').hide();
      $(element).find('#endDate').attr('value', null);
      $(element).find('#exampleDiv').show();
      $(element).find('#formationPeriodDiv').hide();
      $(element).find('#formationPeriod').attr('value', null);
      $(element).find('#livingTimePeriodDiv').hide();
      $(element).find('#livingTimePeriod').attr('value', null);
    } else if (type == 'DATE_RANGE') {
      // Update UI for Date Range selection.
      $(element).find('#startDateDiv').show();
      $(element).find('#endDateDiv').show();
      $(element).find('#exampleDiv').show();
      $(element).find('#formationPeriodDiv').hide();
      $(element).find('#formationPeriod').attr('value', null);
      $(element).find('#livingTimePeriodDiv').hide();      
      $(element).find('#livingTimePeriod').attr('value', null);
    } else if (type == 'FORMATION_PERIOD') {
      // Update UI for Formation Period selection.
      $(element).find('#startDateDiv').hide();
      $(element).find('#startDate').attr('value', null);
      $(element).find('#endDateDiv').hide();
      $(element).find('#endDate').attr('value', null);
      $(element).find('#exampleDiv').hide();
      $(element).find('#formationPeriodDiv').show();
      $(element).find('#livingTimePeriodDiv').hide();      
      $(element).find('#livingTimePeriod').attr('value', null);
    } else if (type == 'LIVING_TIME_PERIOD') {
      // Update UI for Living Time Period selection.
      $(element).find('#startDateDiv').hide();
      $(element).find('#startDate').attr('value', null);
      $(element).find('#endDateDiv').hide();
      $(element).find('#endDateDiv').attr('value', null);
      $(element).find('#exampleDiv').hide();
      $(element).find('#formationPeriodDiv').hide();
      $(element).find('#formationPeriodDiv').attr('value', null);
      $(element).find('#livingTimePeriodDiv').show();      
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
  this.reselect = reselect;
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
    var what = $(this._element).find(this._temporalCoverageSelector);
    var the = $(this._element);
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
   * Modifies a widget based on a type of selection.
   * 
   * @param element a widget element
   * @param type the selection type
   */
  function reselect(element, type) {
    var id = element.attr('id');
    if (type == 'SINGLE_DATE') {
      // Update UI for Single Date selection.
      $(element).find('#startDateDiv').show();
      $(element).find('#endDateDiv').hide();
      $(element).find('#endDate').attr('value', null);
      $(element).find('#exampleDiv').show();
      $(element).find('#formationPeriodDiv').hide();
      $(element).find('#formationPeriod').attr('value', null);
      $(element).find('#livingTimePeriodDiv').hide();
      $(element).find('#livingTimePeriod').attr('value', null);
    } else if (type == 'DATE_RANGE') {
      // Update UI for Date Range selection.
      $(element).find('#startDateDiv').show();
      $(element).find('#endDateDiv').show();
      $(element).find('#exampleDiv').show();
      $(element).find('#formationPeriod').hide();
      $(element).find('#formationPeriod').attr('value', null);
      $(element).find('#livingTimePeriodDiv').hide();      
      $(element).find('#livingTimePeriod').attr('value', null);
    } else if (type == 'FORMATION_PERIOD') {
      // Update UI for Formation Period selection.
      $(element).find('#startDateDiv').hide();
      $(element).find('#endDateDiv').hide();
      $(element).find('#endDate').attr('value','');
      $(element).find('#exampleDiv').hide();
      $(element).find('#formationPeriodDiv').show();
      $(element).find('#livingTimePeriodDiv').hide();      
      $(element).find('#livingTimePeriod').attr('value', null);
    } else if (type == 'LIVING_TIME_PERIOD') {
      // Update UI for Living Time Period selection.
      $(element).find('#startDateDiv').hide();
      $(element).find('#endDateDiv').hide();
      $(element).find('#endDate').attr('value', null);
      $(element).find('#exampleDiv').hide();
      $(element).find('#formationPeriodDiv').hide();
      $(element).find('#formationPeriod').attr('value', null);
      $(element).find('#livingTimePeriodDiv').show();      
    }
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
    e.find('#type').change(function(e) {  
      // Careful: In the change() callback, 'this' refers to the select element.
      // e is the event.
      var s = this.options[this.selectedIndex];
      var sText = s.text;
      var sValue = s.value;
      var id = '#' + $(this).parent().parent().parent().parent().attr('id');
      _this.reselect($(id), sValue);
    });
    e.appendTo(this._element);
    e.show();
    return false;
  }
}

TemporalCoveragePanel.temporalCoverageCount = function() {
  return $('#temporalCoveragePanel').find("div[id^='temporalCoverage']").size();
}

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
  this.showType = showType;
  var methodType = e.find('#type').attr('value');
  this.showType(e, methodType);
  
  function element(val) {
    if (!val) {
      return this._element;
    } else { 
      this._element = val;
      return this;
    }
  }

  /* Show and hide elements as appropriate based on type */
  function showType(element, type) {
    var id = element.attr('id');
    var evalue = element.attr('value');
    var startval =  $(element).find('#rangeStart').attr('value');
    var endval =  $(element).find('#rangeEnd').attr('value');
    var meanval =  $(element).find('#rangeMean').attr('value');
    if (type == 'METHOD_STEP') {
      // Update UI for Method Step selection
      $(element).find('#stepDescriptionDiv').show();
      $(element).find('#studyExtentDiv').hide();
      $(element).find('#studyExtent').attr('value', null);
      $(element).find('#sampleDescriptionDiv').hide();
      $(element).find('#sampleDescription').attr('value', null);
      $(element).find('#qualityControlDiv').hide();
      $(element).find('#qualityControl').attr('value', null);
    } else if (type == 'SAMPLING') {
      // Update UI for Sampling selection
      $(element).find('#stepDescriptionDiv').hide();
      $(element).find('#stepDescription').attr('value', null);
      $(element).find('#studyExtentDiv').show();
      $(element).find('#sampleDescriptionDiv').show();
      $(element).find('#qualityControlDiv').hide();
      $(element).find('#qualityControl').attr('value', null);
    } else if (type == 'QUALITY_CONTROL') {
      // Update UI for Quality Control selection
      $(element).find('#stepDescriptionDiv').hide();
      $(element).find('#stepDescription').attr('value', null);
      $(element).find('#studyExtentDiv').hide();
      $(element).find('#studyExtent').attr('value', null);
      $(element).find('#sampleDescriptionDiv').hide();
      $(element).find('#sampleDescription').attr('value', null);
      $(element).find('#qualityControlDiv').show();
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
  this.reselect = reselect;
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
 
   /* Show and hide elements as appropriate based on type */
  function reselect(element, type) {
    if (type == 'METHOD_STEP') {
      // Update UI for Method Step selection
      $(element).find('#stepDescriptionDiv').show();
      $(element).find('#studyExtentDiv').hide();
      $(element).find('#studyExtent').attr('value', null);
      $(element).find('#sampleDescriptionDiv').hide();
      $(element).find('#sampleDescription').attr('value', null);
      $(element).find('#qualityControlDiv').hide();
      $(element).find('#qualityControl').attr('value', null);
    } else if (type == 'SAMPLING') {
      // Update UI for Sampling selection
      $(element).find('#stepDescriptionDiv').hide();
      $(element).find('#stepDescription').attr('value', null);
      $(element).find('#studyExtentDiv').show();
      $(element).find('#sampleDescriptionDiv').show();
      $(element).find('#qualityControlDiv').hide();
      $(element).find('#qualityControl').attr('value', null);
    } else if (type == 'QUALITY_CONTROL') {
      // Update UI for Quality Control selection
      $(element).find('#stepDescriptionDiv').hide();
      $(element).find('#stepDescription').attr('value', null);
      $(element).find('#studyExtentDiv').hide();
      $(element).find('#studyExtent').attr('value', null);
      $(element).find('#sampleDescriptionDiv').hide();
      $(element).find('#sampleDescription').attr('value', null);
      $(element).find('#qualityControlDiv').show();
    }
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
    e.find('#type').change(function(e) {  
      // Careful: In the change() callback, 'this' refers to the select element.
      // e is the event.
      var s = this.options[this.selectedIndex];
      var sText = s.text;
      var sValue = s.value;
      var id = '#' + $(this).parent().parent().parent().parent().attr('id');
      _this.reselect($(id), sValue);
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
 * The CuratorialUnitWidget class can be used to encapsulate and display a CuratorialUnit grouping.
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
  this.showType = showType;
  var whatthe = e.find('#type');
  var curatorialUnitType = e.find('#type').attr('value');
  this.showType(e, curatorialUnitType);
  
  function element(val) {
    if (!val) {
      return this._element;
    } else { 
      this._element = val;
      return this;
    }
  }

  /* Show and hide elements as appropriate based on type */
  function showType(element, type) {
    var id = element.attr('id');
    var evalue = element.attr('value');
    var startval =  $(element).find('#rangeStart').attr('value');
    var endval =  $(element).find('#rangeEnd').attr('value');
    var meanval =  $(element).find('#rangeMean').attr('value');
    if (type == 'COUNT_WITH_UNCERTAINTY') {
      // Update UI for Count with uncertainty selection.
      $(element).find('#rangeStartDiv').hide();
      $(element).find('#rangeStart').attr('value', null);
      $(element).find('#rangeEndDiv').hide();
      $(element).find('#rangeEnd').attr('value', null);
      $(element).find('#rangeMeanDiv').show();
      $(element).find('#uncertaintyMeasureDiv').show();
    } else if (type == 'COUNT_RANGE') {
      // Update UI for Count Range selection.
      $(element).find('#rangeStartDiv').show();
      $(element).find('#rangeEndDiv').show();
      $(element).find('#rangeMeanDiv').hide();
      $(element).find('#rangeMean').attr('value', null);
      $(element).find('#uncertaintyMeasureDiv').hide();
      $(element).find('#uncertaintyMeasure').attr('value', null);
    }
  }
} 


/**
 * The CuratorialUnitPanel can be used to display a group of CuratorialUnit widgets.
 * 
 */
function CuratorialUnitPanel() {
  this._elementId = '#curatorialUnitPanel';
  this._element = $(this._elementId);
  this._curatorialUnitSelector = "div[id^='curatorialUnit']";
  this._curatorialUnitIdPattern = /curatorialUnit\d+/;
  this.add = add;
  this.reselect = reselect;
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
   * Returns the number of CuratorialUnit div elements in this CuratorialUnit panel.
   * 
   * @return number of CuratorialUnit div elements
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
 
  /* Show and hide elements as appropriate based on type */
  function reselect(element, type) {
    var id = element.attr('id');
    var evalue = element.attr('value');
    var startval =  $(element).find('#rangeStart').attr('value');
    var endval =  $(element).find('#rangeEnd').attr('value');
    var meanval =  $(element).find('#rangeMean').attr('value');
    if (type == 'COUNT_WITH_UNCERTAINTY') {
      // Update UI for Count with uncertainty selection.
      $(element).find('#rangeStartDiv').hide();
      $(element).find('#rangeStart').attr('value', null);
      $(element).find('#rangeEndDiv').hide();
      $(element).find('#rangeEnd').attr('value', null);
      $(element).find('#rangeMeanDiv').show();
      $(element).find('#uncertaintyMeasureDiv').show();
    } else if (type == 'COUNT_RANGE') {
      // Update UI for Count Range selection.
      $(element).find('#rangeStartDiv').show();
      $(element).find('#rangeEndDiv').show();
      $(element).find('#rangeMeanDiv').hide();
      $(element).find('#rangeMean').attr('value', null);
      $(element).find('#uncertaintyMeasureDiv').hide();
      $(element).find('#uncertaintyMeasure').attr('value', null);
    }
  }

  /**
   * Removes the element from the DOM.
   */
  function _remove(element) {
    $(element).remove();    
  }
  
  /**
   * Adds and displays a CuratorialUnit widget and adds a click handler for deleting it.
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
    e.find('#type').change(function(e) {  
      // Careful: In the change() callback, 'this' refers to the select element.
      // e is the event.
      var s = this.options[this.selectedIndex];
      var sText = s.text;
      var sValue = s.value;
      var id = '#' + $(this).parent().parent().parent().parent().attr('id');
      _this.reselect($(id), sValue);
    });
    e.appendTo(this._element);
    e.show();
    return false;
  }
}

CuratorialUnitPanel.curatorialUnitCount = function() {
  return $('#curatorialUnitPanel').find("div[id^='curatorialUnit']").size();
}

/*==== Keyword Sets ====*/

/**
 * The KeywordSetWidget class can be used to encapsulate and display a Keyword Set grouping.
 * 
 */
function KeywordSetWidget(keywordSet) {
  var e = $('#cloneKeywordSet').clone();
  var keywordSetCount = KeywordSetPanel.keywordSetCount();
  e.attr('id', 'keywordSet' + keywordSetCount);
  this._element = e;
  e.find('#removeLink').show();
  this._keywordSet = keywordSet;
  var isKeywordSet = keywordSet instanceof KeywordSet;
  var keywordSetProps = KeywordSet.propertyNames();
  for (p in keywordSetProps) {
    var name = keywordSetProps[p];
    var val = '';
    if (isKeywordSet) {
      /* 
       * get the name of the attribute for the parameter passed to the widget 
       */ 
      val = eval('keywordSet.' + name + '()');
    } 
    e.find('#' + name).attr('name', 'eml.keywords[' + keywordSetCount + '].'+ name);
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
 * The KeywordSetPanel can be used to display a group of KeywordSet widgets.
 * 
 */
function KeywordSetPanel() {
  this._elementId = '#keywordSetPanel';
  this._element = $(this._elementId);
  this._keywordSetSelector = "div[id^='keywordSet']";
  this._keywordSetIdPattern = /keywordSet\d+/;
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
   * Returns the number of keywordSet div elements in this KeywordSetPanel.
   * 
   * @return number of keywordSet div elements
   */
  function size() {
    return $(this._element).find(this._keywordSetSelector).size();
  }

  function _resize() {
   this._element.find(this._keywordSetSelector).each(function(i) {
     var id =  $(this).attr('id');
     if (id.match(this._keywordSetIdPattern)) {
       $(this).attr('id', 'keywordSet' + i);
       name = 'eml.keywords[' + i + '].';
       props = KeywordSet.propertyNames();
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
   * Adds and displays a keywordSet widget and adds a click handler for deleting it.
   */
  function add(widget) {
    if (!(widget instanceof KeywordSetWidget)) {
      alert('Illegal Argument ' + widget + ': Instance of KeywordSetWidget expected');
      return;
    }
    var count = this.size();
    var e = widget.element();
    e.attr('id', 'keywordSet' + count);
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

KeywordSetPanel.keywordSetCount = function() {
  return $('#keywordSetPanel').find("div[id^='keywordSet']").size();
}

/*==== Taxonomic Coverages ====*/

/**
 * The TaxonomicCoverageWidget class can be used to encapsulate and display a TaxonomicCoverage grouping.
 * 
 */
function TaxonomicCoverageWidget(taxonomicCoverage) {
  var e = $('#cloneTaxonomicCoverage').clone();
  var taxonomicCoverageCount = TaxonomicCoveragePanel.taxonomicCoverageCount();
  e.attr('id', 'taxonomicCoverage' + taxonomicCoverageCount);
  this._element = e;
  e.find('#removeLink').show();
  this._taxonomicCoverage = taxonomicCoverage;
  var isTaxonomicCoverage = taxonomicCoverage instanceof TaxonomicCoverage;
  var taxonomicCoverageProps = TaxonomicCoverage.propertyNames();
  var taxonKeywordNames = TaxonKeyword.propertyNames();
  for (p in taxonomicCoverageProps) {
    var name = taxonomicCoverageProps[p];
    var val = '';
    if (isTaxonomicCoverage) {
      /* 
       * get the name of the attribute for the parameter passed to the widget 
       */ 
      val = eval('taxonomicCoverage.' + name + '()');
    } 
    $(e).attr('id', name);
    var isTaxonKeyword = jQuery.inArray(name, taxonKeywordNames);
      isTaxonKeyword = isTaxonKeyword >= 0 ? true : false;
      if (isTaxonKeyword) {
        e.find('#' + name).attr('name', 'eml.taxonomicCoverages[' + taxonomicCoverageCount + '].taxonKeyword.'+ name);
        e.find('#' + name).attr('value', val);
      } else {
        e.find('#' + name).attr('name', 'eml.taxonomicCoverages[' + taxonomicCoverageCount + '].'+ name);
        e.find('#' + name).attr('value', val);
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
 * The TaxonomicCoveragePanel can be used to display a group of TaxonomicCoverage widgets.
 * 
 */
function TaxonomicCoveragePanel() {
  this._elementId = '#taxonomicCoveragePanel';
  this._element = $(this._elementId);
  this._taxonomicCoverageSelector = "div[id^='taxonomicCoverage']";
  this._taxonomicCoverageIdPattern = /taxonomicCoverage\d+/;
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
   * Returns the number of taxonomicCoverage div elements in this TaxonomicCoveragePanel.
   * 
   * @return number of taxonomicCoverage div elements
   */
  function size() {
    return $(this._element).find(this._taxonomicCoverageSelector).size();
  }

  function _resize() {
    this._element.find(this._taxonomicCoverageSelector).each(function(i) {
      var id =  $(this).attr('id');
      if (id.match(this._taxonomicCoverageIdPattern)) {
        $(this).attr('id', 'taxonomicCoverage' + i);
        name = 'eml.taxonomicCoverages[' + i + '].';
        props = TaxonomicCoverage.propertyNames();
        var taxonKeywordNames = TaxonKeyword.propertyNames();
        for (p in props) {
          var prop = props[p];
          var isTaxonKeyword = jQuery.inArray(prop, taxonKeywordNames);
          isTaxonKeyword = isTaxonKeyword >= 0 ? true : false;
          if (isTaxonKeyword) {
            $(this).find('#' + prop).attr('name', 'eml.taxonomicCoverages[' + i + '].taxonKeyword.'+ prop);
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
   * Adds and displays a taxonomicCoverage widget and adds a click handler for deleting it.
   */
  function add(widget) {
    if (!(widget instanceof TaxonomicCoverageWidget)) {
      alert('Illegal Argument ' + widget + ': Instance of TaxonomicCoverageWidget expected');
      return;
    }
    var count = this.size();
    var e = widget.element();
    e.attr('id', 'taxonomicCoverage' + count);
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

TaxonomicCoveragePanel.taxonomicCoverageCount = function() {
  return $('#taxonomicCoveragePanel').find("div[id^='taxonomicCoverage']").size();
}