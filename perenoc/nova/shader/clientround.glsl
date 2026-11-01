#version 120

uniform vec2 location;
uniform vec2 rectSize;
uniform vec2 screenSize;
uniform vec4 color1;
uniform vec4 color2;
uniform vec4 color3;
uniform vec4 color4;
uniform float radius;
uniform float time;
uniform float colorSpeed;
uniform float alpha;
uniform float gradientRange;

#define NOISE .5/255.0

float roundSDF(vec2 p, vec2 b, float r) {
    return length(max(abs(p) - b, vec2(0.0))) - r;
}

vec3 createGradient(vec2 uv) {
    vec3 startColor = color1.rgb + (color2.rgb - color1.rgb) * (0.7 + 0.5 * sin(time * (colorSpeed / 10.0) + (uv.xyx * gradientRange)));

    vec3 endColor = color3.rgb + (color4.rgb - color3.rgb) * (0.7 + 0.55*cos(time * (colorSpeed / 10.0) + ((uv.xyx * 6.0 + uv.x + uv.y))) * (-1.0));

    vec3 mixed = startColor + (endColor - startColor) * (0.7 + 0.55*sin(time * (colorSpeed / 10.0) + ((uv.xyx * 6.0))) * (-1.0));

    float noise = fract(sin(dot(uv * screenSize, vec2(12.9898, 78.233))) * 43758.5453);

    vec3 ditheredColor = mixed * vec3(1.0 - NOISE * noise) + vec3(NOISE * noise);

    vec4 color = vec4(ditheredColor, alpha);

    return color.rgb;
}

void main() {
    vec2 st = gl_TexCoord[0].st;

    vec2 halfSize = rectSize * 0.5;
    vec2 dist = max(abs(((st * screenSize) - location) - rectSize * 0.5) - halfSize + vec2(radius), 0.0);
    float smoothedAlpha = (1.0 - smoothstep(0.0, 2.0, length(dist) - radius)) * color1.a;

    vec3 color = createGradient(st);

    gl_FragColor = vec4(color, smoothedAlpha * alpha);
}
