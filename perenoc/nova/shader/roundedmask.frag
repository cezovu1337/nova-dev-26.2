#version 120

uniform sampler2D texture;
uniform vec2 location, rectSize;

uniform float radius;
float roundSDF(vec2 p, vec2 b, float r) {
    return length(max(abs(p) - b, 0.0)) - r;
}

void main() {
    vec2 rectHalf = rectSize * .5;

    float r = texture2D(texture, gl_TexCoord[0].st).r;
    float g = texture2D(texture, gl_TexCoord[0].st).g;
    float b = texture2D(texture, gl_TexCoord[0].st).b;
    float a = texture2D(texture, gl_TexCoord[0].st).a;


    // Smooth the result (free antialiasing).
    float smoothedAlpha =  (1.0-smoothstep(0.0, 1.0, roundSDF(rectHalf - (gl_TexCoord[0].st * rectSize), rectHalf - radius - 1., radius))) * a;

    gl_FragColor = vec4(r,g,b, smoothedAlpha);// mix(quadColor, shadowColor, 0.0);
}