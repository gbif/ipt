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

function Address() {
  this._address = '';
  this._city = '';
  this._province = '';
  this._country = '';
  this._postalCode = '';
  
  this.address = address;
  this.city = city;
  this.province = province;
  this.country = country;
  this.postalCode = postalCode;
  
  function address(val) {
    if (val == null) {
      return this._address;
    } else { 
      this._address = val;
      return this;
    }
  }
  
  function city(val) {
    if (val == null) {
      return this._city;
    } else { 
      this._city = val;
      return this;
    }
  }
  
  function province(val) {
    if (val == null) {
      return this._province;
    } else { 
      this._province = val;
      return this;
    }
  }
  
  function country(val) {
    if (val == null) {
      return this._country;
    } else { 
      this._country = val;
      return this;
    }
  }
  
  function postalCode(val) {
    if (val == null) {
      return this._postalCode;
    } else { 
      this._postalCode = val;
      return this;
    }
  }
}

Address.propertyNames = function() {
  return ['address', 'city', 'country', 'postalCode', 'province'];
}

function Agent() {
  this._firstName = '';
  this._lastName = '';
  this._organisation = '';
  this._position = '';
  this._address = new Address();
  this._phone = '';
  this._email = '';
  this._role = '';
  this._homepage = '';
  
  this.firstName = firstName;
  this.lastName = lastName;
  this.address = address;
  this.email = email;
  this.homepage = homepage;
  this.organisation = organisation;
  this.phone = phone;
  this.position = position;
  this.role = role;
  this.address = address;
  this.city = city;
  this.province = province;
  this.country = country;
  this.postalCode = postalCode;
  
  function firstName(val) {
    if (val == null) {
      return this._firstName;
    } else { 
      this._firstName = val;
      return this;
    }
  }
  
  function lastName(val) {
    if (val == null) {
      return this._lastName;
    } else { 
      this._lastName = val;
      return this;
    }
  }
  
  function address(val) {
    if (val == null) {
      return this._address;
    } else { 
      this._address = val;
      return this;
    }
  }

  function email(val) {
    if (val == null) {
      return this._email;
    } else { 
      this._email = val;
      return this;
    }
  }
  
  function homepage(val) {
    if (val == null) {
      return this._homepage;
    } else { 
      this._homepage = val;
      return this;
    }
  }
  
  function organisation(val) {
    if (val == null) {
      return this._organisation;
    } else { 
      this._organisation = val;
      return this;
    }
  }
  
  function phone(val) {
    if (val == null) {
      return this._phone;
    } else { 
      this._phone = val;
      return this;
    }
  }
  
  function position(val) {
    if (val == null) {
      return this._position;
    } else { 
      this._position = val;
      return this;
    }
  }
  
  function role(val) {
    if (val == null) {
      return this._role;
    } else { 
      this._role = val;
      return this;
    }
  }
  
  function address(val) {
    if (val == null) {
      return this._address.address();
    } else { 
      this._address.address(val);
      return this;
    }
  }
  
  function city(val) {
    if (val == null) {
      return this._address.city();
    } else { 
      this._address.city(val);
      return this;
    }
  }
  
  function province(val) {
    if (val == null) {
      return this._address.province();
    } else { 
      this._address.province(val);
      return this;
    }
  }
  
  function country(val) {
    if (val == null) {
      return this._address.country();
    } else { 
      this._address.country(val);
      return this;
    }
  }
  
  function postalCode(val) {
    if (val == null) {
      return this._address.postalCode();
    } else { 
      this._address.postalCode(val);
      return this;
    }
  }
}

Agent.propertyNames = function() {
  return ['address', 'city', 'country', 'email', 'firstName', 'homepage', 'lastName', 'organisation', 'phone', 'position', 'postalCode', 'province', 'role'];
}
  
function TemporalCoverage() {
  this._startDate = '';
  this._endDate = '';
  
  this.startDate = startDate;
  this.endDate = endDate;
  
  function startDate(val) {
    if (val == null) {
      return this._startDate;
    } else { 
      this._startDate = val;
      return this;
    }
  }
  
  function endDate(val) {
    if (val == null) {
      return this._endDate;
    } else { 
      this._endDate = val;
      return this;
    }
  }
}

TemporalCoverage.propertyNames = function() {
  return ['startDate', 'endDate'];
}