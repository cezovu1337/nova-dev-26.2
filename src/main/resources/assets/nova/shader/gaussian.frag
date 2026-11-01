#version 120

uniform sampler2D textureIn;
uniform vec2 texelSize, direction;
uniform float radius;
uniform float weights[256];

#define step texelSize * direction

float gauss(float x, float sigma) {
    float pow = x / sigma;
    return (1.0 / (abs(sigma) * 2.50662827463) * exp(-0.5 * pow * pow));
}

void main() {
    vec4 color = vec4(0);
    vec2 texCoord = gl_TexCoord[0].st;

    for (float f = -radius; f <= radius; f++) {
        color += texture2D(textureIn, texCoord + f * texelSize * direction) * gauss(f, radius / 2);
    }

    gl_FragColor = vec4(color.rgb, 1.0);
}