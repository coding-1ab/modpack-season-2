uniform sampler2D VeilDynamicAlbedoSampler;

uniform vec2 ScreenSize;

out vec4 fragColor;

void main() {
    vec2 screenUv = gl_FragCoord.xy / ScreenSize;
    vec4 albedoColor = texture(VeilDynamicAlbedoSampler, screenUv);
    if (albedoColor.a == 0) {
        discard;
    }

    fragColor = vec4(1.0);
}

