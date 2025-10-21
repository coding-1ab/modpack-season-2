Veil adds a custom "scripting language" for injecting into shader sources. This system is intended to adding small hooks
to vanilla shaders to make changes without replacing the entire file.

# Defining Modifications

Shader modifications are loaded before any shaders are loaded. They are located
in `assets/modid/pinwheel/shader_modifiers/shaderid/path/to/shader/filename.vsh.txt`. For example, a modification
for `minecraft:shaders/core/rendertype_solid.vsh` would be located
at `assets/modid/pinwheel/shader_modifiers/minecraft/shaders/core/rendertype_solid.vsh.txt`

This system allows a shader modifier to modify any shader, including pinwheel shaders. Pinwheel shader modification
works exactly like vanilla shader modification.

# Format

The basic syntax is as follows:

```text
#version 330 // required
#priority 1000 // default 1000
#include veil:camera // Test include

// Replaces this with the defined shader
// #replace veil:shader/test

// Vertex Only
[GET_ATTRIBUTE 0] vec3 InPos; // test
[GET_ATTRIBUTE 4] vec3 Nom;

[OUTPUT] // Outputs are guaranteed to be unique
out vec4 Test;
out vec3 TestNormal;

[UNIFORM]
uniform sampler2D Sampler8;

[FUNCTION main(0) HEAD]
TestNormal = #Nom;
Test = vec4(#InPos, 1.0);
```

## Header

`#version` specifies the minimum shader version for this modification. If unsure, use the version in the shader being
modified.

`#priority` is used to determine the order modifications are loaded in. Modifications with a lower priority will load
before others.

`#include` includes another file as specified in [Shader Includes](Shader#includes)

`#replace` is a special option that replaces the targeted shader with another veil shader. This causes all other
modifications to be ignored and should only be used as a last resort.

## Commands

Commands can be added in any order and define where to inject shader code.

- `[GET_ATTRIBUTE #] type name;` Retrieves the specified attribute by id and assigns it to the specified alias for this
  modification. If the attribute doesn't exist it will be added. `#name` can be used anywhere in the file to use that
  attribute.
- `[OUTPUT]` Adds all code following as input variables. An input is automatically added to the next shader in the
  pipeline.
- `[UNIFORM]` Adds all following code at the end of the uniforms block.
- `[FUNCTION name(params) location]` Adds all following code into the specified method at the `HEAD` or `TAIL`

### Functions

This directive specifies what method to inject into. The number of params is optional and is used to specify exactly
what method to inject into. If not specified, all methods with the same name are matched.

The last option determines where to inject in the method. `HEAD` adds code before the method contents and `TAIL` adds
code after the method contents.