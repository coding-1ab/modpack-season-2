#include veil:fog

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec2 texCoord0;
in vec4 vertexColor;
in vec4 lightmapColor;

out vec4 fragColor;

// This is a hack to force-set the normal value with additive blending.
// The normal value will be wrong, but the alternative is worse.
// #veil:normal
const vec3 Normal = vec3(-10.0, -10.0, 10.0);

void main() {
    // #veil:albedo
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if (color.a < 0.01) {
        discard;
    }
    fragColor = linear_fog(color * lightmapColor, vertexDistance, FogStart, FogEnd, FogColor);
}

