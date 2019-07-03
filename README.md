# JsonPath
This is a custom implementation of a JsonPath processor similar to ``com.jayway.jsonpath.JsonPath`` and analogs. 
The reason behind implementing this custom JsonPath processor is in the fact that none of the existing ``JSONPath`` implementations 
allow to retrieve a property of a parent object that JSONPath-matched object is assigned to.

***Example:***
Assume, we need to find a clinic object having only its name and extract the ``clinicId`` for that object where
JSON structure looks like below.
Here ``clinicId`` is represented by a property named ``clinics_123``.
```json
   {
     "clinics": {
         "clinics_123": {
             "address": "123 Washington Way",
             "accessibility": "true",
             "zip": "99011",
             "city": "Seattle",
             "name": "Seattle Medical Center",
             "state": "WA"
         }
     },...
 }
```
In order to retrieve this property ``JsonPath`` has to look like ``$.*[@.name='Seattle Medical Center'].@parent``
where ``@parent`` is a chunk denoting that parent object's property, which found object belongs to, has to be returned.

The result of the ``JSONPath`` query from above will be ``"clinics_123"``.

However much limited comparing to ``com.jayway.jsonpath.JsonPath``, this implementation can be extended to accommodate additional functionality.
