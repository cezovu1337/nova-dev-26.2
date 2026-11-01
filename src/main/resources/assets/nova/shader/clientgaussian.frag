#version 120

uniform sampler2D diffSampler;
uniform sampler2D u_other_sampler;
uniform vec2 u_texel_size;
uniform vec2 u_direction;
uniform float u_radius;
uniform float kernel[128];

uniform float colorSpeed;
uniform float time;
uniform float exposure;
uniform float gradientRange;
uniform vec3 color1;
uniform vec3 color2;
uniform vec3 color3;
uniform vec3 color4;

uniform float alphaz;
uniform bool colorful;
uniform vec3 noColorfulColor;

vec3 ditheredColor(vec3 color, vec2 coords) {
    float noise = fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453);
    return color + (0.5 / 255.0) * (-1.0 + 2.0 * noise);
}

vec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4) {
    vec3 mixed = color1 + (color2 - color1) * coords.y;
    vec3 mixed2 = color3 + (color4 - color3) * coords.y;
    return mixed2 + (mixed - mixed2) * coords.x;
}

vec4 calculateColor(float alpha, vec2 uv) {
    vec3 startColor = color1 + (color2 - color1) * (0.7 + 0.5 * sin(time * (colorSpeed / 10.0) + (uv.xyx * gradientRange)));
    vec3 endColor = color3 + (color4 - color2) * (0.7 + 0.55 * cos(time * (colorSpeed / 10.0) + ((uv.xyx * 6.0 + uv.x + uv.y))) * (-1.0));
    vec3 startColorB = color3 + (color4 - color3) * (0.7 + 0.5 * sin(time * (colorSpeed / 10.0) + (uv.xyx * gradientRange)));
    vec3 endColorB = color4 + (color3 - color4) * (0.7 + 0.55 * cos(time * (colorSpeed / 10.0) + ((uv.xyx * 6.0 + uv.x + uv.y))) * (-1.0));

    vec3 mixed = startColor + (endColor - startColor) * (0.7 + 0.55 * sin(time * (colorSpeed / 10.0) + ((uv.xyx * 6.0))) * (-1.0));
    vec3 mixedD = startColorB + (endColorB - startColorB) * (0.7 + 0.55 * sin(time * (colorSpeed / 10.0) + ((uv.xyx * 6.0))) * (-1.0));
    vec3 mixedB = mixed + (mixedD - mixed) * (0.7 + 0.55 * sin(time * (colorSpeed / 10.0) + ((uv.xyx * 6.0))) * (-1.0));

    return vec4(colorful ? ditheredColor(mixedB, uv) : noColorfulColor, alpha * exposure);
}

void main()
{
    vec2 uv = gl_TexCoord[0].st;

    float alpha = texture2D(u_other_sampler, uv).a;
    if (u_direction.y > 0.5 && !(alpha > 0.001)) {
        discard;
    }

    float half_radius = u_radius / 2.0;
    vec4 pixel_color = texture2D(diffSampler, uv) * kernel[0];

    for (float f = 1.0; f <= u_radius; f++) {
        vec2 offset = f * u_texel_size * u_direction;
        pixel_color += texture2D(diffSampler, uv - offset) * kernel[int(f)];
        pixel_color += texture2D(diffSampler, uv + offset) * kernel[int(f)];
    }

    vec4 s = calculateColor(1.0, uv);
    float outAlpha = (u_direction.y > 0.5) ? (alpha * exposure) : 1.0;
    if (u_direction.y > 0.5 && !(outAlpha > 0.001)) {
        discard;
    }
    gl_FragColor = vec4(vec3(s.r, s.g, s.b), outAlpha);
}
