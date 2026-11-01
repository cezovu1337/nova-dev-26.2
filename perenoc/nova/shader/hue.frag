#version 120

uniform vec2 location, rectSize;
uniform float radius;
uniform float alpha;

#define NOISE .5/255.0

vec3 getRGBFromHue(float hue) {
    hue *= 360.0;
    if (hue < 60.0) {
        return vec3(1, hue / 60, 0);
    } else if (hue < 120) {
        return vec3(1 - (hue - 60) / 60, 1, 0);
    }else if (hue < 180) {
        return vec3(0, 1, (hue - 120) / 60);
    } else if (hue < 240) {
        return vec3(0, 1 - (hue - 180) / 60, 1);
    } else if (hue < 300) {
        return vec3((hue - 240) / 60, 0, 1);
    } else {
        return vec3(1, 0, 1 - (hue - 300) / 60);
    }
}

float roundSDF(vec2 p, vec2 b, float r) {
    return length(max(abs(p) - b , 0.0)) - r;
}

vec3 createGradient(vec2 coords){
    vec3 color = getRGBFromHue(coords.y);
    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));
    return color;
}

void main() {
    vec2 st = gl_TexCoord[0].st;
    vec2 halfSize = rectSize * .5;
    float smoothedAlpha =  (1.0-smoothstep(0.0, 2., roundSDF(halfSize - (gl_TexCoord[0].st * rectSize), halfSize - radius - 1., radius)));
    gl_FragColor = vec4(createGradient(st), smoothedAlpha * alpha);
}