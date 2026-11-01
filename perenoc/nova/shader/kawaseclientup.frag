#version 120

uniform sampler2D inTexture, textureToCheck;
uniform vec2 halfpixel, offset, iResolution;
uniform int check;

uniform float colorSpeed;
uniform float time;
uniform float exposure;
uniform float gradientRange;
uniform vec3 color1;
uniform vec3 color2;
uniform vec3 color3;
uniform vec3 color4;
uniform bool blurAgain;
uniform bool gradient;
uniform bool rainbow;
uniform float alphaz;

vec3 ditheredColor(vec3 color, vec2 coords) {
    float noise = fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453);
    return color + (0.5/255.0) * (-1.0 + 2.0 * noise);
}

vec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4) {
    vec3 mixed = color1 + (color2 - color1) * coords.y;
    vec3 mixed2 = color3 + (color4 - color3) * coords.y;
    return mixed2 + (mixed - mixed2) * coords.x;
}

vec4 getRainbowColor(float alpha, vec2 uv) {
    vec3 startColor = color1 + (color2 - color1) * (0.7 + 0.5 * sin(time * (colorSpeed / 10.0) + (uv.xyx * gradientRange)));
    vec3 endColor = color3 + (color4 - color2) * (0.7 + 0.55*cos(time * (colorSpeed / 10.0) + ((uv.xyx * 6.0 + uv.x + uv.y))) * (-1.0));
    vec3 startColorB = color3 + (color4 - color3) * (0.7 + 0.5 * sin(time * (colorSpeed / 10.0) + (uv.xyx * gradientRange)));
    vec3 endColorB = color4 + (color3 - color4) * (0.7 + 0.55*cos(time * (colorSpeed / 10.0) + ((uv.xyx * 6.0 + uv.x + uv.y))) * (-1.0));

    vec3 mixed = startColor + (endColor - startColor) * (0.7 + 0.55*sin(time * (colorSpeed / 10.0)+((uv.xyx * 6.0))) * (-1.0));
    vec3 mixedD = startColorB + (endColorB - startColorB) * (0.7 + 0.55*sin(time * (colorSpeed / 10.0)+((uv.xyx * 6.0))) * (-1.0));

    vec3 mixedB = mixed + (mixedD - mixed) * (0.7 + 0.55*sin(time * (colorSpeed / 10.0)+((uv.xyx * 6.0))) * (-1.0));

    float noise = fract(sin(dot(uv, vec2(12.9898, 78.233))) * 43758.5453);
    vec4 color = vec4(ditheredColor(mixed, uv), alpha * exposure);
    if (rainbow) {
        return vec4(0.5 + 0.5 * sin(time * (colorSpeed / 10.0)+(uv.xyx * gradientRange)+vec3(0.0, 2.0, 4.0)), 1.0);
    }
    return vec4(ditheredColor(mixedB, uv), alpha * exposure);
}

void main() {
    vec2 uv = vec2(gl_FragCoord.xy / iResolution);
    vec4 sum = texture2D(inTexture, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset);
    sum += texture2D(inTexture, uv + vec2(-halfpixel.x, halfpixel.y) * offset) * 2.0;
    sum += texture2D(inTexture, uv + vec2(0.0, halfpixel.y * 2.0) * offset);
    sum += texture2D(inTexture, uv + vec2(halfpixel.x, halfpixel.y) * offset) * 2.0;
    sum += texture2D(inTexture, uv + vec2(halfpixel.x * 2.0, 0.0) * offset);
    sum += texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset) * 2.0;
    sum += texture2D(inTexture, uv + vec2(0.0, -halfpixel.y * 2.0) * offset);
    sum += texture2D(inTexture, uv + vec2(-halfpixel.x, -halfpixel.y) * offset) * 2.0;

    vec4 overlay = vec4(sum.rgb /12.0, mix(1.0, texture2D(textureToCheck, gl_TexCoord[0].st).a, check));

    vec4 final = vec4(getRainbowColor(1f,uv).rgb,texture2D(textureToCheck, gl_TexCoord[0].st).a * getRainbowColor(1f,uv).a);

    gl_FragColor = final;
}