#ifdef GL_ES
precision highp float;
#endif

uniform float radius; // +- 45
uniform float blurOffer; // +- 25
uniform vec2 rectSize;
uniform float alpha;

uniform vec2 resolution;
uniform vec2 location;
uniform vec2 locationGradient;
uniform vec3 color1, color2, color3, color4;



#define NOISE .5/255.0

vec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4){
    vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);
    //Dithering the color from https://shader-tutorial.dev/advanced/color-banding-dithering/
    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898,78.233))) * 43758.5453));
    return color;
}

void main()
{
    vec2 rectHalf = rectSize * .5;
    vec2 uv = gl_FragCoord.xy - location - (rectHalf);

    float d = sqrt(dot(uv,uv));
    vec2 coords = (gl_FragCoord.xy - locationGradient) / rectSize;
    float t = 1.0 - smoothstep(radius-blurOffer,radius, d);
    gl_FragColor = vec4(createGradient(coords, color1, color2, color3, color4),alpha* t);
}