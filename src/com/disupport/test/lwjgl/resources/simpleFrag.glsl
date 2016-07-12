#version 330

//out vec4 fragColor;
in float depth;

void main() {
    gl_FragColor = vec4(depth, depth, depth, 1.0f);
}
