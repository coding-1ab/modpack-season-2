#include veil:fog

uniform sampler2D Sampler0;
uniform sampler2D DiffuseDepthSampler;
uniform sampler2D Noise;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform mat4 ProjMat;
uniform vec2 ScreenSize;

uniform float time;
uniform int onSublevel;
uniform int layerIndex;
uniform int enabled;

in float vertexDistance2;
in vec4 vertexColor2;
in vec2 texCoord2;
in float ghostLayerFullness2;
in float ghostNoiseMagnitude2;
in float depth;

out vec4 fragColor;

vec3 rgb2hsv(vec3 c)
{
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

float linearizeDepth(float s) {
    // Same calculation mojang does, to linearize depths using the projection matrix values
    return -ProjMat[3].z /  (s * -2.0 + 1.0 - ProjMat[2].z);
}
vec3 getGhostColorHsv(vec3 hsv,bool forwards)
{
    hsv += forwards ? vec3(-0.45,0,0) : vec3(0.25,0,0);
    hsv.x = mod(hsv.x+1,1);
    hsv.z = pow(hsv.z,0.8);
    return hsv;
}
void main() {
    if(enabled == 0)
    discard;
    vec4 color = texture(Sampler0, texCoord2) * vertexColor2;
    vec3 hsv = rgb2hsv(color.rgb);
    if(layerIndex == 0)
    {
        float s = exp(-ghostNoiseMagnitude2);
        hsv.z = pow(hsv.z, s);
        hsv.y = pow(hsv.y, s);
        color.rgb = hsv2rgb(hsv);
    }

    if(onSublevel > 0)
    {
        float ghostAlpha = ghostLayerFullness2 * 0.3;
        if(layerIndex == 0)
        {
            vec3 ghostTotal = (hsv2rgb(getGhostColorHsv(hsv, false))+hsv2rgb(getGhostColorHsv(hsv, true))).rgb;
            color.rgb -= ghostTotal * ghostAlpha * 0.2;
        }else
        {
            hsv = getGhostColorHsv(hsv, layerIndex < 0);
            color.rgb = hsv2rgb(hsv);

            float depthSample = texture(DiffuseDepthSampler, gl_FragCoord.xy / ScreenSize).r;
            float depth2 = linearizeDepth(depthSample);

            float fade = smoothstep(-2, 4, (depth2-depth)*16);
            if (fade < 0.001)
                discard;

            color.w *= ghostAlpha*fade;
        }
    }
    fragColor = linear_fog(color, vertexDistance2, FogStart, FogEnd, FogColor);
}