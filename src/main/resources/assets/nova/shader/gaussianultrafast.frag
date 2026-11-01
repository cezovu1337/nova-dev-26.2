#version 120

uniform sampler2D textureIn;
uniform vec2 texelSize, direction;
uniform float radius;
uniform float weights[16];

#define step texelSize * direction

float gauss(float x, float sigma) {
    return exp(-(x*x)/(2.0*sigma*sigma)) / (sqrt(2.0 * 3.1415) * sigma);
}

void main() {
    vec4 color = vec4(0);
    vec2 texCoord = gl_TexCoord[0].st;
    float gaussSum = 0.0;

    for (int i = 0; i < 16; i++) {
        gaussSum += weights[i];
    }

    for (int i = 0; i < 8; i++) {
        float weightA = weights[i * 2];
        float weightB = weights[i * 2 + 1];
        vec4 texColorA = texture2D(textureIn, texCoord + float(i * 2 - 7) * step) * weightA;
        vec4 texColorB = texture2D(textureIn, texCoord + float(i * 2 + 1 - 7) * step) * weightB;
        color += texColorA + texColorB;
    }

    gl_FragColor = vec4(color.rgb / gaussSum, 1.0);
}