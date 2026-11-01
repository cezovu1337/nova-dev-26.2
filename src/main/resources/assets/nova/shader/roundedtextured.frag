#version 120

uniform vec2 u_size;
uniform sampler2D texture;
uniform float u_radius;

void main(void)
{
    gl_FragColor = vec4(texture2D(texture,gl_TexCoord[0].st).rgb,
    texture2D(texture,gl_TexCoord[0].st).a * smoothstep(1.0, 0.0, length(max((abs(gl_TexCoord[0].st - 0.5) + 0.5)
    * u_size - u_size + u_radius, 0.0))
    - u_radius + 0.5));
}