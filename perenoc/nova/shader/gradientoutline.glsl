#version 120

uniform vec2 u_size;
uniform float u_radius;
uniform float u_border_size;
uniform vec4 color1, color2, color3, color4;
#define NOISE .5/255.0

float roundSDF(vec2 p, vec2 b, float r) {
    return length(max(abs(p) - b, 0.0)) - r;
}

vec4 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4){
    vec4 color = mix(mix(color1, color2, coords.y), mix(color3, color4, coords.y), coords.x);
    color += vec4(mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453)));
    return color;
}


void main(void)
{
    vec2 st = gl_TexCoord[0].st;
    vec4 u_color = createGradient(st, color1, color2, color3, color4);
    vec2 position = (abs(gl_TexCoord[0].st - 0.5) + 0.5) * u_size;
    float distance = length(max(position - u_size + u_radius + u_border_size, 0.0)) - u_radius + 0.5;
    gl_FragColor = vec4(u_color.rgb, u_color.a * (smoothstep(0.0, 1.0, distance) - smoothstep(0.0, 1.0, distance - u_border_size)));
}