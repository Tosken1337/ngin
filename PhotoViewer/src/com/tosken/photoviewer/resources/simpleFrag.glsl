#version 330


uniform sampler2D tex;

in vec2 texCoordOut;

void main() {
    vec4 color = texture2D(tex, texCoordOut.xy);
    gl_FragColor = vec4(color.rgb, 1.0f);
}
