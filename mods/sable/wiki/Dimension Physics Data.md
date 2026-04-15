Sable allows datapacks to specify custom physics parameters for dimensions.

### Examples

A moon dimension with lower gravity, no drag, and no air pressure:
```js
// /data/examplemod/dimension_physics/moon.json
{
    "dimension": "examplemod:moon",
  
    // Default priority of 1000
    // Higher priority configs "win"
    "priority": 1000,
  
    // Modify the gravity to be low
    "base_gravity": [0.0, -4.0, 0.0],
    
    // No air pressure   
    "base_pressure": 0.0,
  
    // No universal drag
    "universal_drag": 0.0, 
    
    // No magnetic north
    "magnetic_north": [0.0, 0.0, 0.0]
}
```